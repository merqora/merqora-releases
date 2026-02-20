-- ═══════════════════════════════════════════════════════════════════════════
-- FIX RLS PARA VERIFICACIÓN - PERMITIR LECTURA DESDE ADMIN-WEB
-- ═══════════════════════════════════════════════════════════════════════════
-- Ejecutar este script en Supabase SQL Editor

-- Primero eliminamos las políticas existentes que pueden estar causando problemas
DROP POLICY IF EXISTS "Users can view own verification requests" ON verification_requests;
DROP POLICY IF EXISTS "Users can create verification requests" ON verification_requests;
DROP POLICY IF EXISTS "Service role can update verification requests" ON verification_requests;
DROP POLICY IF EXISTS "Admins can view all verification requests" ON verification_requests;
DROP POLICY IF EXISTS "Allow all select for authenticated" ON verification_requests;

-- Política: Usuarios autenticados pueden ver TODAS las solicitudes
-- Esto permite que el admin-web (que usa autenticación) pueda ver todas las solicitudes
CREATE POLICY "Authenticated users can view all verification requests"
    ON verification_requests FOR SELECT
    TO authenticated
    USING (true);

-- Política: Usuarios pueden crear sus propias solicitudes
CREATE POLICY "Users can create own verification requests"
    ON verification_requests FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Política: Usuarios autenticados pueden actualizar cualquier solicitud
-- Necesario para que el admin pueda aprobar/rechazar
CREATE POLICY "Authenticated users can update verification requests"
    ON verification_requests FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- Política: Usuarios pueden eliminar sus propias solicitudes pendientes
CREATE POLICY "Users can delete own pending requests"
    ON verification_requests FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id AND status = 'pending');

-- Verificar que RLS está habilitado
ALTER TABLE verification_requests ENABLE ROW LEVEL SECURITY;

-- Mensaje de confirmación
DO $$
BEGIN
    RAISE NOTICE 'RLS policies actualizadas correctamente para verification_requests';
END $$;
