-- ═══════════════════════════════════════════════════════════════════════════════
-- RENDLY - FIX FCM PUSH NOTIFICATION TRIGGER
-- Arregla el problema de tipos UUID vs TEXT
-- ═══════════════════════════════════════════════════════════════════════════════

-- PASO 1: Eliminar trigger y función anterior
DROP TRIGGER IF EXISTS on_notification_insert_send_push ON notifications;
DROP FUNCTION IF EXISTS send_fcm_push_notification();

-- PASO 2: Eliminar tabla pending_push_notifications con definición incorrecta
DROP TABLE IF EXISTS pending_push_notifications;

-- PASO 3: Recrear tabla pending_push_notifications con tipos correctos (TEXT para recipient_id)
CREATE TABLE IF NOT EXISTS pending_push_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_id UUID,  -- Sin FK porque notifications.id es UUID
    recipient_id TEXT NOT NULL,  -- TEXT como en notifications
    tokens TEXT[] NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    image_url TEXT,
    data JSONB DEFAULT '{}',
    is_sent BOOLEAN DEFAULT false,
    sent_at TIMESTAMPTZ,
    error TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pending_push_unsent ON pending_push_notifications(is_sent) WHERE is_sent = false;

-- PASO 4: Recrear función con CAST correcto de TEXT a UUID
CREATE OR REPLACE FUNCTION send_fcm_push_notification()
RETURNS TRIGGER AS $$
DECLARE
    notification_title TEXT;
    notification_body TEXT;
    notification_image TEXT;
    recipient_tokens TEXT[];
    recipient_uuid UUID;
BEGIN
    -- Construir título y cuerpo según el tipo de notificación (sin emojis, profesional)
    CASE NEW.type
        WHEN 'like' THEN
            notification_title := COALESCE(NEW.sender_username, 'Alguien') || ' le dio like a tu publicación';
            notification_body := 'Toca para ver';
        WHEN 'comment' THEN
            notification_title := COALESCE(NEW.sender_username, 'Alguien') || ' comentó tu publicación';
            notification_body := COALESCE(NEW.message, 'Nuevo comentario');
        WHEN 'follow' THEN
            notification_title := COALESCE(NEW.sender_username, 'Alguien') || ' empezó a seguirte';
            notification_body := 'Toca para ver su perfil';
        WHEN 'message' THEN
            notification_title := COALESCE(NEW.sender_username, 'Nuevo mensaje');
            notification_body := COALESCE(NEW.message, 'Tienes un nuevo mensaje');
        WHEN 'sale' THEN
            notification_title := 'Nueva venta';
            notification_body := COALESCE(NEW.message, 'Tienes una nueva venta');
        WHEN 'handshake' THEN
            notification_title := 'Solicitud de handshake';
            notification_body := COALESCE(NEW.sender_username, 'Alguien') || ' quiere hacer un handshake contigo';
        WHEN 'mention' THEN
            notification_title := COALESCE(NEW.sender_username, 'Alguien') || ' te mencionó';
            notification_body := COALESCE(NEW.message, 'Te mencionaron en una publicación');
        ELSE
            notification_title := 'Rendly';
            notification_body := COALESCE(NEW.message, 'Tienes una nueva notificación');
    END CASE;
    
    -- Imagen de la notificación (si hay)
    notification_image := NEW.post_image;
    
    -- Convertir TEXT a UUID para buscar tokens
    BEGIN
        recipient_uuid := NEW.recipient_id::UUID;
    EXCEPTION WHEN OTHERS THEN
        -- Si no se puede convertir, salir sin error
        RETURN NEW;
    END;
    
    -- Obtener tokens FCM activos del destinatario (CAST TEXT a UUID)
    SELECT ARRAY_AGG(token) INTO recipient_tokens
    FROM fcm_tokens
    WHERE user_id = recipient_uuid
    AND is_active = true;
    
    -- Si hay tokens, insertar en cola de push pendientes
    IF recipient_tokens IS NOT NULL AND array_length(recipient_tokens, 1) > 0 THEN
        INSERT INTO pending_push_notifications (
            notification_id,
            recipient_id,
            tokens,
            title,
            body,
            image_url,
            data,
            created_at
        ) VALUES (
            NEW.id,
            NEW.recipient_id,  -- Mantener como TEXT
            recipient_tokens,
            notification_title,
            notification_body,
            notification_image,
            jsonb_build_object(
                'type', NEW.type,
                'notification_id', NEW.id::TEXT,
                'sender_id', NEW.sender_id,
                'sender_username', NEW.sender_username,
                'post_id', NEW.post_id,
                'click_action', 'FLUTTER_NOTIFICATION_CLICK'
            ),
            NOW()
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- PASO 5: Recrear trigger
CREATE TRIGGER on_notification_insert_send_push
    AFTER INSERT ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION send_fcm_push_notification();

-- PASO 6: Actualizar funciones RPC
CREATE OR REPLACE FUNCTION get_pending_push_notifications(batch_size INT DEFAULT 100)
RETURNS TABLE(
    id UUID,
    tokens TEXT[],
    title TEXT,
    body TEXT,
    image_url TEXT,
    data JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        p.id,
        p.tokens,
        p.title,
        p.body,
        p.image_url,
        p.data
    FROM pending_push_notifications p
    WHERE p.is_sent = false
    AND p.retry_count < 3
    ORDER BY p.created_at ASC
    LIMIT batch_size;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE OR REPLACE FUNCTION mark_push_as_sent(push_id UUID, success BOOLEAN, error_msg TEXT DEFAULT NULL)
RETURNS VOID AS $$
BEGIN
    UPDATE pending_push_notifications
    SET 
        is_sent = success,
        sent_at = CASE WHEN success THEN NOW() ELSE NULL END,
        error = error_msg,
        retry_count = CASE WHEN NOT success THEN retry_count + 1 ELSE retry_count END
    WHERE id = push_id;
    
    -- También actualizar la notificación original
    UPDATE notifications n
    SET 
        push_sent = success,
        push_sent_at = CASE WHEN success THEN NOW() ELSE NULL END,
        push_error = error_msg
    FROM pending_push_notifications p
    WHERE p.id = push_id
    AND n.id::TEXT = p.notification_id::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- PASO 7: RLS para pending_push_notifications
ALTER TABLE pending_push_notifications ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Service role access only" ON pending_push_notifications;
CREATE POLICY "Service role access only"
    ON pending_push_notifications FOR ALL
    USING (true);  -- Permitir acceso (la función tiene SECURITY DEFINER)

-- PASO 8: Agregar política para permitir INSERT en notifications desde admin
-- (El usuario autenticado puede enviar notificaciones a cualquiera)
DROP POLICY IF EXISTS "Authenticated users can create notifications" ON notifications;
CREATE POLICY "Authenticated users can create notifications" ON notifications
    FOR INSERT TO authenticated
    WITH CHECK (true);  -- Cualquier usuario autenticado puede crear notificaciones

-- ═══════════════════════════════════════════════════════════════════════════════
-- VERIFICACIÓN: Ejecutar esto para verificar que todo está correcto
-- ═══════════════════════════════════════════════════════════════════════════════
-- SELECT * FROM pending_push_notifications LIMIT 5;
-- SELECT * FROM fcm_tokens LIMIT 5;
