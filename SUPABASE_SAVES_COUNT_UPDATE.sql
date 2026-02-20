-- ============================================
-- ACTUALIZACIÓN: Agregar columna saves_count a posts
-- ============================================

-- Agregar columna saves_count si no existe
ALTER TABLE posts ADD COLUMN IF NOT EXISTS saves_count INTEGER DEFAULT 0;

-- Actualizar saves_count basado en registros existentes en post_saves
UPDATE posts 
SET saves_count = (
    SELECT COUNT(*) 
    FROM post_saves 
    WHERE post_saves.post_id = posts.id
);

-- ============================================
-- INSTRUCCIONES:
-- 1. Ejecutar este SQL en Supabase SQL Editor
-- 2. La columna saves_count se agregará a la tabla posts
-- 3. Se actualizarán los conteos basados en saves existentes
-- ============================================
