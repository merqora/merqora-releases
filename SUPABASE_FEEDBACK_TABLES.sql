-- ════════════════════════════════════════════════════════════════════════════
-- TABLAS PARA FEEDBACK Y REPORTES DE ERRORES
-- ════════════════════════════════════════════════════════════════════════════

-- Tabla: app_feedback
-- Descripción: Almacena comentarios y sugerencias de usuarios para mejorar la app
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
CREATE INDEX idx_feedback_user_id ON app_feedback(user_id);
CREATE INDEX idx_feedback_status ON app_feedback(status);
CREATE INDEX idx_feedback_category ON app_feedback(category);
CREATE INDEX idx_feedback_priority ON app_feedback(priority);
CREATE INDEX idx_feedback_created_at ON app_feedback(created_at DESC);

-- ════════════════════════════════════════════════════════════════════════════
-- Tabla: bug_reports
-- Descripción: Almacena reportes de errores y problemas técnicos
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
CREATE INDEX idx_bug_reports_user_id ON bug_reports(user_id);
CREATE INDEX idx_bug_reports_status ON bug_reports(status);
CREATE INDEX idx_bug_reports_severity ON bug_reports(severity);
CREATE INDEX idx_bug_reports_category ON bug_reports(category);
CREATE INDEX idx_bug_reports_priority ON bug_reports(priority);
CREATE INDEX idx_bug_reports_created_at ON bug_reports(created_at DESC);

-- ════════════════════════════════════════════════════════════════════════════
-- RLS (Row Level Security)
-- ════════════════════════════════════════════════════════════════════════════

-- Habilitar RLS
ALTER TABLE app_feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE bug_reports ENABLE ROW LEVEL SECURITY;

-- Políticas para app_feedback
CREATE POLICY "Los usuarios pueden crear su propio feedback"
    ON app_feedback FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Los usuarios pueden ver su propio feedback"
    ON app_feedback FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Los usuarios pueden actualizar su propio feedback pendiente"
    ON app_feedback FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id AND status = 'pending')
    WITH CHECK (auth.uid() = user_id);

-- Políticas para bug_reports
CREATE POLICY "Los usuarios pueden crear sus propios reportes"
    ON bug_reports FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Los usuarios pueden ver sus propios reportes"
    ON bug_reports FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

CREATE POLICY "Los usuarios pueden actualizar sus reportes abiertos"
    ON bug_reports FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id AND status = 'open')
    WITH CHECK (auth.uid() = user_id);

-- ════════════════════════════════════════════════════════════════════════════
-- POLÍTICAS PARA ADMINISTRADORES (acceso completo)
-- ════════════════════════════════════════════════════════════════════════════

-- IMPORTANTE: Ejecutar estas políticas para permitir que admin-web vea todos los datos
-- Opción 1: Permitir acceso completo a usuarios autenticados (para desarrollo/admin)

CREATE POLICY "Admin puede ver todo el feedback"
    ON app_feedback FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Admin puede actualizar todo el feedback"
    ON app_feedback FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Admin puede ver todos los reportes"
    ON bug_reports FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Admin puede actualizar todos los reportes"
    ON bug_reports FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- ════════════════════════════════════════════════════════════════════════════
-- Triggers para updated_at
-- ════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_feedback_updated_at
    BEFORE UPDATE ON app_feedback
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bug_reports_updated_at
    BEFORE UPDATE ON bug_reports
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ════════════════════════════════════════════════════════════════════════════
-- REALTIME
-- ════════════════════════════════════════════════════════════════════════════

-- Habilitar realtime para las tablas
ALTER PUBLICATION supabase_realtime ADD TABLE app_feedback;
ALTER PUBLICATION supabase_realtime ADD TABLE bug_reports;

-- ════════════════════════════════════════════════════════════════════════════
-- Comentarios en las tablas
-- ════════════════════════════════════════════════════════════════════════════

COMMENT ON TABLE app_feedback IS 'Comentarios y sugerencias de usuarios para mejorar la aplicación';
COMMENT ON TABLE bug_reports IS 'Reportes de errores y problemas técnicos de la aplicación';

COMMENT ON COLUMN app_feedback.category IS 'Tipo de feedback: feature_request, improvement, complaint, praise, other';
COMMENT ON COLUMN app_feedback.status IS 'Estado: pending, reviewing, planned, implemented, rejected, archived';
COMMENT ON COLUMN app_feedback.device_info IS 'Información del dispositivo en formato JSON';

COMMENT ON COLUMN bug_reports.severity IS 'Gravedad del error: low, medium, high, critical';
COMMENT ON COLUMN bug_reports.status IS 'Estado del reporte: open, investigating, in_progress, fixed, wont_fix, duplicate, closed';
COMMENT ON COLUMN bug_reports.device_info IS 'Información técnica del dispositivo en formato JSON';
COMMENT ON COLUMN bug_reports.app_logs IS 'Logs de la aplicación para diagnóstico';
