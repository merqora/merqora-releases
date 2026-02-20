-- ═══════════════════════════════════════════════════════════════
-- POLÍTICA TEMPORAL: Permitir acceso admin a handshakes para testing
-- ═══════════════════════════════════════════════════════════════

-- Opción 1: Política para usuarios admin (basado en email)
-- Reemplaza 'tu-email-admin@ejemplo.com' con tu email de admin
CREATE POLICY "Admins can view all handshakes" ON handshake_transactions
    FOR SELECT USING (
        auth.jwt() ->> 'email' IN (
            'hello.clendova@gmail.com',
            'admin@rendly.com'
            -- Agrega más emails de admin aquí
        )
    );

-- Opción 2 (MÁS SIMPLE para testing): Permitir SELECT a cualquier usuario autenticado
-- ⚠️ SOLO PARA DESARROLLO - Remover en producción
DROP POLICY IF EXISTS "Anyone authenticated can view handshakes for testing" ON handshake_transactions;
CREATE POLICY "Anyone authenticated can view handshakes for testing" ON handshake_transactions
    FOR SELECT USING (auth.uid() IS NOT NULL);

-- Si necesitas que admins también puedan UPDATE (aceptar/rechazar):
DROP POLICY IF EXISTS "Admins can update all handshakes for testing" ON handshake_transactions;
CREATE POLICY "Admins can update all handshakes for testing" ON handshake_transactions
    FOR UPDATE USING (auth.uid() IS NOT NULL);
