-- =====================================================
-- RENDLY AI SUPPORT - DATABASE SCHEMA
-- =====================================================
-- Tablas para persistir conversaciones de soporte IA
-- =====================================================

-- Tabla: support_conversations
-- Almacena las conversaciones de soporte
CREATE TABLE IF NOT EXISTS support_conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES usuarios(user_id) ON DELETE CASCADE,
    session_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'resolved', 'escalated')),
    resolved_by TEXT CHECK (resolved_by IN ('ai', 'human', NULL)),
    escalated_at TIMESTAMPTZ,
    escalated_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices para búsquedas rápidas
CREATE INDEX IF NOT EXISTS idx_support_conversations_user_id ON support_conversations(user_id);
CREATE INDEX IF NOT EXISTS idx_support_conversations_status ON support_conversations(status);
CREATE INDEX IF NOT EXISTS idx_support_conversations_created_at ON support_conversations(created_at DESC);

-- Tabla: support_messages
-- Almacena cada mensaje de la conversación
CREATE TABLE IF NOT EXISTS support_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES support_conversations(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('user', 'ai', 'human_support', 'system')),
    content TEXT NOT NULL,
    
    -- AI Analysis (solo para mensajes de usuario)
    confidence_score INTEGER CHECK (confidence_score >= 0 AND confidence_score <= 100),
    detected_intent TEXT,
    intent_category TEXT,
    clarity_score REAL,
    completeness_score REAL,
    is_aggressive BOOLEAN DEFAULT FALSE,
    is_confused BOOLEAN DEFAULT FALSE,
    matched_keywords TEXT[],
    
    -- Metadata
    faq_matched_id TEXT,
    response_time_ms INTEGER,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_support_messages_conversation_id ON support_messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_support_messages_role ON support_messages(role);
CREATE INDEX IF NOT EXISTS idx_support_messages_created_at ON support_messages(created_at);
CREATE INDEX IF NOT EXISTS idx_support_messages_intent ON support_messages(detected_intent);

-- Tabla: ai_feedback
-- Feedback de usuarios sobre respuestas de IA
CREATE TABLE IF NOT EXISTS ai_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES support_messages(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES usuarios(user_id) ON DELETE CASCADE,
    helpful BOOLEAN NOT NULL,
    feedback_text TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_feedback_message_id ON ai_feedback(message_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_helpful ON ai_feedback(helpful);

-- Tabla: ai_escalations
-- Registro de escalaciones a soporte humano
CREATE TABLE IF NOT EXISTS ai_escalations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES support_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES usuarios(user_id) ON DELETE CASCADE,
    support_agent_id UUID REFERENCES usuarios(user_id),
    reason TEXT NOT NULL,
    confidence_score INTEGER,
    detected_intent TEXT,
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'assigned', 'resolved', 'closed')),
    assigned_at TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ,
    resolution_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_escalations_status ON ai_escalations(status);
CREATE INDEX IF NOT EXISTS idx_ai_escalations_support_agent ON ai_escalations(support_agent_id);

-- Tabla: ai_stats_daily
-- Estadísticas diarias de IA
CREATE TABLE IF NOT EXISTS ai_stats_daily (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    date DATE NOT NULL UNIQUE,
    total_messages INTEGER NOT NULL DEFAULT 0,
    ai_resolved INTEGER NOT NULL DEFAULT 0,
    escalated INTEGER NOT NULL DEFAULT 0,
    blocked INTEGER NOT NULL DEFAULT 0,
    avg_confidence REAL,
    avg_response_time_ms REAL,
    top_intents JSONB,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    not_helpful_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ai_stats_daily_date ON ai_stats_daily(date DESC);

-- =====================================================
-- FUNCIONES Y TRIGGERS
-- =====================================================

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_support_conversation_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para support_conversations
DROP TRIGGER IF EXISTS trigger_update_support_conversation_updated_at ON support_conversations;
CREATE TRIGGER trigger_update_support_conversation_updated_at
    BEFORE UPDATE ON support_conversations
    FOR EACH ROW
    EXECUTE FUNCTION update_support_conversation_updated_at();

-- Función para actualizar estadísticas diarias
CREATE OR REPLACE FUNCTION update_ai_daily_stats()
RETURNS TRIGGER AS $$
DECLARE
    stat_date DATE;
BEGIN
    stat_date := DATE(NEW.created_at);
    
    INSERT INTO ai_stats_daily (date, total_messages)
    VALUES (stat_date, 1)
    ON CONFLICT (date) DO UPDATE SET
        total_messages = ai_stats_daily.total_messages + 1,
        ai_resolved = ai_stats_daily.ai_resolved + 
            CASE WHEN NEW.role = 'ai' THEN 1 ELSE 0 END,
        updated_at = NOW();
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger para actualizar stats
DROP TRIGGER IF EXISTS trigger_update_ai_stats ON support_messages;
CREATE TRIGGER trigger_update_ai_stats
    AFTER INSERT ON support_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_ai_daily_stats();

-- =====================================================
-- RLS (Row Level Security)
-- =====================================================

-- Habilitar RLS
ALTER TABLE support_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE support_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_escalations ENABLE ROW LEVEL SECURITY;

-- Políticas para usuarios (ver sus propias conversaciones)
CREATE POLICY "Users can view own conversations"
    ON support_conversations FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can create conversations"
    ON support_conversations FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own messages"
    ON support_messages FOR SELECT
    USING (
        conversation_id IN (
            SELECT id FROM support_conversations WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Users can create messages"
    ON support_messages FOR INSERT
    WITH CHECK (
        conversation_id IN (
            SELECT id FROM support_conversations WHERE user_id = auth.uid()
        )
    );

CREATE POLICY "Users can submit feedback"
    ON ai_feedback FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can view own feedback"
    ON ai_feedback FOR SELECT
    USING (auth.uid() = user_id);

-- Políticas para servicio (service_role puede hacer todo)
-- El backend de IA usa service_role key

-- =====================================================
-- REALTIME
-- =====================================================

-- Habilitar realtime para notificaciones de escalación
ALTER PUBLICATION supabase_realtime ADD TABLE ai_escalations;
ALTER PUBLICATION supabase_realtime ADD TABLE support_messages;

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

-- Vista: conversaciones activas pendientes de respuesta humana
CREATE OR REPLACE VIEW v_pending_escalations AS
SELECT 
    e.id as escalation_id,
    e.conversation_id,
    e.user_id,
    u.username,
    u.avatar_url,
    e.reason,
    e.confidence_score,
    e.detected_intent,
    e.status,
    e.created_at,
    (SELECT content FROM support_messages 
     WHERE conversation_id = e.conversation_id 
     ORDER BY created_at DESC LIMIT 1) as last_message
FROM ai_escalations e
JOIN usuarios u ON e.user_id = u.user_id
WHERE e.status = 'pending'
ORDER BY e.created_at ASC;

-- Vista: resumen de estadísticas
CREATE OR REPLACE VIEW v_ai_stats_summary AS
SELECT
    SUM(total_messages) as total_messages,
    SUM(ai_resolved) as ai_resolved,
    SUM(escalated) as escalated,
    SUM(blocked) as blocked,
    AVG(avg_confidence) as avg_confidence,
    AVG(avg_response_time_ms) as avg_response_time_ms,
    SUM(helpful_count) as helpful_count,
    SUM(not_helpful_count) as not_helpful_count,
    ROUND(
        SUM(ai_resolved)::NUMERIC / NULLIF(SUM(total_messages), 0) * 100, 
        2
    ) as ai_resolution_rate
FROM ai_stats_daily
WHERE date >= CURRENT_DATE - INTERVAL '30 days';
