-- =====================================================
-- TABLA: rend_comments
-- Comentarios/Opiniones para videos Rends
-- =====================================================

CREATE TABLE IF NOT EXISTS public.rend_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rend_id UUID NOT NULL REFERENCES public.rends(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    likes INTEGER DEFAULT 0,
    rating INTEGER DEFAULT 5 CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_rend_comments_rend_id ON public.rend_comments(rend_id);
CREATE INDEX IF NOT EXISTS idx_rend_comments_user_id ON public.rend_comments(user_id);
CREATE INDEX IF NOT EXISTS idx_rend_comments_created_at ON public.rend_comments(created_at DESC);

-- =====================================================
-- POLÍTICAS RLS (Row Level Security)
-- =====================================================

ALTER TABLE public.rend_comments ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver comentarios
CREATE POLICY "Comentarios de rends son públicos"
ON public.rend_comments
FOR SELECT
USING (true);

-- Política: Usuarios autenticados pueden crear comentarios
CREATE POLICY "Usuarios pueden comentar rends"
ON public.rend_comments
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden actualizar sus propios comentarios
CREATE POLICY "Usuarios pueden actualizar sus comentarios de rends"
ON public.rend_comments
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden eliminar sus propios comentarios
CREATE POLICY "Usuarios pueden eliminar sus comentarios de rends"
ON public.rend_comments
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

-- =====================================================
-- GRANT permisos
-- =====================================================
GRANT SELECT ON public.rend_comments TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rend_comments TO authenticated;
