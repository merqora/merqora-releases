-- =====================================================
-- RENDLY AI SUPPORT - FIX PARA TABLAS
-- =====================================================
-- Ejecutar en Supabase SQL Editor para arreglar las FK
-- =====================================================

-- 0. PRIMERO: Eliminar todas las políticas RLS que bloquean los cambios
DROP POLICY IF EXISTS "Users can view own conversations" ON support_conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON support_conversations;
DROP POLICY IF EXISTS "Users can view own messages" ON support_messages;
DROP POLICY IF EXISTS "Users can create messages" ON support_messages;
DROP POLICY IF EXISTS "Users can submit feedback" ON ai_feedback;
DROP POLICY IF EXISTS "Users can view own feedback" ON ai_feedback;

-- 1. Deshabilitar RLS ANTES de modificar columnas
ALTER TABLE support_conversations DISABLE ROW LEVEL SECURITY;
ALTER TABLE support_messages DISABLE ROW LEVEL SECURITY;
ALTER TABLE ai_feedback DISABLE ROW LEVEL SECURITY;
ALTER TABLE ai_escalations DISABLE ROW LEVEL SECURITY;
ALTER TABLE ai_stats_daily DISABLE ROW LEVEL SECURITY;

-- 2. Eliminar foreign keys restrictivas
ALTER TABLE support_conversations 
DROP CONSTRAINT IF EXISTS support_conversations_user_id_fkey;

ALTER TABLE ai_feedback 
DROP CONSTRAINT IF EXISTS ai_feedback_user_id_fkey;

ALTER TABLE ai_escalations 
DROP CONSTRAINT IF EXISTS ai_escalations_user_id_fkey;

ALTER TABLE ai_escalations 
DROP CONSTRAINT IF EXISTS ai_escalations_support_agent_id_fkey;

-- 3. Cambiar user_id a TEXT para flexibilidad
ALTER TABLE support_conversations 
ALTER COLUMN user_id TYPE TEXT USING user_id::TEXT;

ALTER TABLE ai_feedback 
ALTER COLUMN user_id TYPE TEXT USING user_id::TEXT;

ALTER TABLE ai_escalations 
ALTER COLUMN user_id TYPE TEXT USING user_id::TEXT;

ALTER TABLE ai_escalations 
ALTER COLUMN support_agent_id TYPE TEXT USING support_agent_id::TEXT;

-- 4. Asegurar que el trigger de stats funcione correctamente
CREATE OR REPLACE FUNCTION update_ai_daily_stats()
RETURNS TRIGGER AS $$
DECLARE
    stat_date DATE;
BEGIN
    stat_date := DATE(NEW.created_at);
    
    INSERT INTO ai_stats_daily (date, total_messages, ai_resolved, escalated)
    VALUES (
        stat_date, 
        1,
        CASE WHEN NEW.role = 'ai' THEN 1 ELSE 0 END,
        0
    )
    ON CONFLICT (date) DO UPDATE SET
        total_messages = ai_stats_daily.total_messages + 1,
        ai_resolved = ai_stats_daily.ai_resolved + 
            CASE WHEN NEW.role = 'ai' THEN 1 ELSE 0 END,
        updated_at = NOW();
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Recrear trigger
DROP TRIGGER IF EXISTS trigger_update_ai_stats ON support_messages;
CREATE TRIGGER trigger_update_ai_stats
    AFTER INSERT ON support_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_ai_daily_stats();

-- 6. Actualizar vista de escalaciones pendientes
CREATE OR REPLACE VIEW v_pending_escalations AS
SELECT 
    e.id as escalation_id,
    e.conversation_id,
    e.user_id,
    e.reason,
    e.confidence_score,
    e.detected_intent,
    e.status,
    e.created_at,
    (SELECT content FROM support_messages 
     WHERE conversation_id = e.conversation_id 
     ORDER BY created_at DESC LIMIT 1) as last_message,
    (SELECT COUNT(*) FROM support_messages 
     WHERE conversation_id = e.conversation_id) as message_count
FROM ai_escalations e
WHERE e.status = 'pending'
ORDER BY e.created_at ASC;

-- 7. Verificar que todo esté correcto
SELECT 'support_conversations' as tabla, COUNT(*) as registros FROM support_conversations
UNION ALL
SELECT 'support_messages', COUNT(*) FROM support_messages
UNION ALL
SELECT 'ai_escalations', COUNT(*) FROM ai_escalations
UNION ALL
SELECT 'ai_feedback', COUNT(*) FROM ai_feedback
UNION ALL
SELECT 'ai_stats_daily', COUNT(*) FROM ai_stats_daily;
