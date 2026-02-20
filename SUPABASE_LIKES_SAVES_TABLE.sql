-- ============================================
-- TABLA: post_likes
-- Likes de usuarios a posts
-- ============================================

CREATE TABLE IF NOT EXISTS post_likes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_post_likes_user ON post_likes(user_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_post ON post_likes(post_id);

-- RLS
ALTER TABLE post_likes ENABLE ROW LEVEL SECURITY;

-- Políticas
CREATE POLICY "Users can view all likes"
ON post_likes FOR SELECT
USING (true);

CREATE POLICY "Users can insert their own likes"
ON post_likes FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete their own likes"
ON post_likes FOR DELETE
USING (auth.uid() = user_id);

-- ============================================
-- TABLA: post_saves
-- Guardados de usuarios a posts
-- ============================================

CREATE TABLE IF NOT EXISTS post_saves (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_post_saves_user ON post_saves(user_id);
CREATE INDEX IF NOT EXISTS idx_post_saves_post ON post_saves(post_id);

-- RLS
ALTER TABLE post_saves ENABLE ROW LEVEL SECURITY;

-- Políticas
CREATE POLICY "Users can view all saves"
ON post_saves FOR SELECT
USING (true);

CREATE POLICY "Users can insert their own saves"
ON post_saves FOR INSERT
WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete their own saves"
ON post_saves FOR DELETE
USING (auth.uid() = user_id);

-- ============================================
-- INSTRUCCIONES:
-- 1. Ejecutar este SQL en Supabase SQL Editor
-- 2. Las tablas permitirán persistir likes y saves
-- ============================================
