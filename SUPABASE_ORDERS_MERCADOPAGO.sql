-- ═══════════════════════════════════════════════════════════════════════════════
-- SISTEMA DE ÓRDENES Y MERCADO PAGO - Rendly Uruguay
-- ═══════════════════════════════════════════════════════════════════════════════
-- Tablas para gestionar compras, pagos con Mercado Pago y seguimiento de órdenes.
-- Diseñado para Uruguay como mercado inicial con soporte para expansión.
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1. ÓRDENES DE COMPRA
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number TEXT UNIQUE NOT NULL DEFAULT 'ORD-' || UPPER(SUBSTRING(gen_random_uuid()::text FROM 1 FOR 8)),
    
    -- Comprador
    buyer_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Totales
    subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
    shipping_cost DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    currency TEXT NOT NULL DEFAULT 'UYU',
    
    -- Estado de la orden
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN (
        'pending',           -- Esperando pago
        'payment_processing', -- Procesando pago en MP
        'paid',              -- Pagado exitosamente
        'preparing',         -- Vendedor preparando
        'shipped',           -- Enviado
        'delivered',         -- Entregado
        'completed',         -- Completado (con review)
        'cancelled',         -- Cancelada
        'refunded'           -- Reembolsada
    )),
    
    -- Dirección de envío
    shipping_address_id UUID REFERENCES addresses(id),
    shipping_method TEXT DEFAULT 'standard',
    tracking_number TEXT,
    estimated_delivery_date TIMESTAMPTZ,
    
    -- Notas
    buyer_notes TEXT,
    seller_notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    paid_at TIMESTAMPTZ,
    shipped_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ
);

-- 2. ITEMS DE LA ORDEN
CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    
    -- Producto
    post_id TEXT NOT NULL,
    product_id TEXT,
    
    -- Vendedor
    seller_id UUID NOT NULL REFERENCES auth.users(id),
    
    -- Detalles del item
    title TEXT NOT NULL,
    image_url TEXT,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price DECIMAL(12,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    
    -- Variantes seleccionadas
    selected_color TEXT,
    selected_size TEXT,
    
    -- Estado individual (para multi-vendedor)
    item_status TEXT NOT NULL DEFAULT 'pending' CHECK (item_status IN (
        'pending', 'confirmed', 'preparing', 'shipped', 'delivered', 'cancelled', 'refunded'
    )),
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. PAGOS CON MERCADO PAGO
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    
    -- Identificadores de Mercado Pago
    mp_payment_id TEXT,
    mp_preference_id TEXT,
    mp_merchant_order_id TEXT,
    mp_external_reference TEXT UNIQUE,
    
    -- Monto
    amount DECIMAL(12,2) NOT NULL,
    currency TEXT NOT NULL DEFAULT 'UYU',
    
    -- Estado del pago
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN (
        'pending',          -- Pendiente
        'in_process',       -- En proceso
        'approved',         -- Aprobado
        'rejected',         -- Rechazado
        'cancelled',        -- Cancelado
        'refunded',         -- Reembolsado
        'charged_back'      -- Contracargo
    )),
    status_detail TEXT,
    
    -- Método de pago
    payment_method_id TEXT,
    payment_type TEXT,
    installments INT DEFAULT 1,
    
    -- Datos adicionales de MP
    mp_response JSONB,
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    approved_at TIMESTAMPTZ,
    refunded_at TIMESTAMPTZ
);

-- 4. HISTORIAL DE ESTADOS DE ÓRDENES
CREATE TABLE IF NOT EXISTS order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    
    from_status TEXT,
    to_status TEXT NOT NULL,
    changed_by UUID REFERENCES auth.users(id),
    reason TEXT,
    metadata JSONB,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. ESTADÍSTICAS DE VENDEDOR (actualizada automáticamente)
CREATE TABLE IF NOT EXISTS seller_stats (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Ventas
    total_sales INT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(14,2) NOT NULL DEFAULT 0,
    completed_orders INT NOT NULL DEFAULT 0,
    cancelled_orders INT NOT NULL DEFAULT 0,
    
    -- Tiempos
    avg_response_time_minutes INT DEFAULT NULL,
    avg_shipping_time_hours INT DEFAULT NULL,
    
    -- Ratings
    total_ratings INT NOT NULL DEFAULT 0,
    sum_ratings INT NOT NULL DEFAULT 0,
    avg_rating DECIMAL(3,2) GENERATED ALWAYS AS (
        CASE WHEN total_ratings > 0 THEN sum_ratings::decimal / total_ratings ELSE 0 END
    ) STORED,
    
    -- Reputación (0-100)
    reputation_score INT NOT NULL DEFAULT 70 CHECK (reputation_score BETWEEN 0 AND 100),
    
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- ÍNDICES
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_seller ON order_items(seller_id);
CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_mp_id ON payments(mp_payment_id);
CREATE INDEX IF NOT EXISTS idx_payments_external_ref ON payments(mp_external_reference);

-- ═══════════════════════════════════════════════════════════════════════════════
-- RLS POLICIES
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE orders ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_status_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE seller_stats ENABLE ROW LEVEL SECURITY;

-- Orders: Comprador ve sus órdenes, vendedor ve órdenes con sus items
CREATE POLICY "Buyers can view own orders" ON orders
    FOR SELECT USING (auth.uid() = buyer_id);

CREATE POLICY "Buyers can create orders" ON orders
    FOR INSERT WITH CHECK (auth.uid() = buyer_id);

CREATE POLICY "Sellers can view orders with their items" ON orders
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM order_items 
            WHERE order_items.order_id = orders.id 
            AND order_items.seller_id = auth.uid()
        )
    );

-- Order items: Comprador y vendedor pueden ver
CREATE POLICY "Users can view relevant order items" ON order_items
    FOR SELECT USING (
        auth.uid() = seller_id OR
        EXISTS (SELECT 1 FROM orders WHERE orders.id = order_items.order_id AND orders.buyer_id = auth.uid())
    );

CREATE POLICY "System can insert order items" ON order_items
    FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM orders WHERE orders.id = order_items.order_id AND orders.buyer_id = auth.uid())
    );

-- Payments: Solo comprador puede ver
CREATE POLICY "Buyers can view own payments" ON payments
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM orders WHERE orders.id = payments.order_id AND orders.buyer_id = auth.uid())
    );

-- Seller stats: Público para lectura
CREATE POLICY "Anyone can view seller stats" ON seller_stats
    FOR SELECT USING (true);

CREATE POLICY "Users can update own stats" ON seller_stats
    FOR UPDATE USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- FUNCIONES Y TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════════

-- Función para actualizar updated_at
CREATE OR REPLACE FUNCTION update_order_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_order_updated_at();

CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_order_updated_at();

-- Función para registrar cambios de estado
CREATE OR REPLACE FUNCTION log_order_status_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO order_status_history (order_id, from_status, to_status)
        VALUES (NEW.id, OLD.status, NEW.status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_order_status_log
    AFTER UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION log_order_status_change();

-- Función para actualizar stats del vendedor cuando se completa una orden
CREATE OR REPLACE FUNCTION update_seller_stats_on_order_complete()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.item_status = 'delivered' AND OLD.item_status != 'delivered' THEN
        INSERT INTO seller_stats (user_id, total_sales, total_revenue, completed_orders)
        VALUES (NEW.seller_id, 1, NEW.total_price, 1)
        ON CONFLICT (user_id) DO UPDATE SET
            total_sales = seller_stats.total_sales + 1,
            total_revenue = seller_stats.total_revenue + NEW.total_price,
            completed_orders = seller_stats.completed_orders + 1,
            updated_at = NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_seller_stats
    AFTER UPDATE ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION update_seller_stats_on_order_complete();

-- Función para inicializar seller_stats cuando un usuario hace su primera venta
CREATE OR REPLACE FUNCTION ensure_seller_stats_exists()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO seller_stats (user_id)
    VALUES (NEW.seller_id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_ensure_seller_stats
    AFTER INSERT ON order_items
    FOR EACH ROW
    EXECUTE FUNCTION ensure_seller_stats_exists();

-- ═══════════════════════════════════════════════════════════════════════════════
-- HABILITAR REALTIME
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER PUBLICATION supabase_realtime ADD TABLE orders;
ALTER PUBLICATION supabase_realtime ADD TABLE order_items;
ALTER PUBLICATION supabase_realtime ADD TABLE payments;
