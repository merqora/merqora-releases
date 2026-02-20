-- =====================================================
-- TABLA STORIES PARA RENDLY
-- Ejecutar en Supabase SQL Editor
-- =====================================================

-- 1. ELIMINAR tabla existente (si existe)
DROP TABLE IF EXISTS stories CASCADE;

-- 2. CREAR nueva tabla stories
CREATE TABLE stories (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    media_url TEXT NOT NULL,
    media_type TEXT NOT NULL DEFAULT 'photo',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ DEFAULT (NOW() + INTERVAL '24 hours'),
    views INTEGER DEFAULT 0,
    is_highlight BOOLEAN DEFAULT FALSE
);

-- 3. Crear índices para mejor rendimiento
CREATE INDEX idx_stories_user_id ON stories(user_id);
CREATE INDEX idx_stories_expires_at ON stories(expires_at);
CREATE INDEX idx_stories_created_at ON stories(created_at DESC);

-- 4. Habilitar Row Level Security (RLS)
ALTER TABLE stories ENABLE ROW LEVEL SECURITY;

-- 5. Políticas RLS

-- Política para SELECT: usuarios pueden ver sus propias stories y stories no expiradas de otros
CREATE POLICY "Users can view active stories" ON stories
    FOR SELECT
    USING (
        expires_at > NOW() OR user_id = auth.uid()
    );

-- Política para INSERT: usuarios autenticados pueden crear sus propias stories
CREATE POLICY "Users can create own stories" ON stories
    FOR INSERT
    WITH CHECK (
        auth.uid() = user_id
    );

-- Política para UPDATE: usuarios pueden actualizar sus propias stories
CREATE POLICY "Users can update own stories" ON stories
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Política para DELETE: usuarios pueden eliminar sus propias stories
CREATE POLICY "Users can delete own stories" ON stories
    FOR DELETE
    USING (auth.uid() = user_id);

-- 6. Función para limpiar stories expiradas (opcional, ejecutar con pg_cron)
CREATE OR REPLACE FUNCTION cleanup_expired_stories()
RETURNS void AS $$
BEGIN
    DELETE FROM stories 
    WHERE expires_at < NOW() 
    AND is_highlight = FALSE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- INSTRUCCIONES:
-- 1. Copia todo este SQL
-- 2. Ve a Supabase Dashboard > SQL Editor
-- 3. Pega y ejecuta
-- =====================================================
