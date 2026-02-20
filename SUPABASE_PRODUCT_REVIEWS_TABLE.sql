-- =====================================================
-- SISTEMA UNIFICADO DE OPINIONES DE PRODUCTOS
-- =====================================================
-- Este sistema permite que las opiniones de Posts y Rends
-- se muestren juntas cuando pertenecen al mismo producto.

-- =====================================================
-- PASO 1: Agregar product_id a la tabla posts
-- =====================================================
ALTER TABLE public.posts 
ADD COLUMN IF NOT EXISTS product_id UUID DEFAULT gen_random_uuid();

-- Crear índice para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_posts_product_id ON public.posts(product_id);

-- =====================================================
-- PASO 2: Agregar product_id a la tabla rends
-- =====================================================
ALTER TABLE public.rends 
ADD COLUMN IF NOT EXISTS product_id UUID DEFAULT gen_random_uuid();

-- Crear índice para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_rends_product_id ON public.rends(product_id);

-- =====================================================
-- PASO 3: Crear tabla unificada de opiniones
-- =====================================================
CREATE TABLE IF NOT EXISTS public.product_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    source_type TEXT NOT NULL CHECK (source_type IN ('post', 'rend')),
    source_id UUID NOT NULL,
    text TEXT NOT NULL,
    rating INTEGER DEFAULT 5 CHECK (rating >= 1 AND rating <= 5),
    likes INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_product_reviews_product_id ON public.product_reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_user_id ON public.product_reviews(user_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_source ON public.product_reviews(source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_product_reviews_created_at ON public.product_reviews(created_at DESC);

-- =====================================================
-- POLÍTICAS RLS (Row Level Security)
-- =====================================================

ALTER TABLE public.product_reviews ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver opiniones
CREATE POLICY "Opiniones de productos son públicas"
ON public.product_reviews
FOR SELECT
USING (true);

-- Política: Usuarios autenticados pueden crear opiniones
CREATE POLICY "Usuarios pueden opinar sobre productos"
ON public.product_reviews
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden actualizar sus propias opiniones
CREATE POLICY "Usuarios pueden actualizar sus opiniones"
ON public.product_reviews
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden eliminar sus propias opiniones
CREATE POLICY "Usuarios pueden eliminar sus opiniones"
ON public.product_reviews
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

-- =====================================================
-- GRANT permisos
-- =====================================================
GRANT SELECT ON public.product_reviews TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.product_reviews TO authenticated;

-- =====================================================
-- FUNCIÓN: Obtener todas las opiniones de un producto
-- Busca por product_id en posts y rends, luego obtiene
-- todas las opiniones asociadas a esos product_ids
-- =====================================================
CREATE OR REPLACE FUNCTION get_product_reviews(p_source_id UUID)
RETURNS TABLE (
    review_id UUID,
    product_id UUID,
    user_id UUID,
    source_type TEXT,
    source_id UUID,
    text TEXT,
    rating INTEGER,
    likes INTEGER,
    created_at TIMESTAMPTZ,
    username TEXT,
    avatar_url TEXT
) AS $$
DECLARE
    v_product_id UUID;
BEGIN
    -- Primero intentar obtener product_id de posts
    SELECT p.product_id INTO v_product_id FROM public.posts p WHERE p.id = p_source_id;
    
    -- Si no se encontró, intentar con rends
    IF v_product_id IS NULL THEN
        SELECT r.product_id INTO v_product_id FROM public.rends r WHERE r.id = p_source_id;
    END IF;
    
    -- Si aún no hay product_id, usar el source_id como fallback
    IF v_product_id IS NULL THEN
        v_product_id := p_source_id;
    END IF;
    
    -- Retornar todas las opiniones del producto
    RETURN QUERY
    SELECT 
        pr.id as review_id,
        pr.product_id,
        pr.user_id,
        pr.source_type,
        pr.source_id,
        pr.text,
        pr.rating,
        pr.likes,
        pr.created_at,
        u.username,
        u.avatar_url
    FROM public.product_reviews pr
    LEFT JOIN public.usuarios u ON u.user_id = pr.user_id
    WHERE pr.product_id = v_product_id
    ORDER BY pr.created_at DESC;
END;
$$ LANGUAGE plpgsql;
