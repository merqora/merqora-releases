-- Tabla para highlights del perfil
CREATE TABLE IF NOT EXISTS highlights (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title VARCHAR(50) NOT NULL,
    cover_url TEXT,
    category VARCHAR(20) DEFAULT 'CUSTOM',
    stories_count INTEGER DEFAULT 0,
    is_new BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índice para buscar highlights por usuario
CREATE INDEX IF NOT EXISTS idx_highlights_user_id ON highlights(user_id);

-- Tabla para stories dentro de highlights
CREATE TABLE IF NOT EXISTS highlight_stories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    highlight_id UUID NOT NULL REFERENCES highlights(id) ON DELETE CASCADE,
    story_id UUID REFERENCES stories(id) ON DELETE SET NULL,
    media_url TEXT NOT NULL,
    media_type VARCHAR(10) DEFAULT 'image',
    position INTEGER DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índice para buscar stories por highlight
CREATE INDEX IF NOT EXISTS idx_highlight_stories_highlight_id ON highlight_stories(highlight_id);

-- Tabla para publicaciones (posts)
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    caption TEXT,
    media_url TEXT NOT NULL,
    media_type VARCHAR(10) DEFAULT 'image',
    likes_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    views_count INTEGER DEFAULT 0,
    is_featured BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índice para buscar posts por usuario
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);

-- RLS Policies para highlights
ALTER TABLE highlights ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view all highlights" ON highlights
    FOR SELECT USING (true);

CREATE POLICY "Users can insert own highlights" ON highlights
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own highlights" ON highlights
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own highlights" ON highlights
    FOR DELETE USING (auth.uid() = user_id);

-- RLS Policies para highlight_stories
ALTER TABLE highlight_stories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view all highlight stories" ON highlight_stories
    FOR SELECT USING (true);

CREATE POLICY "Users can insert to own highlights" ON highlight_stories
    FOR INSERT WITH CHECK (
        EXISTS (SELECT 1 FROM highlights WHERE id = highlight_id AND user_id = auth.uid())
    );

CREATE POLICY "Users can delete from own highlights" ON highlight_stories
    FOR DELETE USING (
        EXISTS (SELECT 1 FROM highlights WHERE id = highlight_id AND user_id = auth.uid())
    );

-- RLS Policies para posts
ALTER TABLE posts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view all posts" ON posts
    FOR SELECT USING (true);

CREATE POLICY "Users can insert own posts" ON posts
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own posts" ON posts
    FOR UPDATE USING (auth.uid() = user_id);

CREATE POLICY "Users can delete own posts" ON posts
    FOR DELETE USING (auth.uid() = user_id);
