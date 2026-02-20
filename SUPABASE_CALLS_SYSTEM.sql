-- ═══════════════════════════════════════════════════════════════
-- SISTEMA DE LLAMADAS - Rendly
-- Señalización WebRTC via Supabase Realtime
-- ═══════════════════════════════════════════════════════════════

-- Tabla principal de llamadas
CREATE TABLE IF NOT EXISTS calls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    caller_id UUID NOT NULL REFERENCES auth.users(id),
    callee_id UUID NOT NULL REFERENCES auth.users(id),
    
    -- Estado de la llamada
    status TEXT NOT NULL DEFAULT 'ringing'
        CHECK (status IN ('ringing', 'answered', 'ended', 'missed', 'rejected', 'busy')),
    
    -- Tipo de llamada
    call_type TEXT NOT NULL DEFAULT 'voice'
        CHECK (call_type IN ('voice', 'video')),
    
    -- WebRTC signaling
    offer_sdp TEXT,
    answer_sdp TEXT,
    
    -- Timestamps
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    answered_at TIMESTAMPTZ,
    ended_at TIMESTAMPTZ,
    
    -- Duración en segundos (calculada al finalizar)
    duration_seconds INT DEFAULT 0,
    
    -- Motivo de finalización
    end_reason TEXT DEFAULT 'normal'
        CHECK (end_reason IN ('normal', 'missed', 'rejected', 'busy', 'network_error', 'timeout'))
);

-- Tabla de ICE candidates (intercambio en tiempo real)
CREATE TABLE IF NOT EXISTS call_ice_candidates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_id UUID NOT NULL REFERENCES calls(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES auth.users(id),
    candidate TEXT NOT NULL,
    sdp_mid TEXT,
    sdp_m_line_index INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices para rendimiento
CREATE INDEX IF NOT EXISTS idx_calls_caller_id ON calls(caller_id);
CREATE INDEX IF NOT EXISTS idx_calls_callee_id ON calls(callee_id);
CREATE INDEX IF NOT EXISTS idx_calls_status ON calls(status);
CREATE INDEX IF NOT EXISTS idx_calls_created_at ON calls(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_call_ice_call_id ON call_ice_candidates(call_id);
CREATE INDEX IF NOT EXISTS idx_call_ice_sender_id ON call_ice_candidates(sender_id);

-- Habilitar Realtime para señalización en tiempo real
-- REPLICA IDENTITY FULL es OBLIGATORIO para que los filtros de Realtime funcionen
ALTER TABLE calls REPLICA IDENTITY FULL;
ALTER TABLE call_ice_candidates REPLICA IDENTITY FULL;

ALTER PUBLICATION supabase_realtime ADD TABLE calls;
ALTER PUBLICATION supabase_realtime ADD TABLE call_ice_candidates;

-- ═══════════════════════════════════════════════════════════════
-- RLS (Row Level Security)
-- ═══════════════════════════════════════════════════════════════

ALTER TABLE calls ENABLE ROW LEVEL SECURITY;
ALTER TABLE call_ice_candidates ENABLE ROW LEVEL SECURITY;

-- Calls: participantes pueden ver y modificar sus llamadas
CREATE POLICY "Users can view their calls"
    ON calls FOR SELECT
    USING (auth.uid() = caller_id OR auth.uid() = callee_id);

CREATE POLICY "Users can create calls"
    ON calls FOR INSERT
    WITH CHECK (auth.uid() = caller_id);

CREATE POLICY "Participants can update calls"
    ON calls FOR UPDATE
    USING (auth.uid() = caller_id OR auth.uid() = callee_id);

-- ICE candidates: participantes de la llamada pueden leer/escribir
CREATE POLICY "Call participants can view ICE candidates"
    ON call_ice_candidates FOR SELECT
    USING (
        EXISTS (
            SELECT 1 FROM calls 
            WHERE calls.id = call_ice_candidates.call_id 
            AND (calls.caller_id = auth.uid() OR calls.callee_id = auth.uid())
        )
    );

CREATE POLICY "Call participants can add ICE candidates"
    ON call_ice_candidates FOR INSERT
    WITH CHECK (auth.uid() = sender_id);

-- ═══════════════════════════════════════════════════════════════
-- Función para obtener historial de llamadas de un usuario
-- ═══════════════════════════════════════════════════════════════
CREATE OR REPLACE FUNCTION get_call_history(p_user_id UUID, p_limit INT DEFAULT 50)
RETURNS TABLE (
    call_id UUID,
    other_user_id UUID,
    other_username TEXT,
    other_avatar TEXT,
    call_type TEXT,
    status TEXT,
    is_outgoing BOOLEAN,
    duration_seconds INT,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id AS call_id,
        CASE WHEN c.caller_id = p_user_id THEN c.callee_id ELSE c.caller_id END AS other_user_id,
        u.username AS other_username,
        u.avatar_url AS other_avatar,
        c.call_type,
        c.status,
        (c.caller_id = p_user_id) AS is_outgoing,
        c.duration_seconds,
        c.created_at
    FROM calls c
    JOIN usuarios u ON u.user_id = CASE WHEN c.caller_id = p_user_id THEN c.callee_id ELSE c.caller_id END
    WHERE c.caller_id = p_user_id OR c.callee_id = p_user_id
    ORDER BY c.created_at DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Limpiar llamadas huérfanas (ringing por más de 60 segundos)
CREATE OR REPLACE FUNCTION cleanup_stale_calls()
RETURNS void AS $$
BEGIN
    UPDATE calls
    SET status = 'missed',
        ended_at = now(),
        end_reason = 'timeout'
    WHERE status = 'ringing'
    AND created_at < now() - INTERVAL '60 seconds';
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
