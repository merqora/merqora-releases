-- ============================================================
-- 005: Order PDFs — Comprobantes de transacciones
-- ============================================================

CREATE TABLE order_pdfs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role TEXT NOT NULL CHECK (role IN ('buyer', 'seller')),
    file_url TEXT NOT NULL,
    file_name TEXT NOT NULL,
    file_size BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_pdfs_user ON order_pdfs(user_id, created_at DESC);
CREATE INDEX idx_order_pdfs_order ON order_pdfs(order_id);

ALTER TABLE order_pdfs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "users can view own pdfs" ON order_pdfs
    FOR SELECT USING (auth.uid() = user_id);
CREATE POLICY "users can insert own pdfs" ON order_pdfs
    FOR INSERT WITH CHECK (auth.uid() = user_id);
CREATE POLICY "users can delete own pdfs" ON order_pdfs
    FOR DELETE USING (auth.uid() = user_id);
