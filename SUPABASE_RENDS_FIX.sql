-- =====================================================
-- FIX: Agregar columnas faltantes a la tabla rends
-- Ejecutar este SQL en Supabase SQL Editor
-- =====================================================

-- 1. Agregar columna saves_count (faltante en la tabla original)
ALTER TABLE public.rends 
ADD COLUMN IF NOT EXISTS saves_count INTEGER DEFAULT 0;

-- 2. Agregar columna product_image para imagen del producto anclado
-- (separada del thumbnail del video)
ALTER TABLE public.rends 
ADD COLUMN IF NOT EXISTS product_image TEXT;

-- =====================================================
-- VERIFICAR: La tabla comments debe existir
-- Si no existe, crear con esta estructura:
-- =====================================================

CREATE TABLE IF NOT EXISTS public.comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL,  -- Puede ser ID de post O de rend
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    likes INTEGER DEFAULT 0,
    rating INTEGER DEFAULT 5 CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para comments
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON public.comments(post_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON public.comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON public.comments(created_at DESC);

-- RLS para comments
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver comentarios
DROP POLICY IF EXISTS "Comentarios son públicos" ON public.comments;
CREATE POLICY "Comentarios son públicos"
ON public.comments
FOR SELECT
USING (true);

-- Política: Usuarios autenticados pueden crear comentarios
DROP POLICY IF EXISTS "Usuarios pueden crear comentarios" ON public.comments;
CREATE POLICY "Usuarios pueden crear comentarios"
ON public.comments
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden actualizar sus propios comentarios
DROP POLICY IF EXISTS "Usuarios pueden actualizar sus comentarios" ON public.comments;
CREATE POLICY "Usuarios pueden actualizar sus comentarios"
ON public.comments
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id);

-- Política: Usuarios pueden eliminar sus propios comentarios
DROP POLICY IF EXISTS "Usuarios pueden eliminar sus comentarios" ON public.comments;
CREATE POLICY "Usuarios pueden eliminar sus comentarios"
ON public.comments
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

-- Permisos
GRANT SELECT ON public.comments TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.comments TO authenticated;

-- =====================================================
-- VERIFICAR estructura final de rends
-- =====================================================
-- Ejecutar para ver las columnas actuales:
-- SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'rends';
