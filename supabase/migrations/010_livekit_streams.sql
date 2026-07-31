-- ═════════════════════════════════════════════════════════════
-- MERCORA - LiveKit Live Streams Migration
-- Agrega columna room_name a live_streams para integración con LiveKit
-- ═════════════════════════════════════════════════════════════

-- Agregar columna room_name si no existe
ALTER TABLE public.live_streams 
ADD COLUMN IF NOT EXISTS room_name TEXT;

-- Crear índice para búsqueda por room_name
CREATE INDEX IF NOT EXISTS idx_live_streams_room_name ON public.live_streams(room_name);

-- Comentario para documentación
COMMENT ON COLUMN public.live_streams.room_name IS 'Nombre de la sala de LiveKit para transmisión';
