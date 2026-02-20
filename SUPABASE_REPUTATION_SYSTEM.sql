-- ═══════════════════════════════════════════════════════════════
-- SISTEMA DE REPUTACIÓN EN TIEMPO REAL
-- Integrado con handshake_transactions
-- ═══════════════════════════════════════════════════════════════

-- 1. Agregar columna reputation_score a usuarios si no existe
-- La reputación es un porcentaje de 0 a 100
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS reputation_score DECIMAL(5,2) DEFAULT 50.00;

-- Asegurar que esté en el rango correcto
ALTER TABLE usuarios 
ADD CONSTRAINT IF NOT EXISTS check_reputation_range 
CHECK (reputation_score >= 0 AND reputation_score <= 100);

-- 2. Tabla de historial de cambios de reputación (auditoría)
CREATE TABLE IF NOT EXISTS reputation_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    previous_score DECIMAL(5,2),
    new_score DECIMAL(5,2),
    change_amount DECIMAL(5,2) NOT NULL,
    reason TEXT NOT NULL,  -- 'HANDSHAKE_COMPLETED', 'HANDSHAKE_CANCELLED_WAITING', 'HANDSHAKE_CANCELLED_ACCEPTED'
    related_handshake_id UUID REFERENCES handshake_transactions(id),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_reputation_history_user ON reputation_history(user_id);
CREATE INDEX IF NOT EXISTS idx_reputation_history_date ON reputation_history(created_at DESC);

-- RLS para historial
ALTER TABLE reputation_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own reputation history" ON reputation_history
    FOR SELECT USING (auth.uid()::text = user_id);

-- 3. Función para actualizar reputación
CREATE OR REPLACE FUNCTION update_user_reputation(
    p_user_id TEXT,
    p_change_amount DECIMAL(5,2),
    p_reason TEXT,
    p_handshake_id UUID DEFAULT NULL
)
RETURNS DECIMAL(5,2) AS $$
DECLARE
    v_current_score DECIMAL(5,2);
    v_new_score DECIMAL(5,2);
BEGIN
    -- Obtener score actual
    SELECT COALESCE(reputation_score, 50.00) INTO v_current_score
    FROM usuarios
    WHERE user_id = p_user_id;
    
    -- Calcular nuevo score (limitado entre 0 y 100)
    v_new_score := GREATEST(0, LEAST(100, v_current_score + p_change_amount));
    
    -- Actualizar usuario
    UPDATE usuarios
    SET reputation_score = v_new_score
    WHERE user_id = p_user_id;
    
    -- Registrar en historial
    INSERT INTO reputation_history (user_id, previous_score, new_score, change_amount, reason, related_handshake_id)
    VALUES (p_user_id, v_current_score, v_new_score, p_change_amount, p_reason, p_handshake_id);
    
    RETURN v_new_score;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 4. Trigger automático cuando se completa un handshake
-- Aumenta reputación de ambos participantes
CREATE OR REPLACE FUNCTION on_handshake_completed()
RETURNS TRIGGER AS $$
DECLARE
    v_boost DECIMAL(5,2);
BEGIN
    -- Solo ejecutar cuando el status cambia a COMPLETED
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        -- Boost aleatorio entre 2 y 5 puntos
        v_boost := 2 + (random() * 3);
        
        -- Aumentar reputación del iniciador
        PERFORM update_user_reputation(
            NEW.initiator_id,
            v_boost,
            'HANDSHAKE_COMPLETED',
            NEW.id
        );
        
        -- Aumentar reputación del receptor
        PERFORM update_user_reputation(
            NEW.receiver_id,
            v_boost,
            'HANDSHAKE_COMPLETED',
            NEW.id
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS handshake_completed_reputation ON handshake_transactions;
CREATE TRIGGER handshake_completed_reputation
    AFTER UPDATE ON handshake_transactions
    FOR EACH ROW
    WHEN (NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED')
    EXECUTE FUNCTION on_handshake_completed();

-- 5. Trigger automático cuando se cancela un handshake
-- Penaliza según el estado: -2% en WAITING, -5% en ACCEPTED
CREATE OR REPLACE FUNCTION on_handshake_cancelled()
RETURNS TRIGGER AS $$
DECLARE
    v_penalty DECIMAL(5,2);
    v_reason TEXT;
    v_canceller_id TEXT;
BEGIN
    -- Solo ejecutar cuando el status cambia a CANCELLED
    IF NEW.status = 'CANCELLED' AND OLD.status != 'CANCELLED' THEN
        -- Determinar quién canceló (el que hizo la última actualización)
        v_canceller_id := auth.uid()::text;
        
        -- Penalización según estado anterior
        IF OLD.status = 'PROPOSED' THEN
            -- Cancelar en estado WAITING: -2%
            v_penalty := -2.00;
            v_reason := 'HANDSHAKE_CANCELLED_WAITING';
        ELSIF OLD.status IN ('ACCEPTED', 'IN_PROGRESS') THEN
            -- Cancelar después de aceptar: -5%
            v_penalty := -5.00;
            v_reason := 'HANDSHAKE_CANCELLED_ACCEPTED';
        ELSE
            -- Otros casos: sin penalización
            RETURN NEW;
        END IF;
        
        -- Aplicar penalización solo al que canceló
        PERFORM update_user_reputation(
            v_canceller_id,
            v_penalty,
            v_reason,
            NEW.id
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS handshake_cancelled_reputation ON handshake_transactions;
CREATE TRIGGER handshake_cancelled_reputation
    AFTER UPDATE ON handshake_transactions
    FOR EACH ROW
    WHEN (NEW.status = 'CANCELLED' AND OLD.status != 'CANCELLED')
    EXECUTE FUNCTION on_handshake_cancelled();

-- 6. Habilitar Realtime para usuarios (para que la reputación se actualice en tiempo real)
-- Nota: Si ya está habilitado, este comando no hace nada
DO $$
BEGIN
    ALTER PUBLICATION supabase_realtime ADD TABLE usuarios;
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- 7. Grants
GRANT EXECUTE ON FUNCTION update_user_reputation TO authenticated;
GRANT ALL ON reputation_history TO authenticated;

-- 8. Inicializar reputación en 50 para usuarios que no tienen
UPDATE usuarios SET reputation_score = 50.00 WHERE reputation_score IS NULL;
