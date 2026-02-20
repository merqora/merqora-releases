-- ═══════════════════════════════════════════════════════════════════════════════
-- TABLA: story_views
-- Registro de usuarios que vieron cada story
-- ═══════════════════════════════════════════════════════════════════════════════

-- Crear tabla story_views
CREATE TABLE IF NOT EXISTS public.story_views (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    story_id UUID NOT NULL REFERENCES public.stories(id) ON DELETE CASCADE,
    viewer_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    viewed_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Evitar duplicados: un usuario solo puede ver una story una vez
    UNIQUE(story_id, viewer_id)
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_story_views_story_id ON public.story_views(story_id);
CREATE INDEX IF NOT EXISTS idx_story_views_viewer_id ON public.story_views(viewer_id);
CREATE INDEX IF NOT EXISTS idx_story_views_viewed_at ON public.story_views(viewed_at DESC);

-- Habilitar RLS
ALTER TABLE public.story_views ENABLE ROW LEVEL SECURITY;

-- Políticas RLS

-- Los usuarios pueden ver las vistas de sus propias stories
CREATE POLICY "Users can view their own story views" ON public.story_views
    FOR SELECT
    USING (
        story_id IN (
            SELECT id FROM public.stories WHERE user_id = auth.uid()
        )
    );

-- Los usuarios pueden insertar vistas (registrar que vieron una story)
CREATE POLICY "Users can insert story views" ON public.story_views
    FOR INSERT
    WITH CHECK (viewer_id = auth.uid());

-- Función para obtener el conteo de vistas de una story
CREATE OR REPLACE FUNCTION get_story_views_count(p_story_id UUID)
RETURNS INTEGER AS $$
BEGIN
    RETURN (SELECT COUNT(*) FROM public.story_views WHERE story_id = p_story_id);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para registrar una vista y actualizar el contador en stories
CREATE OR REPLACE FUNCTION record_story_view(p_story_id UUID, p_viewer_id UUID)
RETURNS VOID AS $$
BEGIN
    -- Insertar vista (ignorar si ya existe)
    INSERT INTO public.story_views (story_id, viewer_id)
    VALUES (p_story_id, p_viewer_id)
    ON CONFLICT (story_id, viewer_id) DO NOTHING;
    
    -- Actualizar contador en la tabla stories
    UPDATE public.stories
    SET views = (SELECT COUNT(*) FROM public.story_views WHERE story_id = p_story_id)
    WHERE id = p_story_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Comentarios
COMMENT ON TABLE public.story_views IS 'Registro de usuarios que vieron cada story';
COMMENT ON COLUMN public.story_views.story_id IS 'ID de la story vista';
COMMENT ON COLUMN public.story_views.viewer_id IS 'ID del usuario que vio la story';
COMMENT ON COLUMN public.story_views.viewed_at IS 'Fecha y hora en que se vio la story';
