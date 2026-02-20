-- ═══════════════════════════════════════════════════════════════════════════════
-- TABLA: live_streams - Transmisiones en vivo activas
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS public.live_streams (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    broadcaster_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    broadcaster_username TEXT NOT NULL,
    broadcaster_avatar TEXT,
    broadcaster_store_name TEXT,
    title TEXT DEFAULT 'En vivo',
    viewer_count INTEGER DEFAULT 0,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_live_streams_active ON public.live_streams(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_live_streams_broadcaster ON public.live_streams(broadcaster_id);
CREATE INDEX IF NOT EXISTS idx_live_streams_started ON public.live_streams(started_at DESC);

-- RLS (Row Level Security)
ALTER TABLE public.live_streams ENABLE ROW LEVEL SECURITY;

-- Política: Cualquiera puede ver streams activos
CREATE POLICY "Anyone can view active streams" ON public.live_streams
    FOR SELECT USING (is_active = TRUE);

-- Política: Solo el broadcaster puede crear/modificar su stream
CREATE POLICY "Broadcasters can manage their streams" ON public.live_streams
    FOR ALL USING (auth.uid() = broadcaster_id);

-- Política: Insertar stream (usuario autenticado)
CREATE POLICY "Authenticated users can start streams" ON public.live_streams
    FOR INSERT WITH CHECK (auth.uid() = broadcaster_id);

-- Habilitar Realtime para la tabla
ALTER PUBLICATION supabase_realtime ADD TABLE public.live_streams;

-- ═══════════════════════════════════════════════════════════════════════════════
-- FUNCIÓN: Limpiar streams inactivos antiguos (más de 24 horas)
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION cleanup_old_streams()
RETURNS void AS $$
BEGIN
    DELETE FROM public.live_streams
    WHERE is_active = FALSE
    AND started_at < NOW() - INTERVAL '24 hours';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
