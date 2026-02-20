-- ═══════════════════════════════════════════════════════════════════════════════
-- SISTEMA DE OFERTAS DINÁMICAS - Merqora
-- ═══════════════════════════════════════════════════════════════════════════════
-- Tablas: offer_campaigns, offer_items
-- Permite crear campañas de ofertas con fechas, categorías y productos asociados.

-- ───────────────────────────────────────────────────────────────────
-- 1. CAMPAÑAS DE OFERTAS
-- ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS offer_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,                          -- "Flash Sale", "Solo Hoy", etc.
    slug TEXT NOT NULL UNIQUE,                   -- "flash", "today", "week", "clearance"
    description TEXT,
    icon TEXT DEFAULT 'local_offer',             -- Nombre del ícono Material
    banner_gradient_start TEXT DEFAULT '#FF6B35',
    banner_gradient_end TEXT DEFAULT '#0A3D62',
    max_discount INT DEFAULT 50,                 -- Descuento máximo que muestra el banner
    starts_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    ends_at TIMESTAMPTZ NOT NULL DEFAULT (now() + interval '24 hours'),
    is_active BOOLEAN DEFAULT true,
    priority INT DEFAULT 0,                      -- Orden de aparición (mayor = primero)
    created_at TIMESTAMPTZ DEFAULT now()
);

-- ───────────────────────────────────────────────────────────────────
-- 2. PRODUCTOS EN OFERTA
-- ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS offer_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES offer_campaigns(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    discount_percent INT NOT NULL CHECK (discount_percent BETWEEN 1 AND 99),
    original_price DOUBLE PRECISION,             -- Se calcula automáticamente si es NULL
    is_featured BOOLEAN DEFAULT false,           -- Destacado en carrusel superior
    created_at TIMESTAMPTZ DEFAULT now(),
    
    UNIQUE(campaign_id, post_id)                 -- Un producto solo puede estar 1 vez por campaña
);

-- ───────────────────────────────────────────────────────────────────
-- 3. ÍNDICES
-- ───────────────────────────────────────────────────────────────────
CREATE INDEX idx_offer_campaigns_active ON offer_campaigns(is_active, ends_at);
CREATE INDEX idx_offer_campaigns_slug ON offer_campaigns(slug);
CREATE INDEX idx_offer_items_campaign ON offer_items(campaign_id);
CREATE INDEX idx_offer_items_post ON offer_items(post_id);
CREATE INDEX idx_offer_items_featured ON offer_items(is_featured) WHERE is_featured = true;

-- ───────────────────────────────────────────────────────────────────
-- 4. RLS (Row Level Security)
-- ───────────────────────────────────────────────────────────────────
ALTER TABLE offer_campaigns ENABLE ROW LEVEL SECURITY;
ALTER TABLE offer_items ENABLE ROW LEVEL SECURITY;

-- Todos pueden leer ofertas activas
CREATE POLICY "Ofertas activas visibles para todos"
    ON offer_campaigns FOR SELECT
    USING (is_active = true AND ends_at > now());

CREATE POLICY "Items de oferta visibles para todos"
    ON offer_items FOR SELECT
    USING (true);

-- Solo usuarios verificados pueden crear/editar (normalmente se hace via service_role)
CREATE POLICY "Usuarios verificados pueden gestionar campañas"
    ON offer_campaigns FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM usuarios 
            WHERE user_id = auth.uid() 
            AND is_verified = true
        )
    );

CREATE POLICY "Usuarios verificados pueden editar campañas"
    ON offer_campaigns FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM usuarios 
            WHERE user_id = auth.uid() 
            AND is_verified = true
        )
    );

CREATE POLICY "Usuarios verificados pueden eliminar campañas"
    ON offer_campaigns FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM usuarios 
            WHERE user_id = auth.uid() 
            AND is_verified = true
        )
    );

CREATE POLICY "Usuarios verificados pueden gestionar items de oferta"
    ON offer_items FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM usuarios 
            WHERE user_id = auth.uid() 
            AND is_verified = true
        )
    );

-- ───────────────────────────────────────────────────────────────────
-- 5. FUNCIÓN: Obtener ofertas activas con productos
-- ───────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION get_active_offers()
RETURNS TABLE (
    campaign_id UUID,
    campaign_name TEXT,
    campaign_slug TEXT,
    campaign_description TEXT,
    campaign_icon TEXT,
    banner_gradient_start TEXT,
    banner_gradient_end TEXT,
    max_discount INT,
    starts_at TIMESTAMPTZ,
    ends_at TIMESTAMPTZ,
    priority INT,
    items_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        oc.id,
        oc.name,
        oc.slug,
        oc.description,
        oc.icon,
        oc.banner_gradient_start,
        oc.banner_gradient_end,
        oc.max_discount,
        oc.starts_at,
        oc.ends_at,
        oc.priority,
        COUNT(oi.id) as items_count
    FROM offer_campaigns oc
    LEFT JOIN offer_items oi ON oi.campaign_id = oc.id
    WHERE oc.is_active = true AND oc.ends_at > now()
    GROUP BY oc.id
    ORDER BY oc.priority DESC, oc.created_at DESC;
END;
$$ LANGUAGE plpgsql;

-- ───────────────────────────────────────────────────────────────────
-- 6. DATOS INICIALES - Campañas por defecto
-- ───────────────────────────────────────────────────────────────────
INSERT INTO offer_campaigns (name, slug, description, icon, banner_gradient_start, banner_gradient_end, max_discount, starts_at, ends_at, is_active, priority)
VALUES
    ('Flash Sale', 'flash', 'Ofertas relámpago por tiempo limitado', 'flash_on', '#FF6B35', '#0A3D62', 70, now(), now() + interval '6 hours', true, 4),
    ('Solo Hoy', 'today', 'Descuentos exclusivos solo por hoy', 'today', '#0A3D62', '#2E8B57', 50, now(), now() + interval '24 hours', true, 3),
    ('Esta Semana', 'week', 'Las mejores ofertas de la semana', 'date_range', '#11998E', '#38EF7D', 40, now(), now() + interval '7 days', true, 2),
    ('Liquidación', 'clearance', 'Últimas unidades a precios increíbles', 'local_fire_department', '#FF6B35', '#2E8B57', 60, now(), now() + interval '30 days', true, 1)
ON CONFLICT (slug) DO NOTHING;

-- ───────────────────────────────────────────────────────────────────
-- 7. AUTO-POPULAR: Agregar posts existentes como ofertas
-- ───────────────────────────────────────────────────────────────────
-- Flash: los 10 más recientes con descuento aleatorio alto
INSERT INTO offer_items (campaign_id, post_id, discount_percent, is_featured)
SELECT 
    (SELECT id FROM offer_campaigns WHERE slug = 'flash'),
    p.id,
    (50 + floor(random() * 20))::int,
    (row_number() OVER (ORDER BY p.created_at DESC)) <= 3
FROM posts p
ORDER BY p.created_at DESC
LIMIT 10
ON CONFLICT (campaign_id, post_id) DO NOTHING;

-- Solo Hoy: los 10 más likeados
INSERT INTO offer_items (campaign_id, post_id, discount_percent, is_featured)
SELECT 
    (SELECT id FROM offer_campaigns WHERE slug = 'today'),
    p.id,
    (30 + floor(random() * 20))::int,
    (row_number() OVER (ORDER BY p.likes_count DESC)) <= 3
FROM posts p
ORDER BY p.likes_count DESC
LIMIT 10
ON CONFLICT (campaign_id, post_id) DO NOTHING;

-- Esta Semana: los 10 con más reviews
INSERT INTO offer_items (campaign_id, post_id, discount_percent, is_featured)
SELECT 
    (SELECT id FROM offer_campaigns WHERE slug = 'week'),
    p.id,
    (20 + floor(random() * 20))::int,
    (row_number() OVER (ORDER BY p.reviews_count DESC)) <= 3
FROM posts p
ORDER BY p.reviews_count DESC
LIMIT 10
ON CONFLICT (campaign_id, post_id) DO NOTHING;

-- Liquidación: 10 random
INSERT INTO offer_items (campaign_id, post_id, discount_percent, is_featured)
SELECT 
    (SELECT id FROM offer_campaigns WHERE slug = 'clearance'),
    p.id,
    (40 + floor(random() * 20))::int,
    (row_number() OVER (ORDER BY random())) <= 3
FROM posts p
ORDER BY random()
LIMIT 10
ON CONFLICT (campaign_id, post_id) DO NOTHING;
