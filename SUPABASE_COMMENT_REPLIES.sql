-- =====================================================
-- SISTEMA DE RESPUESTAS A OPINIONES (Comment Replies)
-- =====================================================
-- Permite responder a opiniones en la tabla product_reviews.
-- Las respuestas se almacenan en la misma tabla con parent_id.

-- =====================================================
-- Agregar parent_id a la tabla product_reviews
-- =====================================================
ALTER TABLE public.product_reviews
ADD COLUMN IF NOT EXISTS parent_id UUID DEFAULT NULL;

CREATE INDEX IF NOT EXISTS idx_product_reviews_parent_id ON public.product_reviews(parent_id);

-- =====================================================
-- Verificar estructura de la tabla
-- =====================================================
-- Puedes ejecutar esta query para verificar que el campo se agregó correctamente:
-- SELECT column_name, data_type, is_nullable 
-- FROM information_schema.columns 
-- WHERE table_name = 'product_reviews' AND column_name = 'parent_id';
