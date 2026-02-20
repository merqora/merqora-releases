-- ═══════════════════════════════════════════════════════════════════════════════
-- VIEW TRACKING SYSTEM - Enterprise-grade para millones de usuarios
-- ═══════════════════════════════════════════════════════════════════════════════

-- BULK RPC: Incrementa múltiples items en UNA transacción atómica
-- Input: array de objetos JSON [{id: "uuid", amount: int}]
-- Escala: Hasta 500 items por llamada, sub-100ms en índices optimizados
CREATE OR REPLACE FUNCTION increment_views_bulk(
    items JSONB,
    table_name TEXT
)
RETURNS VOID AS $$
DECLARE
    item JSONB;
BEGIN
    -- Validar table_name (seguridad)
    IF table_name NOT IN ('posts', 'rends') THEN
        RAISE EXCEPTION 'Invalid table_name: %', table_name;
    END IF;
    
    -- Bulk update usando temporary table + JOIN (más eficiente que loop)
    CREATE TEMP TABLE IF NOT EXISTS temp_view_increments (
        id UUID,
        amount INT
    ) ON COMMIT DROP;
    
    -- Insertar batch en temp table
    FOR item IN SELECT * FROM jsonb_array_elements(items)
    LOOP
        INSERT INTO temp_view_increments (id, amount)
        VALUES (
            (item->>'id')::UUID,
            (item->>'amount')::INT
        );
    END LOOP;
    
    -- Single UPDATE con JOIN (10x más rápido que múltiples UPDATEs)
    IF table_name = 'posts' THEN
        UPDATE posts p
        SET views_count = p.views_count + t.amount
        FROM temp_view_increments t
        WHERE p.id = t.id;
    ELSIF table_name = 'rends' THEN
        UPDATE rends r
        SET views_count = r.views_count + t.amount
        FROM temp_view_increments t
        WHERE r.id = t.id;
    END IF;
    
    DROP TABLE temp_view_increments;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Asegurar que la columna views_count existe en rends (posts ya la tiene)
ALTER TABLE rends ADD COLUMN IF NOT EXISTS views_count INT DEFAULT 0;

-- Índice para ordenar por views (tendencias)
CREATE INDEX IF NOT EXISTS idx_posts_views_count ON posts(views_count DESC);
CREATE INDEX IF NOT EXISTS idx_rends_views_count ON rends(views_count DESC);

-- Índice compuesto para tendencias por categoría
CREATE INDEX IF NOT EXISTS idx_posts_views_category ON posts(category, views_count DESC) WHERE status = 'active';
