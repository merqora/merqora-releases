-- ═══════════════════════════════════════════════════════════════════════════════
-- TABLA DE CARRITO DE COMPRAS - PERSISTENCIA EN SUPABASE
-- ═══════════════════════════════════════════════════════════════════════════════

-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. TABLA CART_ITEMS
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    selected_color TEXT,
    selected_size TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Un usuario solo puede tener un item por combinación de post/color/size
    UNIQUE(user_id, post_id, selected_color, selected_size)
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. ÍNDICES
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE INDEX IF NOT EXISTS idx_cart_items_user_id ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_post_id ON cart_items(post_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. TRIGGER PARA UPDATED_AT
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_cart_item_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS cart_items_update_timestamp ON cart_items;
CREATE TRIGGER cart_items_update_timestamp
    BEFORE UPDATE ON cart_items
    FOR EACH ROW
    EXECUTE FUNCTION update_cart_item_timestamp();

-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. RLS (ROW LEVEL SECURITY)
-- ═══════════════════════════════════════════════════════════════════════════════

ALTER TABLE cart_items ENABLE ROW LEVEL SECURITY;

-- Los usuarios solo pueden ver su propio carrito
DROP POLICY IF EXISTS "Users can view own cart" ON cart_items;
CREATE POLICY "Users can view own cart" ON cart_items
    FOR SELECT USING (user_id = auth.uid());

-- Los usuarios solo pueden agregar a su propio carrito
DROP POLICY IF EXISTS "Users can add to own cart" ON cart_items;
CREATE POLICY "Users can add to own cart" ON cart_items
    FOR INSERT WITH CHECK (user_id = auth.uid());

-- Los usuarios solo pueden actualizar su propio carrito
DROP POLICY IF EXISTS "Users can update own cart" ON cart_items;
CREATE POLICY "Users can update own cart" ON cart_items
    FOR UPDATE USING (user_id = auth.uid());

-- Los usuarios solo pueden eliminar de su propio carrito
DROP POLICY IF EXISTS "Users can delete from own cart" ON cart_items;
CREATE POLICY "Users can delete from own cart" ON cart_items
    FOR DELETE USING (user_id = auth.uid());

-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. FUNCIÓN PARA OBTENER CARRITO CON DATOS DEL POST
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION get_user_cart()
RETURNS TABLE (
    id UUID,
    post_id UUID,
    quantity INTEGER,
    selected_color TEXT,
    selected_size TEXT,
    created_at TIMESTAMPTZ,
    post_data JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        ci.id,
        ci.post_id,
        ci.quantity,
        ci.selected_color,
        ci.selected_size,
        ci.created_at,
        jsonb_build_object(
            'id', p.id,
            'title', p.title,
            'price', p.price,
            'images', p.images,
            'user_id', p.user_id
        ) as post_data
    FROM cart_items ci
    JOIN posts p ON p.id = ci.post_id
    WHERE ci.user_id = auth.uid()
    ORDER BY ci.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 6. FUNCIÓN PARA UPSERT (AGREGAR O ACTUALIZAR CANTIDAD)
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION upsert_cart_item(
    p_post_id UUID,
    p_quantity INTEGER,
    p_selected_color TEXT DEFAULT NULL,
    p_selected_size TEXT DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    v_id UUID;
BEGIN
    INSERT INTO cart_items (user_id, post_id, quantity, selected_color, selected_size)
    VALUES (auth.uid(), p_post_id, p_quantity, p_selected_color, p_selected_size)
    ON CONFLICT (user_id, post_id, selected_color, selected_size)
    DO UPDATE SET 
        quantity = cart_items.quantity + EXCLUDED.quantity,
        updated_at = NOW()
    RETURNING id INTO v_id;
    
    RETURN v_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 7. PERMISOS
-- ═══════════════════════════════════════════════════════════════════════════════

GRANT SELECT, INSERT, UPDATE, DELETE ON cart_items TO authenticated;
GRANT EXECUTE ON FUNCTION get_user_cart() TO authenticated;
GRANT EXECUTE ON FUNCTION upsert_cart_item(UUID, INTEGER, TEXT, TEXT) TO authenticated;

-- ═══════════════════════════════════════════════════════════════════════════════
-- NOTAS:
-- ═══════════════════════════════════════════════════════════════════════════════
-- 
-- - El carrito se guarda automáticamente por usuario
-- - RLS asegura que cada usuario solo vea su carrito
-- - upsert_cart_item maneja agregar o incrementar cantidad automáticamente
-- - Al eliminar un post, se elimina del carrito (CASCADE)
--
-- ═══════════════════════════════════════════════════════════════════════════════
