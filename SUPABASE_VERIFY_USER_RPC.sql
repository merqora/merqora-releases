-- ============================================
-- Función RPC para verificar usuarios (bypasa RLS)
-- ============================================
-- Ejecutar en SQL Editor de Supabase

CREATE OR REPLACE FUNCTION verify_user(target_user_id UUID, v_type TEXT DEFAULT 'personal')
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.usuarios
    SET 
        is_verified = true,
        verified_at = NOW(),
        verification_type = v_type
    WHERE user_id = target_user_id;
END;
$$;

-- Dar permisos
GRANT EXECUTE ON FUNCTION verify_user TO authenticated;
GRANT EXECUTE ON FUNCTION verify_user TO anon;
