-- ═══════════════════════════════════════════════════════════════
-- RLS Policy: Permitir DELETE en live_streams para admins
-- ═══════════════════════════════════════════════════════════════

-- Habilitar RLS si no está
ALTER TABLE public.live_streams ENABLE ROW LEVEL SECURITY;

-- Eliminar policy existente si ya hay una
DROP POLICY IF EXISTS admin_delete_live_streams ON public.live_streams;

-- Policy: solo usuarios verificados pueden eliminar transmisiones
CREATE POLICY admin_delete_live_streams ON public.live_streams
  FOR DELETE
  USING (
    auth.uid() IN (
      SELECT user_id FROM public.usuarios WHERE is_verified = true
    )
  );

COMMENT ON POLICY admin_delete_live_streams ON public.live_streams IS
  'Permite eliminar transmisiones solo a usuarios verificados (admin)';
