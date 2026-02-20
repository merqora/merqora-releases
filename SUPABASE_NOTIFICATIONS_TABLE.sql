-- Tabla de notificaciones para Rendly
-- Ejecutar en Supabase SQL Editor

CREATE TABLE IF NOT EXISTS notifications (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    recipient_id TEXT NOT NULL,           -- user_id del usuario que recibe la notificación
    sender_id TEXT NOT NULL,              -- user_id del usuario que genera la acción
    sender_username TEXT NOT NULL,        -- username del sender para mostrar
    sender_avatar TEXT,                   -- avatar del sender
    type TEXT NOT NULL,                   -- 'like', 'save', 'follow', 'comment', 'mention'
    post_id TEXT,                         -- ID del post relacionado (si aplica)
    post_image TEXT,                      -- Primera imagen del post (para preview)
    message TEXT,                         -- Mensaje adicional (para comentarios)
    is_read BOOLEAN DEFAULT false,        -- Si ya fue leída
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(recipient_id, is_read);

-- Habilitar Row Level Security
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- Política: Los usuarios pueden ver sus propias notificaciones
CREATE POLICY "Users can view own notifications" ON notifications
    FOR SELECT TO authenticated
    USING (auth.uid()::text = recipient_id);

-- Política: Los usuarios autenticados pueden crear notificaciones
CREATE POLICY "Authenticated users can create notifications" ON notifications
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid()::text = sender_id);

-- Política: Los usuarios pueden marcar como leídas sus notificaciones
CREATE POLICY "Users can update own notifications" ON notifications
    FOR UPDATE TO authenticated
    USING (auth.uid()::text = recipient_id);

-- Política: Los usuarios pueden eliminar sus notificaciones
CREATE POLICY "Users can delete own notifications" ON notifications
    FOR DELETE TO authenticated
    USING (auth.uid()::text = recipient_id);

-- Habilitar Realtime para esta tabla
ALTER PUBLICATION supabase_realtime ADD TABLE notifications;

-- Función para limpiar notificaciones antiguas (más de 30 días)
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS void AS $$
BEGIN
    DELETE FROM notifications WHERE created_at < NOW() - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;
