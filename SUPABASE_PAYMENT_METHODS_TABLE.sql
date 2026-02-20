-- ══════════════════════════════════════════════════════════════════════════════
-- PAYMENT METHODS TABLE - Sistema de métodos de pago para Rendly
-- ══════════════════════════════════════════════════════════════════════════════

-- Tabla principal de métodos de pago
CREATE TABLE IF NOT EXISTS payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    
    -- Tipo de método de pago
    method_type TEXT NOT NULL CHECK (method_type IN ('credit_card', 'debit_card', 'bank_account', 'digital_wallet')),
    
    -- Información de la tarjeta (encriptada/tokenizada en producción)
    card_brand TEXT, -- visa, mastercard, amex, etc.
    card_last_four TEXT NOT NULL, -- Últimos 4 dígitos
    card_bin TEXT, -- Primeros 6 dígitos (para identificar banco/tipo)
    card_holder_name TEXT NOT NULL,
    expiry_month INTEGER CHECK (expiry_month >= 1 AND expiry_month <= 12),
    expiry_year INTEGER CHECK (expiry_year >= 2024),
    
    -- Token de pago (para procesadores como Stripe, MercadoPago, etc.)
    payment_token TEXT, -- Token del procesador de pagos
    processor TEXT DEFAULT 'internal', -- stripe, mercadopago, etc.
    
    -- Metadatos
    label TEXT, -- "Tarjeta personal", "Trabajo", etc.
    is_default BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    
    -- Validación nativa
    validation_score REAL DEFAULT 0.0, -- Score del motor C++ (0-100)
    validation_status TEXT DEFAULT 'pending' CHECK (validation_status IN ('pending', 'valid', 'suspicious', 'invalid', 'expired')),
    
    -- Información adicional
    billing_address_id UUID REFERENCES addresses(id) ON DELETE SET NULL,
    country_code TEXT DEFAULT 'UY',
    currency TEXT DEFAULT 'UYU',
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_used_at TIMESTAMP WITH TIME ZONE,
    
    -- Constraints
    CONSTRAINT unique_card_per_user UNIQUE (user_id, card_last_four, card_brand)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_payment_methods_user_id ON payment_methods(user_id);
CREATE INDEX IF NOT EXISTS idx_payment_methods_default ON payment_methods(user_id, is_default) WHERE is_default = TRUE;
CREATE INDEX IF NOT EXISTS idx_payment_methods_status ON payment_methods(validation_status);

-- Trigger para updated_at
CREATE OR REPLACE FUNCTION update_payment_methods_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_payment_methods_updated_at ON payment_methods;
CREATE TRIGGER trigger_payment_methods_updated_at
    BEFORE UPDATE ON payment_methods
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_methods_updated_at();

-- Función para asegurar solo un método de pago por defecto por usuario
CREATE OR REPLACE FUNCTION ensure_single_default_payment_method()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_default = TRUE THEN
        UPDATE payment_methods
        SET is_default = FALSE
        WHERE user_id = NEW.user_id
          AND id != NEW.id
          AND is_default = TRUE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_single_default_payment ON payment_methods;
CREATE TRIGGER trigger_single_default_payment
    BEFORE INSERT OR UPDATE OF is_default ON payment_methods
    FOR EACH ROW
    WHEN (NEW.is_default = TRUE)
    EXECUTE FUNCTION ensure_single_default_payment_method();

-- RLS Policies
ALTER TABLE payment_methods ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own payment methods" ON payment_methods;
CREATE POLICY "Users can view own payment methods" ON payment_methods
    FOR SELECT USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own payment methods" ON payment_methods;
CREATE POLICY "Users can insert own payment methods" ON payment_methods
    FOR INSERT WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can update own payment methods" ON payment_methods;
CREATE POLICY "Users can update own payment methods" ON payment_methods
    FOR UPDATE USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete own payment methods" ON payment_methods;
CREATE POLICY "Users can delete own payment methods" ON payment_methods
    FOR DELETE USING (auth.uid() = user_id);

-- ══════════════════════════════════════════════════════════════════════════════
-- TABLA DE TRANSACCIONES (para historial)
-- ══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    payment_method_id UUID REFERENCES payment_methods(id) ON DELETE SET NULL,
    
    -- Detalles de la transacción
    amount DECIMAL(12, 2) NOT NULL,
    currency TEXT DEFAULT 'UYU',
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'completed', 'failed', 'refunded', 'cancelled')),
    
    -- Referencias
    order_id UUID, -- Referencia al pedido
    external_transaction_id TEXT, -- ID del procesador de pagos
    
    -- Metadatos
    description TEXT,
    metadata JSONB DEFAULT '{}',
    
    -- Timestamps
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON payment_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_payment_method ON payment_transactions(payment_method_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON payment_transactions(status);

-- RLS para transacciones
ALTER TABLE payment_transactions ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own transactions" ON payment_transactions;
CREATE POLICY "Users can view own transactions" ON payment_transactions
    FOR SELECT USING (auth.uid() = user_id);
