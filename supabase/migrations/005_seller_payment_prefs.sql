-- Seller Payment Preferences
-- Almacena los métodos de pago que cada vendedor acepta

CREATE TABLE IF NOT EXISTS seller_payment_prefs (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    accepts_mercado_pago BOOLEAN NOT NULL DEFAULT TRUE,
    accepts_card BOOLEAN NOT NULL DEFAULT TRUE,
    accepts_bank_transfer BOOLEAN NOT NULL DEFAULT TRUE,
    accepts_cash BOOLEAN NOT NULL DEFAULT FALSE,
    accepts_prex BOOLEAN NOT NULL DEFAULT FALSE,
    max_installments INT NOT NULL DEFAULT 12,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Trigger para actualizar updated_at automáticamente
CREATE OR REPLACE FUNCTION update_seller_payment_prefs_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_seller_payment_prefs_updated_at
    BEFORE UPDATE ON seller_payment_prefs
    FOR EACH ROW
    EXECUTE FUNCTION update_seller_payment_prefs_updated_at();

-- Índice para búsquedas por user_id
CREATE INDEX IF NOT EXISTS idx_seller_payment_prefs_user ON seller_payment_prefs(user_id);

-- Seguridad a nivel de fila
ALTER TABLE seller_payment_prefs ENABLE ROW LEVEL SECURITY;

-- Políticas:
-- SELECT: el usuario puede ver sus propias prefs, cualquier usuario puede ver las prefs de otros vendedores (necesario para ProductPage)
CREATE POLICY select_own_payment_prefs ON seller_payment_prefs
    FOR SELECT
    USING (
        auth.uid() = user_id
        OR EXISTS (
            SELECT 1 FROM posts WHERE posts.user_id = seller_payment_prefs.user_id
        )
    );

-- INSERT: solo el propio usuario
CREATE POLICY insert_own_payment_prefs ON seller_payment_prefs
    FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- UPDATE: solo el propio usuario
CREATE POLICY update_own_payment_prefs ON seller_payment_prefs
    FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);
