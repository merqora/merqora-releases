-- ═══════════════════════════════════════════════════════════════
-- SISTEMA DE HANDSHAKE - Confirmación de compras/ventas en persona
-- ═══════════════════════════════════════════════════════════════

-- Tabla principal de transacciones handshake
CREATE TABLE IF NOT EXISTS handshake_transactions (
    id TEXT PRIMARY KEY DEFAULT gen_random_uuid()::text,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    
    -- Participantes
    initiator_id TEXT NOT NULL,        -- Quien inició el handshake
    receiver_id TEXT NOT NULL,         -- Quien debe confirmar/rechazar
    
    -- Detalles de la transacción
    product_description TEXT NOT NULL,
    agreed_price DECIMAL(12, 2) NOT NULL,
    
    -- Estado del handshake
    -- PROPOSED: Iniciador envió propuesta, esperando respuesta
    -- ACCEPTED: Receptor aceptó, ambos deben encontrarse
    -- RENEGOTIATING: Receptor propuso cambios
    -- REJECTED: Receptor rechazó
    -- IN_PROGRESS: Ambos confirmaron encuentro, en proceso
    -- COMPLETED: Ambos confirmaron la transacción exitosa
    -- CANCELLED: Cancelado por alguno de los participantes
    -- DISPUTED: Hay una disputa
    status TEXT NOT NULL DEFAULT 'PROPOSED' CHECK (
        status IN ('PROPOSED', 'ACCEPTED', 'RENEGOTIATING', 'REJECTED', 
                   'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'DISPUTED')
    ),
    
    -- Confirmaciones individuales
    initiator_confirmed BOOLEAN DEFAULT FALSE,
    receiver_confirmed BOOLEAN DEFAULT FALSE,
    
    -- Renegociación (si aplica)
    counter_price DECIMAL(12, 2),
    counter_message TEXT,
    
    -- Datos de QR para confirmación offline
    qr_secret_initiator TEXT,   -- Secret para el QR del iniciador
    qr_secret_receiver TEXT,    -- Secret para el QR del receptor
    qr_scanned_at TIMESTAMPTZ,  -- Cuando se escaneó el QR
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    accepted_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    
    -- Índices para búsquedas rápidas
    CONSTRAINT different_users CHECK (initiator_id != receiver_id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_handshake_conversation ON handshake_transactions(conversation_id);
CREATE INDEX IF NOT EXISTS idx_handshake_initiator ON handshake_transactions(initiator_id);
CREATE INDEX IF NOT EXISTS idx_handshake_receiver ON handshake_transactions(receiver_id);
CREATE INDEX IF NOT EXISTS idx_handshake_status ON handshake_transactions(status);
CREATE INDEX IF NOT EXISTS idx_handshake_pending ON handshake_transactions(receiver_id, status) 
    WHERE status = 'PROPOSED';

-- Trigger para actualizar updated_at
CREATE OR REPLACE FUNCTION update_handshake_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS handshake_updated_at ON handshake_transactions;
CREATE TRIGGER handshake_updated_at
    BEFORE UPDATE ON handshake_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_handshake_timestamp();

-- RLS Policies
ALTER TABLE handshake_transactions ENABLE ROW LEVEL SECURITY;

-- Los usuarios pueden ver sus propios handshakes
CREATE POLICY "Users can view own handshakes" ON handshake_transactions
    FOR SELECT USING (
        auth.uid()::text = initiator_id OR 
        auth.uid()::text = receiver_id
    );

-- ADMIN/TESTING: Permitir lectura anónima (para admin-web)
CREATE POLICY "Allow anonymous read for testing" ON handshake_transactions
    FOR SELECT USING (true);

-- Los usuarios pueden crear handshakes
CREATE POLICY "Users can create handshakes" ON handshake_transactions
    FOR INSERT WITH CHECK (auth.uid()::text = initiator_id);

-- Los participantes pueden actualizar el handshake
CREATE POLICY "Participants can update handshakes" ON handshake_transactions
    FOR UPDATE USING (
        auth.uid()::text = initiator_id OR 
        auth.uid()::text = receiver_id
    );

-- ADMIN/TESTING: Permitir actualización anónima (para admin-web)
CREATE POLICY "Allow anonymous update for testing" ON handshake_transactions
    FOR UPDATE USING (true);

-- Habilitar Realtime para esta tabla
ALTER PUBLICATION supabase_realtime ADD TABLE handshake_transactions;

-- ═══════════════════════════════════════════════════════════════
-- HISTORIAL DE CAMBIOS DE ESTADO (para auditoría)
-- ═══════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS handshake_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    handshake_id TEXT NOT NULL REFERENCES handshake_transactions(id) ON DELETE CASCADE,
    previous_status TEXT,
    new_status TEXT NOT NULL,
    changed_by TEXT NOT NULL,
    change_reason TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_handshake_history ON handshake_history(handshake_id);

-- RLS para historial
ALTER TABLE handshake_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Participants can view history" ON handshake_history
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM handshake_transactions ht 
            WHERE ht.id = handshake_id 
            AND (auth.uid()::text = ht.initiator_id OR auth.uid()::text = ht.receiver_id)
        )
    );

-- Trigger para registrar cambios de estado automáticamente
CREATE OR REPLACE FUNCTION log_handshake_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO handshake_history (handshake_id, previous_status, new_status, changed_by)
        VALUES (NEW.id, OLD.status, NEW.status, auth.uid()::text);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS handshake_status_log ON handshake_transactions;
CREATE TRIGGER handshake_status_log
    AFTER UPDATE ON handshake_transactions
    FOR EACH ROW
    EXECUTE FUNCTION log_handshake_status_change();

-- ═══════════════════════════════════════════════════════════════
-- FUNCIÓN RPC PARA CONFIRMAR HANDSHAKE (maneja cast UUID)
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION confirm_handshake(
    p_handshake_id TEXT,
    p_field TEXT
)
RETURNS VOID AS $$
BEGIN
    IF p_field = 'initiator_confirmed' THEN
        UPDATE handshake_transactions 
        SET initiator_confirmed = TRUE, status = 'IN_PROGRESS'
        WHERE id = p_handshake_id::uuid;
    ELSIF p_field = 'receiver_confirmed' THEN
        UPDATE handshake_transactions 
        SET receiver_confirmed = TRUE, status = 'IN_PROGRESS'
        WHERE id = p_handshake_id::uuid;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════
-- FUNCIÓN RPC PARA ACTUALIZAR HANDSHAKE (maneja cast UUID)
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_handshake_status(
    p_handshake_id TEXT,
    p_status TEXT
)
RETURNS VOID AS $$
BEGIN
    UPDATE handshake_transactions 
    SET status = p_status
    WHERE id = p_handshake_id::uuid;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════
-- FUNCIÓN PARA COMPLETAR HANDSHAKE (cuando ambos confirman)
-- ═══════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION complete_handshake_if_ready()
RETURNS TRIGGER AS $$
BEGIN
    -- Si ambos confirmaron, marcar como completado
    IF NEW.initiator_confirmed = TRUE AND NEW.receiver_confirmed = TRUE THEN
        NEW.status := 'COMPLETED';
        NEW.completed_at := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS handshake_auto_complete ON handshake_transactions;
CREATE TRIGGER handshake_auto_complete
    BEFORE UPDATE ON handshake_transactions
    FOR EACH ROW
    WHEN (NEW.initiator_confirmed = TRUE AND NEW.receiver_confirmed = TRUE)
    EXECUTE FUNCTION complete_handshake_if_ready();
