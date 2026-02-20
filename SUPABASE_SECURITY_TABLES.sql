-- =====================================================
-- TABLAS DE SEGURIDAD PARA RENDLY
-- Ejecutar en Supabase SQL Editor
-- =====================================================

-- =====================================================
-- 1. CONFIGURACIÓN DE SEGURIDAD DEL USUARIO
-- =====================================================
CREATE TABLE IF NOT EXISTS user_security_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    biometric_enabled BOOLEAN DEFAULT false,
    two_factor_enabled BOOLEAN DEFAULT false,
    two_factor_method TEXT DEFAULT 'totp', -- 'totp', 'sms', 'email'
    login_alerts_enabled BOOLEAN DEFAULT true,
    require_strong_password BOOLEAN DEFAULT true,
    password_min_length INTEGER DEFAULT 8,
    require_uppercase BOOLEAN DEFAULT true,
    require_number BOOLEAN DEFAULT true,
    require_special_char BOOLEAN DEFAULT false,
    last_password_change TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id)
);

-- =====================================================
-- 2. DISPOSITIVOS DE CONFIANZA
-- =====================================================
CREATE TABLE IF NOT EXISTS user_trusted_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    device_id TEXT NOT NULL,
    device_name TEXT NOT NULL,
    device_type TEXT NOT NULL, -- 'android', 'ios', 'web', 'desktop'
    device_model TEXT,
    os_version TEXT,
    app_version TEXT,
    is_current BOOLEAN DEFAULT false,
    is_trusted BOOLEAN DEFAULT true,
    last_used_at TIMESTAMPTZ DEFAULT NOW(),
    last_ip_address INET,
    last_location TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, device_id)
);

-- =====================================================
-- 3. SESIONES ACTIVAS
-- =====================================================
CREATE TABLE IF NOT EXISTS user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_token TEXT NOT NULL,
    device_id TEXT,
    device_name TEXT,
    device_type TEXT,
    ip_address INET,
    location TEXT,
    user_agent TEXT,
    is_active BOOLEAN DEFAULT true,
    started_at TIMESTAMPTZ DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ
);

-- =====================================================
-- 4. HISTORIAL DE ACTIVIDAD DE SEGURIDAD
-- =====================================================
CREATE TABLE IF NOT EXISTS user_activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL, -- 'login', 'logout', 'password_change', 'settings_change', 'device_added', 'device_removed', 'session_ended', '2fa_enabled', '2fa_disabled', 'suspicious_login'
    description TEXT,
    ip_address INET,
    location TEXT,
    device_info TEXT,
    user_agent TEXT,
    is_suspicious BOOLEAN DEFAULT false,
    risk_level TEXT DEFAULT 'low', -- 'low', 'medium', 'high', 'critical'
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- 5. ACTIVIDAD SOSPECHOSA
-- =====================================================
CREATE TABLE IF NOT EXISTS suspicious_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    activity_type TEXT NOT NULL, -- 'unusual_location', 'multiple_failed_logins', 'new_device', 'unusual_time', 'rapid_requests'
    description TEXT NOT NULL,
    ip_address INET,
    location TEXT,
    device_info TEXT,
    risk_level TEXT DEFAULT 'medium', -- 'low', 'medium', 'high', 'critical'
    is_resolved BOOLEAN DEFAULT false,
    resolved_at TIMESTAMPTZ,
    resolved_by TEXT, -- 'user', 'system', 'admin'
    resolution_note TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- 6. CÓDIGOS DE RECUPERACIÓN 2FA
-- =====================================================
CREATE TABLE IF NOT EXISTS user_recovery_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    code_hash TEXT NOT NULL, -- Código hasheado
    is_used BOOLEAN DEFAULT false,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================
-- ÍNDICES PARA MEJOR RENDIMIENTO
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_security_settings_user ON user_security_settings(user_id);
CREATE INDEX IF NOT EXISTS idx_trusted_devices_user ON user_trusted_devices(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_active ON user_sessions(user_id, is_active);
CREATE INDEX IF NOT EXISTS idx_activity_logs_user ON user_activity_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_activity_logs_date ON user_activity_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_suspicious_user ON suspicious_activities(user_id);
CREATE INDEX IF NOT EXISTS idx_suspicious_unresolved ON suspicious_activities(user_id, is_resolved);

-- =====================================================
-- POLÍTICAS RLS (Row Level Security)
-- =====================================================

-- Habilitar RLS en todas las tablas
ALTER TABLE user_security_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_trusted_devices ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_activity_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE suspicious_activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_recovery_codes ENABLE ROW LEVEL SECURITY;

-- Políticas para user_security_settings
DROP POLICY IF EXISTS "Users can view own security settings" ON user_security_settings;
CREATE POLICY "Users can view own security settings" ON user_security_settings
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update own security settings" ON user_security_settings;
CREATE POLICY "Users can update own security settings" ON user_security_settings
    FOR UPDATE USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own security settings" ON user_security_settings;
CREATE POLICY "Users can insert own security settings" ON user_security_settings
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Políticas para user_trusted_devices
DROP POLICY IF EXISTS "Users can view own devices" ON user_trusted_devices;
CREATE POLICY "Users can view own devices" ON user_trusted_devices
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can manage own devices" ON user_trusted_devices;
CREATE POLICY "Users can manage own devices" ON user_trusted_devices
    FOR ALL USING (auth.uid() = user_id);

-- Políticas para user_sessions
DROP POLICY IF EXISTS "Users can view own sessions" ON user_sessions;
CREATE POLICY "Users can view own sessions" ON user_sessions
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can manage own sessions" ON user_sessions;
CREATE POLICY "Users can manage own sessions" ON user_sessions
    FOR ALL USING (auth.uid() = user_id);

-- Políticas para user_activity_logs
DROP POLICY IF EXISTS "Users can view own activity" ON user_activity_logs;
CREATE POLICY "Users can view own activity" ON user_activity_logs
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own activity" ON user_activity_logs;
CREATE POLICY "Users can insert own activity" ON user_activity_logs
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Políticas para suspicious_activities
DROP POLICY IF EXISTS "Users can view own suspicious activities" ON suspicious_activities;
CREATE POLICY "Users can view own suspicious activities" ON suspicious_activities
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can resolve own suspicious activities" ON suspicious_activities;
CREATE POLICY "Users can resolve own suspicious activities" ON suspicious_activities
    FOR UPDATE USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "System can insert suspicious activities" ON suspicious_activities;
CREATE POLICY "System can insert suspicious activities" ON suspicious_activities
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Políticas para user_recovery_codes
DROP POLICY IF EXISTS "Users can view own recovery codes" ON user_recovery_codes;
CREATE POLICY "Users can view own recovery codes" ON user_recovery_codes
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can manage own recovery codes" ON user_recovery_codes;
CREATE POLICY "Users can manage own recovery codes" ON user_recovery_codes
    FOR ALL USING (auth.uid() = user_id);

-- =====================================================
-- FUNCIONES ÚTILES
-- =====================================================

-- Función para crear configuración de seguridad por defecto
CREATE OR REPLACE FUNCTION create_default_security_settings()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO user_security_settings (user_id)
    VALUES (NEW.id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger para crear configuración automáticamente al registrar usuario
DROP TRIGGER IF EXISTS on_auth_user_created_security ON auth.users;
CREATE TRIGGER on_auth_user_created_security
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION create_default_security_settings();

-- Función para registrar actividad
CREATE OR REPLACE FUNCTION log_user_activity(
    p_user_id UUID,
    p_activity_type TEXT,
    p_description TEXT,
    p_ip_address INET DEFAULT NULL,
    p_location TEXT DEFAULT NULL,
    p_device_info TEXT DEFAULT NULL,
    p_is_suspicious BOOLEAN DEFAULT false,
    p_risk_level TEXT DEFAULT 'low',
    p_metadata JSONB DEFAULT '{}'
)
RETURNS UUID AS $$
DECLARE
    v_log_id UUID;
BEGIN
    INSERT INTO user_activity_logs (
        user_id, activity_type, description, ip_address, 
        location, device_info, is_suspicious, risk_level, metadata
    )
    VALUES (
        p_user_id, p_activity_type, p_description, p_ip_address,
        p_location, p_device_info, p_is_suspicious, p_risk_level, p_metadata
    )
    RETURNING id INTO v_log_id;
    
    -- Si es sospechoso, crear también registro en suspicious_activities
    IF p_is_suspicious THEN
        INSERT INTO suspicious_activities (
            user_id, activity_type, description, ip_address,
            location, device_info, risk_level
        )
        VALUES (
            p_user_id, p_activity_type, p_description, p_ip_address,
            p_location, p_device_info, p_risk_level
        );
    END IF;
    
    RETURN v_log_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para cerrar todas las sesiones de un usuario
CREATE OR REPLACE FUNCTION close_all_user_sessions(p_user_id UUID, p_except_session_id UUID DEFAULT NULL)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    UPDATE user_sessions
    SET is_active = false, ended_at = NOW()
    WHERE user_id = p_user_id 
      AND is_active = true
      AND (p_except_session_id IS NULL OR id != p_except_session_id);
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    
    -- Registrar actividad
    PERFORM log_user_activity(
        p_user_id,
        'session_ended',
        'Todas las sesiones fueron cerradas',
        NULL, NULL, NULL, false, 'low',
        jsonb_build_object('sessions_closed', v_count)
    );
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- HABILITAR REALTIME
-- =====================================================
ALTER PUBLICATION supabase_realtime ADD TABLE user_security_settings;
ALTER PUBLICATION supabase_realtime ADD TABLE user_trusted_devices;
ALTER PUBLICATION supabase_realtime ADD TABLE user_sessions;
ALTER PUBLICATION supabase_realtime ADD TABLE suspicious_activities;

-- =====================================================
-- DATOS DE PRUEBA (opcional, comentar en producción)
-- =====================================================
-- INSERT INTO user_security_settings (user_id, biometric_enabled, login_alerts_enabled)
-- SELECT id, false, true FROM auth.users ON CONFLICT DO NOTHING;

SELECT 'Tablas de seguridad creadas exitosamente' AS status;
