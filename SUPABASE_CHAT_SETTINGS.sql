-- ═══════════════════════════════════════════════════════════════════════════════
-- RENDLY - CHAT SETTINGS TABLE
-- Per-conversation mute settings for notifications
-- ═══════════════════════════════════════════════════════════════════════════════

-- Tabla para silenciar notificaciones de conversaciones específicas
CREATE TABLE IF NOT EXISTS muted_chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    muted_until TIMESTAMPTZ, -- NULL = muted indefinitely
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, conversation_id)
);

CREATE INDEX IF NOT EXISTS idx_muted_chats_user ON muted_chats(user_id);

-- RLS
ALTER TABLE muted_chats ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view their own muted chats" ON muted_chats
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can mute chats" ON muted_chats
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can unmute chats" ON muted_chats
    FOR DELETE USING (auth.uid() = user_id);
