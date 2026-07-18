-- ============================================================================
-- SOLICITUDES DE SEGUIMIENTO PARA PERFILES PRIVADOS
-- ============================================================================
-- Este script permite que los dueños de perfiles privados acepten o
-- rechacen solicitudes de seguimiento.
-- ============================================================================

-- PASO 1: Actualizar RLS policies para permitir al dueño del perfil
-- aceptar (UPDATE) o rechazar (DELETE) solicitudes pendientes
-- ============================================================================

DROP POLICY IF EXISTS "Users can update their follow status" ON followers;
CREATE POLICY "Users can update follow status" ON followers
    FOR UPDATE USING (
        auth.uid() = follower_id OR 
        (auth.uid() = followed_id AND is_pending = true)
    );

DROP POLICY IF EXISTS "Users can unfollow" ON followers;
CREATE POLICY "Users can unfollow" ON followers
    FOR DELETE USING (
        auth.uid() = follower_id OR
        (auth.uid() = followed_id AND is_pending = true)
    );
