-- Tabla de métodos de cobro de vendedores
CREATE TABLE IF NOT EXISTS payout_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    mp_email TEXT,
    default_method TEXT DEFAULT 'mercadopago',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id)
);

-- Tabla de desembolsos a vendedores
CREATE TABLE IF NOT EXISTS disbursements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    seller_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount DECIMAL(10,2) NOT NULL,
    mp_payment_id TEXT,
    status TEXT DEFAULT 'pending',
    seller_mp_email TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_payout_methods_user ON payout_methods(user_id);
CREATE INDEX IF NOT EXISTS idx_disbursements_order ON disbursements(order_id);
CREATE INDEX IF NOT EXISTS idx_disbursements_seller ON disbursements(seller_id);

-- RLS
ALTER TABLE payout_methods ENABLE ROW LEVEL SECURITY;
ALTER TABLE disbursements ENABLE ROW LEVEL SECURITY;

-- Políticas para payout_methods
CREATE POLICY "Users can view their own payout methods"
    ON payout_methods FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert their own payout methods"
    ON payout_methods FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update their own payout methods"
    ON payout_methods FOR UPDATE
    USING (auth.uid() = user_id);

-- Políticas para disbursements (solo lectura para vendedores)
CREATE POLICY "Sellers can view their own disbursements"
    ON disbursements FOR SELECT
    USING (auth.uid() = seller_id);
