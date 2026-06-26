-- =====================================================
-- MIGRACIÓN: Campos de interacciones, privacidad,
-- hashtags y categorías para Rends
-- =====================================================

-- 1. Nuevos campos en la tabla rends
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS visibility TEXT DEFAULT 'public' CHECK (visibility IN ('public', 'followers', 'private'));
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS allow_opinions BOOLEAN DEFAULT true;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS allow_consults BOOLEAN DEFAULT true;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS allow_downloads BOOLEAN DEFAULT false;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS allow_shares BOOLEAN DEFAULT true;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS hashtags TEXT[] DEFAULT '{}';
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS category TEXT;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS location TEXT;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS product_image TEXT;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS product_id UUID;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS reviews_count INTEGER DEFAULT 0;
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS saves_count INTEGER DEFAULT 0;

-- Índice para búsqueda por categoría
CREATE INDEX IF NOT EXISTS idx_rends_category ON public.rends(category);
-- Índice GIN para búsqueda de hashtags
CREATE INDEX IF NOT EXISTS idx_rends_hashtags ON public.rends USING GIN(hashtags);

-- =====================================================
-- 2. TABLA: rend_hashtag_stats
-- Conteo de uso de hashtags para la sección de tendencias
-- =====================================================
CREATE TABLE IF NOT EXISTS public.rend_hashtag_stats (
    tag TEXT PRIMARY KEY,
    usage_count INTEGER DEFAULT 1,
    last_used_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER TABLE public.rend_hashtag_stats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Hashtag stats son públicos" ON public.rend_hashtag_stats FOR SELECT USING (true);
CREATE POLICY "Usuarios autenticados actualizan hashtags" ON public.rend_hashtag_stats FOR INSERT TO authenticated WITH CHECK (true);
CREATE POLICY "Usuarios autenticados incrementan hashtags" ON public.rend_hashtag_stats FOR UPDATE TO authenticated USING (true) WITH CHECK (true);

GRANT SELECT ON public.rend_hashtag_stats TO anon;
GRANT SELECT, INSERT, UPDATE ON public.rend_hashtag_stats TO authenticated;

-- =====================================================
-- 3. TABLA: rend_category_stats
-- Conteo de uso de categorías para tendencias
-- =====================================================
CREATE TABLE IF NOT EXISTS public.rend_category_stats (
    category TEXT PRIMARY KEY,
    usage_count INTEGER DEFAULT 1,
    last_used_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

ALTER TABLE public.rend_category_stats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Category stats son públicos" ON public.rend_category_stats FOR SELECT USING (true);
CREATE POLICY "Usuarios autenticados insertan category stats" ON public.rend_category_stats FOR INSERT TO authenticated WITH CHECK (true);
CREATE POLICY "Usuarios autenticados actualizan category stats" ON public.rend_category_stats FOR UPDATE TO authenticated USING (true) WITH CHECK (true);

GRANT SELECT ON public.rend_category_stats TO anon;
GRANT SELECT, INSERT, UPDATE ON public.rend_category_stats TO authenticated;

-- =====================================================
-- 4. RPC: Incrementar hashtag stats (upsert atómico)
-- =====================================================
CREATE OR REPLACE FUNCTION increment_hashtag_stats(p_tags TEXT[])
RETURNS VOID AS $$
BEGIN
    INSERT INTO public.rend_hashtag_stats (tag, usage_count, last_used_at)
    SELECT unnest(p_tags), 1, NOW()
    ON CONFLICT (tag) DO UPDATE
    SET usage_count = rend_hashtag_stats.usage_count + 1,
        last_used_at = NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- 5. RPC: Incrementar category stats (upsert atómico)
-- =====================================================
CREATE OR REPLACE FUNCTION increment_category_stats(p_category TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO public.rend_category_stats (category, usage_count, last_used_at)
    VALUES (p_category, 1, NOW())
    ON CONFLICT (category) DO UPDATE
    SET usage_count = rend_category_stats.usage_count + 1,
        last_used_at = NOW();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- 6. RPC: Obtener hashtags trending (top 20 por uso)
-- =====================================================
CREATE OR REPLACE FUNCTION get_trending_hashtags(p_limit INTEGER DEFAULT 20)
RETURNS TABLE(tag TEXT, usage_count INTEGER) AS $$
BEGIN
    RETURN QUERY
    SELECT h.tag, h.usage_count
    FROM public.rend_hashtag_stats h
    ORDER BY h.usage_count DESC, h.last_used_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- 7. RPC: Obtener categorías populares (ordenadas por uso)
-- =====================================================
CREATE OR REPLACE FUNCTION get_popular_categories(p_limit INTEGER DEFAULT 12)
RETURNS TABLE(category TEXT, usage_count INTEGER) AS $$
BEGIN
    RETURN QUERY
    SELECT c.category, c.usage_count
    FROM public.rend_category_stats c
    ORDER BY c.usage_count DESC, c.last_used_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
