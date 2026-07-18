-- ════════════════════════════════════════════════════════════════
-- MERCADO PAGO MARKETPLACE + SPLIT PAYMENTS
-- ════════════════════════════════════════════════════════════════
-- Arquitectura: Vinzay actúa como Marketplace en MP.
-- Los vendedores se conectan via OAuth.
-- MP divide el pago automáticamente: vendedor recibe (total - comisión),
-- Vinzay recibe la comisión. Vinzay NUNCA custodia dinero real.
-- ════════════════════════════════════════════════════════════════

-- ============================================================
-- 1. CONEXIONES OAUTH DE VENDEDORES
-- ============================================================
CREATE TABLE IF NOT EXISTS mercadopago_connections (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    mercadopago_user_id TEXT NOT NULL,
    mercadopago_seller_id TEXT,
    access_token_encrypted TEXT NOT NULL,
    refresh_token_encrypted TEXT NOT NULL,
    token_expires_at TIMESTAMPTZ,
    public_key TEXT,
    refresh_token_expires_at TIMESTAMPTZ,
    scope TEXT,
    conexion_estado TEXT NOT NULL DEFAULT 'activa'
        CHECK (conexion_estado IN ('activa', 'token_expirado', 'revocada', 'error')),
    conexion_iniciada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    conexion_actualizada_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ultima_verificacion_en TIMESTAMPTZ,
    ultimo_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_mp_connections_mp_user
    ON mercadopago_connections(mercadopago_user_id);

-- Solo el propio usuario puede ver su conexión
ALTER TABLE mercadopago_connections ENABLE ROW LEVEL SECURITY;

CREATE POLICY mp_connections_select ON mercadopago_connections
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY mp_connections_insert ON mercadopago_connections
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY mp_connections_update ON mercadopago_connections
    FOR UPDATE USING (auth.uid() = user_id);

-- ============================================================
-- 2. PLATFORM SETTINGS (comisión, cuentas MP de Vinzay)
-- ============================================================
CREATE TABLE IF NOT EXISTS platform_settings (
    key TEXT PRIMARY KEY,
    value JSONB NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Insertar defaults
INSERT INTO platform_settings (key, value) VALUES
    ('commission_percentage', '{"percentage": 10.0, "min_fee": 5.0, "max_fee": 5000.0, "currency": "UYU"}'::jsonb),
    ('mercadopago', '{"marketplace_id": "", "platform_access_token_encrypted": "", "platform_user_id": "", "app_id": "", "client_id": "", "sandbox": true}'::jsonb),
    ('seller_oauth', '{"redirect_uri": "vinzay://mp-oauth/callback", "auth_url_template": "https://auth.mercadopago.com.uy/authorization?client_id={client_id}&response_type=code&platform_id=mp&redirect_uri={redirect_uri}&state={state}"}'::jsonb),
    ('webhook_secret', '{"secret": ""}'::jsonb)
ON CONFLICT (key) DO NOTHING;

ALTER TABLE platform_settings ENABLE ROW LEVEL SECURITY;

-- Solo admins pueden leer/escribir platform_settings
CREATE POLICY platform_settings_select ON platform_settings
    FOR SELECT USING (auth.role() = 'service_role');

CREATE POLICY platform_settings_insert ON platform_settings
    FOR INSERT WITH CHECK (auth.role() = 'service_role');

CREATE POLICY platform_settings_update ON platform_settings
    FOR UPDATE USING (auth.role() = 'service_role');

-- ============================================================
-- 3. EXTENDER ORDERS PARA SPLIT PAYMENTS
-- ============================================================
ALTER TABLE orders ADD COLUMN IF NOT EXISTS mp_application_fee DECIMAL(12,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS mp_sponsor_id TEXT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS split_info JSONB;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS commission_percentage DECIMAL(5,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS commission_amount DECIMAL(12,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS seller_net_amount DECIMAL(12,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS idempotency_key TEXT UNIQUE;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS paid_at TIMESTAMPTZ;

-- ============================================================
-- 4. EXTENDER PAYMENTS PARA SPLIT INFO
-- ============================================================
ALTER TABLE payments ADD COLUMN IF NOT EXISTS mp_fee_details JSONB;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS mp_net_received_amount DECIMAL(12,2);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS mp_commission_amount DECIMAL(12,2);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS mp_financial_status TEXT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method_type TEXT;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS installments_count INT;

-- ============================================================
-- 5. AUDITORÍA DE PAGOS
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id TEXT NOT NULL,
    order_id TEXT,
    event_type TEXT NOT NULL,
    mp_status TEXT,
    mp_status_detail TEXT,
    raw_payload JSONB,
    mp_signature TEXT,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processing_result TEXT,
    error_message TEXT,
    idempotency_key TEXT
);

CREATE INDEX IF NOT EXISTS idx_payment_audit_payment ON payment_audit_log(payment_id);
CREATE INDEX IF NOT EXISTS idx_payment_audit_order ON payment_audit_log(order_id);
CREATE INDEX IF NOT EXISTS idx_payment_audit_event ON payment_audit_log(event_type);

ALTER TABLE payment_audit_log ENABLE ROW LEVEL SECURITY;

-- Solo service_role puede leer la auditoría
CREATE POLICY payment_audit_select ON payment_audit_log
    FOR SELECT USING (auth.role() = 'service_role');

CREATE POLICY payment_audit_insert ON payment_audit_log
    FOR INSERT WITH CHECK (auth.role() = 'service_role');

-- ============================================================
-- 6. VISTA: SALDO VISUAL DEL VENDEDOR
-- ============================================================
-- Datos puramente informativos. El dinero real está en MP.
CREATE OR REPLACE VIEW seller_financial_summary AS
SELECT
    oi.seller_id,
    COUNT(DISTINCT o.id) FILTER (WHERE o.status = 'paid' OR o.status = 'shipped' OR o.status = 'delivered') AS total_orders,
    COUNT(DISTINCT o.id) FILTER (WHERE o.status = 'delivered') AS completed_orders,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS gross_sales,
    COALESCE(SUM(o.commission_amount) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS total_commission,
    COALESCE(SUM(oi.total_price - o.commission_amount) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS net_earnings,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status = 'paid'), 0) AS pending_clearance,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status = 'delivered'), 0) AS cleared_for_payment
FROM order_items oi
JOIN orders o ON o.id = oi.order_id
GROUP BY oi.seller_id;

-- ============================================================
-- 7. FUNCIÓN: CREAR IDEMPOTENCY KEY
-- ============================================================
CREATE OR REPLACE FUNCTION generate_idempotency_key()
RETURNS TEXT
LANGUAGE SQL
AS $$ SELECT encode(gen_random_bytes(24), 'hex') $$;

-- ============================================================
-- 8. FUNCIÓN: REGISTRAR AUDITORÍA
-- ============================================================
CREATE OR REPLACE FUNCTION log_payment_event(
    p_payment_id TEXT,
    p_order_id TEXT,
    p_event_type TEXT,
    p_mp_status TEXT,
    p_mp_status_detail TEXT,
    p_raw_payload JSONB,
    p_mp_signature TEXT,
    p_result TEXT,
    p_error TEXT,
    p_idempotency_key TEXT DEFAULT NULL
) RETURNS UUID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_id UUID;
BEGIN
    INSERT INTO payment_audit_log (
        payment_id, order_id, event_type, mp_status, mp_status_detail,
        raw_payload, mp_signature, processing_result, error_message, idempotency_key
    ) VALUES (
        p_payment_id, p_order_id, p_event_type, p_mp_status, p_mp_status_detail,
        p_raw_payload, p_mp_signature, p_result, p_error, p_idempotency_key
    )
    RETURNING id INTO v_id;
    RETURN v_id;
END;
$$;

-- ============================================================
-- 9. FUNCIÓN: ACTUALIZAR ESTADO DE ORDEN POR PAGO
-- ============================================================
CREATE OR REPLACE FUNCTION update_order_from_payment(
    p_order_id TEXT,
    p_mp_payment_id TEXT,
    p_new_status TEXT,
    p_fee_details JSONB DEFAULT NULL,
    p_net_amount DECIMAL DEFAULT NULL
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_order_status TEXT;
    v_current_status TEXT;
BEGIN
    SELECT status INTO v_current_status FROM orders WHERE id = p_order_id;

    v_order_status := CASE p_new_status
        WHEN 'approved' THEN 'paid'
        WHEN 'in_process' THEN 'pending'
        WHEN 'pending' THEN 'pending'
        WHEN 'rejected' THEN 'payment_failed'
        WHEN 'cancelled' THEN 'cancelled'
        WHEN 'refunded' THEN 'refunded'
        WHEN 'charge_back' THEN 'refunded'
        ELSE v_current_status
    END;

    UPDATE orders SET
        status = v_order_status,
        updated_at = NOW(),
        paid_at = CASE WHEN p_new_status = 'approved' AND paid_at IS NULL THEN NOW() ELSE paid_at END
    WHERE id = p_order_id;

    UPDATE payments SET
        status = p_new_status,
        mp_fee_details = COALESCE(p_fee_details, mp_fee_details),
        mp_net_received_amount = COALESCE(p_net_amount, mp_net_received_amount),
        updated_at = NOW()
    WHERE order_id = p_order_id AND mp_payment_id = p_mp_payment_id;

    INSERT INTO order_status_history (order_id, from_status, to_status, changed_by, notes)
    VALUES (p_order_id, v_current_status, v_order_status, 'system',
            'Auto-actualizado por webhook MP. Payment: ' || p_new_status);

    RETURN TRUE;
END;
$$;

-- ============================================================
-- 10. FUNCIÓN: OBTENER TOKEN DE VENDEDOR (SOLO SERVER-SIDE)
-- ============================================================
CREATE OR REPLACE FUNCTION get_seller_mp_token(p_seller_id UUID)
RETURNS TABLE (
    mercadopago_user_id TEXT,
    access_token_encrypted TEXT,
    refresh_token_encrypted TEXT,
    token_expires_at TIMESTAMPTZ
)
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT
        mc.mercadopago_user_id,
        mc.access_token_encrypted,
        mc.refresh_token_encrypted,
        mc.token_expires_at
    FROM mercadopago_connections mc
    WHERE mc.user_id = p_seller_id
      AND mc.conexion_estado = 'activa';
END;
$$;

-- ============================================================
-- 11. FUNCIÓN: CALCULAR COMISIÓN
-- ============================================================
CREATE OR REPLACE FUNCTION calculate_commission(p_amount DECIMAL)
RETURNS TABLE (
    commission_amount DECIMAL,
    seller_net DECIMAL,
    percentage_used DECIMAL
)
LANGUAGE plpgsql
STABLE
AS $$
DECLARE
    v_pct DECIMAL;
    v_min_fee DECIMAL;
    v_max_fee DECIMAL;
    v_raw DECIMAL;
BEGIN
    SELECT
        (value->>'percentage')::DECIMAL,
        (value->>'min_fee')::DECIMAL,
        (value->>'max_fee')::DECIMAL
    INTO v_pct, v_min_fee, v_max_fee
    FROM platform_settings
    WHERE key = 'commission_percentage';

    v_raw := p_amount * (v_pct / 100.0);
    commission_amount := GREATEST(LEAST(v_raw, v_max_fee), v_min_fee);
    seller_net := p_amount - commission_amount;
    percentage_used := v_pct;

    RETURN NEXT;
END;
$$;

-- ============================================================
-- 12. TRIGGER: VALIDAR QUE EL VENDEDOR TENGA CONEXIÓN MP
-- ============================================================
CREATE OR REPLACE FUNCTION validate_seller_mp_connection()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_has_connection BOOLEAN;
BEGIN
    SELECT EXISTS(
        SELECT 1 FROM mercadopago_connections
        WHERE user_id = NEW.user_id AND conexion_estado = 'activa'
    ) INTO v_has_connection;

    IF NOT v_has_connection THEN
        RAISE EXCEPTION 'El vendedor no tiene una cuenta de Mercado Pago conectada. Debe conectarla desde Configuración > Medios de pago.';
    END IF;

    RETURN NEW;
END;
$$;
