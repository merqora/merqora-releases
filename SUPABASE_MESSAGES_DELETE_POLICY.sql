-- ═══════════════════════════════════════════════════════════════════════════════
-- AGREGAR POLÍTICA DELETE PARA MENSAJES
-- Ejecutar en Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════════════════════

-- Eliminar política anterior si existe
DROP POLICY IF EXISTS "Users can delete their own messages" ON messages;

-- Crear nueva política para permitir DELETE
CREATE POLICY "Users can delete their own messages" ON messages
    FOR DELETE USING (sender_id = auth.uid());

-- ═══════════════════════════════════════════════════════════════════════════════
-- ¡LISTO! Ahora los usuarios pueden eliminar sus propios mensajes
-- ═══════════════════════════════════════════════════════════════════════════════
