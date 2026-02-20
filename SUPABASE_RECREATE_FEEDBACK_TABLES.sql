-- ════════════════════════════════════════════════════════════════════════════
-- RECREAR TABLAS DE FEEDBACK Y AI FEEDBACK
-- Ejecutar en Supabase SQL Editor
-- ════════════════════════════════════════════════════════════════════════════

-- ════════════════════════════════════════════════════════════════════════════
-- 1. TABLA: app_feedback
-- ════════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS app_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Contenido del feedback
    category VARCHAR(50) NOT NULL CHECK (category IN ('feature_request', 'improvement', 'complaint', 'praise', 'other')),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    
    -- Metadata del usuario
    user_name VARCHAR(255),
    user_email VARCHAR(255),
    
    -- Metadata del dispositivo (opcional)
    device_info JSONB,
    app_version VARCHAR(50),
    
    -- Estado del feedback
    status VARCHAR(50) DEFAULT 'pending' CHECK (status IN ('pending', 'reviewing', 'planned', 'implemented', 'rejected', 'archived')),
    priority VARCHAR(50) DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'critical')),
    
    -- Respuesta del equipo
    admin_response TEXT,
    admin_user_id UUID REFERENCES auth.users(id),
    responded_at TIMESTAMPTZ,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para app_feedback
CREATE INDEX IF NOT EXISTS idx_feedback_user_id ON app_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_feedback_status ON app_feedback(status);
CREATE INDEX IF NOT EXISTS idx_feedback_category ON app_feedback(category);
CREATE INDEX IF NOT EXISTS idx_feedback_priority ON app_feedback(priority);
CREATE INDEX IF NOT EXISTS idx_feedback_created_at ON app_feedback(created_at DESC);

-- ════════════════════════════════════════════════════════════════════════════
-- 2. TABLA: bug_reports
-- ════════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS bug_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Información del reporte
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    steps_to_reproduce TEXT,
    expected_behavior TEXT,
    actual_behavior TEXT,
    
    -- Severidad y categoría
    severity VARCHAR(50) DEFAULT 'medium' CHECK (severity IN ('low', 'medium', 'high', 'critical')),
    category VARCHAR(50) CHECK (category IN ('crash', 'ui', 'performance', 'data', 'network', 'security', 'other')),
    
    -- Metadata del usuario
    user_name VARCHAR(255),
    user_email VARCHAR(255),
    
    -- Información técnica
    device_info JSONB,
    app_version VARCHAR(50),
    os_version VARCHAR(50),
    app_logs TEXT,
    screenshot_url VARCHAR(500),
    
    -- Flags de inclusión
    include_device_info BOOLEAN DEFAULT false,
    include_logs BOOLEAN DEFAULT false,
    
    -- Estado del reporte
    status VARCHAR(50) DEFAULT 'open' CHECK (status IN ('open', 'investigating', 'in_progress', 'fixed', 'wont_fix', 'duplicate', 'closed')),
    priority VARCHAR(50) DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'critical')),
    
    -- Asignación y seguimiento
    assigned_to UUID REFERENCES auth.users(id),
    admin_notes TEXT,
    resolution_notes TEXT,
    resolved_at TIMESTAMPTZ,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para bug_reports
CREATE INDEX IF NOT EXISTS idx_bug_reports_user_id ON bug_reports(user_id);
CREATE INDEX IF NOT EXISTS idx_bug_reports_status ON bug_reports(status);
CREATE INDEX IF NOT EXISTS idx_bug_reports_severity ON bug_reports(severity);
CREATE INDEX IF NOT EXISTS idx_bug_reports_category ON bug_reports(category);
CREATE INDEX IF NOT EXISTS idx_bug_reports_priority ON bug_reports(priority);
CREATE INDEX IF NOT EXISTS idx_bug_reports_created_at ON bug_reports(created_at DESC);

-- ════════════════════════════════════════════════════════════════════════════
-- 3. TABLA: ai_feedback
-- ════════════════════════════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Referencias (opcionales para flexibilidad)
    message_id UUID REFERENCES support_messages(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES support_conversations(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Tipo de feedback
    feedback_type TEXT DEFAULT 'user_rating' CHECK (feedback_type IN ('user_rating', 'agent_response', 'resolution_feedback')),
    
    -- Calificación y feedback
    helpful BOOLEAN NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    feedback_text TEXT,
    
    -- Datos para aprendizaje (cuando es agent_response)
    user_message TEXT,
    agent_response TEXT,
    
    -- Timestamp
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices para ai_feedback
CREATE INDEX IF NOT EXISTS idx_ai_feedback_message_id ON ai_feedback(message_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_conversation_id ON ai_feedback(conversation_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_user_id ON ai_feedback(user_id);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_helpful ON ai_feedback(helpful);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_feedback_type ON ai_feedback(feedback_type);
CREATE INDEX IF NOT EXISTS idx_ai_feedback_created_at ON ai_feedback(created_at DESC);

-- ════════════════════════════════════════════════════════════════════════════
-- 4. TRIGGERS para updated_at
-- ════════════════════════════════════════════════════════════════════════════

-- Crear función si no existe
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers
DROP TRIGGER IF EXISTS update_feedback_updated_at ON app_feedback;
CREATE TRIGGER update_feedback_updated_at
    BEFORE UPDATE ON app_feedback
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_bug_reports_updated_at ON bug_reports;
CREATE TRIGGER update_bug_reports_updated_at
    BEFORE UPDATE ON bug_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ════════════════════════════════════════════════════════════════════════════
-- 5. ROW LEVEL SECURITY (RLS)
-- ════════════════════════════════════════════════════════════════════════════

-- Habilitar RLS
ALTER TABLE app_feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE bug_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_feedback ENABLE ROW LEVEL SECURITY;

-- Políticas para app_feedback
DROP POLICY IF EXISTS "Los usuarios pueden crear su propio feedback" ON app_feedback;
CREATE POLICY "Los usuarios pueden crear su propio feedback"
    ON app_feedback FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Usuarios autenticados pueden ver todo el feedback" ON app_feedback;
CREATE POLICY "Usuarios autenticados pueden ver todo el feedback"
    ON app_feedback FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Usuarios autenticados pueden actualizar feedback" ON app_feedback;
CREATE POLICY "Usuarios autenticados pueden actualizar feedback"
    ON app_feedback FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- Políticas para bug_reports
DROP POLICY IF EXISTS "Los usuarios pueden crear sus propios reportes" ON bug_reports;
CREATE POLICY "Los usuarios pueden crear sus propios reportes"
    ON bug_reports FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Usuarios autenticados pueden ver todos los reportes" ON bug_reports;
CREATE POLICY "Usuarios autenticados pueden ver todos los reportes"
    ON bug_reports FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Usuarios autenticados pueden actualizar reportes" ON bug_reports;
CREATE POLICY "Usuarios autenticados pueden actualizar reportes"
    ON bug_reports FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- Políticas para ai_feedback
DROP POLICY IF EXISTS "Usuarios pueden crear feedback" ON ai_feedback;
CREATE POLICY "Usuarios pueden crear feedback"
    ON ai_feedback FOR INSERT
    TO authenticated
    WITH CHECK (true);

DROP POLICY IF EXISTS "Usuarios pueden ver todo el feedback" ON ai_feedback;
CREATE POLICY "Usuarios pueden ver todo el feedback"
    ON ai_feedback FOR SELECT
    TO authenticated
    USING (true);

DROP POLICY IF EXISTS "Usuarios pueden actualizar feedback" ON ai_feedback;
CREATE POLICY "Usuarios pueden actualizar feedback"
    ON ai_feedback FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- ════════════════════════════════════════════════════════════════════════════
-- 6. HABILITAR REALTIME
-- ════════════════════════════════════════════════════════════════════════════

ALTER PUBLICATION supabase_realtime ADD TABLE app_feedback;
ALTER PUBLICATION supabase_realtime ADD TABLE bug_reports;
ALTER PUBLICATION supabase_realtime ADD TABLE ai_feedback;

-- ════════════════════════════════════════════════════════════════════════════
-- ✅ COMPLETADO
-- ════════════════════════════════════════════════════════════════════════════
-- Las tres tablas han sido recreadas con todas sus políticas, índices y triggers.
-- Verifica con: SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename IN ('app_feedback', 'bug_reports', 'ai_feedback');
