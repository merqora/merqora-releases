-- ════════════════════════════════════════════════════════════════════════════
-- FIX: Políticas RLS para que admin-web pueda ver feedback y reportes
-- Ejecutar en Supabase SQL Editor
-- ════════════════════════════════════════════════════════════════════════════

-- Eliminar políticas restrictivas si existen (pueden causar conflicto)
DROP POLICY IF EXISTS "Los usuarios pueden ver su propio feedback" ON app_feedback;
DROP POLICY IF EXISTS "Los usuarios pueden ver sus propios reportes" ON bug_reports;

-- Crear nuevas políticas que permitan acceso completo para usuarios autenticados
-- Esto permite que admin-web vea todos los registros

CREATE POLICY "Usuarios autenticados pueden ver todo el feedback"
    ON app_feedback FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Usuarios autenticados pueden actualizar feedback"
    ON app_feedback FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Usuarios autenticados pueden ver todos los reportes"
    ON bug_reports FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Usuarios autenticados pueden actualizar reportes"
    ON bug_reports FOR UPDATE
    TO authenticated
    USING (true)
    WITH CHECK (true);

-- Verificar que las políticas INSERT sigan funcionando
-- (ya deberían existir, pero por si acaso)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'app_feedback' AND policyname LIKE '%crear%'
    ) THEN
        CREATE POLICY "Los usuarios pueden crear su propio feedback"
            ON app_feedback FOR INSERT
            TO authenticated
            WITH CHECK (auth.uid() = user_id);
    END IF;
    
    IF NOT EXISTS (
        SELECT 1 FROM pg_policies 
        WHERE tablename = 'bug_reports' AND policyname LIKE '%crear%'
    ) THEN
        CREATE POLICY "Los usuarios pueden crear sus propios reportes"
            ON bug_reports FOR INSERT
            TO authenticated
            WITH CHECK (auth.uid() = user_id);
    END IF;
END $$;

-- Verificar que realtime esté habilitado
ALTER PUBLICATION supabase_realtime ADD TABLE IF NOT EXISTS app_feedback;
ALTER PUBLICATION supabase_realtime ADD TABLE IF NOT EXISTS bug_reports;

-- ════════════════════════════════════════════════════════════════════════════
-- VERIFICAR: Ejecutar esta query para ver las políticas activas
-- ════════════════════════════════════════════════════════════════════════════
-- SELECT * FROM pg_policies WHERE tablename IN ('app_feedback', 'bug_reports');
