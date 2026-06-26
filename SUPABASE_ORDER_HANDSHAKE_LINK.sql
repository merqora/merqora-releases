-- ═══════════════════════════════════════════════════════════════════════════════
-- LINK ORDERS WITH HANDSHAKE TRANSACTIONS
-- Adds handshake_id column to orders table to connect order status
-- with the handshake confirmation system from chat
-- ═══════════════════════════════════════════════════════════════════════════════

-- Add handshake_id column to orders table (TEXT to match handshake_transactions.id)
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS handshake_id TEXT REFERENCES handshake_transactions(id) ON DELETE SET NULL;

-- Index for fast lookups
CREATE INDEX IF NOT EXISTS idx_orders_handshake_id ON orders(handshake_id) WHERE handshake_id IS NOT NULL;

-- Function to auto-complete order when handshake is completed
CREATE OR REPLACE FUNCTION sync_order_from_handshake()
RETURNS TRIGGER AS $$
BEGIN
    -- When handshake status changes to COMPLETED, update linked order
    IF NEW.status = 'COMPLETED' AND OLD.status != 'COMPLETED' THEN
        UPDATE orders 
        SET status = 'completed',
            delivered_at = NOW(),
            updated_at = NOW()
        WHERE handshake_id = NEW.id
          AND status NOT IN ('completed', 'cancelled', 'refunded');
    END IF;
    
    -- When handshake is cancelled/rejected, don't auto-cancel order
    -- (seller might want to propose a new handshake)
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: sync order status when handshake updates
DROP TRIGGER IF EXISTS trigger_sync_order_handshake ON handshake_transactions;
CREATE TRIGGER trigger_sync_order_handshake
    AFTER UPDATE ON handshake_transactions
    FOR EACH ROW
    EXECUTE FUNCTION sync_order_from_handshake();

-- Function to link a handshake to an order (called from app or RPC)
CREATE OR REPLACE FUNCTION link_handshake_to_order(
    p_order_id UUID,
    p_handshake_id TEXT
) RETURNS VOID AS $$
BEGIN
    UPDATE orders 
    SET handshake_id = p_handshake_id,
        updated_at = NOW()
    WHERE id = p_order_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
