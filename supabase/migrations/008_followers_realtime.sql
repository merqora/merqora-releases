-- ============================================================================
-- HABILITAR REPLICATION EN TIEMPO REAL PARA LA TABLA followers
-- ============================================================================
-- Necesario para que FollowersRepository pueda escuchar cambios en las
-- relaciones de seguimiento (ej: is_pending false → true) y actualizar
-- la UI del perfil en tiempo real.
-- ============================================================================

-- Agregar la tabla followers a la publicación de Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE followers;

-- También aseguramos que replica identity esté configurado correctamente
-- para que los eventos UPDATE incluyan los datos viejos y nuevos
ALTER TABLE followers REPLICA IDENTITY FULL;
