-- Migration 006: RLS policies for deleting orders
-- Enable delete on orders, order_items, and payments for buyers/sellers

-- Orders: allow buyer or any seller of items in the order to delete
CREATE POLICY "Users can delete own orders"
    ON orders FOR DELETE
    USING (
        auth.uid() = buyer_id
        OR auth.uid()::text IN (
            SELECT seller_id::text FROM order_items WHERE order_id = orders.id
        )
    );

-- Order items: cascade delete when order is deleted by authorized user
CREATE POLICY "Users can delete order items of own orders"
    ON order_items FOR DELETE
    USING (
        auth.uid() IN (
            SELECT buyer_id FROM orders WHERE id = order_id
        )
        OR auth.uid()::text = seller_id::text
    );

-- Payments: allow delete when order is deleted by authorized user
CREATE POLICY "Users can delete payments of own orders"
    ON payments FOR DELETE
    USING (
        auth.uid() IN (
            SELECT buyer_id FROM orders WHERE id = order_id
        )
        OR auth.uid()::text IN (
            SELECT seller_id::text FROM order_items WHERE order_id = payments.order_id
        )
    );
