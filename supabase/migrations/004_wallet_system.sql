-- ============================================================
-- 004: Wallet System — Billetera virtual para vendedores
-- ============================================================

-- Billetera de cada usuario
CREATE TABLE wallets (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    currency TEXT NOT NULL DEFAULT 'UYU',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Historial de transacciones de la billetera
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL CHECK (type IN ('credit', 'debit', 'withdrawal', 'refund')),
    amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reference_type TEXT,  -- 'order', 'withdrawal'
    reference_id TEXT,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'completed' CHECK (status IN ('completed', 'pending', 'failed')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Solicitudes de retiro
CREATE TABLE withdrawals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    amount DECIMAL(12,2) NOT NULL,
    method TEXT NOT NULL CHECK (method IN ('bank_transfer', 'prex', 'mercadopago')),
    destination JSONB,  -- datos de destino (cuenta, alias, etc)
    status TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'rejected')),
    admin_notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,
    CONSTRAINT positive_amount CHECK (amount > 0)
);

-- Índices
CREATE INDEX idx_wallet_transactions_user ON wallet_transactions(wallet_user_id, created_at DESC);
CREATE INDEX idx_withdrawals_user ON withdrawals(user_id, created_at DESC);
CREATE INDEX idx_withdrawals_status ON withdrawals(status);

-- RLS
ALTER TABLE wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE wallet_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE withdrawals ENABLE ROW LEVEL SECURITY;

-- Políticas: cada usuario ve solo sus propios datos
CREATE POLICY "users can view own wallet" ON wallets
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "users can insert own wallet" ON wallets
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "users can update own wallet" ON wallets
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "users can view own transactions" ON wallet_transactions
    FOR SELECT USING (auth.uid() = wallet_user_id);
CREATE POLICY "system can insert transactions" ON wallet_transactions
    FOR INSERT WITH CHECK (auth.uid() = wallet_user_id);

CREATE POLICY "users can view own withdrawals" ON withdrawals
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "users can insert withdrawals" ON withdrawals
    FOR INSERT WITH CHECK (auth.uid() = user_id);

-- Trigger auto-update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_wallets_updated_at
    BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
