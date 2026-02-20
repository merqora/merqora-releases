-- ════════════════════════════════════════════════════════════════════════════
-- ACTUALIZACIÓN DE TABLA ai_feedback PARA SISTEMA DE APRENDIZAJE MEJORADO
-- ════════════════════════════════════════════════════════════════════════════

-- Agregar campos para calificación de estrellas y aprendizaje de IA
ALTER TABLE ai_feedback 
ADD COLUMN IF NOT EXISTS rating INTEGER CHECK (rating >= 1 AND rating <= 5),
ADD COLUMN IF NOT EXISTS feedback_type VARCHAR(50) DEFAULT 'message_feedback',
ADD COLUMN IF NOT EXISTS conversation_id UUID REFERENCES support_conversations(id) ON DELETE CASCADE,
ADD COLUMN IF NOT EXISTS user_message TEXT,
ADD COLUMN IF NOT EXISTS agent_response TEXT;

-- Índices para búsquedas rápidas de aprendizaje
CREATE INDEX IF NOT EXISTS idx_ai_feedback_rating ON ai_feedback(rating);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_type ON ai_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_conversation ON ai_feedback(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_helpful_created ON ai_feedback(helpful, created_at DESC);

-- Vista para feedback de aprendizaje de IA (solo calificaciones positivas)
CREATE OR REPLACE VIEW v_ai_learning_feedback AS
SELECT 
    f.id,
    f.conversation_id,
    f.user_id,
    f.message_id,
    f.helpful,
    f.rating,
    f.feedback_text,
    f.feedback_type,
    f.user_message,
    f.agent_response,
    f.created_at,
    u.username,
    u.avatar_url,
    -- Extraer el último mensaje del usuario de la conversación si no está en feedback
    COALESCE(
        f.user_message,
        (SELECT content FROM support_messages 
         WHERE conversation_id = f.conversation_id 
         AND role = 'user' 
         ORDER BY created_at DESC 
         LIMIT 1)
    ) as query,
    -- Extraer la respuesta del agente si no está en feedback
    COALESCE(
        f.agent_response,
        (SELECT content FROM support_messages 
         WHERE conversation_id = f.conversation_id 
         AND role IN ('human_support', 'ai') 
         ORDER BY created_at DESC 
         LIMIT 1)
    ) as response
FROM ai_feedback f
LEFT JOIN usuarios u ON f.user_id = u.user_id
WHERE f.helpful = true AND f.rating >= 4  -- Solo feedback positivo para aprendizaje
ORDER BY f.created_at DESC;

-- Vista para análisis de feedback (incluye todo)
CREATE OR REPLACE VIEW v_feedback_analysis AS
SELECT 
    f.id,
    f.conversation_id,
    f.user_id,
    f.helpful,
    f.rating,
    f.feedback_text,
    f.feedback_type,
    f.created_at,
    u.username,
    u.avatar_url,
    sc.status as conversation_status,
    sc.resolved_by,
    -- Contar mensajes en la conversación
    (SELECT COUNT(*) FROM support_messages 
     WHERE conversation_id = f.conversation_id) as message_count,
    -- Obtener el intent detectado
    (SELECT detected_intent FROM support_messages 
     WHERE conversation_id = f.conversation_id 
     AND role = 'user' 
     ORDER BY created_at DESC 
     LIMIT 1) as detected_intent
FROM ai_feedback f
LEFT JOIN usuarios u ON f.user_id = u.user_id
LEFT JOIN support_conversations sc ON f.conversation_id = sc.id
ORDER BY f.created_at DESC;

-- Comentarios en las nuevas columnas
COMMENT ON COLUMN ai_feedback.rating IS 'Calificación de 1-5 estrellas del usuario';
COMMENT ON COLUMN ai_feedback.feedback_type IS 'Tipo: message_feedback, resolution_feedback, agent_response';
COMMENT ON COLUMN ai_feedback.conversation_id IS 'ID de la conversación completa';
COMMENT ON COLUMN ai_feedback.user_message IS 'Mensaje original del usuario (para aprendizaje)';
COMMENT ON COLUMN ai_feedback.agent_response IS 'Respuesta del agente/IA (para aprendizaje)';

-- Política para que la web pueda ver todos los feedbacks (ya existe pero por si acaso)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE schemaname = 'public' 
        AND tablename = 'ai_feedback' 
        AND policyname = 'Admin can view all feedback'
    ) THEN
        CREATE POLICY "Admin can view all feedback"
            ON ai_feedback FOR SELECT
            TO authenticated
            USING (true);
    END IF;
END $$;
