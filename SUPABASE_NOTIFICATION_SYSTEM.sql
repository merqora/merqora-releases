-- =====================================================
-- SISTEMA DE NOTIFICACIONES ESCALABLE PARA HANDSHAKES
-- Diseñado para manejar miles de usuarios simultáneos
-- =====================================================

-- 1. Tabla de confirmaciones pendientes
-- Registra cuando un usuario confirma y el otro no
CREATE TABLE IF NOT EXISTS pending_confirmations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    handshake_id TEXT NOT NULL REFERENCES handshake_transactions(id) ON DELETE CASCADE,
    confirmed_user_id UUID NOT NULL REFERENCES auth.users(id),
    pending_user_id UUID NOT NULL REFERENCES auth.users(id),
    confirmed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    notification_1h_sent BOOLEAN DEFAULT FALSE,
    notification_3h_sent BOOLEAN DEFAULT FALSE,
    notification_5h_sent BOOLEAN DEFAULT FALSE,
    auto_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    UNIQUE(handshake_id)
);

-- 2. Tabla de notificaciones del sistema
-- Notificaciones push/in-app para los usuarios
CREATE TABLE IF NOT EXISTS system_notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id),
    type TEXT NOT NULL, -- 'CONFIRMATION_REMINDER', 'AUTO_COMPLETED', 'REPUTATION_PENALTY', etc.
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    data JSONB DEFAULT '{}', -- Datos adicionales (handshake_id, etc.)
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para búsquedas eficientes
CREATE INDEX IF NOT EXISTS idx_pending_confirmations_pending_user 
    ON pending_confirmations(pending_user_id);
CREATE INDEX IF NOT EXISTS idx_pending_confirmations_confirmed_at 
    ON pending_confirmations(confirmed_at);
CREATE INDEX IF NOT EXISTS idx_system_notifications_user_unread 
    ON system_notifications(user_id, read) WHERE read = FALSE;
CREATE INDEX IF NOT EXISTS idx_system_notifications_created 
    ON system_notifications(created_at DESC);

-- 3. Habilitar Realtime para notificaciones instantáneas
ALTER PUBLICATION supabase_realtime ADD TABLE system_notifications;

-- 4. RLS Policies
ALTER TABLE pending_confirmations ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_notifications ENABLE ROW LEVEL SECURITY;

-- Usuarios solo ven sus propias notificaciones
CREATE POLICY "Users can view own notifications" ON system_notifications
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can update own notifications" ON system_notifications
    FOR UPDATE USING (auth.uid() = user_id);

-- Solo el sistema puede crear/modificar pending_confirmations
CREATE POLICY "Service role only for pending_confirmations" ON pending_confirmations
    FOR ALL USING (auth.role() = 'service_role');

-- 5. Función para registrar confirmación parcial
-- Se llama cuando UN usuario confirma pero el otro no
CREATE OR REPLACE FUNCTION register_partial_confirmation(
    p_handshake_id TEXT,
    p_confirmed_user_id UUID,
    p_pending_user_id UUID
) RETURNS VOID AS $$
BEGIN
    INSERT INTO pending_confirmations (handshake_id, confirmed_user_id, pending_user_id)
    VALUES (p_handshake_id, p_confirmed_user_id, p_pending_user_id)
    ON CONFLICT (handshake_id) DO NOTHING;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 6. Función principal de procesamiento de timeouts
-- Ejecutada por pg_cron cada minuto
CREATE OR REPLACE FUNCTION process_pending_confirmations() RETURNS VOID AS $$
DECLARE
    r RECORD;
    hours_elapsed NUMERIC;
BEGIN
    FOR r IN 
        SELECT pc.*, ht.product_description, ht.agreed_price
        FROM pending_confirmations pc
        JOIN handshake_transactions ht ON pc.handshake_id = ht.id
        WHERE pc.auto_completed = FALSE
          AND ht.status IN ('ACCEPTED', 'IN_PROGRESS')
    LOOP
        hours_elapsed := EXTRACT(EPOCH FROM (NOW() - r.confirmed_at)) / 3600;
        
        -- Notificación a 1 hora
        IF hours_elapsed >= 1 AND NOT r.notification_1h_sent THEN
            INSERT INTO system_notifications (user_id, type, title, message, data)
            VALUES (
                r.pending_user_id,
                'CONFIRMATION_REMINDER',
                '⏰ Confirmación pendiente',
                'Tienes una transacción pendiente de confirmar. El otro usuario ya confirmó.',
                jsonb_build_object(
                    'handshake_id', r.handshake_id,
                    'product', r.product_description,
                    'hours_remaining', 5
                )
            );
            UPDATE pending_confirmations SET notification_1h_sent = TRUE WHERE id = r.id;
        END IF;
        
        -- Notificación a 3 horas
        IF hours_elapsed >= 3 AND NOT r.notification_3h_sent THEN
            INSERT INTO system_notifications (user_id, type, title, message, data)
            VALUES (
                r.pending_user_id,
                'CONFIRMATION_REMINDER',
                '⚠️ Confirmación urgente',
                'Quedan 3 horas para confirmar tu transacción. Si no confirmas, se completará automáticamente.',
                jsonb_build_object(
                    'handshake_id', r.handshake_id,
                    'product', r.product_description,
                    'hours_remaining', 3
                )
            );
            UPDATE pending_confirmations SET notification_3h_sent = TRUE WHERE id = r.id;
        END IF;
        
        -- Notificación a 5 horas
        IF hours_elapsed >= 5 AND NOT r.notification_5h_sent THEN
            INSERT INTO system_notifications (user_id, type, title, message, data)
            VALUES (
                r.pending_user_id,
                'CONFIRMATION_REMINDER',
                '🚨 Última oportunidad',
                '¡Solo queda 1 hora! Si no confirmas, perderás reputación.',
                jsonb_build_object(
                    'handshake_id', r.handshake_id,
                    'product', r.product_description,
                    'hours_remaining', 1
                )
            );
            UPDATE pending_confirmations SET notification_5h_sent = TRUE WHERE id = r.id;
        END IF;
        
        -- Auto-completar a las 6 horas
        IF hours_elapsed >= 6 THEN
            -- Completar la transacción
            UPDATE handshake_transactions 
            SET status = 'COMPLETED',
                initiator_confirmed = TRUE,
                receiver_confirmed = TRUE,
                completed_at = NOW()
            WHERE id = r.handshake_id;
            
            -- Aplicar penalización de reputación (-3%)
            UPDATE usuarios 
            SET reputation_score = GREATEST(0, reputation_score - 3)
            WHERE user_id = r.pending_user_id;
            
            -- Notificación de penalización
            INSERT INTO system_notifications (user_id, type, title, message, data)
            VALUES (
                r.pending_user_id,
                'REPUTATION_PENALTY',
                '📉 Penalización de reputación',
                'La transacción se completó automáticamente. Perdiste 3% de reputación por no confirmar a tiempo.',
                jsonb_build_object(
                    'handshake_id', r.handshake_id,
                    'product', r.product_description,
                    'penalty', -3
                )
            );
            
            -- Notificación al otro usuario de que se completó
            INSERT INTO system_notifications (user_id, type, title, message, data)
            VALUES (
                r.confirmed_user_id,
                'AUTO_COMPLETED',
                '✅ Transacción completada',
                'La transacción se completó automáticamente porque el otro usuario no confirmó a tiempo.',
                jsonb_build_object(
                    'handshake_id', r.handshake_id,
                    'product', r.product_description
                )
            );
            
            -- Marcar como procesado
            UPDATE pending_confirmations SET auto_completed = TRUE WHERE id = r.id;
        END IF;
    END LOOP;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 7. Configurar pg_cron para ejecutar cada minuto
-- NOTA: Ejecutar esto en Supabase Dashboard > SQL Editor
-- SELECT cron.schedule('process-pending-confirmations', '* * * * *', 'SELECT process_pending_confirmations()');

-- 8. Trigger para detectar confirmaciones parciales automáticamente
CREATE OR REPLACE FUNCTION on_handshake_confirmation() RETURNS TRIGGER AS $$
DECLARE
    v_initiator_id UUID;
    v_receiver_id UUID;
BEGIN
    -- Solo actuar cuando hay una confirmación parcial
    IF NEW.status IN ('ACCEPTED', 'IN_PROGRESS') THEN
        v_initiator_id := NEW.initiator_id::UUID;
        v_receiver_id := NEW.receiver_id::UUID;
        
        -- Iniciador confirmó, receptor no
        IF NEW.initiator_confirmed = TRUE AND NEW.receiver_confirmed = FALSE THEN
            PERFORM register_partial_confirmation(NEW.id, v_initiator_id, v_receiver_id);
        -- Receptor confirmó, iniciador no
        ELSIF NEW.receiver_confirmed = TRUE AND NEW.initiator_confirmed = FALSE THEN
            PERFORM register_partial_confirmation(NEW.id, v_receiver_id, v_initiator_id);
        -- Ambos confirmaron - eliminar de pendientes
        ELSIF NEW.initiator_confirmed = TRUE AND NEW.receiver_confirmed = TRUE THEN
            DELETE FROM pending_confirmations WHERE handshake_id = NEW.id;
        END IF;
    END IF;
    
    -- Si se cancela o completa manualmente, limpiar
    IF NEW.status IN ('CANCELLED', 'REJECTED', 'COMPLETED') THEN
        DELETE FROM pending_confirmations WHERE handshake_id = NEW.id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Crear trigger
DROP TRIGGER IF EXISTS handshake_confirmation_trigger ON handshake_transactions;
CREATE TRIGGER handshake_confirmation_trigger
    AFTER UPDATE ON handshake_transactions
    FOR EACH ROW
    EXECUTE FUNCTION on_handshake_confirmation();

-- 9. Función para obtener notificaciones no leídas (eficiente)
CREATE OR REPLACE FUNCTION get_unread_notifications(p_user_id UUID, p_limit INT DEFAULT 20)
RETURNS TABLE (
    id UUID,
    type TEXT,
    title TEXT,
    message TEXT,
    data JSONB,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT n.id, n.type, n.title, n.message, n.data, n.created_at
    FROM system_notifications n
    WHERE n.user_id = p_user_id AND n.read = FALSE
    ORDER BY n.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 10. Función para marcar notificaciones como leídas
CREATE OR REPLACE FUNCTION mark_notifications_read(p_notification_ids UUID[])
RETURNS VOID AS $$
BEGIN
    UPDATE system_notifications 
    SET read = TRUE 
    WHERE id = ANY(p_notification_ids) AND user_id = auth.uid();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- INSTRUCCIONES DE CONFIGURACIÓN
-- =====================================================
-- 
-- 1. Ejecutar este script en Supabase Dashboard > SQL Editor
-- 
-- 2. Habilitar pg_cron (si no está habilitado):
--    - Ir a Database > Extensions
--    - Buscar "pg_cron" y habilitarlo
-- 
-- 3. Configurar el cron job:
--    SELECT cron.schedule(
--      'process-pending-confirmations',
--      '* * * * *',  -- Cada minuto
--      'SELECT process_pending_confirmations()'
--    );
--
-- 4. Verificar que Realtime esté habilitado para system_notifications:
--    - Ir a Database > Replication
--    - Asegurar que system_notifications está en la publicación
--
-- =====================================================
