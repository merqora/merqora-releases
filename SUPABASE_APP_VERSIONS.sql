-- ═══════════════════════════════════════════════════════════════
-- MERQORA - Sistema de Versiones de App (APK Distribution)
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar en Supabase SQL Editor
-- Después de ejecutar, crear el bucket 'app-releases' en Storage

-- 1. Tabla de versiones
CREATE TABLE IF NOT EXISTS app_versions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    version_name TEXT NOT NULL,          -- e.g. "1.0.0"
    version_code INTEGER NOT NULL,       -- e.g. 1
    changelog TEXT,                       -- Notas de la versión
    file_url TEXT,                        -- URL pública del APK
    file_path TEXT,                       -- Path en Storage bucket
    file_size_mb NUMERIC(6,1),           -- Tamaño en MB
    min_android TEXT DEFAULT '8.0',       -- Versión mínima de Android
    is_latest BOOLEAN DEFAULT false,      -- Si es la versión actual
    download_count INTEGER DEFAULT 0,     -- Contador de descargas
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Índices
CREATE INDEX IF NOT EXISTS idx_app_versions_latest ON app_versions (is_latest) WHERE is_latest = true;
CREATE INDEX IF NOT EXISTS idx_app_versions_created ON app_versions (created_at DESC);

-- 3. Trigger para updated_at
CREATE OR REPLACE FUNCTION update_app_versions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_app_versions_updated_at ON app_versions;
CREATE TRIGGER trigger_app_versions_updated_at
    BEFORE UPDATE ON app_versions
    FOR EACH ROW
    EXECUTE FUNCTION update_app_versions_updated_at();

-- 4. RLS
ALTER TABLE app_versions ENABLE ROW LEVEL SECURITY;

-- Todos pueden leer (para la página de descarga pública)
CREATE POLICY "App versions visibles para todos"
    ON app_versions FOR SELECT
    USING (true);

-- Solo usuarios verificados pueden insertar/actualizar/eliminar
CREATE POLICY "Usuarios verificados pueden gestionar versiones"
    ON app_versions FOR INSERT
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );

CREATE POLICY "Usuarios verificados pueden actualizar versiones"
    ON app_versions FOR UPDATE
    USING (
        EXISTS (
            SELECT 1 FROM usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );

CREATE POLICY "Usuarios verificados pueden eliminar versiones"
    ON app_versions FOR DELETE
    USING (
        EXISTS (
            SELECT 1 FROM usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );

-- 5. Función para incrementar descargas (callable via RPC)
CREATE OR REPLACE FUNCTION increment_download_count(version_id UUID)
RETURNS void AS $$
BEGIN
    UPDATE app_versions
    SET download_count = download_count + 1
    WHERE id = version_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ═══════════════════════════════════════════════════════════════
-- IMPORTANTE: Después de ejecutar este SQL, hacer manualmente
-- en Supabase Dashboard → Storage:
--
-- 1. Crear bucket: "app-releases"
-- 2. Marcar como PUBLIC
-- 3. En Policies, agregar:
--    - SELECT: Permitir a todos (para descargas públicas)
--    - INSERT/UPDATE/DELETE: Solo authenticated users
--
-- O ejecutar este SQL para las policies del bucket:
-- ═══════════════════════════════════════════════════════════════

-- Storage policies (ejecutar si el bucket ya existe)
-- Nota: Primero crear el bucket 'app-releases' como público desde el dashboard

INSERT INTO storage.buckets (id, name, public)
VALUES ('app-releases', 'app-releases', true)
ON CONFLICT (id) DO UPDATE SET public = true;

-- Permitir lectura pública
CREATE POLICY "Descargas públicas de APK"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'app-releases');

-- Permitir upload solo a usuarios verificados
CREATE POLICY "Upload APK solo verificados"
    ON storage.objects FOR INSERT
    WITH CHECK (
        bucket_id = 'app-releases'
        AND EXISTS (
            SELECT 1 FROM public.usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );

-- Permitir update solo a usuarios verificados
CREATE POLICY "Update APK solo verificados"
    ON storage.objects FOR UPDATE
    USING (
        bucket_id = 'app-releases'
        AND EXISTS (
            SELECT 1 FROM public.usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );

-- Permitir delete solo a usuarios verificados
CREATE POLICY "Delete APK solo verificados"
    ON storage.objects FOR DELETE
    USING (
        bucket_id = 'app-releases'
        AND EXISTS (
            SELECT 1 FROM public.usuarios
            WHERE user_id = auth.uid()
            AND is_verified = true
        )
    );
