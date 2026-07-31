-- ============================================================
-- MIGRATION 012 - SECURITY HARDENING: RLS + PRIVILEGES
-- ============================================================
-- Cierra las brechas de seguridad identificadas en la auditoría:
--   1. verify_user ejecutable por anon/authenticated (auto-verificación)
--   2. KYC (verification_requests) visible/editable por cualquier autenticado
--   3. Feedback/reportes con UPDATE anónimo (admin-web sin auth)
--   4. Handshakes visibles/editables por cualquier autenticado
--   5. RLS deshabilitado en tablas de AI support
--   6. ai_training_* con "Admin full access" para cualquier autenticado
--   7. notifications INSERT con CHECK(true)
--   8. Vistas sin security_invoker (bypasan RLS)
-- Idempotente y transaccional: si algo falla, se revierte todo.

BEGIN;

-- ============================================================
-- 0. HELPER: is_admin() basado en emails de admin en auth.users
-- ============================================================
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM auth.users
        WHERE id = auth.uid()
          AND email IN ('hello.clendova@gmail.com', 'admin@mercora.app')
    );
$$;

-- ============================================================
-- 1. verify_user: solo admins pueden ejecutar
-- ============================================================
CREATE OR REPLACE FUNCTION verify_user(target_user_id UUID, v_type TEXT DEFAULT 'personal')
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'Solo los administradores pueden verificar usuarios';
    END IF;
    UPDATE public.usuarios
    SET
        is_verified = true,
        verified_at = NOW(),
        verification_type = v_type
    WHERE user_id = target_user_id;
END;
$$;

REVOKE EXECUTE ON FUNCTION verify_user(UUID, TEXT) FROM anon;
REVOKE EXECUTE ON FUNCTION verify_user(UUID, TEXT) FROM authenticated;
GRANT EXECUTE ON FUNCTION verify_user(UUID, TEXT) TO authenticated;

-- ============================================================
-- 2. verification_requests (KYC): solo admin ve/edita todo
-- ============================================================
DO $$
BEGIN
    IF to_regclass('public.verification_requests') IS NULL THEN RETURN; END IF;
    EXECUTE 'DROP POLICY IF EXISTS "Authenticated users can view all verification requests" ON verification_requests';
    EXECUTE 'DROP POLICY IF EXISTS "Authenticated users can update verification requests" ON verification_requests';
    EXECUTE 'DROP POLICY IF EXISTS "Users can view own verification requests" ON verification_requests';
    EXECUTE 'CREATE POLICY "Users can view own verification requests" ON verification_requests
        FOR SELECT TO authenticated
        USING (user_id = auth.uid())';
    EXECUTE 'CREATE POLICY "Admins can view all verification requests" ON verification_requests
        FOR SELECT TO authenticated
        USING (public.is_admin())';
    EXECUTE 'CREATE POLICY "Admins can update verification requests" ON verification_requests
        FOR UPDATE TO authenticated
        USING (public.is_admin())
        WITH CHECK (public.is_admin())';
END $$;

-- ============================================================
-- 3. app_feedback: sin acceso anónimo; solo admin edita
-- ============================================================
DO $$
BEGIN
    IF to_regclass('public.app_feedback') IS NULL THEN RETURN; END IF;
    EXECUTE 'DROP POLICY IF EXISTS "Anon puede ver feedback" ON app_feedback';
    EXECUTE 'DROP POLICY IF EXISTS "Anon puede actualizar feedback" ON app_feedback';
    EXECUTE 'DROP POLICY IF EXISTS "Usuarios autenticados pueden ver todo el feedback" ON app_feedback';
    EXECUTE 'DROP POLICY IF EXISTS "Usuarios autenticados pueden actualizar feedback" ON app_feedback';
    EXECUTE 'CREATE POLICY "Admins can view all feedback" ON app_feedback
        FOR SELECT TO authenticated
        USING (public.is_admin())';
    EXECUTE 'CREATE POLICY "Admins can update feedback" ON app_feedback
        FOR UPDATE TO authenticated
        USING (public.is_admin())
        WITH CHECK (public.is_admin())';
END $$;

-- ============================================================
-- 4. bug_reports: sin acceso anónimo; solo admin edita
-- ============================================================
DO $$
BEGIN
    IF to_regclass('public.bug_reports') IS NULL THEN RETURN; END IF;
    EXECUTE 'DROP POLICY IF EXISTS "Anon puede ver reportes" ON bug_reports';
    EXECUTE 'DROP POLICY IF EXISTS "Anon puede actualizar reportes" ON bug_reports';
    EXECUTE 'DROP POLICY IF EXISTS "Usuarios autenticados pueden ver todos los reportes" ON bug_reports';
    EXECUTE 'DROP POLICY IF EXISTS "Usuarios autenticados pueden actualizar reportes" ON bug_reports';
    EXECUTE 'CREATE POLICY "Admins can view all reports" ON bug_reports
        FOR SELECT TO authenticated
        USING (public.is_admin())';
    EXECUTE 'CREATE POLICY "Admins can update reports" ON bug_reports
        FOR UPDATE TO authenticated
        USING (public.is_admin())
        WITH CHECK (public.is_admin())';
END $$;

-- ============================================================
-- 5. handshake_transactions: solo participantes y admin
-- ============================================================
DO $$
BEGIN
    IF to_regclass('public.handshake_transactions') IS NULL THEN RETURN; END IF;
    EXECUTE 'DROP POLICY IF EXISTS "Allow anonymous read for testing" ON handshake_transactions';
    EXECUTE 'DROP POLICY IF EXISTS "Allow anonymous update for testing" ON handshake_transactions';
    EXECUTE 'DROP POLICY IF EXISTS "Anyone authenticated can view handshakes for testing" ON handshake_transactions';
    EXECUTE 'DROP POLICY IF EXISTS "Admins can update all handshakes for testing" ON handshake_transactions';
    EXECUTE 'DROP POLICY IF EXISTS "Users can view own handshakes" ON handshake_transactions';
    EXECUTE 'CREATE POLICY "Users can view own handshakes" ON handshake_transactions
        FOR SELECT TO authenticated
        USING (auth.uid()::text = initiator_id OR auth.uid()::text = receiver_id)';
    EXECUTE 'CREATE POLICY "Admins can view all handshakes" ON handshake_transactions
        FOR SELECT TO authenticated
        USING (public.is_admin())';
    EXECUTE 'CREATE POLICY "Admins can update all handshakes" ON handshake_transactions
        FOR UPDATE TO authenticated
        USING (public.is_admin())
        WITH CHECK (public.is_admin())';
END $$;

-- ============================================================
-- 6. AI training: "Admin full access" -> solo admin real
-- ============================================================
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY['ai_training_data','ai_training_runs','ai_intent_corrections','ai_metrics_snapshots','ai_prompt_versions']
    LOOP
        IF to_regclass('public.' || t) IS NULL THEN CONTINUE; END IF;
        EXECUTE format('DROP POLICY IF EXISTS "Admin full access %s" ON %I', substr(t, 4), t);
        EXECUTE format('DROP POLICY IF EXISTS "Admins manage %s" ON %I', substr(t, 4), t);
        EXECUTE format('CREATE POLICY "Admins manage %s" ON %I
            FOR ALL TO authenticated
            USING (public.is_admin())
            WITH CHECK (public.is_admin())', substr(t, 4), t);
    END LOOP;
END $$;

-- ============================================================
-- 7. AI SUPPORT: re-habilitar RLS + políticas por propietario
-- ============================================================
ALTER TABLE IF EXISTS support_conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS support_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS ai_feedback ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS ai_escalations ENABLE ROW LEVEL SECURITY;
ALTER TABLE IF EXISTS ai_stats_daily ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF to_regclass('public.support_conversations') IS NOT NULL THEN
        EXECUTE 'DROP POLICY IF EXISTS "Users can view own conversations" ON support_conversations';
        EXECUTE 'DROP POLICY IF EXISTS "Users can create conversations" ON support_conversations';
        EXECUTE 'CREATE POLICY "Users can view own conversations" ON support_conversations
            FOR SELECT TO authenticated
            USING (user_id::text = auth.uid()::text OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Users can create conversations" ON support_conversations
            FOR INSERT TO authenticated
            WITH CHECK (user_id::text = auth.uid()::text OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Admins can update conversations" ON support_conversations
            FOR UPDATE TO authenticated
            USING (public.is_admin())
            WITH CHECK (public.is_admin())';
    END IF;
    IF to_regclass('public.support_messages') IS NOT NULL THEN
        EXECUTE 'DROP POLICY IF EXISTS "Users can view own messages" ON support_messages';
        EXECUTE 'DROP POLICY IF EXISTS "Users can create messages" ON support_messages';
        EXECUTE 'CREATE POLICY "Users can view own messages" ON support_messages
            FOR SELECT TO authenticated
            USING (
                conversation_id IN (
                    SELECT id FROM support_conversations
                    WHERE user_id::text = auth.uid()::text
                )
                OR public.is_admin()
            )';
        EXECUTE 'CREATE POLICY "Users can create messages" ON support_messages
            FOR INSERT TO authenticated
            WITH CHECK (
                conversation_id IN (
                    SELECT id FROM support_conversations
                    WHERE user_id::text = auth.uid()::text
                )
                OR public.is_admin()
            )';
        EXECUTE 'CREATE POLICY "Admins can update messages" ON support_messages
            FOR UPDATE TO authenticated
            USING (public.is_admin())
            WITH CHECK (public.is_admin())';
    END IF;
    IF to_regclass('public.ai_feedback') IS NOT NULL THEN
        EXECUTE 'DROP POLICY IF EXISTS "Users can submit feedback" ON ai_feedback';
        EXECUTE 'DROP POLICY IF EXISTS "Users can view own feedback" ON ai_feedback';
        EXECUTE 'DROP POLICY IF EXISTS "Usuarios pueden crear feedback" ON ai_feedback';
        EXECUTE 'DROP POLICY IF EXISTS "Usuarios pueden ver todo el feedback" ON ai_feedback';
        EXECUTE 'DROP POLICY IF EXISTS "Usuarios pueden actualizar feedback" ON ai_feedback';
        EXECUTE 'CREATE POLICY "Users can create feedback" ON ai_feedback
            FOR INSERT TO authenticated
            WITH CHECK (user_id::text = auth.uid()::text OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Users can view own feedback" ON ai_feedback
            FOR SELECT TO authenticated
            USING (user_id::text = auth.uid()::text OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Admins can update ai_feedback" ON ai_feedback
            FOR UPDATE TO authenticated
            USING (public.is_admin())
            WITH CHECK (public.is_admin())';
    END IF;
    IF to_regclass('public.ai_escalations') IS NOT NULL THEN
        EXECUTE 'CREATE POLICY "Admins can view ai_escalations" ON ai_escalations
            FOR SELECT TO authenticated
            USING (public.is_admin())';
        EXECUTE 'CREATE POLICY "Admins can update ai_escalations" ON ai_escalations
            FOR UPDATE TO authenticated
            USING (public.is_admin())
            WITH CHECK (public.is_admin())';
    END IF;
END $$;

-- ai_stats_daily: reemplazar política pública por admin-only
DO $$
BEGIN
    IF to_regclass('public.ai_stats_daily') IS NOT NULL THEN
        EXECUTE 'DROP POLICY IF EXISTS "Service role full access stats_daily" ON ai_stats_daily';
        EXECUTE 'CREATE POLICY "Admins can view ai_stats_daily" ON ai_stats_daily
            FOR SELECT TO authenticated
            USING (public.is_admin())';
    END IF;
END $$;

-- ============================================================
-- 8. notifications: INSERT propio según el esquema existente
-- ============================================================
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'notifications'
          AND column_name = 'sender_id'
    ) THEN
        EXECUTE 'DROP POLICY IF EXISTS "Authenticated users can create notifications" ON notifications';
        EXECUTE 'CREATE POLICY "Users can create notifications" ON notifications
            FOR INSERT TO authenticated
            WITH CHECK (sender_id = auth.uid()::text)';
    ELSIF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'notifications'
          AND column_name = 'user_id'
          AND data_type = 'uuid'
    ) THEN
        EXECUTE 'DROP POLICY IF EXISTS "Users can view own notifications" ON notifications';
        EXECUTE 'DROP POLICY IF EXISTS "Users can create notifications" ON notifications';
        EXECUTE 'DROP POLICY IF EXISTS "Users can update own notifications" ON notifications';
        EXECUTE 'CREATE POLICY "Users can view own notifications" ON notifications
            FOR SELECT TO authenticated
            USING (user_id = auth.uid() OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Users can create notifications" ON notifications
            FOR INSERT TO authenticated
            WITH CHECK (user_id = auth.uid() OR public.is_admin())';
        EXECUTE 'CREATE POLICY "Users can update own notifications" ON notifications
            FOR UPDATE TO authenticated
            USING (user_id = auth.uid())
            WITH CHECK (user_id = auth.uid())';
    END IF;
END $$;

-- ============================================================
-- 9. Vistas: security_invoker para que RLS aplique
-- ============================================================
DO $$
DECLARE
    v TEXT;
BEGIN
    FOREACH v IN ARRAY ARRAY['v_feedback_analysis','v_intent_performance','v_pending_escalations','v_ai_stats_summary','verification_stats']
    LOOP
        IF to_regclass('public.' || v) IS NOT NULL THEN
            EXECUTE format('ALTER VIEW %I SET (security_invoker = true)', v);
        END IF;
    END LOOP;
END $$;

-- ============================================================
-- 10. Realtime: quitar tablas sensibles de la publicación pública
-- ============================================================
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'app_feedback') THEN
        ALTER PUBLICATION supabase_realtime DROP TABLE app_feedback;
    END IF;
    IF EXISTS (SELECT 1 FROM pg_publication_tables WHERE pubname = 'supabase_realtime' AND tablename = 'bug_reports') THEN
        ALTER PUBLICATION supabase_realtime DROP TABLE bug_reports;
    END IF;
END $$;

COMMIT;
