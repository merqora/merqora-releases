-- ═══════════════════════════════════════════════════════════════
-- ACTUALIZACIÓN: Agregar columnas de personalización a highlights
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar este SQL en Supabase para agregar soporte completo de marcos

-- Agregar columna frame_style con valor por defecto 'CLASSIC'
ALTER TABLE highlights 
ADD COLUMN IF NOT EXISTS frame_style TEXT DEFAULT 'CLASSIC';

-- Agregar columna frame_color para color personalizado del marco
ALTER TABLE highlights 
ADD COLUMN IF NOT EXISTS frame_color TEXT DEFAULT 'CATEGORY';

-- Agregar columna background_color para color de fondo del highlight
ALTER TABLE highlights 
ADD COLUMN IF NOT EXISTS background_color TEXT DEFAULT 'DEFAULT';

-- Agregar columna icon para el icono del highlight
ALTER TABLE highlights 
ADD COLUMN IF NOT EXISTS icon TEXT DEFAULT 'Star';

-- Actualizar highlights existentes para que tengan los valores por defecto
UPDATE highlights SET frame_style = 'CLASSIC' WHERE frame_style IS NULL;
UPDATE highlights SET frame_color = 'CATEGORY' WHERE frame_color IS NULL;
UPDATE highlights SET background_color = 'DEFAULT' WHERE background_color IS NULL;
UPDATE highlights SET icon = 'Star' WHERE icon IS NULL;

-- Verificar que se agregaron correctamente
SELECT column_name, data_type, column_default 
FROM information_schema.columns 
WHERE table_name = 'highlights' 
AND column_name IN ('frame_style', 'frame_color', 'background_color', 'icon');

-- ═══════════════════════════════════════════════════════════════
-- VALORES VÁLIDOS:
-- ═══════════════════════════════════════════════════════════════
-- frame_style: CLASSIC, THIN, BOLD, DOUBLE, DASHED, GLOW, NEON, RAINBOW, GOLDEN, DIAMOND
-- frame_color: CATEGORY, PURPLE, BLUE, CYAN, GREEN, ORANGE, PINK, RED, GOLD, WHITE
-- background_color: DEFAULT, PURPLE, PINK, BLUE, GREEN, ORANGE, CYAN, GOLD
-- icon: Estrella, Corazón, Magia, Diamante, Trofeo, Fuego, Rayo, Celebración, Destello, Trending, Compras, Regalo
-- ═══════════════════════════════════════════════════════════════
