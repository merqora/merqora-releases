-- ═══════════════════════════════════════════════════════════════════════════════
-- 013: Cerrar políticas RLS abiertas del chat y feedback legacy
--
-- Problemas que corrige:
-- 1. conversations/conversation_participants/messages: políticas USING(true) /
--    WITH CHECK(true) dejaban todo el chat visible y escribible por cualquiera.
-- 2. "Admin puede ver/actualizar todo el feedback" (USING true) en app_feedback y
--    bug_reports: cualquier usuario veía/borraba feedback ajeno.
-- 3. "Admin can view all feedback" (USING true) en ai_feedback.
-- 4. user_presence: "Users can view all presence" (USING true).
--
-- Enfoque: funciones SECURITY DEFINER para la comprobación de participación
-- (evita recursión infinita de RLS, ya que las políticas consultan la misma tabla
-- conversation_participants).
--
-- El app inserta ahora los participantes de forma SECUENCIAL (primero self, luego
-- el otro usuario) para que la política de INSERT por participación funcione.
-- ═══════════════════════════════════════════════════════════════════════════════

BEGIN;

-- ─────────────────────────────────────────────────────────────────────────────
-- HELPERS SECURITY DEFINER (sin recursión RLS)
-- ─────────────────────────────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.is_chat_participant(p_conversation_id UUID)
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM conversation_participants
        WHERE conversation_id = p_conversation_id
          AND user_id = auth.uid()
    );
$$;

REVOKE ALL ON FUNCTION public.is_chat_participant(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_chat_participant(UUID) TO authenticated;

CREATE OR REPLACE FUNCTION public.is_chat_partner(p_user_id UUID)
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1 FROM conversation_participants cp_me
        JOIN conversation_participants cp_other
          ON cp_other.conversation_id = cp_me.conversation_id
         AND cp_other.user_id = p_user_id
        WHERE cp_me.user_id = auth.uid()
    );
$$;

REVOKE ALL ON FUNCTION public.is_chat_partner(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_chat_partner(UUID) TO authenticated;

-- ─────────────────────────────────────────────────────────────────────────────
-- conversations: solo participantes (o admin) pueden ver/actualizar
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can view all conversations" ON conversations;
DROP POLICY IF EXISTS "Users can update all conversations" ON conversations;
DROP POLICY IF EXISTS "Users can view their conversations" ON conversations;
DROP POLICY IF EXISTS "Users can update their conversations" ON conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON conversations;

CREATE POLICY "Users can create conversations" ON conversations
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Participants can view conversations" ON conversations
    FOR SELECT TO authenticated
    USING (public.is_chat_participant(id) OR public.is_admin());

CREATE POLICY "Participants can update conversations" ON conversations
    FOR UPDATE TO authenticated
    USING (public.is_chat_participant(id) OR public.is_admin())
    WITH CHECK (public.is_chat_participant(id) OR public.is_admin());

-- ─────────────────────────────────────────────────────────────────────────────
-- conversation_participants: solo participantes (o admin) ven/agregan/actualizan
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can view all participants" ON conversation_participants;
DROP POLICY IF EXISTS "Users can add participants" ON conversation_participants;
DROP POLICY IF EXISTS "Users can update their participant record" ON conversation_participants;
DROP POLICY IF EXISTS "Users can update any participant record" ON conversation_participants;
DROP POLICY IF EXISTS "Users can view participants of their conversations" ON conversation_participants;

CREATE POLICY "Participants can view participants" ON conversation_participants
    FOR SELECT TO authenticated
    USING (
        user_id = auth.uid()
        OR public.is_chat_participant(conversation_id)
        OR public.is_admin()
    );

CREATE POLICY "Participants can add participants" ON conversation_participants
    FOR INSERT TO authenticated
    WITH CHECK (
        user_id = auth.uid()
        OR public.is_chat_participant(conversation_id)
        OR public.is_admin()
    );

CREATE POLICY "Participants can update participant records" ON conversation_participants
    FOR UPDATE TO authenticated
    USING (
        user_id = auth.uid()
        OR public.is_chat_participant(conversation_id)
        OR public.is_admin()
    )
    WITH CHECK (
        user_id = auth.uid()
        OR public.is_chat_participant(conversation_id)
        OR public.is_admin()
    );

-- ─────────────────────────────────────────────────────────────────────────────
-- messages: participantes ven/envian/actualizan; admin siempre (admin-web)
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can view all messages" ON messages;
DROP POLICY IF EXISTS "Users can send messages" ON messages;
DROP POLICY IF EXISTS "Users can update their own messages" ON messages;
DROP POLICY IF EXISTS "Participants can update messages" ON messages;
DROP POLICY IF EXISTS "Users can view messages in their conversations" ON messages;
DROP POLICY IF EXISTS "Users can send messages to their conversations" ON messages;

CREATE POLICY "Participants can view messages" ON messages
    FOR SELECT TO authenticated
    USING (public.is_chat_participant(conversation_id) OR public.is_admin());

CREATE POLICY "Participants can send messages" ON messages
    FOR INSERT TO authenticated
    WITH CHECK (
        sender_id = auth.uid()
        OR public.is_admin()
    );

CREATE POLICY "Participants can update messages" ON messages
    FOR UPDATE TO authenticated
    USING (public.is_chat_participant(conversation_id) OR public.is_admin())
    WITH CHECK (public.is_chat_participant(conversation_id) OR public.is_admin());

-- ─────────────────────────────────────────────────────────────────────────────
-- user_presence: propia + partners de chat + admin
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Users can view all presence" ON user_presence;

CREATE POLICY "Chat partners can view presence" ON user_presence
    FOR SELECT TO authenticated
    USING (
        user_id = auth.uid()
        OR public.is_chat_partner(user_id)
        OR public.is_admin()
    );

-- ─────────────────────────────────────────────────────────────────────────────
-- app_feedback / bug_reports: eliminar políticas legacy "Admin puede ..." (USING true)
-- (el 012 ya creó políticas admin correctas "Admins can view/update ...")
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Admin puede ver todo el feedback" ON app_feedback;
DROP POLICY IF EXISTS "Admin puede actualizar todo el feedback" ON app_feedback;
DROP POLICY IF EXISTS "Admin puede ver todos los reportes" ON bug_reports;
DROP POLICY IF EXISTS "Admin puede actualizar todos los reportes" ON bug_reports;

-- ─────────────────────────────────────────────────────────────────────────────
-- ai_feedback: eliminar "Admin can view all feedback" (USING true) y añadir
-- política admin de SELECT que faltaba en el 012
-- ─────────────────────────────────────────────────────────────────────────────

DROP POLICY IF EXISTS "Admin can view all feedback" ON ai_feedback;

DO $$
BEGIN
    IF to_regclass('public.ai_feedback') IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1 FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'ai_feedback'
              AND policyname = 'Admins can view ai_feedback'
        ) THEN
            EXECUTE 'CREATE POLICY "Admins can view ai_feedback" ON ai_feedback
                FOR SELECT TO authenticated
                USING (public.is_admin())';
        END IF;
    END IF;
END $$;

-- ─────────────────────────────────────────────────────────────────────────────
-- user_presence en realtime: el RLS se aplica por suscriptor; se deja así.
-- ─────────────────────────────────────────────────────────────────────────────

COMMIT;
