-- ═══════════════════════════════════════════════════════════════════════════
-- SISTEMA DE VERIFICACIÓN DE CUENTAS - RENDLY
-- ═══════════════════════════════════════════════════════════════════════════

-- Tabla de solicitudes de verificación
CREATE TABLE IF NOT EXISTS verification_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Tipo de verificación
    verification_type VARCHAR(50) NOT NULL DEFAULT 'personal', -- personal, business, brand
    
    -- Estado de la solicitud
    status VARCHAR(50) NOT NULL DEFAULT 'pending', -- pending, under_review, approved, rejected
    
    -- Información del solicitante (snapshot al momento de la solicitud)
    username VARCHAR(255),
    display_name VARCHAR(255),
    email VARCHAR(255),
    avatar_url TEXT,
    bio TEXT,
    website_url TEXT,
    followers_count INTEGER DEFAULT 0,
    following_count INTEGER DEFAULT 0,
    posts_count INTEGER DEFAULT 0,
    account_created_at TIMESTAMPTZ,
    
    -- Información adicional para verificación
    full_legal_name VARCHAR(255),
    document_type VARCHAR(50), -- dni, passport, drivers_license
    document_number VARCHAR(100),
    document_front_url TEXT,
    document_back_url TEXT,
    selfie_with_document_url TEXT,
    
    -- Para negocios/marcas
    business_name VARCHAR(255),
    business_registration_number VARCHAR(100),
    business_document_url TEXT,
    business_website VARCHAR(255),
    business_social_links JSONB DEFAULT '[]'::jsonb,
    
    -- Razón de solicitud
    reason_for_verification TEXT,
    notable_presence TEXT, -- Presencia notable en otras plataformas
    media_coverage TEXT, -- Cobertura mediática
    
    -- Revisión del admin
    reviewed_by UUID REFERENCES auth.users(id),
    reviewed_at TIMESTAMPTZ,
    review_notes TEXT,
    rejection_reason TEXT,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índices para búsqueda eficiente
CREATE INDEX IF NOT EXISTS idx_verification_requests_user_id ON verification_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_verification_requests_status ON verification_requests(status);
CREATE INDEX IF NOT EXISTS idx_verification_requests_created_at ON verification_requests(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_verification_requests_type ON verification_requests(verification_type);

-- Agregar campo is_verified a la tabla usuarios si no existe
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'usuarios' AND column_name = 'is_verified') THEN
        ALTER TABLE usuarios ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'usuarios' AND column_name = 'verified_at') THEN
        ALTER TABLE usuarios ADD COLUMN verified_at TIMESTAMPTZ;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'usuarios' AND column_name = 'verification_type') THEN
        ALTER TABLE usuarios ADD COLUMN verification_type VARCHAR(50);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'usuarios' AND column_name = 'is_active') THEN
        ALTER TABLE usuarios ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'usuarios' AND column_name = 'deactivated_at') THEN
        ALTER TABLE usuarios ADD COLUMN deactivated_at TIMESTAMPTZ;
    END IF;
END $$;

-- Índice para usuarios verificados
CREATE INDEX IF NOT EXISTS idx_usuarios_verified ON usuarios(is_verified) WHERE is_verified = TRUE;

-- RLS Policies
ALTER TABLE verification_requests ENABLE ROW LEVEL SECURITY;

-- Usuarios pueden ver sus propias solicitudes
CREATE POLICY "Users can view own verification requests"
    ON verification_requests FOR SELECT
    USING (auth.uid() = user_id);

-- Usuarios pueden crear solicitudes
CREATE POLICY "Users can create verification requests"
    ON verification_requests FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Solo admins pueden actualizar solicitudes (se maneja via service role)
CREATE POLICY "Service role can update verification requests"
    ON verification_requests FOR UPDATE
    USING (true);

-- Función para aprobar verificación
CREATE OR REPLACE FUNCTION approve_verification(
    p_request_id UUID,
    p_reviewer_id UUID,
    p_notes TEXT DEFAULT NULL
) RETURNS BOOLEAN AS $$
DECLARE
    v_user_id UUID;
    v_verification_type VARCHAR(50);
BEGIN
    -- Obtener datos de la solicitud
    SELECT user_id, verification_type INTO v_user_id, v_verification_type
    FROM verification_requests
    WHERE id = p_request_id AND status = 'pending';
    
    IF v_user_id IS NULL THEN
        RETURN FALSE;
    END IF;
    
    -- Actualizar solicitud
    UPDATE verification_requests
    SET status = 'approved',
        reviewed_by = p_reviewer_id,
        reviewed_at = NOW(),
        review_notes = p_notes,
        updated_at = NOW()
    WHERE id = p_request_id;
    
    -- Marcar usuario como verificado
    UPDATE usuarios
    SET is_verified = TRUE,
        verified_at = NOW(),
        verification_type = v_verification_type
    WHERE user_id = v_user_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para rechazar verificación
CREATE OR REPLACE FUNCTION reject_verification(
    p_request_id UUID,
    p_reviewer_id UUID,
    p_rejection_reason TEXT,
    p_notes TEXT DEFAULT NULL
) RETURNS BOOLEAN AS $$
BEGIN
    UPDATE verification_requests
    SET status = 'rejected',
        reviewed_by = p_reviewer_id,
        reviewed_at = NOW(),
        rejection_reason = p_rejection_reason,
        review_notes = p_notes,
        updated_at = NOW()
    WHERE id = p_request_id AND status IN ('pending', 'under_review');
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para revocar verificación
CREATE OR REPLACE FUNCTION revoke_verification(
    p_user_id UUID,
    p_reason TEXT DEFAULT NULL
) RETURNS BOOLEAN AS $$
BEGIN
    UPDATE usuarios
    SET is_verified = FALSE,
        verified_at = NULL,
        verification_type = NULL
    WHERE user_id = p_user_id;
    
    -- Registrar en la última solicitud
    UPDATE verification_requests
    SET review_notes = COALESCE(review_notes, '') || E'\n[REVOCADO] ' || COALESCE(p_reason, 'Sin razón especificada'),
        updated_at = NOW()
    WHERE user_id = p_user_id
    ORDER BY created_at DESC
    LIMIT 1;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Vista para estadísticas de verificación (admin)
CREATE OR REPLACE VIEW verification_stats AS
SELECT 
    COUNT(*) FILTER (WHERE status = 'pending') as pending_count,
    COUNT(*) FILTER (WHERE status = 'under_review') as under_review_count,
    COUNT(*) FILTER (WHERE status = 'approved') as approved_count,
    COUNT(*) FILTER (WHERE status = 'rejected') as rejected_count,
    COUNT(*) as total_requests,
    COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '24 hours') as requests_last_24h,
    COUNT(*) FILTER (WHERE created_at > NOW() - INTERVAL '7 days') as requests_last_7d
FROM verification_requests;

COMMENT ON TABLE verification_requests IS 'Solicitudes de verificación de cuentas';
COMMENT ON COLUMN verification_requests.verification_type IS 'Tipo: personal, business, brand';
COMMENT ON COLUMN verification_requests.status IS 'Estado: pending, under_review, approved, rejected';
