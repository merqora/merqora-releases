-- ═══════════════════════════════════════════════════════════════════════════════
-- MEJORAS PARA EL SISTEMA DE TRANSACCIONES
-- ═══════════════════════════════════════════════════════════════════════════════
-- Este script agrega índices y columna updated_at a payments para optimizar
-- las consultas de historial de compras y ventas
-- ═══════════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. AGREGAR COLUMNA updated_at A PAYMENTS SI NO EXISTE
-- ═══════════════════════════════════════════════════════════════════════════════
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'payments' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE payments ADD COLUMN updated_at TIMESTAMPTZ DEFAULT NOW();
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. ÍNDICES PARA OPTIMIZAR CONSULTAS DE TRANSACCIONES
-- ═══════════════════════════════════════════════════════════════════════════════

-- Índice para buscar órdenes por comprador (mis compras)
CREATE INDEX IF NOT EXISTS idx_orders_buyer_id ON orders(buyer_id);

-- Índice para buscar órdenes por estado
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Índice compuesto para órdenes de comprador ordenadas por fecha
CREATE INDEX IF NOT EXISTS idx_orders_buyer_created ON orders(buyer_id, created_at DESC);

-- Índice para buscar items por vendedor (mis ventas)
CREATE INDEX IF NOT EXISTS idx_order_items_seller_id ON order_items(seller_id);

-- Índice compuesto para items de vendedor con orden
CREATE INDEX IF NOT EXISTS idx_order_items_seller_order ON order_items(seller_id, order_id);

-- Índice para pagos por orden
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);

-- Índice para pagos por estado
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. VISTA PARA TRANSACCIONES DEL USUARIO (COMPRAS + VENTAS)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Vista que muestra todas las compras de un usuario
CREATE OR REPLACE VIEW user_purchases AS
SELECT 
    o.id,
    o.order_number,
    o.buyer_id,
    o.subtotal,
    o.shipping_cost,
    o.discount_amount,
    o.total_amount,
    o.currency,
    o.status,
    o.created_at,
    o.paid_at,
    o.shipped_at,
    o.delivered_at,
    o.tracking_number,
    p.status as payment_status,
    p.payment_method_id,
    p.installments,
    COUNT(oi.id) as items_count,
    SUM(oi.quantity) as total_items
FROM orders o
LEFT JOIN payments p ON p.order_id = o.id
LEFT JOIN order_items oi ON oi.order_id = o.id
GROUP BY o.id, p.id;

-- Vista que muestra todas las ventas de un usuario
CREATE OR REPLACE VIEW user_sales AS
SELECT 
    o.id as order_id,
    o.order_number,
    o.buyer_id,
    u.username as buyer_username,
    u.avatar_url as buyer_avatar_url,
    oi.seller_id,
    o.status,
    o.created_at,
    o.paid_at,
    o.shipped_at,
    o.delivered_at,
    o.tracking_number,
    SUM(oi.total_price) as seller_revenue,
    COUNT(oi.id) as items_sold,
    SUM(oi.quantity) as total_quantity
FROM order_items oi
JOIN orders o ON o.id = oi.order_id
LEFT JOIN usuarios u ON u.id = o.buyer_id
GROUP BY o.id, oi.seller_id, u.username, u.avatar_url;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. FUNCIÓN PARA OBTENER RESUMEN DE TRANSACCIONES
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION get_user_transactions_summary(user_uuid UUID)
RETURNS JSON AS $$
DECLARE
    result JSON;
BEGIN
    SELECT json_build_object(
        'total_purchases', (
            SELECT COUNT(*) FROM orders WHERE buyer_id = user_uuid
        ),
        'total_purchases_amount', (
            SELECT COALESCE(SUM(total_amount), 0) 
            FROM orders 
            WHERE buyer_id = user_uuid 
            AND status IN ('paid', 'shipped', 'delivered', 'completed')
        ),
        'pending_purchases', (
            SELECT COUNT(*) 
            FROM orders 
            WHERE buyer_id = user_uuid 
            AND status IN ('pending', 'payment_processing')
        ),
        'total_sales', (
            SELECT COUNT(DISTINCT order_id) 
            FROM order_items 
            WHERE seller_id = user_uuid
        ),
        'total_sales_amount', (
            SELECT COALESCE(SUM(oi.total_price), 0) 
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            WHERE oi.seller_id = user_uuid 
            AND o.status IN ('paid', 'shipped', 'delivered', 'completed')
        ),
        'pending_sales', (
            SELECT COUNT(DISTINCT oi.order_id) 
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            WHERE oi.seller_id = user_uuid 
            AND o.status IN ('pending', 'payment_processing')
        )
    ) INTO result;
    
    RETURN result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. TRIGGER PARA ACTUALIZAR updated_at EN ORDERS
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_order_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS orders_update_timestamp ON orders;
CREATE TRIGGER orders_update_timestamp
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_order_timestamp();

-- ═══════════════════════════════════════════════════════════════════════════════
-- 6. ACTUALIZAR ÓRDENES EXISTENTES CON STATUS 'PENDING' QUE TIENEN PAGO APROBADO
-- ═══════════════════════════════════════════════════════════════════════════════

-- Actualizar órdenes que tienen pago aprobado pero siguen en pending
UPDATE orders o
SET 
    status = 'paid',
    paid_at = COALESCE(p.approved_at, NOW()),
    updated_at = NOW()
FROM payments p
WHERE p.order_id = o.id
AND p.status = 'approved'
AND o.status = 'pending';

-- ═══════════════════════════════════════════════════════════════════════════════
-- 7. RLS PARA LAS VISTAS
-- ═══════════════════════════════════════════════════════════════════════════════

-- Permitir a usuarios ver sus propias compras
DROP POLICY IF EXISTS "Users can view own purchases" ON orders;
CREATE POLICY "Users can view own purchases" ON orders
    FOR SELECT USING (buyer_id = auth.uid());

-- Permitir a vendedores ver órdenes donde tienen items
DROP POLICY IF EXISTS "Sellers can view orders with their items" ON orders;
CREATE POLICY "Sellers can view orders with their items" ON orders
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM order_items 
            WHERE order_items.order_id = orders.id 
            AND order_items.seller_id = auth.uid()
        )
    );

-- ═══════════════════════════════════════════════════════════════════════════════
-- 8. GRANT PERMISSIONS
-- ═══════════════════════════════════════════════════════════════════════════════

GRANT SELECT ON user_purchases TO authenticated;
GRANT SELECT ON user_sales TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_transactions_summary(UUID) TO authenticated;

-- ═══════════════════════════════════════════════════════════════════════════════
-- NOTAS DE IMPLEMENTACIÓN:
-- ═══════════════════════════════════════════════════════════════════════════════
-- 
-- Este script:
-- 1. Agrega índices para optimizar consultas de compras y ventas
-- 2. Crea vistas útiles para el dashboard de transacciones
-- 3. Agrega función RPC para obtener resumen de transacciones
-- 4. Corrige órdenes que estaban en "pending" pero con pago aprobado
-- 5. Agrega trigger para actualizar updated_at automáticamente
--
-- Ejecutar en Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════════════════════
