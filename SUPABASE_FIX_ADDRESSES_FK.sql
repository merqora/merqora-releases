-- ═══════════════════════════════════════════════════════════════════════════════════
-- FIX: addresses.user_id FK constraint
-- 
-- PROBLEM: addresses.user_id references usuarios(id) which is the auto-generated PK,
-- NOT the auth user UUID. The app passes auth.uid() as user_id, which doesn't exist
-- in usuarios.id (it exists in usuarios.user_id).
--
-- FIX: Drop the FK to usuarios(id) and add FK to auth.users(id) directly.
-- This matches how all other tables work (notifications, privacy, etc.)
-- ═══════════════════════════════════════════════════════════════════════════════════

-- Step 1: Drop the broken FK constraint
ALTER TABLE addresses DROP CONSTRAINT IF EXISTS addresses_user_id_fkey;

-- Step 2: Add correct FK referencing auth.users(id)
ALTER TABLE addresses 
    ADD CONSTRAINT addresses_user_id_fkey 
    FOREIGN KEY (user_id) REFERENCES auth.users(id) ON DELETE CASCADE;

-- Verify: Run this SELECT to confirm it works
-- SELECT * FROM addresses LIMIT 1;
