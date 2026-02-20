-- =====================================================
-- TABLA: usuarios
-- Almacena los perfiles de usuarios de Rendly
-- =====================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Info básica
    email TEXT,
    username TEXT NOT NULL DEFAULT '',
    nombre TEXT,
    nombre_tienda TEXT,
    descripcion TEXT,
    genero TEXT,
    fecha_nacimiento DATE,
    fecha_registro TIMESTAMPTZ DEFAULT NOW(),
    
    -- Imágenes
    avatar_url TEXT,
    banner_url TEXT,
    
    -- Redes sociales
    facebook TEXT,
    whatsapp TEXT,
    twitter TEXT,
    instagram TEXT,
    linkedin TEXT,
    tiktok TEXT,
    
    -- Ubicación y actividad
    ubicacion TEXT,
    ultima_actividad TIMESTAMPTZ,
    
    -- Estado
    rol TEXT DEFAULT 'user',
    is_online BOOLEAN DEFAULT FALSE,
    last_online TIMESTAMPTZ,
    is_anonymous BOOLEAN DEFAULT FALSE,
    recibir_novedades BOOLEAN DEFAULT FALSE,
    tiene_tienda BOOLEAN DEFAULT FALSE,
    
    -- Stats
    reputacion INTEGER DEFAULT 0,
    seguidores INTEGER DEFAULT 0,
    
    -- Moderación
    baneado BOOLEAN DEFAULT FALSE,
    motivo_baneo TEXT
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_usuarios_user_id ON usuarios(user_id);
CREATE INDEX IF NOT EXISTS idx_usuarios_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);

-- RLS
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;

-- Política: Todos pueden ver usuarios
CREATE POLICY "Usuarios son públicos" ON usuarios
    FOR SELECT
    USING (true);

-- Política: Usuarios autenticados pueden crear su perfil
CREATE POLICY "Usuarios crean su perfil" ON usuarios
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios actualizan solo su perfil
CREATE POLICY "Usuarios actualizan su perfil" ON usuarios
    FOR UPDATE
    USING (auth.uid() = user_id);

-- GRANT
GRANT ALL ON usuarios TO authenticated;
GRANT SELECT ON usuarios TO anon;
