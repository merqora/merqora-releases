-- ═══════════════════════════════════════════════════════════════════════════════
-- RENDLY - FCM TOKENS TABLE
-- Para notificaciones push con Firebase Cloud Messaging
-- ═══════════════════════════════════════════════════════════════════════════════

-- Tabla para almacenar tokens FCM de dispositivos
CREATE TABLE IF NOT EXISTS fcm_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token TEXT NOT NULL,
    device_info TEXT,
    platform TEXT DEFAULT 'android', -- android, ios, web
    app_version TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Un usuario puede tener múltiples dispositivos, pero cada token es único
    UNIQUE(token)
);

-- Índices para búsquedas eficientes
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_user_id ON fcm_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_fcm_tokens_active ON fcm_tokens(is_active) WHERE is_active = true;

-- Trigger para actualizar updated_at
CREATE OR REPLACE FUNCTION update_fcm_tokens_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS fcm_tokens_updated_at ON fcm_tokens;
CREATE TRIGGER fcm_tokens_updated_at
    BEFORE UPDATE ON fcm_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_fcm_tokens_updated_at();

-- RLS (Row Level Security)
ALTER TABLE fcm_tokens ENABLE ROW LEVEL SECURITY;

-- Políticas de seguridad
CREATE POLICY "Users can view their own tokens"
    ON fcm_tokens FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own tokens"
    ON fcm_tokens FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own tokens"
    ON fcm_tokens FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "Users can delete their own tokens"
    ON fcm_tokens FOR DELETE
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- TABLA DE NOTIFICACIONES PENDIENTES
-- Para enviar notificaciones cuando el usuario esté offline
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS pending_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL, -- message, like, comment, follow, sale, handshake, mention, promotion
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    data JSONB DEFAULT '{}', -- Datos adicionales (target_id, sender_id, etc.)
    image_url TEXT,
    is_sent BOOLEAN DEFAULT false,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '7 days')
);

CREATE INDEX IF NOT EXISTS idx_pending_notifications_user ON pending_notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_pending_notifications_unsent ON pending_notifications(is_sent) WHERE is_sent = false;

-- RLS
ALTER TABLE pending_notifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own notifications"
    ON pending_notifications FOR SELECT
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- FUNCIÓN PARA ENVIAR NOTIFICACIONES PUSH
-- Llamada desde Edge Functions o triggers
-- ═══════════════════════════════════════════════════════════════════════════════

-- Función RPC para obtener tokens de un usuario
CREATE OR REPLACE FUNCTION get_user_fcm_tokens(target_user_id UUID)
RETURNS TABLE(token TEXT, platform TEXT) AS $$
BEGIN
    RETURN QUERY
    SELECT ft.token, ft.platform
    FROM fcm_tokens ft
    WHERE ft.user_id = target_user_id
    AND ft.is_active = true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para crear notificación pendiente
CREATE OR REPLACE FUNCTION create_pending_notification(
    p_user_id UUID,
    p_type TEXT,
    p_title TEXT,
    p_body TEXT,
    p_data JSONB DEFAULT '{}',
    p_image_url TEXT DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    notification_id UUID;
BEGIN
    INSERT INTO pending_notifications (user_id, type, title, body, data, image_url)
    VALUES (p_user_id, p_type, p_title, p_body, p_data, p_image_url)
    RETURNING id INTO notification_id;
    
    RETURN notification_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- TRIGGERS PARA AUTO-NOTIFICACIONES
-- ═══════════════════════════════════════════════════════════════════════════════

-- Trigger: Notificar cuando alguien envía un mensaje
CREATE OR REPLACE FUNCTION notify_new_message()
RETURNS TRIGGER AS $$
DECLARE
    sender_name TEXT;
BEGIN
    -- Obtener nombre del remitente
    SELECT username INTO sender_name
    FROM usuarios
    WHERE id = NEW.sender_id;
    
    -- Crear notificación pendiente para el receptor
    IF NEW.receiver_id IS NOT NULL AND NEW.sender_id != NEW.receiver_id THEN
        PERFORM create_pending_notification(
            NEW.receiver_id,
            'message',
            sender_name,
            LEFT(NEW.content, 100),
            jsonb_build_object(
                'sender_id', NEW.sender_id,
                'sender_name', sender_name,
                'chat_id', NEW.conversation_id
            )
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger: Notificar cuando alguien da like a un post
CREATE OR REPLACE FUNCTION notify_new_like()
RETURNS TRIGGER AS $$
DECLARE
    liker_name TEXT;
    post_owner_id UUID;
    post_title TEXT;
BEGIN
    -- Obtener info
    SELECT username INTO liker_name FROM usuarios WHERE id = NEW.user_id;
    SELECT user_id, title INTO post_owner_id, post_title FROM posts WHERE id = NEW.post_id;
    
    -- No notificar si es tu propio post
    IF post_owner_id IS NOT NULL AND NEW.user_id != post_owner_id THEN
        PERFORM create_pending_notification(
            post_owner_id,
            'like',
            '❤️ Nuevo like',
            liker_name || ' le dio like a tu publicación',
            jsonb_build_object(
                'sender_id', NEW.user_id,
                'sender_name', liker_name,
                'post_id', NEW.post_id,
                'post_title', LEFT(post_title, 50)
            )
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger: Notificar nuevo seguidor
CREATE OR REPLACE FUNCTION notify_new_follower()
RETURNS TRIGGER AS $$
DECLARE
    follower_name TEXT;
    follower_avatar TEXT;
BEGIN
    SELECT username, avatar_url INTO follower_name, follower_avatar
    FROM usuarios WHERE id = NEW.follower_id;
    
    IF NEW.follower_id != NEW.following_id THEN
        PERFORM create_pending_notification(
            NEW.following_id,
            'follow',
            '👤 Nuevo seguidor',
            follower_name || ' empezó a seguirte',
            jsonb_build_object(
                'sender_id', NEW.follower_id,
                'sender_name', follower_name,
                'sender_avatar', follower_avatar
            )
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Aplicar triggers (descomenta según necesites)
-- DROP TRIGGER IF EXISTS on_new_message_notify ON messages;
-- CREATE TRIGGER on_new_message_notify AFTER INSERT ON messages
--     FOR EACH ROW EXECUTE FUNCTION notify_new_message();

-- DROP TRIGGER IF EXISTS on_new_like_notify ON post_likes;
-- CREATE TRIGGER on_new_like_notify AFTER INSERT ON post_likes
--     FOR EACH ROW EXECUTE FUNCTION notify_new_like();

-- DROP TRIGGER IF EXISTS on_new_follower_notify ON followers;
-- CREATE TRIGGER on_new_follower_notify AFTER INSERT ON followers
--     FOR EACH ROW EXECUTE FUNCTION notify_new_follower();
