-- ═══════════════════════════════════════════════════════════════════════════════
-- RENDLY - FCM PUSH NOTIFICATION TRIGGER
-- Envía notificaciones push automáticamente cuando se inserta en 'notifications'
-- ═══════════════════════════════════════════════════════════════════════════════

-- Primero, agregar columna para tracking si ya se envió la push notification
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS push_sent BOOLEAN DEFAULT false;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS push_sent_at TIMESTAMPTZ;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS push_error TEXT;

-- Función que prepara y envía notificación push via Edge Function
CREATE OR REPLACE FUNCTION send_fcm_push_notification()
RETURNS TRIGGER AS $$
DECLARE
    fcm_tokens_result RECORD;
    notification_title TEXT;
    notification_body TEXT;
    notification_image TEXT;
    recipient_tokens TEXT[];
BEGIN
    -- Construir título y cuerpo según el tipo de notificación
    CASE NEW.type
        WHEN 'like' THEN
            notification_title := '❤️ ' || COALESCE(NEW.sender_username, 'Alguien') || ' le dio like a tu publicación';
            notification_body := 'Toca para ver tu publicación';
        WHEN 'comment' THEN
            notification_title := '💬 ' || COALESCE(NEW.sender_username, 'Alguien') || ' comentó tu publicación';
            notification_body := COALESCE(NEW.message, 'Nuevo comentario en tu publicación');
        WHEN 'follow' THEN
            notification_title := '👤 ' || COALESCE(NEW.sender_username, 'Alguien') || ' empezó a seguirte';
            notification_body := 'Toca para ver su perfil';
        WHEN 'message' THEN
            notification_title := '💬 ' || COALESCE(NEW.sender_username, 'Nuevo mensaje');
            notification_body := COALESCE(NEW.message, 'Tienes un nuevo mensaje');
        WHEN 'sale' THEN
            notification_title := '🎉 ¡Nueva venta!';
            notification_body := COALESCE(NEW.message, 'Tienes una nueva venta');
        WHEN 'handshake' THEN
            notification_title := '🤝 Solicitud de handshake';
            notification_body := COALESCE(NEW.sender_username, 'Alguien') || ' quiere hacer un handshake contigo';
        WHEN 'mention' THEN
            notification_title := '📢 ' || COALESCE(NEW.sender_username, 'Alguien') || ' te mencionó';
            notification_body := COALESCE(NEW.message, 'Te mencionaron en una publicación');
        ELSE
            notification_title := 'Rendly';
            notification_body := COALESCE(NEW.message, 'Tienes una nueva notificación');
    END CASE;
    
    -- Imagen de la notificación (si hay)
    notification_image := NEW.post_image;
    
    -- Obtener tokens FCM activos del destinatario
    SELECT ARRAY_AGG(token) INTO recipient_tokens
    FROM fcm_tokens
    WHERE user_id = NEW.recipient_id
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
            NEW.recipient_id,
            recipient_tokens,
            notification_title,
            notification_body,
            notification_image,
            jsonb_build_object(
                'type', NEW.type,
                'notification_id', NEW.id,
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

-- Tabla de cola de push notifications pendientes
CREATE TABLE IF NOT EXISTS pending_push_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    notification_id UUID REFERENCES notifications(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL,
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

-- Trigger que se activa al insertar una notificación
DROP TRIGGER IF EXISTS on_notification_insert_send_push ON notifications;
CREATE TRIGGER on_notification_insert_send_push
    AFTER INSERT ON notifications
    FOR EACH ROW
    EXECUTE FUNCTION send_fcm_push_notification();

-- ═══════════════════════════════════════════════════════════════════════════════
-- FUNCIÓN RPC PARA ENVIAR PUSH MANUALMENTE (llamada desde Edge Function o Admin)
-- ═══════════════════════════════════════════════════════════════════════════════

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

-- Marcar push como enviado
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
    AND n.id = p.notification_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- POLÍTICA RLS PARA ADMIN
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE pending_push_notifications ENABLE ROW LEVEL SECURITY;

-- Solo el servicio puede acceder a esta tabla
CREATE POLICY "Service role access only"
    ON pending_push_notifications FOR ALL
    USING (auth.role() = 'service_role');
