-- ═══════════════════════════════════════════════════════════════════════════════
-- ZONES SYSTEM - Búsqueda geográfica de productos y vendedores
-- ═══════════════════════════════════════════════════════════════════════════════

-- 1) Agregar coordenadas del vendedor a posts (se llenan desde addresses al publicar)
ALTER TABLE posts ADD COLUMN IF NOT EXISTS seller_latitude DOUBLE PRECISION;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS seller_longitude DOUBLE PRECISION;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS seller_city TEXT;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS seller_country TEXT DEFAULT 'Argentina';

-- 2) Índice espacial para búsquedas por proximidad
CREATE INDEX IF NOT EXISTS idx_posts_seller_location 
ON posts(seller_latitude, seller_longitude) 
WHERE seller_latitude IS NOT NULL AND status = 'active';

CREATE INDEX IF NOT EXISTS idx_posts_seller_city 
ON posts(seller_city) WHERE seller_city IS NOT NULL AND status = 'active';

-- 3) Auto-fill location cuando se crea un post (trigger)
CREATE OR REPLACE FUNCTION fill_post_seller_location()
RETURNS TRIGGER AS $$
DECLARE
    addr RECORD;
BEGIN
    -- Buscar dirección default del vendedor
    SELECT latitude, longitude, city, country 
    INTO addr
    FROM addresses 
    WHERE user_id = NEW.user_id AND is_default = true
    LIMIT 1;
    
    -- Si no tiene default, usar cualquier dirección
    IF addr IS NULL THEN
        SELECT latitude, longitude, city, country 
        INTO addr
        FROM addresses 
        WHERE user_id = NEW.user_id
        ORDER BY created_at DESC
        LIMIT 1;
    END IF;
    
    IF addr IS NOT NULL THEN
        NEW.seller_latitude := addr.latitude;
        NEW.seller_longitude := addr.longitude;
        NEW.seller_city := addr.city;
        NEW.seller_country := addr.country;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_fill_post_seller_location ON posts;
CREATE TRIGGER trg_fill_post_seller_location
    BEFORE INSERT ON posts
    FOR EACH ROW
    EXECUTE FUNCTION fill_post_seller_location();

-- 4) Backfill posts existentes con ubicación del vendedor
UPDATE posts p
SET 
    seller_latitude = a.latitude,
    seller_longitude = a.longitude,
    seller_city = a.city,
    seller_country = a.country
FROM (
    SELECT DISTINCT ON (user_id) user_id, latitude, longitude, city, country
    FROM addresses
    ORDER BY user_id, is_default DESC, created_at DESC
) a
WHERE p.user_id = a.user_id AND p.seller_latitude IS NULL;

-- 5) RPC: Contar posts y vendedores cercanos (usa Haversine simplificado)
CREATE OR REPLACE FUNCTION get_zone_stats(
    user_lat DOUBLE PRECISION,
    user_lon DOUBLE PRECISION,
    radius_km DOUBLE PRECISION DEFAULT 50.0
)
RETURNS JSONB AS $$
DECLARE
    result JSONB;
    post_count INT;
    seller_count INT;
BEGIN
    -- Haversine approximation: 1 grado ≈ 111km
    -- Pre-filtro con bounding box para performance
    WITH nearby AS (
        SELECT DISTINCT user_id, id
        FROM posts
        WHERE status = 'active'
          AND seller_latitude IS NOT NULL
          AND seller_latitude BETWEEN user_lat - (radius_km / 111.0) AND user_lat + (radius_km / 111.0)
          AND seller_longitude BETWEEN user_lon - (radius_km / (111.0 * COS(RADIANS(user_lat)))) 
                                   AND user_lon + (radius_km / (111.0 * COS(RADIANS(user_lat))))
          AND (
            6371 * ACOS(
              LEAST(1.0, GREATEST(-1.0,
                COS(RADIANS(user_lat)) * COS(RADIANS(seller_latitude)) * 
                COS(RADIANS(seller_longitude) - RADIANS(user_lon)) + 
                SIN(RADIANS(user_lat)) * SIN(RADIANS(seller_latitude))
              ))
            )
          ) <= radius_km
    )
    SELECT 
        COUNT(DISTINCT id),
        COUNT(DISTINCT user_id)
    INTO post_count, seller_count
    FROM nearby;
    
    result := jsonb_build_object(
        'post_count', post_count,
        'seller_count', seller_count
    );
    
    RETURN result;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- 6) Tabla de búsquedas populares por zona (analytics)
CREATE TABLE IF NOT EXISTS zone_popular_searches (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    query TEXT NOT NULL,
    city TEXT,
    country TEXT DEFAULT 'Argentina',
    search_count INT DEFAULT 1,
    icon_name TEXT, -- nombre del icono Material para mostrar en UI
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Índice para queries frecuentes por ciudad
CREATE INDEX IF NOT EXISTS idx_zone_popular_city 
ON zone_popular_searches(city, search_count DESC);

-- Seed de búsquedas populares iniciales
INSERT INTO zone_popular_searches (query, city, country, search_count, icon_name) VALUES
('iPhone', NULL, 'Argentina', 500, 'PhoneIphone'),
('Zapatillas Nike', NULL, 'Argentina', 420, 'Hiking'),
('PlayStation', NULL, 'Argentina', 380, 'SportsEsports'),
('Bicicleta', NULL, 'Argentina', 350, 'DirectionsBike'),
('Laptop', NULL, 'Argentina', 310, 'Laptop'),
('Auriculares', NULL, 'Argentina', 290, 'Headphones'),
('Monitor', NULL, 'Argentina', 250, 'Monitor'),
('Silla gamer', NULL, 'Argentina', 230, 'Chair')
ON CONFLICT DO NOTHING;

-- RLS
ALTER TABLE zone_popular_searches ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Public read zone_popular_searches" ON zone_popular_searches
    FOR SELECT USING (true);
