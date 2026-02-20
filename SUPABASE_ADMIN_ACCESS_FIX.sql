-- ════════════════════════════════════════════════════════════════════════════
-- FIX: Permitir acceso anónimo para admin-web (sin autenticación)
-- Ejecutar en Supabase SQL Editor
-- ════════════════════════════════════════════════════════════════════════════

-- NOTA: Esto permite que cualquiera con la anon key pueda leer los datos.
-- Para producción, deberías agregar autenticación a admin-web.

-- ════════════════════════════════════════════════════════════════════════════
-- POLÍTICAS PARA app_feedback
-- ════════════════════════════════════════════════════════════════════════════

-- Permitir SELECT anónimo (para admin-web)
CREATE POLICY "Anon puede ver feedback"
    ON app_feedback FOR SELECT
    TO anon
    USING (true);

-- Permitir UPDATE anónimo (para que admin pueda cambiar estado/prioridad)
CREATE POLICY "Anon puede actualizar feedback"
    ON app_feedback FOR UPDATE
    TO anon
    USING (true)
    WITH CHECK (true);

-- ════════════════════════════════════════════════════════════════════════════
-- POLÍTICAS PARA bug_reports
-- ════════════════════════════════════════════════════════════════════════════

-- Permitir SELECT anónimo
CREATE POLICY "Anon puede ver reportes"
    ON bug_reports FOR SELECT
    TO anon
    USING (true);

-- Permitir UPDATE anónimo
CREATE POLICY "Anon puede actualizar reportes"
    ON bug_reports FOR UPDATE
    TO anon
    USING (true)
    WITH CHECK (true);

-- ════════════════════════════════════════════════════════════════════════════
-- HABILITAR REALTIME PARA ANON
-- ════════════════════════════════════════════════════════════════════════════

-- Asegurar que realtime esté habilitado para estas tablas
DO $$
BEGIN
    -- Verificar si las tablas ya están en la publicación
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'app_feedback'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE app_feedback;
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_publication_tables 
        WHERE pubname = 'supabase_realtime' AND tablename = 'bug_reports'
    ) THEN
        ALTER PUBLICATION supabase_realtime ADD TABLE bug_reports;
    END IF;
END $$;

-- ════════════════════════════════════════════════════════════════════════════
-- VERIFICACIÓN
-- ════════════════════════════════════════════════════════════════════════════

-- Ejecutar para verificar las políticas activas:
-- SELECT schemaname, tablename, policyname, roles, cmd 
-- FROM pg_policies 
-- WHERE tablename IN ('app_feedback', 'bug_reports')
-- ORDER BY tablename, policyname;

-- Verificar realtime:
-- SELECT * FROM pg_publication_tables WHERE pubname = 'supabase_realtime';
