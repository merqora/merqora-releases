-- ============================================================================
-- SISTEMA DE SEGUIDORES Y CLIENTES - Rendly
-- ============================================================================
-- Este script elimina las columnas antiguas de la tabla usuarios y crea
-- una nueva tabla 'followers' para manejar seguidores y clientes.
-- ============================================================================

-- PASO 1: Eliminar columnas antiguas de la tabla usuarios
-- (Ejecutar primero si existen estas columnas)
-- ============================================================================

ALTER TABLE usuarios 
DROP COLUMN IF EXISTS reputacion;

ALTER TABLE usuarios 
DROP COLUMN IF EXISTS seguidores;

-- PASO 2: Agregar columna is_verified a usuarios (si no existe)
-- ============================================================================

ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;

-- PASO 3: Crear tabla de seguidores/clientes
-- ============================================================================

CREATE TABLE IF NOT EXISTS followers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Usuario que sigue
    follower_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Usuario siendo seguido
    followed_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Tipo de relación: false = seguidor normal, true = cliente
    is_client BOOLEAN DEFAULT false,
    
    -- Estado de solicitud: true = pendiente de aceptación del vendedor
    is_pending BOOLEAN DEFAULT false,
    
    -- Fecha de creación
    created_at TIMESTAMPTZ DEFAULT now(),
    
    -- Fecha de última actualización (para cuando cambia de seguidor a cliente)
    updated_at TIMESTAMPTZ DEFAULT now(),
    
    -- Evitar duplicados: un usuario solo puede seguir a otro una vez
    UNIQUE(follower_id, followed_id),
    
    -- Evitar seguirse a uno mismo
    CONSTRAINT no_self_follow CHECK (follower_id != followed_id)
);

-- Si la tabla ya existe, agregar la columna is_pending
ALTER TABLE followers ADD COLUMN IF NOT EXISTS is_pending BOOLEAN DEFAULT false;

-- PASO 4: Crear índices para búsquedas rápidas
-- ============================================================================

-- Índice para buscar seguidores de un usuario
CREATE INDEX IF NOT EXISTS idx_followers_followed_id ON followers(followed_id);

-- Índice para buscar a quién sigue un usuario
CREATE INDEX IF NOT EXISTS idx_followers_follower_id ON followers(follower_id);

-- Índice para filtrar por clientes
CREATE INDEX IF NOT EXISTS idx_followers_is_client ON followers(is_client) WHERE is_client = true;

-- PASO 5: Habilitar Row Level Security (RLS)
-- ============================================================================

ALTER TABLE followers ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver las relaciones de seguimiento
CREATE POLICY "Followers are viewable by everyone" ON followers
    FOR SELECT USING (true);

-- Política: Solo usuarios autenticados pueden seguir
CREATE POLICY "Users can follow others" ON followers
    FOR INSERT WITH CHECK (auth.uid() = follower_id);

-- Política: Solo el seguidor puede actualizar su relación (ej: pasar a cliente)
CREATE POLICY "Users can update their follow status" ON followers
    FOR UPDATE USING (auth.uid() = follower_id);

-- Política: Solo el seguidor puede dejar de seguir
CREATE POLICY "Users can unfollow" ON followers
    FOR DELETE USING (auth.uid() = follower_id);

-- PASO 6: Función para contar seguidores
-- ============================================================================

CREATE OR REPLACE FUNCTION get_followers_count(user_id UUID)
RETURNS INTEGER AS $$
BEGIN
    RETURN (SELECT COUNT(*) FROM followers WHERE followed_id = user_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- PASO 7: Función para contar clientes
-- ============================================================================

CREATE OR REPLACE FUNCTION get_clients_count(user_id UUID)
RETURNS INTEGER AS $$
BEGIN
    RETURN (SELECT COUNT(*) FROM followers WHERE followed_id = user_id AND is_client = true);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- PASO 8: Función para calcular reputación basada en ventas/reviews
-- ============================================================================
-- La reputación se calculará dinámicamente basándose en:
-- - Número de clientes
-- - Reviews positivas (futuro)
-- - Tiempo en la plataforma
-- - Productos vendidos (futuro)

CREATE OR REPLACE FUNCTION get_reputation(user_id UUID)
RETURNS INTEGER AS $$
DECLARE
    clients_count INTEGER;
    base_reputation INTEGER := 70; -- Reputación base
BEGIN
    -- Contar clientes
    SELECT COUNT(*) INTO clients_count 
    FROM followers 
    WHERE followed_id = user_id AND is_client = true;
    
    -- Calcular reputación: base + bonus por clientes (max 30 puntos extra)
    RETURN LEAST(100, base_reputation + (clients_count * 2));
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- NOTAS DE USO:
-- ============================================================================
-- 
-- Para seguir a alguien (como seguidor normal):
-- INSERT INTO followers (follower_id, followed_id, is_client) 
-- VALUES ('mi_user_id', 'otro_user_id', false);
--
-- Para convertirse en cliente:
-- UPDATE followers SET is_client = true, updated_at = now()
-- WHERE follower_id = 'mi_user_id' AND followed_id = 'otro_user_id';
--
-- Para dejar de seguir:
-- DELETE FROM followers 
-- WHERE follower_id = 'mi_user_id' AND followed_id = 'otro_user_id';
--
-- Para obtener conteos:
-- SELECT get_followers_count('user_id');
-- SELECT get_clients_count('user_id');
-- SELECT get_reputation('user_id');
-- ============================================================================
