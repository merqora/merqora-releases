-- =====================================================
-- TABLA: posts
-- Almacena las publicaciones de los usuarios
-- Las imágenes se guardan en Cloudinary, aquí solo URLs
-- =====================================================

-- Crear tabla posts
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Contenido principal
    title TEXT DEFAULT '',
    description TEXT,
    price DECIMAL(10, 2) DEFAULT 0,
    category TEXT DEFAULT '',
    condition TEXT DEFAULT '',
    
    -- Tags para búsqueda
    tags TEXT[] DEFAULT '{}',
    
    -- URLs de imágenes (Cloudinary)
    images TEXT[] DEFAULT '{}',
    
    -- Estado de la publicación
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'sold', 'paused', 'deleted')),
    
    -- Contadores
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    shares_count INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Colección/Cover
    is_collection BOOLEAN DEFAULT FALSE,
    cover_type TEXT DEFAULT 'image',
    cover_image_index INTEGER DEFAULT 0,
    cover_url TEXT
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_status ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_category ON posts(category);

-- Trigger para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_posts_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_posts_updated_at ON posts;
CREATE TRIGGER trigger_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_posts_updated_at();

-- RLS (Row Level Security)
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

-- Política: Todos pueden ver posts activos
CREATE POLICY "Posts activos son públicos" ON posts
    FOR SELECT
    USING (status = 'active');

-- Política: Usuarios autenticados pueden ver sus propios posts (cualquier estado)
CREATE POLICY "Usuarios ven sus propios posts" ON posts
    FOR SELECT
    USING (auth.uid() = user_id);

-- Política: Usuarios autenticados pueden crear posts
CREATE POLICY "Usuarios pueden crear posts" ON posts
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios solo pueden actualizar sus propios posts
CREATE POLICY "Usuarios actualizan sus posts" ON posts
    FOR UPDATE
    USING (auth.uid() = user_id);

-- Política: Usuarios solo pueden eliminar sus propios posts
CREATE POLICY "Usuarios eliminan sus posts" ON posts
    FOR DELETE
    USING (auth.uid() = user_id);

-- =====================================================
-- GRANT permisos
-- =====================================================
GRANT ALL ON posts TO authenticated;
GRANT SELECT ON posts TO anon;
