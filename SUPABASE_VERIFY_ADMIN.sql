-- ═══════════════════════════════════════════════════════════════
-- VERIFICAR Y DAR ACCESO ADMIN
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar en Supabase SQL Editor

-- 1. Ver tu usuario actual (ejecutar esto primero para obtener tu user_id)
SELECT 
    user_id,
    username,
    email,
    is_verified,
    created_at
FROM public.usuarios
WHERE email = 'soporte.merqora@gmail.com';

-- 2. Si tu usuario existe pero no está verificado, ejecutar esto:
UPDATE public.usuarios
SET is_verified = true
WHERE email = 'soporte.merqora@gmail.com';

-- 3. Si tu usuario NO existe en la tabla usuarios, primero necesitas:
--    a) Registrarte en la app Android
--    b) O crear manualmente el registro (reemplaza 'TU_USER_ID' con el UUID de auth.users)

-- Para ver tu UUID de auth.users:
SELECT id, email FROM auth.users WHERE email = 'soporte.merqora@gmail.com';

-- Luego insertar en usuarios (reemplazar 'TU_USER_ID_AQUI'):
INSERT INTO public.usuarios (user_id, username, email, is_verified, avatar_url)
VALUES (
    'TU_USER_ID_AQUI'::uuid,  -- Reemplazar con el UUID de arriba
    'admin',
    'soporte.merqora@gmail.com',
    true,
    null
)
ON CONFLICT (user_id) DO UPDATE 
SET is_verified = true;

-- 4. Verificar que quedó correcto:
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.is_verified,
    au.email as auth_email
FROM public.usuarios u
LEFT JOIN auth.users au ON u.user_id = au.id
WHERE u.email = 'soporte.merqora@gmail.com';
