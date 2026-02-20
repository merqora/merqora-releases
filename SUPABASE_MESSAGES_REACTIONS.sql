-- =====================================================
-- ACTUALIZACIÓN: Agregar columna reactions a messages
-- =====================================================
-- Ejecutar este script en Supabase SQL Editor para habilitar
-- las reacciones con emojis en los mensajes del chat.
-- =====================================================

-- Agregar columna reactions a la tabla messages
ALTER TABLE messages 
ADD COLUMN IF NOT EXISTS reactions JSONB DEFAULT NULL;

-- Comentario descriptivo
COMMENT ON COLUMN messages.reactions IS 'JSON con reacciones: {"emoji": ["user_id1", "user_id2"]}';

-- Índice para búsquedas en reactions (opcional, mejora rendimiento)
CREATE INDEX IF NOT EXISTS idx_messages_reactions 
ON messages USING GIN (reactions) 
WHERE reactions IS NOT NULL;

-- Habilitar realtime para la columna reactions
-- (Ya debería estar habilitado si messages está en realtime)

-- Verificar la estructura
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'messages' 
ORDER BY ordinal_position;
