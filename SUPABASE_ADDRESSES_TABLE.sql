-- ═══════════════════════════════════════════════════════════════════════════════════
-- RENDLY - ADDRESSES TABLE
-- Sistema profesional de gestión de direcciones con validación y scoring
-- ═══════════════════════════════════════════════════════════════════════════════════

-- Habilitar extensión PostGIS para tipos geográficos
CREATE EXTENSION IF NOT EXISTS postgis;

-- Eliminar tabla si existe (desarrollo)
DROP TABLE IF EXISTS addresses CASCADE;

-- Crear tipo ENUM para el origen de la dirección
DROP TYPE IF EXISTS address_source CASCADE;
CREATE TYPE address_source AS ENUM ('gps', 'manual', 'autocomplete');

-- Crear tipo ENUM para el estado de verificación
DROP TYPE IF EXISTS address_status CASCADE;
CREATE TYPE address_status AS ENUM ('valid', 'suspicious', 'invalid', 'pending');

-- Crear tipo ENUM para el tipo de dirección
DROP TYPE IF EXISTS address_type CASCADE;
CREATE TYPE address_type AS ENUM ('home', 'work', 'other');

-- ═══════════════════════════════════════════════════════════════════════════════════
-- TABLA PRINCIPAL DE DIRECCIONES
-- ═══════════════════════════════════════════════════════════════════════════════════
CREATE TABLE addresses (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    
    -- Información de la dirección
    label VARCHAR(100) NOT NULL DEFAULT 'Mi dirección',
    address_type address_type NOT NULL DEFAULT 'home',
    
    -- Dirección formateada
    formatted_address TEXT NOT NULL,
    street_address VARCHAR(255),
    street_number VARCHAR(20),
    apartment VARCHAR(50),
    floor VARCHAR(20),
    neighborhood VARCHAR(100),
    city VARCHAR(100) NOT NULL,
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100) NOT NULL DEFAULT 'Uruguay',
    country_code VARCHAR(3) DEFAULT 'AR',
    
    -- Coordenadas geográficas
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    
    -- Validación y scoring (procesado por motor C++)
    confidence_score INTEGER NOT NULL DEFAULT 0 CHECK (confidence_score >= 0 AND confidence_score <= 100),
    status address_status NOT NULL DEFAULT 'pending',
    is_verified BOOLEAN NOT NULL DEFAULT false,
    
    -- Métricas de validación detalladas
    text_coord_consistency FLOAT DEFAULT 0,  -- Consistencia texto ↔ coordenadas (0-1)
    gps_geocode_distance FLOAT DEFAULT 0,     -- Distancia GPS ↔ punto geocodificado (metros)
    address_completeness FLOAT DEFAULT 0,     -- Completitud de la dirección (0-1)
    
    -- Metadatos
    source address_source NOT NULL DEFAULT 'manual',
    raw_input TEXT,                           -- Input original del usuario
    geocode_provider VARCHAR(50),             -- Google, Mapbox, etc.
    geocode_place_id VARCHAR(255),            -- ID del lugar del proveedor
    
    -- Flags
    is_default BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Instrucciones adicionales
    delivery_instructions TEXT,
    reference_point VARCHAR(255),             -- Punto de referencia cercano
    
    -- Timestamps
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    verified_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    
    -- Índice geoespacial
    location GEOGRAPHY(POINT, 4326) GENERATED ALWAYS AS (
        ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
    ) STORED
);

-- ═══════════════════════════════════════════════════════════════════════════════════
-- ÍNDICES
-- ═══════════════════════════════════════════════════════════════════════════════════
CREATE INDEX idx_addresses_user_id ON addresses(user_id);
CREATE INDEX idx_addresses_user_active ON addresses(user_id, is_active);
CREATE INDEX idx_addresses_user_default ON addresses(user_id, is_default) WHERE is_default = true;
CREATE INDEX idx_addresses_status ON addresses(status);
CREATE INDEX idx_addresses_location ON addresses USING GIST(location);
CREATE INDEX idx_addresses_created ON addresses(created_at DESC);

-- ═══════════════════════════════════════════════════════════════════════════════════
-- FUNCIONES Y TRIGGERS
-- ═══════════════════════════════════════════════════════════════════════════════════

-- Trigger para actualizar updated_at
CREATE OR REPLACE FUNCTION update_addresses_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_addresses_updated
    BEFORE UPDATE ON addresses
    FOR EACH ROW
    EXECUTE FUNCTION update_addresses_timestamp();

-- Función para asegurar solo una dirección default por usuario
CREATE OR REPLACE FUNCTION ensure_single_default_address()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_default = true THEN
        UPDATE addresses 
        SET is_default = false 
        WHERE user_id = NEW.user_id 
        AND id != NEW.id 
        AND is_default = true;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_single_default_address
    BEFORE INSERT OR UPDATE OF is_default ON addresses
    FOR EACH ROW
    WHEN (NEW.is_default = true)
    EXECUTE FUNCTION ensure_single_default_address();

-- Función para calcular distancia entre dos puntos (para validación)
CREATE OR REPLACE FUNCTION calculate_address_distance(
    lat1 DOUBLE PRECISION,
    lon1 DOUBLE PRECISION,
    lat2 DOUBLE PRECISION,
    lon2 DOUBLE PRECISION
)
RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN ST_Distance(
        ST_SetSRID(ST_MakePoint(lon1, lat1), 4326)::geography,
        ST_SetSRID(ST_MakePoint(lon2, lat2), 4326)::geography
    );
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- ═══════════════════════════════════════════════════════════════════════════════════
-- ROW LEVEL SECURITY
-- ═══════════════════════════════════════════════════════════════════════════════════
ALTER TABLE addresses ENABLE ROW LEVEL SECURITY;

-- Política: usuarios solo pueden ver sus propias direcciones
CREATE POLICY "Users can view own addresses"
    ON addresses FOR SELECT
    USING (auth.uid() = user_id);

-- Política: usuarios solo pueden insertar sus propias direcciones
CREATE POLICY "Users can insert own addresses"
    ON addresses FOR INSERT
    WITH CHECK (auth.uid() = user_id);

-- Política: usuarios solo pueden actualizar sus propias direcciones
CREATE POLICY "Users can update own addresses"
    ON addresses FOR UPDATE
    USING (auth.uid() = user_id)
    WITH CHECK (auth.uid() = user_id);

-- Política: usuarios solo pueden eliminar sus propias direcciones
CREATE POLICY "Users can delete own addresses"
    ON addresses FOR DELETE
    USING (auth.uid() = user_id);

-- ═══════════════════════════════════════════════════════════════════════════════════
-- FUNCIONES RPC PARA OPERACIONES COMUNES
-- ═══════════════════════════════════════════════════════════════════════════════════

-- Obtener direcciones de un usuario con la default primero
CREATE OR REPLACE FUNCTION get_user_addresses(p_user_id UUID)
RETURNS SETOF addresses AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM addresses
    WHERE user_id = p_user_id AND is_active = true
    ORDER BY is_default DESC, last_used_at DESC NULLS LAST, created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Establecer dirección como default
CREATE OR REPLACE FUNCTION set_default_address(p_address_id UUID, p_user_id UUID)
RETURNS BOOLEAN AS $$
BEGIN
    -- Verificar que la dirección pertenece al usuario
    IF NOT EXISTS (SELECT 1 FROM addresses WHERE id = p_address_id AND user_id = p_user_id) THEN
        RETURN false;
    END IF;
    
    -- Quitar default de otras direcciones
    UPDATE addresses SET is_default = false WHERE user_id = p_user_id AND is_default = true;
    
    -- Establecer nueva default
    UPDATE addresses SET is_default = true, last_used_at = NOW() WHERE id = p_address_id;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Soft delete de dirección
CREATE OR REPLACE FUNCTION deactivate_address(p_address_id UUID, p_user_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_was_default BOOLEAN;
BEGIN
    -- Obtener si era default
    SELECT is_default INTO v_was_default 
    FROM addresses 
    WHERE id = p_address_id AND user_id = p_user_id;
    
    IF v_was_default IS NULL THEN
        RETURN false;
    END IF;
    
    -- Desactivar la dirección
    UPDATE addresses 
    SET is_active = false, is_default = false 
    WHERE id = p_address_id;
    
    -- Si era default, establecer otra como default
    IF v_was_default THEN
        UPDATE addresses 
        SET is_default = true 
        WHERE id = (
            SELECT id 
            FROM addresses 
            WHERE user_id = p_user_id 
            AND is_active = true 
            AND id != p_address_id
            ORDER BY last_used_at DESC NULLS LAST
            LIMIT 1
        );
    END IF;
    
    RETURN true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════════════════════════
-- COMENTARIOS
-- ═══════════════════════════════════════════════════════════════════════════════════
COMMENT ON TABLE addresses IS 'Direcciones de usuarios con validación avanzada y scoring de confianza';
COMMENT ON COLUMN addresses.confidence_score IS 'Score de confianza 0-100 calculado por motor C++';
COMMENT ON COLUMN addresses.text_coord_consistency IS 'Consistencia entre texto y coordenadas (0-1)';
COMMENT ON COLUMN addresses.gps_geocode_distance IS 'Distancia en metros entre GPS y geocodificación';
COMMENT ON COLUMN addresses.address_completeness IS 'Nivel de completitud de la dirección (0-1)';
COMMENT ON COLUMN addresses.source IS 'Origen de la dirección: gps, manual, autocomplete';
