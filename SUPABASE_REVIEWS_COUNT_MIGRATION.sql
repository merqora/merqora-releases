-- =====================================================
-- MIGRACIÓN: comments_count -> reviews_count
-- =====================================================
-- Elimina la columna comments_count y crea reviews_count
-- en las tablas posts y rends.
-- =====================================================

-- 1. Agregar nueva columna reviews_count a posts
ALTER TABLE public.posts ADD COLUMN IF NOT EXISTS reviews_count INTEGER DEFAULT 0;

-- 2. Agregar nueva columna reviews_count a rends
ALTER TABLE public.rends ADD COLUMN IF NOT EXISTS reviews_count INTEGER DEFAULT 0;

-- 3. Migrar datos existentes de comments_count a reviews_count (si hay datos)
UPDATE public.posts SET reviews_count = COALESCE(comments_count, 0) WHERE reviews_count = 0;
UPDATE public.rends SET reviews_count = COALESCE(comments_count, 0) WHERE reviews_count = 0;

-- 4. Eliminar columna antigua comments_count
ALTER TABLE public.posts DROP COLUMN IF EXISTS comments_count;
ALTER TABLE public.rends DROP COLUMN IF EXISTS comments_count;

-- =====================================================
-- FUNCIÓN: Incrementar reviews_count automáticamente
-- =====================================================

-- Función para actualizar el contador cuando se agrega una review
-- ACTUALIZA AMBAS TABLAS (posts y rends) que compartan el mismo product_id
CREATE OR REPLACE FUNCTION update_reviews_count()
RETURNS TRIGGER AS $$
BEGIN
    -- Actualizar TODOS los posts que tengan este product_id
    UPDATE public.posts 
    SET reviews_count = reviews_count + 1 
    WHERE product_id = NEW.product_id;
    
    -- Actualizar TODOS los rends que tengan este product_id
    UPDATE public.rends 
    SET reviews_count = reviews_count + 1 
    WHERE product_id = NEW.product_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Función para decrementar cuando se elimina una review
-- DECREMENTA AMBAS TABLAS (posts y rends) que compartan el mismo product_id
CREATE OR REPLACE FUNCTION decrement_reviews_count()
RETURNS TRIGGER AS $$
BEGIN
    -- Decrementar TODOS los posts que tengan este product_id
    UPDATE public.posts 
    SET reviews_count = GREATEST(reviews_count - 1, 0) 
    WHERE product_id = OLD.product_id;
    
    -- Decrementar TODOS los rends que tengan este product_id
    UPDATE public.rends 
    SET reviews_count = GREATEST(reviews_count - 1, 0) 
    WHERE product_id = OLD.product_id;
    
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- Eliminar triggers existentes si existen
DROP TRIGGER IF EXISTS trigger_increment_reviews_count ON public.product_reviews;
DROP TRIGGER IF EXISTS trigger_decrement_reviews_count ON public.product_reviews;

-- Crear triggers
CREATE TRIGGER trigger_increment_reviews_count
    AFTER INSERT ON public.product_reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_reviews_count();

CREATE TRIGGER trigger_decrement_reviews_count
    AFTER DELETE ON public.product_reviews
    FOR EACH ROW
    EXECUTE FUNCTION decrement_reviews_count();

-- =====================================================
-- RECALCULAR CONTADORES EXISTENTES (opcional)
-- =====================================================
-- Si ya tienes reviews en product_reviews, ejecuta esto:

UPDATE public.posts p
SET reviews_count = (
    SELECT COUNT(*) FROM public.product_reviews pr 
    WHERE pr.source_id = p.id AND pr.source_type = 'post'
);

UPDATE public.rends r
SET reviews_count = (
    SELECT COUNT(*) FROM public.product_reviews pr 
    WHERE pr.source_id = r.id AND pr.source_type = 'rend'
);
