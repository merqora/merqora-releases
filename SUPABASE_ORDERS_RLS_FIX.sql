-- ═══════════════════════════════════════════════════════════════════════════════
-- FIX: Recursión infinita en políticas RLS de orders
-- ═══════════════════════════════════════════════════════════════════════════════
-- El problema: orders policy consulta order_items, y order_items policy consulta orders
-- Solución: Simplificar las políticas para evitar referencias cruzadas

-- 1. Eliminar políticas problemáticas
DROP POLICY IF EXISTS "Buyers can view own orders" ON orders;
DROP POLICY IF EXISTS "Buyers can create orders" ON orders;
DROP POLICY IF EXISTS "Sellers can view orders with their items" ON orders;
DROP POLICY IF EXISTS "Users can view relevant order items" ON order_items;
DROP POLICY IF EXISTS "System can insert order items" ON order_items;
DROP POLICY IF EXISTS "Buyers can view own payments" ON payments;

-- 2. Crear políticas simples para ORDERS (sin referencias a order_items)
CREATE POLICY "orders_select_buyer" ON orders
    FOR SELECT USING (auth.uid() = buyer_id);

CREATE POLICY "orders_insert_buyer" ON orders
    FOR INSERT WITH CHECK (auth.uid() = buyer_id);

CREATE POLICY "orders_update_buyer" ON orders
    FOR UPDATE USING (auth.uid() = buyer_id);

-- 3. Crear políticas simples para ORDER_ITEMS (sin referencias a orders en SELECT)
-- Usar SECURITY DEFINER function para evitar recursión
CREATE OR REPLACE FUNCTION get_order_buyer(order_uuid UUID)
RETURNS UUID
LANGUAGE sql
SECURITY DEFINER
STABLE
AS $$
    SELECT buyer_id FROM orders WHERE id = order_uuid;
$$;

CREATE POLICY "order_items_select_seller" ON order_items
    FOR SELECT USING (auth.uid() = seller_id);

CREATE POLICY "order_items_select_buyer" ON order_items
    FOR SELECT USING (auth.uid() = get_order_buyer(order_id));

CREATE POLICY "order_items_insert" ON order_items
    FOR INSERT WITH CHECK (auth.uid() = get_order_buyer(order_id));

-- 4. Crear políticas simples para PAYMENTS
CREATE POLICY "payments_select_buyer" ON payments
    FOR SELECT USING (auth.uid() = get_order_buyer(order_id));

CREATE POLICY "payments_insert" ON payments
    FOR INSERT WITH CHECK (auth.uid() = get_order_buyer(order_id));

CREATE POLICY "payments_update" ON payments
    FOR UPDATE USING (auth.uid() = get_order_buyer(order_id));

-- 5. Política para order_status_history
DROP POLICY IF EXISTS "Users can view order status history" ON order_status_history;
CREATE POLICY "order_history_select" ON order_status_history
    FOR SELECT USING (auth.uid() = get_order_buyer(order_id));

CREATE POLICY "order_history_insert" ON order_status_history
    FOR INSERT WITH CHECK (auth.uid() = get_order_buyer(order_id));

-- 6. Políticas para SELLER_STATS
DROP POLICY IF EXISTS "Anyone can view seller stats" ON seller_stats;
DROP POLICY IF EXISTS "Users can update own stats" ON seller_stats;

CREATE POLICY "seller_stats_select" ON seller_stats
    FOR SELECT USING (true);

CREATE POLICY "seller_stats_insert" ON seller_stats
    FOR INSERT WITH CHECK (true);

CREATE POLICY "seller_stats_update" ON seller_stats
    FOR UPDATE USING (true);

-- 7. Hacer las funciones de triggers SECURITY DEFINER para bypass RLS
CREATE OR REPLACE FUNCTION ensure_seller_stats_exists()
RETURNS TRIGGER 
SECURITY DEFINER
AS $$
BEGIN
    INSERT INTO seller_stats (user_id)
    VALUES (NEW.seller_id)
    ON CONFLICT (user_id) DO NOTHING;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION update_seller_stats_on_order_complete()
RETURNS TRIGGER
SECURITY DEFINER
AS $$
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
