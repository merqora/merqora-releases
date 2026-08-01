-- Add avatar_shape column to usuarios table
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS avatar_shape TEXT DEFAULT 'circle';

-- Update existing users to have circle shape
UPDATE usuarios SET avatar_shape = 'circle' WHERE avatar_shape IS NULL;
