-- ============================================================================
-- USER SETTINGS - Privacy, Language, Story Hidden Users, Mention Search
-- Run in Supabase SQL Editor
-- ============================================================================

-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. PRIVACY SETTINGS TABLE
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS user_privacy_settings (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    profile_visibility TEXT DEFAULT 'public' CHECK (profile_visibility IN ('public', 'followers', 'private')),
    show_online_status BOOLEAN DEFAULT true,
    show_last_seen BOOLEAN DEFAULT true,
    show_activity_status BOOLEAN DEFAULT true,
    allow_tagging BOOLEAN DEFAULT true,
    allow_mentions BOOLEAN DEFAULT true,
    show_likes BOOLEAN DEFAULT true,
    show_purchase_activity BOOLEAN DEFAULT true,
    hide_story_enabled BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE user_privacy_settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Anyone can read privacy settings"
    ON user_privacy_settings FOR SELECT USING (true);

CREATE POLICY "Users can insert own privacy settings"
    ON user_privacy_settings FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own privacy settings"
    ON user_privacy_settings FOR UPDATE
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. STORY HIDDEN USERS TABLE
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS story_hidden_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    hidden_user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, hidden_user_id)
);

CREATE INDEX IF NOT EXISTS idx_story_hidden_user ON story_hidden_users(user_id);
CREATE INDEX IF NOT EXISTS idx_story_hidden_target ON story_hidden_users(hidden_user_id);

ALTER TABLE story_hidden_users ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own hidden list"
    ON story_hidden_users FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own hidden list"
    ON story_hidden_users FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can delete own hidden list"
    ON story_hidden_users FOR DELETE
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. LANGUAGE PREFERENCES TABLE
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS user_language_preferences (
    user_id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    language_code TEXT DEFAULT 'es',
    auto_detect BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE user_language_preferences ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own language"
    ON user_language_preferences FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own language"
    ON user_language_preferences FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own language"
    ON user_language_preferences FOR UPDATE
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. TRIGGERS FOR updated_at
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION update_user_settings_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_privacy_updated ON user_privacy_settings;
CREATE TRIGGER trigger_privacy_updated
    BEFORE UPDATE ON user_privacy_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_user_settings_timestamp();

DROP TRIGGER IF EXISTS trigger_language_updated ON user_language_preferences;
CREATE TRIGGER trigger_language_updated
    BEFORE UPDATE ON user_language_preferences
    FOR EACH ROW
    EXECUTE FUNCTION update_user_settings_timestamp();

-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. RPC: Search users for @ mentions
-- Returns users prioritized by interaction (followers, messages, etc.)
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION search_users_for_mention(
    p_current_user_id UUID,
    p_query TEXT DEFAULT '',
    p_limit INT DEFAULT 15
)
RETURNS TABLE(
    mention_user_id UUID,
    mention_username TEXT,
    mention_avatar_url TEXT,
    mention_nombre_tienda TEXT,
    mention_is_verified BOOLEAN,
    mention_is_following BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.user_id,
        u.username,
        u.avatar_url,
        u.nombre_tienda,
        COALESCE(u.is_verified, false),
        EXISTS(
            SELECT 1 FROM followers f 
            WHERE f.follower_id = p_current_user_id AND f.following_id = u.user_id
        ) as is_following
    FROM usuarios u
    LEFT JOIN user_privacy_settings ups ON u.user_id = ups.user_id
    WHERE u.user_id != p_current_user_id
        AND (COALESCE(ups.allow_mentions, true) = true)
        AND (
            p_query = '' 
            OR u.username ILIKE '%' || p_query || '%'
            OR u.nombre_tienda ILIKE '%' || p_query || '%'
        )
    ORDER BY 
        EXISTS(
            SELECT 1 FROM followers f 
            WHERE f.follower_id = p_current_user_id AND f.following_id = u.user_id
        ) DESC,
        u.username ASC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 6. RPC: Check if user can view stories
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE FUNCTION can_view_stories(p_viewer_id UUID, p_owner_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_visibility TEXT;
BEGIN
    -- Check if viewer is hidden
    IF EXISTS (
        SELECT 1 FROM story_hidden_users 
        WHERE user_id = p_owner_id AND hidden_user_id = p_viewer_id
    ) THEN
        RETURN false;
    END IF;
    
    -- Check profile visibility
    SELECT profile_visibility INTO v_visibility
    FROM user_privacy_settings
    WHERE user_id = p_owner_id;
    
    IF v_visibility = 'private' THEN
        RETURN EXISTS (
            SELECT 1 FROM followers 
            WHERE follower_id = p_viewer_id AND following_id = p_owner_id
        );
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 7. Update user_presence to respect privacy settings
-- ═══════════════════════════════════════════════════════════════════════════════

-- RPC to get online status respecting privacy
CREATE OR REPLACE FUNCTION get_user_online_status(p_user_id UUID)
RETURNS TABLE(
    is_online BOOLEAN,
    last_seen TIMESTAMPTZ
) AS $$
DECLARE
    v_show_online BOOLEAN;
    v_show_last_seen BOOLEAN;
BEGIN
    SELECT ups.show_online_status, ups.show_last_seen
    INTO v_show_online, v_show_last_seen
    FROM user_privacy_settings ups
    WHERE ups.user_id = p_user_id;
    
    -- Defaults if no settings found
    v_show_online := COALESCE(v_show_online, true);
    v_show_last_seen := COALESCE(v_show_last_seen, true);
    
    RETURN QUERY
    SELECT 
        CASE WHEN v_show_online THEN up.is_online ELSE false END,
        CASE WHEN v_show_last_seen THEN up.last_seen ELSE NULL END
    FROM user_presence up
    WHERE up.user_id = p_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- DONE! Tables: user_privacy_settings, story_hidden_users, user_language_preferences
-- RPCs: search_users_for_mention, can_view_stories, get_user_online_status
-- ============================================================================
