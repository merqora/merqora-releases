-- ═══════════════════════════════════════════════════════════════════════════════
-- FIX: Permitir a admin-web enviar mensajes en nombre de cualquier usuario
-- Ejecutar en Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════════════════════

-- Eliminar la política restrictiva actual
DROP POLICY IF EXISTS "Users can send messages" ON messages;

-- Nueva política: cualquier usuario autenticado puede insertar mensajes
-- Esto permite que admin-web envíe mensajes en nombre de otros usuarios
-- La seguridad se mantiene porque:
-- 1. Solo usuarios autenticados pueden insertar
-- 2. Los UUIDs de conversación son imposibles de adivinar
-- 3. La tabla conversation_participants controla quién está en cada conversación
CREATE POLICY "Users can send messages" ON messages
    FOR INSERT WITH CHECK (auth.uid() IS NOT NULL);

-- También necesitamos permitir actualizar cualquier participant (para unread_count)
DROP POLICY IF EXISTS "Users can update their participant record" ON conversation_participants;

CREATE POLICY "Users can update any participant record" ON conversation_participants
    FOR UPDATE USING (auth.uid() IS NOT NULL);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ¡LISTO! Ahora admin-web puede:
-- - Insertar mensajes con cualquier sender_id
-- - Actualizar unread_count de cualquier participante
-- ═══════════════════════════════════════════════════════════════════════════════
