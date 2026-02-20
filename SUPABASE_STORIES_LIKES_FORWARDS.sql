-- =====================================================
-- ACTUALIZACIÓN TABLA STORIES - LIKES Y REENVIADOS
-- Ejecutar en Supabase SQL Editor
-- =====================================================

-- 1. AGREGAR columnas likes y forwarded a la tabla stories
ALTER TABLE stories 
ADD COLUMN IF NOT EXISTS likes INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS forwarded INTEGER DEFAULT 0;

-- 2. Crear tabla para registrar likes de stories (para evitar duplicados)
CREATE TABLE IF NOT EXISTS story_likes (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(story_id, user_id)
);

-- 3. Crear tabla para registrar reenvíos de stories
CREATE TABLE IF NOT EXISTS story_forwards (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    story_id UUID NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    recipient_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    message TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_story_likes_story_id ON story_likes(story_id);
CREATE INDEX IF NOT EXISTS idx_story_likes_user_id ON story_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_story_forwards_story_id ON story_forwards(story_id);
CREATE INDEX IF NOT EXISTS idx_story_forwards_sender_id ON story_forwards(sender_id);

-- 5. Habilitar RLS en las nuevas tablas
ALTER TABLE story_likes ENABLE ROW LEVEL SECURITY;
ALTER TABLE story_forwards ENABLE ROW LEVEL SECURITY;

-- 6. Políticas RLS para story_likes

-- SELECT: usuarios pueden ver sus propios likes
CREATE POLICY "Users can view own story likes" ON story_likes
    FOR SELECT
    USING (user_id = auth.uid());

-- INSERT: usuarios autenticados pueden dar like
CREATE POLICY "Users can like stories" ON story_likes
    FOR INSERT
    WITH CHECK (user_id = auth.uid());

-- DELETE: usuarios pueden quitar su propio like
CREATE POLICY "Users can unlike stories" ON story_likes
    FOR DELETE
    USING (user_id = auth.uid());

-- 7. Políticas RLS para story_forwards

-- SELECT: usuarios pueden ver reenvíos donde son sender o recipient
CREATE POLICY "Users can view own story forwards" ON story_forwards
    FOR SELECT
    USING (sender_id = auth.uid() OR recipient_id = auth.uid());

-- INSERT: usuarios autenticados pueden reenviar stories
CREATE POLICY "Users can forward stories" ON story_forwards
    FOR INSERT
    WITH CHECK (sender_id = auth.uid());

-- 8. Función para dar like a una story (con actualización de contador)
CREATE OR REPLACE FUNCTION like_story(p_story_id UUID)
RETURNS void AS $$
BEGIN
    -- Insertar el like (ignorar si ya existe)
    INSERT INTO story_likes (story_id, user_id)
    VALUES (p_story_id, auth.uid())
    ON CONFLICT (story_id, user_id) DO NOTHING;
    
    -- Actualizar contador en la tabla stories
    UPDATE stories 
    SET likes = (SELECT COUNT(*) FROM story_likes WHERE story_id = p_story_id)
    WHERE id = p_story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 9. Función para quitar like de una story
CREATE OR REPLACE FUNCTION unlike_story(p_story_id UUID)
RETURNS void AS $$
BEGIN
    -- Eliminar el like
    DELETE FROM story_likes 
    WHERE story_id = p_story_id AND user_id = auth.uid();
    
    -- Actualizar contador en la tabla stories
    UPDATE stories 
    SET likes = (SELECT COUNT(*) FROM story_likes WHERE story_id = p_story_id)
    WHERE id = p_story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 10. Función para reenviar una story
CREATE OR REPLACE FUNCTION forward_story(
    p_story_id UUID,
    p_recipient_id UUID,
    p_message TEXT DEFAULT NULL
)
RETURNS void AS $$
BEGIN
    -- Insertar el reenvío
    INSERT INTO story_forwards (story_id, sender_id, recipient_id, message)
    VALUES (p_story_id, auth.uid(), p_recipient_id, p_message);
    
    -- Actualizar contador en la tabla stories
    UPDATE stories 
    SET forwarded = (SELECT COUNT(*) FROM story_forwards WHERE story_id = p_story_id)
    WHERE id = p_story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- INSTRUCCIONES:
-- 1. Copia todo este SQL
-- 2. Ve a Supabase Dashboard > SQL Editor
-- 3. Pega y ejecuta
-- 
-- NOTA: Esto añade las columnas 'likes' y 'forwarded' a la
-- tabla stories existente, y crea las tablas auxiliares
-- para registrar los likes y reenvíos individuales.
-- =====================================================
