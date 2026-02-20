-- ═══════════════════════════════════════════════════════════════════════════════
-- MIGRATION: Add warranty, returns_accepted, and colors columns to posts table
-- Run this in Supabase SQL Editor
-- ═══════════════════════════════════════════════════════════════════════════════

-- Add warranty column (text, default 'Sin garantía')
ALTER TABLE posts 
ADD COLUMN IF NOT EXISTS warranty TEXT DEFAULT 'Sin garantía';

-- Add returns_accepted column (boolean, default false)
ALTER TABLE posts 
ADD COLUMN IF NOT EXISTS returns_accepted BOOLEAN DEFAULT false;

-- Add colors column (jsonb array, default empty array)
ALTER TABLE posts 
ADD COLUMN IF NOT EXISTS colors JSONB DEFAULT '[]'::jsonb;

-- ═══════════════════════════════════════════════════════════════════════════════
-- VERIFY: Check the new columns were added
-- ═══════════════════════════════════════════════════════════════════════════════
-- SELECT column_name, data_type, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'posts' 
-- AND column_name IN ('warranty', 'returns_accepted', 'colors');

-- ═══════════════════════════════════════════════════════════════════════════════
-- OPTIONAL: Update existing posts with default values (if needed)
-- ═══════════════════════════════════════════════════════════════════════════════
-- UPDATE posts SET warranty = 'Sin garantía' WHERE warranty IS NULL;
-- UPDATE posts SET returns_accepted = false WHERE returns_accepted IS NULL;
-- UPDATE posts SET colors = '[]'::jsonb WHERE colors IS NULL;
