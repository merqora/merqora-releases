-- =====================================================
-- TABLA: rends
-- Almacena los videos cortos tipo TikTok/Reels
-- =====================================================

CREATE TABLE IF NOT EXISTS public.rends (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL DEFAULT '',
    description TEXT,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    product_title TEXT,
    product_price NUMERIC(10,2),
    product_link TEXT,
    duration INTEGER DEFAULT 0,
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    shares_count INTEGER DEFAULT 0,
    status TEXT DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'deleted')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_rends_user_id ON public.rends(user_id);
CREATE INDEX IF NOT EXISTS idx_rends_created_at ON public.rends(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_rends_status ON public.rends(status);

-- Trigger para updated_at
CREATE OR REPLACE FUNCTION update_rends_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_rends_updated_at ON public.rends;
CREATE TRIGGER trigger_rends_updated_at
    BEFORE UPDATE ON public.rends
    FOR EACH ROW
    EXECUTE FUNCTION update_rends_updated_at();

-- =====================================================
-- POLÍTICAS RLS (Row Level Security)
-- =====================================================

ALTER TABLE public.rends ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver rends activos
CREATE POLICY "Rends activos son públicos"
ON public.rends
FOR SELECT
USING (status = 'active');

-- Política: Usuarios autenticados pueden crear rends
CREATE POLICY "Usuarios pueden crear rends"
ON public.rends
FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden actualizar sus propios rends
CREATE POLICY "Usuarios pueden actualizar sus rends"
ON public.rends
FOR UPDATE
TO authenticated
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios pueden eliminar sus propios rends
CREATE POLICY "Usuarios pueden eliminar sus rends"
ON public.rends
FOR DELETE
TO authenticated
USING (auth.uid() = user_id);

-- =====================================================
-- GRANT permisos
-- =====================================================
GRANT SELECT ON public.rends TO anon;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.rends TO authenticated;
