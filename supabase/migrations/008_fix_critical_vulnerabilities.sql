-- ════════════════════════════════════════════════════════════════
-- FIX CRITICAL VULNERABILITIES — Auditoría v1
-- ════════════════════════════════════════════════════════════════
-- CRIT-1, CRIT-2, CRIT-3, CRIT-5, CRIT-6
-- HIGH-3, MED-3, MED-4, MED-5, MED-6
-- ════════════════════════════════════════════════════════════════

-- ============================================================
-- CRIT-6: Idempotency determinista + protección DB-level
-- ============================================================
ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_idempotency_key_key;
CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_unique_payment
    ON orders(id) WHERE status IN ('paid', 'payment_processing');

CREATE UNIQUE INDEX IF NOT EXISTS idx_payments_unique_order
    ON payments(order_id) WHERE status = 'approved';

CREATE INDEX IF NOT EXISTS idx_orders_payment_status_idempotent
    ON orders(id, status);

-- ============================================================
-- CRIT-2: Función unificada de transición de estado
-- ============================================================
CREATE OR REPLACE FUNCTION transition_order_status(
    p_order_id TEXT,
    p_new_status TEXT,
    p_changed_by TEXT DEFAULT 'system',
    p_notes TEXT DEFAULT NULL
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_old_status TEXT;
    v_current_mp_status TEXT;
BEGIN
    SELECT status, payment_status INTO v_old_status, v_current_mp_status
    FROM orders WHERE id = p_order_id;

    IF v_old_status = p_new_status THEN
        RETURN FALSE;
    END IF;

    UPDATE orders SET
        status = p_new_status,
        updated_at = NOW(),
        paid_at = CASE
            WHEN p_new_status = 'paid' AND paid_at IS NULL THEN NOW()
            ELSE paid_at
        END
    WHERE id = p_order_id;

    INSERT INTO order_status_history (order_id, from_status, to_status, changed_by, notes)
    VALUES (p_order_id, v_old_status, p_new_status, p_changed_by,
            COALESCE(p_notes, 'Transición de ' || v_old_status || ' a ' || p_new_status));

    RETURN TRUE;
END;
$$;

-- ============================================================
-- CRIT-1: flag_stats_credited + trigger de stats idempotente
-- ============================================================
ALTER TABLE orders ADD COLUMN IF NOT EXISTS stats_credited BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_orders_stats_credited ON orders(id) WHERE stats_credited = FALSE;

CREATE OR REPLACE FUNCTION credit_seller_stats_idempotent(
    p_order_id TEXT
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_already_credited BOOLEAN;
    v_seller_id TEXT;
    v_net_amount DECIMAL;
BEGIN
    SELECT stats_credited INTO v_already_credited FROM orders WHERE id = p_order_id;
    IF v_already_credited THEN
        RETURN FALSE;
    END IF;

    FOR v_seller_id, v_net_amount IN
        SELECT oi.seller_id,
               COALESCE(SUM(oi.total_price - COALESCE(o.commission_amount, oi.total_price * 0.10)), 0)
        FROM order_items oi
        JOIN orders o ON o.id = oi.order_id
        WHERE oi.order_id = p_order_id
        GROUP BY oi.seller_id
    LOOP
        UPDATE seller_stats SET
            total_sales = total_sales + 1,
            total_revenue = total_revenue + v_net_amount,
            completed_orders = completed_orders + 1,
            updated_at = NOW()
        WHERE user_id = v_seller_id;

        IF NOT FOUND THEN
            INSERT INTO seller_stats (user_id, total_sales, total_revenue, completed_orders)
            VALUES (v_seller_id, 1, v_net_amount, 1);
        END IF;
    END LOOP;

    UPDATE orders SET stats_credited = TRUE WHERE id = p_order_id;
    RETURN TRUE;
END;
$$;

-- ============================================================
-- CRIT-3: Configuración del Marketplace User ID de Vinzay
-- ============================================================
INSERT INTO platform_settings (key, value) VALUES
    ('marketplace_user_id', '{"mp_user_id": "", "description": "MP User ID de Vinzay (la plataforma), usado como sponsor_id en Split Payments"}'::jsonb)
ON CONFLICT (key) DO NOTHING;

-- ============================================================
-- HIGH-3: Trigger para validar conexión MP del vendedor
-- ============================================================
DROP TRIGGER IF EXISTS trg_validate_seller_mp_before_order ON orders;

CREATE TRIGGER trg_validate_seller_mp_before_order
    BEFORE INSERT ON orders
    FOR EACH ROW
    EXECUTE FUNCTION validate_seller_mp_connection();

-- ============================================================
-- MED-3: Índices faltantes
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_mp_connections_estado ON mercadopago_connections(conexion_estado);
CREATE INDEX IF NOT EXISTS idx_payment_audit_idempotency ON payment_audit_log(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_order_items_seller ON order_items(seller_id);
CREATE INDEX IF NOT EXISTS idx_orders_buyer_status ON orders(buyer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_seller_lookup ON orders(id) WHERE status IN ('paid', 'shipped', 'delivered');

-- ============================================================
-- MED-4: Vista corregida — COALESCE en commission_amount
-- ============================================================
CREATE OR REPLACE VIEW seller_financial_summary AS
SELECT
    oi.seller_id,
    COUNT(DISTINCT o.id) FILTER (WHERE o.status = 'paid' OR o.status = 'shipped' OR o.status = 'delivered') AS total_orders,
    COUNT(DISTINCT o.id) FILTER (WHERE o.status = 'delivered') AS completed_orders,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS gross_sales,
    COALESCE(SUM(COALESCE(o.commission_amount, oi.total_price * 0.10)) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS total_commission,
    COALESCE(SUM(oi.total_price - COALESCE(o.commission_amount, oi.total_price * 0.10)) FILTER (WHERE o.status IN ('paid', 'shipped', 'delivered')), 0) AS net_earnings,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status = 'paid'), 0) AS pending_clearance,
    COALESCE(SUM(oi.total_price) FILTER (WHERE o.status = 'delivered'), 0) AS cleared_for_payment
FROM order_items oi
JOIN orders o ON o.id = oi.order_id
GROUP BY oi.seller_id;

-- ============================================================
-- MED-5: Stock management concurrente
-- ============================================================
ALTER TABLE posts ADD COLUMN IF NOT EXISTS stock INTEGER NOT NULL DEFAULT -1;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS stock_reserved INTEGER NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS stock_updated_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_posts_stock ON posts(id) WHERE stock >= 0;

CREATE OR REPLACE FUNCTION reserve_stock(
    p_post_id TEXT,
    p_quantity INTEGER
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_available INTEGER;
BEGIN
    SELECT stock - stock_reserved INTO v_available
    FROM posts WHERE id = p_post_id
    FOR UPDATE;

    IF v_available IS NULL OR v_available < p_quantity THEN
        RETURN FALSE;
    END IF;

    UPDATE posts SET
        stock_reserved = stock_reserved + p_quantity,
        stock_updated_at = NOW()
    WHERE id = p_post_id;

    RETURN TRUE;
END;
$$;

CREATE OR REPLACE FUNCTION confirm_stock(
    p_post_id TEXT,
    p_quantity INTEGER
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE posts SET
        stock = stock - p_quantity,
        stock_reserved = GREATEST(stock_reserved - p_quantity, 0),
        stock_updated_at = NOW()
    WHERE id = p_post_id;
    RETURN FOUND;
END;
$$;

CREATE OR REPLACE FUNCTION release_stock(
    p_post_id TEXT,
    p_quantity INTEGER
) RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE posts SET
        stock_reserved = GREATEST(stock_reserved - p_quantity, 0),
        stock_updated_at = NOW()
    WHERE id = p_post_id;
    RETURN FOUND;
END;
$$;

-- ============================================================
-- MED-6: Sincronización payment_status ↔ order status
-- ============================================================
CREATE OR REPLACE FUNCTION sync_payment_order_status()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    IF NEW.payment_status IS DISTINCT FROM OLD.payment_status THEN
        NEW.status := CASE NEW.payment_status
            WHEN 'approved' THEN 'paid'
            WHEN 'rejected' THEN 'payment_failed'
            WHEN 'refunded' THEN 'refunded'
            WHEN 'cancelled' THEN 'cancelled'
            WHEN 'charged_back' THEN 'refunded'
            ELSE NEW.status
        END;
        NEW.updated_at := NOW();
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_payment_status ON orders;
CREATE TRIGGER trg_sync_payment_status
    BEFORE UPDATE OF payment_status ON orders
    FOR EACH ROW
    EXECUTE FUNCTION sync_payment_order_status();

-- ============================================================
-- MED-2: Bloquear auto-compra a nivel DB
-- Se ejecuta al insertar order_items (no orders) porque
-- el buyer_id solo se conoce cuando el item se asocia a una orden.
-- BEFORE INSERT para evitar inserts innecesarios.
-- ============================================================
CREATE OR REPLACE FUNCTION block_self_purchase()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_buyer_id UUID;
BEGIN
    SELECT buyer_id INTO v_buyer_id FROM orders WHERE id = NEW.order_id;
    IF v_buyer_id = NEW.seller_id THEN
        RAISE EXCEPTION 'No puedes comprar tus propios productos.';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_block_self_purchase ON orders;
DROP TRIGGER IF EXISTS trg_block_self_purchase ON order_items;
CREATE TRIGGER trg_block_self_purchase
    BEFORE INSERT ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION block_self_purchase();

-- ============================================================
-- CRIT-5: Validación de seller_id vs order_items
-- ============================================================
CREATE OR REPLACE FUNCTION validate_order_seller(
    p_order_id TEXT,
    p_seller_id TEXT
) RETURNS BOOLEAN
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
AS $$
BEGIN
    RETURN EXISTS(
        SELECT 1 FROM order_items
        WHERE order_id = p_order_id AND seller_id = p_seller_id
    );
END;
$$;

-- ============================================================
-- CRIT-1 / CRIT-2: update_order_from_payment corregido (sin historial duplicado)
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
    v_already_processed BOOLEAN;
BEGIN
    SELECT status INTO v_current_status FROM orders WHERE id = p_order_id;

    IF v_current_status IN ('paid', 'refunded', 'cancelled') AND p_new_status = 'approved' THEN
        RETURN FALSE;
    END IF;

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

    IF v_current_status = v_order_status THEN
        UPDATE payments SET
            mp_fee_details = COALESCE(p_fee_details, mp_fee_details),
            mp_net_received_amount = COALESCE(p_net_amount, mp_net_received_amount),
            updated_at = NOW()
        WHERE order_id = p_order_id AND mp_payment_id = p_mp_payment_id;
        RETURN TRUE;
    END IF;

    PERFORM transition_order_status(p_order_id, v_order_status, 'system',
        'Auto-actualizado por webhook MP. Payment: ' || p_new_status);

    UPDATE payments SET
        status = p_new_status,
        mp_fee_details = COALESCE(p_fee_details, mp_fee_details),
        mp_net_received_amount = COALESCE(p_net_amount, mp_net_received_amount),
        payment_method_type = COALESCE(payment_method_type, 'credit_card'),
        updated_at = NOW()
    WHERE order_id = p_order_id AND mp_payment_id = p_mp_payment_id;

    IF p_new_status = 'approved' THEN
        PERFORM credit_seller_stats_idempotent(p_order_id);
    END IF;

    RETURN TRUE;
END;
$$;
