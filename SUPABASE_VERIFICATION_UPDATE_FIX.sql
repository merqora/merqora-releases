-- ============================================
-- FIX: Permitir actualización de is_verified desde admin
-- ============================================
-- El problema es que RLS está bloqueando la actualización
-- de is_verified en la tabla usuarios desde admin-web.

-- Opción 1: Crear política para admins (recomendado)
-- Primero, necesitas una forma de identificar admins

-- Crear política que permita a usuarios autenticados actualizar is_verified
-- (Esto asume que admin-web usa service_role key o un admin autenticado)

-- Si usas service_role key en admin-web, RLS se bypassa automáticamente.
-- Verifica que supabaseClient.js use la service_role key, no la anon key.

-- Si necesitas una política específica para admins:
CREATE POLICY "Admins can update user verification" ON public.usuarios
    FOR UPDATE
    USING (true)
    WITH CHECK (true);

-- O más específicamente, solo para el campo is_verified:
-- Primero, verifica que la tabla usuarios tenga las políticas correctas

-- Listar políticas actuales (ejecutar en SQL Editor de Supabase):
-- SELECT * FROM pg_policies WHERE tablename = 'usuarios';

-- Si el problema persiste, una solución temporal es usar una función RPC:

CREATE OR REPLACE FUNCTION verify_user(target_user_id UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER  -- Esto hace que se ejecute con permisos del dueño
AS $$
BEGIN
    UPDATE public.usuarios
    SET 
        is_verified = true,
        verified_at = NOW(),
        verification_type = 'personal'
    WHERE user_id = target_user_id;
END;
$$;

-- Dar permisos para ejecutar la función
GRANT EXECUTE ON FUNCTION verify_user TO authenticated;
GRANT EXECUTE ON FUNCTION verify_user TO service_role;

-- Luego en admin-web puedes llamar:
-- await supabase.rpc('verify_user', { target_user_id: request.user_id })

-- ALTERNATIVA MÁS SIMPLE:
-- Verificar que admin-web use el service_role key en lugar del anon key
-- El service_role key bypassa RLS automáticamente

-- En admin-web/src/supabaseClient.js, debe verse algo así:
-- const supabase = createClient(
--   import.meta.env.VITE_SUPABASE_URL,
--   import.meta.env.VITE_SUPABASE_SERVICE_ROLE_KEY  -- NO anon key
-- )
