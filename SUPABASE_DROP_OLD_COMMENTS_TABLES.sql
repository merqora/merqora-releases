-- =====================================================
-- ELIMINAR TABLAS ANTIGUAS DE COMENTARIOS
-- =====================================================
-- Ya que ahora todo se guarda en product_reviews,
-- estas tablas ya no son necesarias.
-- =====================================================

-- IMPORTANTE: Ejecutar DESPUÉS de migrar datos existentes
-- si hay comentarios que quieras conservar.

-- Eliminar tabla comments (comentarios de posts)
DROP TABLE IF EXISTS public.comments CASCADE;

-- Eliminar tabla rend_comments (comentarios de rends)
DROP TABLE IF EXISTS public.rend_comments CASCADE;

-- =====================================================
-- NOTA: Si tienes datos existentes en estas tablas que
-- quieras conservar, primero debes migrarlos a 
-- product_reviews antes de ejecutar este script.
-- =====================================================
