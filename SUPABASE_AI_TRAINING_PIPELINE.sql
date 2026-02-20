-- ════════════════════════════════════════════════════════════════════════════
-- MERQORA AI - TRAINING PIPELINE & CONTINUOUS LEARNING DATABASE SCHEMA
-- ════════════════════════════════════════════════════════════════════════════
-- Sistema completo de aprendizaje continuo con:
-- 1. Dataset estructurado para entrenamiento
-- 2. Correcciones humanas (feedback loop)
-- 3. Métricas de evaluación automática
-- 4. Historial de modelos entrenados
-- ════════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════
-- 1. TABLA: ai_training_data
-- Dataset estructurado para entrenamiento
-- ═══════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_training_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Datos del mensaje original
    user_message TEXT NOT NULL,
    detected_intent TEXT,
    ai_response TEXT,
    confidence_score INTEGER CHECK (confidence_score >= 0 AND confidence_score <= 100),
    
    -- Correcciones humanas
    correct_intent TEXT,
    correct_response TEXT,
    corrected_by_human BOOLEAN DEFAULT FALSE,
    corrected_by UUID REFERENCES usuarios(user_id),
    corrected_at TIMESTAMPTZ,
    
    -- Estado de clasificación
    intent_correct BOOLEAN,  -- NULL = no revisado, TRUE = correcto, FALSE = error
    response_correct BOOLEAN,
    escalated BOOLEAN DEFAULT FALSE,
    escalation_reason TEXT,
    
    -- Contexto
    conversation_id UUID REFERENCES support_conversations(id) ON DELETE SET NULL,
    message_id UUID REFERENCES support_messages(id) ON DELETE SET NULL,
    user_id UUID REFERENCES usuarios(user_id) ON DELETE SET NULL,
    session_id TEXT,
    
    -- Metadata de análisis
    category TEXT,
    clarity_score REAL,
    completeness_score REAL,
    matched_keywords TEXT[],
    response_time_ms INTEGER,
    response_source TEXT CHECK (response_source IN ('faq', 'llm', 'local_model', 'cache', 'agent_learning', 'reasoning', 'escalation')),
    
    -- Estado del dataset
    dataset_status TEXT DEFAULT 'pending' CHECK (dataset_status IN ('pending', 'reviewed', 'approved', 'rejected', 'used_in_training')),
    included_in_training_run UUID,  -- Referencia al training run que usó este dato
    
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices para consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_training_data_intent ON ai_training_data(detected_intent);
CREATE INDEX IF NOT EXISTS idx_training_data_correct_intent ON ai_training_data(correct_intent);
CREATE INDEX IF NOT EXISTS idx_training_data_status ON ai_training_data(dataset_status);
CREATE INDEX IF NOT EXISTS idx_training_data_corrected ON ai_training_data(corrected_by_human);
CREATE INDEX IF NOT EXISTS idx_training_data_intent_mismatch ON ai_training_data(detected_intent, correct_intent) WHERE intent_correct = FALSE;
CREATE INDEX IF NOT EXISTS idx_training_data_created ON ai_training_data(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_training_data_confidence ON ai_training_data(confidence_score);
CREATE INDEX IF NOT EXISTS idx_training_data_escalated ON ai_training_data(escalated);

-- ═══════════════════════════════════════
-- 2. TABLA: ai_training_runs
-- Historial de entrenamientos del modelo
-- ═══════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_training_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Info del entrenamiento
    run_name TEXT NOT NULL,
    run_type TEXT NOT NULL CHECK (run_type IN ('full', 'incremental', 'intent_only', 'response_only')),
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'running', 'completed', 'failed', 'cancelled')),
    
    -- Dataset usado
    training_samples INTEGER DEFAULT 0,
    validation_samples INTEGER DEFAULT 0,
    test_samples INTEGER DEFAULT 0,
    total_intents INTEGER DEFAULT 0,
    
    -- Métricas pre-entrenamiento (baseline)
    baseline_intent_accuracy REAL,
    baseline_escalation_rate REAL,
    baseline_avg_confidence REAL,
    
    -- Métricas post-entrenamiento
    intent_accuracy REAL,
    intent_precision REAL,
    intent_recall REAL,
    intent_f1_score REAL,
    escalation_rate REAL,
    avg_confidence REAL,
    response_quality_score REAL,
    
    -- Métricas por intent (JSON con accuracy per intent)
    per_intent_metrics JSONB,
    confusion_matrix JSONB,
    
    -- Configuración
    model_config JSONB,  -- Hiperparámetros, tipo de modelo, etc.
    dataset_filters JSONB,  -- Filtros aplicados al dataset
    
    -- Archivos
    model_file_path TEXT,
    dataset_file_path TEXT,
    report_file_path TEXT,
    
    -- Timestamps
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    triggered_by UUID REFERENCES usuarios(user_id)
);

CREATE INDEX IF NOT EXISTS idx_training_runs_status ON ai_training_runs(status);
CREATE INDEX IF NOT EXISTS idx_training_runs_created ON ai_training_runs(created_at DESC);

-- ═══════════════════════════════════════
-- 3. TABLA: ai_intent_corrections
-- Log detallado de correcciones de intent
-- ═══════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_intent_corrections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    training_data_id UUID NOT NULL REFERENCES ai_training_data(id) ON DELETE CASCADE,
    
    -- Corrección
    original_intent TEXT NOT NULL,
    corrected_intent TEXT NOT NULL,
    original_response TEXT,
    corrected_response TEXT,
    correction_notes TEXT,
    should_have_escalated BOOLEAN DEFAULT FALSE,
    
    -- Quién corrigió
    corrected_by UUID NOT NULL REFERENCES usuarios(user_id),
    
    -- Metadata
    confidence_at_detection INTEGER,
    user_message TEXT NOT NULL,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_intent_corrections_original ON ai_intent_corrections(original_intent);
CREATE INDEX IF NOT EXISTS idx_intent_corrections_corrected ON ai_intent_corrections(corrected_intent);
CREATE INDEX IF NOT EXISTS idx_intent_corrections_created ON ai_intent_corrections(created_at DESC);

-- ═══════════════════════════════════════
-- 4. TABLA: ai_metrics_snapshots
-- Snapshots periódicos de métricas
-- ═══════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_metrics_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Período
    period_start TIMESTAMPTZ NOT NULL,
    period_end TIMESTAMPTZ NOT NULL,
    period_type TEXT NOT NULL CHECK (period_type IN ('hourly', 'daily', 'weekly', 'monthly')),
    
    -- Métricas de volumen
    total_messages INTEGER DEFAULT 0,
    ai_resolved INTEGER DEFAULT 0,
    escalated INTEGER DEFAULT 0,
    blocked INTEGER DEFAULT 0,
    
    -- Métricas de calidad
    intent_accuracy REAL,  -- % de intents correctos (basado en correcciones)
    avg_confidence REAL,
    avg_response_time_ms REAL,
    
    -- Métricas de feedback
    helpful_count INTEGER DEFAULT 0,
    not_helpful_count INTEGER DEFAULT 0,
    helpful_rate REAL,
    
    -- Métricas de escalación
    escalation_rate REAL,
    avg_escalation_confidence REAL,
    
    -- Correcciones humanas
    human_corrections INTEGER DEFAULT 0,
    correction_rate REAL,  -- % de respuestas corregidas
    
    -- Desglose por categoría
    metrics_by_category JSONB,
    metrics_by_intent JSONB,
    top_misclassified_intents JSONB,
    
    -- Resolución
    avg_resolution_time_seconds REAL,
    first_contact_resolution_rate REAL,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_metrics_snapshots_period ON ai_metrics_snapshots(period_type, period_start);
CREATE INDEX IF NOT EXISTS idx_metrics_snapshots_type ON ai_metrics_snapshots(period_type, created_at DESC);

-- ═══════════════════════════════════════
-- 5. TABLA: ai_prompt_versions
-- Versionado del prompt base
-- ═══════════════════════════════════════
CREATE TABLE IF NOT EXISTS ai_prompt_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    version INTEGER NOT NULL,
    prompt_template TEXT NOT NULL,
    few_shot_examples JSONB,  -- Ejemplos reales extraídos del dataset
    
    -- Métricas asociadas a esta versión
    intent_accuracy REAL,
    avg_confidence REAL,
    escalation_rate REAL,
    
    is_active BOOLEAN DEFAULT FALSE,
    activated_at TIMESTAMPTZ,
    deactivated_at TIMESTAMPTZ,
    
    notes TEXT,
    created_by UUID REFERENCES usuarios(user_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_prompt_versions_active ON ai_prompt_versions(is_active) WHERE is_active = TRUE;

-- ═══════════════════════════════════════
-- FUNCIONES RPC
-- ═══════════════════════════════════════

-- Función: Obtener métricas del pipeline de training
CREATE OR REPLACE FUNCTION get_ai_training_metrics(
    p_days INTEGER DEFAULT 30
)
RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    SELECT json_build_object(
        'total_training_samples', (SELECT COUNT(*) FROM ai_training_data WHERE created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'pending_review', (SELECT COUNT(*) FROM ai_training_data WHERE dataset_status = 'pending' AND created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'reviewed', (SELECT COUNT(*) FROM ai_training_data WHERE dataset_status = 'reviewed' AND created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'approved', (SELECT COUNT(*) FROM ai_training_data WHERE dataset_status = 'approved'),
        'human_corrections', (SELECT COUNT(*) FROM ai_training_data WHERE corrected_by_human = TRUE AND created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'intent_mismatches', (SELECT COUNT(*) FROM ai_training_data WHERE intent_correct = FALSE AND created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'avg_confidence', (SELECT ROUND(AVG(confidence_score)::NUMERIC, 1) FROM ai_training_data WHERE created_at >= NOW() - (p_days || ' days')::INTERVAL),
        'escalation_rate', (
            SELECT ROUND(
                (COUNT(*) FILTER (WHERE escalated = TRUE))::NUMERIC / 
                NULLIF(COUNT(*)::NUMERIC, 0) * 100, 1
            )
            FROM ai_training_data 
            WHERE created_at >= NOW() - (p_days || ' days')::INTERVAL
        ),
        'intent_accuracy', (
            SELECT ROUND(
                (COUNT(*) FILTER (WHERE intent_correct = TRUE))::NUMERIC / 
                NULLIF((COUNT(*) FILTER (WHERE intent_correct IS NOT NULL))::NUMERIC, 0) * 100, 1
            )
            FROM ai_training_data 
            WHERE created_at >= NOW() - (p_days || ' days')::INTERVAL
        ),
        'corrections_by_category', (
            SELECT json_agg(row_to_json(t)) FROM (
                SELECT 
                    COALESCE(correct_intent, detected_intent) as intent,
                    COUNT(*) as count
                FROM ai_training_data
                WHERE corrected_by_human = TRUE 
                AND created_at >= NOW() - (p_days || ' days')::INTERVAL
                GROUP BY COALESCE(correct_intent, detected_intent)
                ORDER BY count DESC
                LIMIT 20
            ) t
        ),
        'top_confused_intents', (
            SELECT json_agg(row_to_json(t)) FROM (
                SELECT 
                    detected_intent,
                    correct_intent,
                    COUNT(*) as count
                FROM ai_training_data
                WHERE intent_correct = FALSE 
                AND correct_intent IS NOT NULL
                AND created_at >= NOW() - (p_days || ' days')::INTERVAL
                GROUP BY detected_intent, correct_intent
                ORDER BY count DESC
                LIMIT 10
            ) t
        ),
        'last_training_run', (
            SELECT row_to_json(t) FROM (
                SELECT id, run_name, status, intent_accuracy, intent_f1_score, 
                       training_samples, completed_at
                FROM ai_training_runs
                ORDER BY created_at DESC
                LIMIT 1
            ) t
        ),
        'training_runs_count', (SELECT COUNT(*) FROM ai_training_runs WHERE status = 'completed')
    ) INTO result;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función: Obtener datos para entrenamiento (dataset limpio)
CREATE OR REPLACE FUNCTION get_training_dataset(
    p_min_confidence INTEGER DEFAULT 0,
    p_only_reviewed BOOLEAN DEFAULT FALSE,
    p_limit INTEGER DEFAULT 10000
)
RETURNS TABLE (
    id UUID,
    user_message TEXT,
    intent TEXT,
    response TEXT,
    confidence_score INTEGER,
    category TEXT,
    was_corrected BOOLEAN,
    was_escalated BOOLEAN,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        td.id,
        td.user_message,
        COALESCE(td.correct_intent, td.detected_intent) as intent,
        COALESCE(td.correct_response, td.ai_response) as response,
        td.confidence_score,
        td.category,
        td.corrected_by_human as was_corrected,
        td.escalated as was_escalated,
        td.created_at
    FROM ai_training_data td
    WHERE td.confidence_score >= p_min_confidence
    AND (NOT p_only_reviewed OR td.dataset_status IN ('reviewed', 'approved', 'used_in_training'))
    AND td.dataset_status != 'rejected'
    AND COALESCE(td.correct_intent, td.detected_intent) IS NOT NULL
    AND COALESCE(td.correct_intent, td.detected_intent) != 'unknown'
    ORDER BY 
        -- Priorizar datos corregidos por humanos
        td.corrected_by_human DESC,
        -- Luego por confianza
        td.confidence_score DESC,
        td.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función: Registrar corrección humana
CREATE OR REPLACE FUNCTION submit_intent_correction(
    p_training_data_id UUID,
    p_correct_intent TEXT,
    p_correct_response TEXT DEFAULT NULL,
    p_should_escalate BOOLEAN DEFAULT FALSE,
    p_notes TEXT DEFAULT NULL,
    p_corrector_id UUID DEFAULT NULL
)
RETURNS JSON AS $$
DECLARE
    v_training_data ai_training_data%ROWTYPE;
BEGIN
    -- Obtener dato original
    SELECT * INTO v_training_data FROM ai_training_data WHERE id = p_training_data_id;
    
    IF v_training_data.id IS NULL THEN
        RETURN json_build_object('success', false, 'error', 'Training data not found');
    END IF;
    
    -- Actualizar training_data
    UPDATE ai_training_data SET
        correct_intent = p_correct_intent,
        correct_response = COALESCE(p_correct_response, correct_response),
        corrected_by_human = TRUE,
        corrected_by = p_corrector_id,
        corrected_at = NOW(),
        intent_correct = (v_training_data.detected_intent = p_correct_intent),
        response_correct = CASE 
            WHEN p_correct_response IS NOT NULL THEN FALSE 
            ELSE response_correct 
        END,
        dataset_status = 'reviewed',
        updated_at = NOW()
    WHERE id = p_training_data_id;
    
    -- Registrar en log de correcciones
    INSERT INTO ai_intent_corrections (
        training_data_id, original_intent, corrected_intent,
        original_response, corrected_response, correction_notes,
        should_have_escalated, corrected_by, confidence_at_detection,
        user_message
    ) VALUES (
        p_training_data_id,
        v_training_data.detected_intent,
        p_correct_intent,
        v_training_data.ai_response,
        p_correct_response,
        p_notes,
        p_should_escalate,
        p_corrector_id,
        v_training_data.confidence_score,
        v_training_data.user_message
    );
    
    RETURN json_build_object(
        'success', true,
        'was_mismatch', v_training_data.detected_intent != p_correct_intent,
        'original_intent', v_training_data.detected_intent,
        'correct_intent', p_correct_intent
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función: Snapshot de métricas periódico
CREATE OR REPLACE FUNCTION create_metrics_snapshot(
    p_period_type TEXT DEFAULT 'daily'
)
RETURNS UUID AS $$
DECLARE
    v_start TIMESTAMPTZ;
    v_end TIMESTAMPTZ;
    v_snapshot_id UUID;
BEGIN
    v_end := NOW();
    v_start := CASE p_period_type
        WHEN 'hourly' THEN v_end - INTERVAL '1 hour'
        WHEN 'daily' THEN v_end - INTERVAL '1 day'
        WHEN 'weekly' THEN v_end - INTERVAL '7 days'
        WHEN 'monthly' THEN v_end - INTERVAL '30 days'
    END;
    
    INSERT INTO ai_metrics_snapshots (
        period_start, period_end, period_type,
        total_messages, ai_resolved, escalated, blocked,
        intent_accuracy, avg_confidence, avg_response_time_ms,
        helpful_count, not_helpful_count, helpful_rate,
        escalation_rate, human_corrections, correction_rate,
        metrics_by_intent, top_misclassified_intents
    )
    SELECT
        v_start, v_end, p_period_type,
        COUNT(*),
        COUNT(*) FILTER (WHERE NOT td.escalated),
        COUNT(*) FILTER (WHERE td.escalated),
        0,
        -- Intent accuracy (based on corrections)
        ROUND(
            (COUNT(*) FILTER (WHERE td.intent_correct = TRUE))::NUMERIC / 
            NULLIF((COUNT(*) FILTER (WHERE td.intent_correct IS NOT NULL))::NUMERIC, 0) * 100, 1
        ),
        ROUND(AVG(td.confidence_score)::NUMERIC, 1),
        ROUND(AVG(td.response_time_ms)::NUMERIC, 0),
        (SELECT COUNT(*) FROM ai_feedback WHERE helpful = TRUE AND created_at BETWEEN v_start AND v_end),
        (SELECT COUNT(*) FROM ai_feedback WHERE helpful = FALSE AND created_at BETWEEN v_start AND v_end),
        (
            SELECT ROUND(
                (COUNT(*) FILTER (WHERE helpful = TRUE))::NUMERIC / 
                NULLIF(COUNT(*)::NUMERIC, 0) * 100, 1
            ) FROM ai_feedback WHERE created_at BETWEEN v_start AND v_end
        ),
        ROUND(
            (COUNT(*) FILTER (WHERE td.escalated))::NUMERIC / 
            NULLIF(COUNT(*)::NUMERIC, 0) * 100, 1
        ),
        COUNT(*) FILTER (WHERE td.corrected_by_human),
        ROUND(
            (COUNT(*) FILTER (WHERE td.corrected_by_human))::NUMERIC / 
            NULLIF(COUNT(*)::NUMERIC, 0) * 100, 1
        ),
        -- Metrics by intent
        (
            SELECT json_agg(row_to_json(t)) FROM (
                SELECT 
                    COALESCE(td2.correct_intent, td2.detected_intent) as intent,
                    COUNT(*) as total,
                    ROUND(AVG(td2.confidence_score)::NUMERIC, 1) as avg_confidence,
                    COUNT(*) FILTER (WHERE td2.intent_correct = TRUE) as correct,
                    COUNT(*) FILTER (WHERE td2.intent_correct = FALSE) as incorrect
                FROM ai_training_data td2
                WHERE td2.created_at BETWEEN v_start AND v_end
                GROUP BY COALESCE(td2.correct_intent, td2.detected_intent)
                ORDER BY total DESC
            ) t
        ),
        -- Top misclassified
        (
            SELECT json_agg(row_to_json(t)) FROM (
                SELECT 
                    td2.detected_intent,
                    td2.correct_intent,
                    COUNT(*) as count
                FROM ai_training_data td2
                WHERE td2.intent_correct = FALSE 
                AND td2.correct_intent IS NOT NULL
                AND td2.created_at BETWEEN v_start AND v_end
                GROUP BY td2.detected_intent, td2.correct_intent
                ORDER BY count DESC
                LIMIT 10
            ) t
        )
    FROM ai_training_data td
    WHERE td.created_at BETWEEN v_start AND v_end
    RETURNING id INTO v_snapshot_id;
    
    RETURN v_snapshot_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════
-- TRIGGER: Auto-detectar misclassifications
-- ═══════════════════════════════════════
CREATE OR REPLACE FUNCTION auto_detect_misclassification()
RETURNS TRIGGER AS $$
BEGIN
    -- Si el intent fue corregido y no coincide con el detectado
    IF NEW.correct_intent IS NOT NULL AND NEW.detected_intent IS NOT NULL 
       AND NEW.correct_intent != NEW.detected_intent THEN
        NEW.intent_correct := FALSE;
    END IF;
    
    -- Si fue corregido y coincide
    IF NEW.correct_intent IS NOT NULL AND NEW.detected_intent IS NOT NULL
       AND NEW.correct_intent = NEW.detected_intent THEN
        NEW.intent_correct := TRUE;
    END IF;
    
    NEW.updated_at := NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_auto_detect_misclassification ON ai_training_data;
CREATE TRIGGER trigger_auto_detect_misclassification
    BEFORE UPDATE ON ai_training_data
    FOR EACH ROW
    EXECUTE FUNCTION auto_detect_misclassification();

-- ═══════════════════════════════════════
-- RLS POLICIES
-- ═══════════════════════════════════════
ALTER TABLE ai_training_data ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_training_runs ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_intent_corrections ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_metrics_snapshots ENABLE ROW LEVEL SECURITY;
ALTER TABLE ai_prompt_versions ENABLE ROW LEVEL SECURITY;

-- Service role puede hacer todo (el backend usa service key)
-- Los admins pueden ver y editar todo
CREATE POLICY "Admin full access training_data" ON ai_training_data FOR ALL TO authenticated USING (true) WITH CHECK (true);
CREATE POLICY "Admin full access training_runs" ON ai_training_runs FOR ALL TO authenticated USING (true) WITH CHECK (true);
CREATE POLICY "Admin full access corrections" ON ai_intent_corrections FOR ALL TO authenticated USING (true) WITH CHECK (true);
CREATE POLICY "Admin full access metrics" ON ai_metrics_snapshots FOR SELECT TO authenticated USING (true);
CREATE POLICY "Admin full access prompts" ON ai_prompt_versions FOR ALL TO authenticated USING (true) WITH CHECK (true);

-- ═══════════════════════════════════════
-- VISTAS ÚTILES
-- ═══════════════════════════════════════

-- Vista: Datos pendientes de revisión humana
CREATE OR REPLACE VIEW v_pending_review AS
SELECT 
    td.id,
    td.user_message,
    td.detected_intent,
    td.correct_intent,
    td.ai_response,
    td.confidence_score,
    td.category,
    td.escalated,
    td.intent_correct,
    td.dataset_status,
    td.created_at,
    u.username as user_username
FROM ai_training_data td
LEFT JOIN usuarios u ON td.user_id = u.user_id
WHERE td.dataset_status = 'pending'
ORDER BY 
    td.confidence_score ASC,  -- Menor confianza primero (más probable error)
    td.created_at DESC;

-- Vista: Errores de clasificación para análisis
CREATE OR REPLACE VIEW v_classification_errors AS
SELECT 
    td.id,
    td.user_message,
    td.detected_intent as ai_said,
    td.correct_intent as should_be,
    td.confidence_score,
    td.ai_response,
    td.correct_response,
    td.category,
    td.created_at,
    ic.correction_notes,
    u.username as corrected_by_username
FROM ai_training_data td
LEFT JOIN ai_intent_corrections ic ON td.id = ic.training_data_id
LEFT JOIN usuarios u ON ic.corrected_by = u.user_id
WHERE td.intent_correct = FALSE
ORDER BY td.created_at DESC;

-- Vista: Resumen de rendimiento por intent
CREATE OR REPLACE VIEW v_intent_performance AS
SELECT 
    COALESCE(td.correct_intent, td.detected_intent) as intent,
    COUNT(*) as total_samples,
    COUNT(*) FILTER (WHERE td.intent_correct = TRUE) as correct_classifications,
    COUNT(*) FILTER (WHERE td.intent_correct = FALSE) as misclassifications,
    ROUND(
        (COUNT(*) FILTER (WHERE td.intent_correct = TRUE))::NUMERIC / 
        NULLIF((COUNT(*) FILTER (WHERE td.intent_correct IS NOT NULL))::NUMERIC, 0) * 100, 1
    ) as accuracy,
    ROUND(AVG(td.confidence_score)::NUMERIC, 1) as avg_confidence,
    COUNT(*) FILTER (WHERE td.escalated) as escalation_count,
    COUNT(*) FILTER (WHERE td.corrected_by_human) as human_corrections
FROM ai_training_data td
WHERE td.created_at >= NOW() - INTERVAL '30 days'
GROUP BY COALESCE(td.correct_intent, td.detected_intent)
ORDER BY total_samples DESC;

-- ═══════════════════════════════════════
-- REALTIME
-- ═══════════════════════════════════════
ALTER PUBLICATION supabase_realtime ADD TABLE ai_training_data;
ALTER PUBLICATION supabase_realtime ADD TABLE ai_training_runs;
