-- =============================================
-- TABLAS DE INTERACCIONES DE USUARIO
-- Bloqueos, Reportes, Posts Ocultos
-- =============================================

-- Tabla de usuarios bloqueados
CREATE TABLE IF NOT EXISTS blocked_users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    blocker_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(blocker_id, blocked_id)
);

-- Tabla de reportes de contenido
CREATE TABLE IF NOT EXISTS content_reports (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    reporter_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    content_type TEXT NOT NULL CHECK (content_type IN ('post', 'rend', 'user', 'comment', 'message', 'story')),
    content_id UUID NOT NULL,
    reported_user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    reason TEXT NOT NULL CHECK (reason IN ('spam', 'inappropriate', 'harassment', 'fake', 'violence', 'hate_speech', 'scam', 'other')),
    description TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'reviewing', 'resolved', 'dismissed')),
    admin_notes TEXT,
    resolved_by UUID REFERENCES auth.users(id),
    resolved_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Tabla de posts ocultos por usuario
CREATE TABLE IF NOT EXISTS hidden_posts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL,
    hidden_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

-- Tabla de usuarios silenciados (no bloqueados, solo ocultar su contenido)
CREATE TABLE IF NOT EXISTS muted_users (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    muter_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    muted_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(muter_id, muted_id)
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocker ON blocked_users(blocker_id);
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocked ON blocked_users(blocked_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_reporter ON content_reports(reporter_id);
CREATE INDEX IF NOT EXISTS idx_content_reports_status ON content_reports(status);
CREATE INDEX IF NOT EXISTS idx_content_reports_content ON content_reports(content_type, content_id);
CREATE INDEX IF NOT EXISTS idx_hidden_posts_user ON hidden_posts(user_id);
CREATE INDEX IF NOT EXISTS idx_muted_users_muter ON muted_users(muter_id);

-- RLS Policies
ALTER TABLE blocked_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE content_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE hidden_posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE muted_users ENABLE ROW LEVEL SECURITY;

-- Políticas para blocked_users
CREATE POLICY "Users can view their own blocks" ON blocked_users
    FOR SELECT USING (auth.uid() = blocker_id);

CREATE POLICY "Users can block others" ON blocked_users
    FOR INSERT WITH CHECK (auth.uid() = blocker_id);

CREATE POLICY "Users can unblock" ON blocked_users
    FOR DELETE USING (auth.uid() = blocker_id);

-- Políticas para content_reports
CREATE POLICY "Users can view their own reports" ON content_reports
    FOR SELECT USING (auth.uid() = reporter_id);

CREATE POLICY "Users can create reports" ON content_reports
    FOR INSERT WITH CHECK (auth.uid() = reporter_id);

-- Políticas para hidden_posts
CREATE POLICY "Users can view their hidden posts" ON hidden_posts
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can hide posts" ON hidden_posts
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can unhide posts" ON hidden_posts
    FOR DELETE USING (auth.uid() = user_id);

-- Políticas para muted_users
CREATE POLICY "Users can view their mutes" ON muted_users
    FOR SELECT USING (auth.uid() = muter_id);

CREATE POLICY "Users can mute others" ON muted_users
    FOR INSERT WITH CHECK (auth.uid() = muter_id);

CREATE POLICY "Users can unmute" ON muted_users
    FOR DELETE USING (auth.uid() = muter_id);

-- Función para verificar si un usuario está bloqueado
CREATE OR REPLACE FUNCTION is_user_blocked(blocker UUID, blocked UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM blocked_users 
        WHERE blocker_id = blocker AND blocked_id = blocked
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Función para verificar si un post está oculto
CREATE OR REPLACE FUNCTION is_post_hidden(p_user_id UUID, p_post_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM hidden_posts 
        WHERE user_id = p_user_id AND post_id = p_post_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
