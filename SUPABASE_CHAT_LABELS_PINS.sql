-- ═══════════════════════════════════════════════════════════════
-- SISTEMA DE ETIQUETAS Y CHATS FIJADOS
-- ═══════════════════════════════════════════════════════════════

-- 1. Tabla de chats fijados
CREATE TABLE IF NOT EXISTS pinned_chats (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    pinned_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, conversation_id)
);

-- 2. Tabla de etiquetas de chat (definidas por el usuario)
CREATE TABLE IF NOT EXISTS chat_labels (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    color TEXT NOT NULL DEFAULT '#7C3AED', -- Color hex
    icon TEXT DEFAULT NULL, -- Emoji opcional
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    sort_order INT DEFAULT 0
);

-- 3. Tabla de asignaciones de etiquetas a conversaciones
CREATE TABLE IF NOT EXISTS chat_label_assignments (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    label_id UUID NOT NULL REFERENCES chat_labels(id) ON DELETE CASCADE,
    assigned_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, conversation_id, label_id)
);

-- ═══════════════════════════════════════════════════════════════
-- ÍNDICES
-- ═══════════════════════════════════════════════════════════════

CREATE INDEX IF NOT EXISTS idx_pinned_chats_user ON pinned_chats(user_id);
CREATE INDEX IF NOT EXISTS idx_pinned_chats_conv ON pinned_chats(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_labels_user ON chat_labels(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_label_assignments_user ON chat_label_assignments(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_label_assignments_conv ON chat_label_assignments(conversation_id);
CREATE INDEX IF NOT EXISTS idx_chat_label_assignments_label ON chat_label_assignments(label_id);

-- ═══════════════════════════════════════════════════════════════
-- RLS (Row Level Security)
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE pinned_chats ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_labels ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_label_assignments ENABLE ROW LEVEL SECURITY;

-- Pinned chats: solo el dueño
CREATE POLICY "Users can manage their own pinned chats" ON pinned_chats
    FOR ALL USING (auth.uid() = user_id);

-- Chat labels: solo el dueño
CREATE POLICY "Users can manage their own labels" ON chat_labels
    FOR ALL USING (auth.uid() = user_id);

-- Chat label assignments: solo el dueño
CREATE POLICY "Users can manage their own label assignments" ON chat_label_assignments
    FOR ALL USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════
-- ETIQUETAS POR DEFECTO (se crean por trigger al primer uso)
-- ═══════════════════════════════════════════════════════════════

-- Función para crear etiquetas por defecto cuando un usuario las usa por primera vez
CREATE OR REPLACE FUNCTION create_default_labels_if_needed(p_user_id UUID)
RETURNS VOID AS $$
BEGIN
    -- Solo crear si el usuario no tiene etiquetas
    IF NOT EXISTS (SELECT 1 FROM chat_labels WHERE user_id = p_user_id) THEN
        INSERT INTO chat_labels (user_id, name, color, icon, sort_order) VALUES
            (p_user_id, 'Nuevo cliente', '#22C55E', '🟢', 0),
            (p_user_id, 'Pago pendiente', '#EF4444', '🔴', 1),
            (p_user_id, 'Pedido en proceso', '#F59E0B', '🟡', 2),
            (p_user_id, 'Artículo reservado', '#3B82F6', '🔵', 3),
            (p_user_id, 'Cliente frecuente', '#8B5CF6', '⭐', 4),
            (p_user_id, 'Consulta', '#06B6D4', '💬', 5);
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger updated_at para chat_labels
CREATE OR REPLACE FUNCTION update_chat_labels_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER chat_labels_updated_at
    BEFORE UPDATE ON chat_labels
    FOR EACH ROW
    EXECUTE FUNCTION update_chat_labels_updated_at();
