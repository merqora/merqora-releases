-- ═══════════════════════════════════════════════════════════════
-- AGREGAR COLUMNA saves_count A LA TABLA rends
-- ═══════════════════════════════════════════════════════════════

-- Agregar columna saves_count si no existe
ALTER TABLE rends 
ADD COLUMN IF NOT EXISTS saves_count INTEGER DEFAULT 0;

-- Verificar que la columna existe
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'rends' 
AND column_name IN ('saves_count', 'shares_count', 'comments_count', 'likes_count');
