-- ═══════════════════════════════════════════════════════════════════════════════
-- SISTEMA DE PRESENCIA Y ESTADOS DE MENSAJES - RENDLY CHAT
-- Ejecutar en Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════════
-- PASO 1: AGREGAR COLUMNA DE ESTADO A MENSAJES (para sistema de ticks)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Agregar columna 'status' a messages si no existe
-- Valores: 'sent' (1 tick), 'delivered' (2 ticks), 'read' (2 ticks azules)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'status') THEN
        ALTER TABLE messages ADD COLUMN status TEXT DEFAULT 'sent';
    END IF;
END $$;

-- Agregar columna 'delivered_at' para saber cuándo se entregó
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'delivered_at') THEN
        ALTER TABLE messages ADD COLUMN delivered_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- Agregar columna 'read_at' para saber cuándo se leyó
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'messages' AND column_name = 'read_at') THEN
        ALTER TABLE messages ADD COLUMN read_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;

-- Índice para búsquedas por status
CREATE INDEX IF NOT EXISTS idx_messages_status ON messages(status);

-- ═══════════════════════════════════════════════════════════════════════════════
-- PASO 2: TABLA DE PRESENCIA DE USUARIOS (opcional, para persistencia)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Tabla para rastrear última actividad de usuarios
CREATE TABLE IF NOT EXISTS user_presence (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    is_online BOOLEAN DEFAULT FALSE,
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Habilitar RLS
ALTER TABLE user_presence ENABLE ROW LEVEL SECURITY;

-- Políticas RLS para user_presence
DROP POLICY IF EXISTS "Users can view all presence" ON user_presence;
CREATE POLICY "Users can view all presence" ON user_presence
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "Users can update their presence" ON user_presence;
CREATE POLICY "Users can update their presence" ON user_presence
    FOR UPDATE USING (user_id = auth.uid());

DROP POLICY IF EXISTS "Users can insert their presence" ON user_presence;
CREATE POLICY "Users can insert their presence" ON user_presence
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Habilitar Realtime para user_presence
ALTER PUBLICATION supabase_realtime ADD TABLE user_presence;

-- ═══════════════════════════════════════════════════════════════════════════════
-- PASO 3: POLÍTICA RLS PARA ACTUALIZAR MENSAJES (MARCAR COMO LEÍDOS)
-- ═══════════════════════════════════════════════════════════════════════════════

-- IMPORTANTE: Esta política permite a los participantes de una conversación
-- actualizar los mensajes (para marcarlos como leídos/entregados)

-- Primero, eliminar política anterior si existe
DROP POLICY IF EXISTS "Participants can update messages" ON messages;

-- Política para que los participantes de una conversación puedan actualizar mensajes
-- (necesario para marcar como leídos los mensajes que reciben)
CREATE POLICY "Participants can update messages" ON messages
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM conversation_participants cp
            WHERE cp.conversation_id = messages.conversation_id
            AND cp.user_id = auth.uid()
        )
    );

-- ═══════════════════════════════════════════════════════════════════════════════
-- PASO 4: FUNCIONES HELPER (OPCIONALES)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Función para marcar mensajes como entregados
CREATE OR REPLACE FUNCTION mark_messages_delivered(p_conversation_id UUID, p_user_id UUID)
RETURNS void AS $$
BEGIN
    UPDATE messages 
    SET status = 'delivered', delivered_at = NOW()
    WHERE conversation_id = p_conversation_id 
      AND sender_id != p_user_id 
      AND status = 'sent';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para marcar mensajes como leídos
CREATE OR REPLACE FUNCTION mark_messages_read(p_conversation_id UUID, p_user_id UUID)
RETURNS void AS $$
BEGIN
    UPDATE messages 
    SET status = 'read', read_at = NOW(), is_read = TRUE
    WHERE conversation_id = p_conversation_id 
      AND sender_id != p_user_id 
      AND status IN ('sent', 'delivered');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- ¡LISTO! 
-- - messages ahora tiene: status, delivered_at, read_at
-- - user_presence rastrea online/offline
-- - Funciones para actualizar estados de mensajes
-- ═══════════════════════════════════════════════════════════════════════════════
