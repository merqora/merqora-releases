-- ════════════════════════════════════════════════════════════════════════════
-- ACTUALIZACIÓN: Tabla ai_feedback para aprendizaje de IA
-- Ejecutar en Supabase SQL Editor
-- ════════════════════════════════════════════════════════════════════════════

-- Agregar columnas adicionales para el aprendizaje de IA
ALTER TABLE ai_feedback ADD COLUMN IF NOT EXISTS conversation_id UUID REFERENCES support_conversations(id) ON DELETE CASCADE;
ALTER TABLE ai_feedback ADD COLUMN IF NOT EXISTS feedback_type TEXT DEFAULT 'user_rating' CHECK (feedback_type IN ('user_rating', 'agent_response', 'resolution_feedback'));
ALTER TABLE ai_feedback ADD COLUMN IF NOT EXISTS user_message TEXT;
ALTER TABLE ai_feedback ADD COLUMN IF NOT EXISTS agent_response TEXT;
ALTER TABLE ai_feedback ADD COLUMN IF NOT EXISTS rating INTEGER CHECK (rating >= 1 AND rating <= 5);

-- Hacer message_id opcional (puede no haber mensaje específico en algunos casos)
ALTER TABLE ai_feedback ALTER COLUMN message_id DROP NOT NULL;

-- Crear índices para las nuevas columnas
CREATE INDEX IF NOT EXISTS idx_ai_feedback_conversation_id ON ai_feedback(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_feedback_type ON ai_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_created_at ON ai_feedback(created_at DESC);

-- Actualizar políticas RLS para permitir acceso completo a usuarios autenticados
DROP POLICY IF EXISTS "Users can submit feedback" ON ai_feedback;
DROP POLICY IF EXISTS "Users can view own feedback" ON ai_feedback;

CREATE POLICY "Usuarios pueden crear feedback"
    ON ai_feedback FOR INSERT
    TO authenticated
    WITH CHECK (true);

CREATE POLICY "Usuarios pueden ver todo el feedback"
    ON ai_feedback FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Usuarios pueden actualizar feedback"
    ON ai_feedback FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- Habilitar realtime para ai_feedback
ALTER PUBLICATION supabase_realtime ADD TABLE IF NOT EXISTS ai_feedback;

-- ════════════════════════════════════════════════════════════════════════════
-- VERIFICAR: Ver estructura actual de la tabla
-- ════════════════════════════════════════════════════════════════════════════
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'ai_feedback';
