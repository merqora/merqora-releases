-- ═══════════════════════════════════════════════════════════════
-- AGREGAR COLUMNA is_verified A LA TABLA usuarios
-- ═══════════════════════════════════════════════════════════════
-- Ejecutar en Supabase SQL Editor

-- 1. Agregar columna is_verified (si no existe)
ALTER TABLE public.usuarios 
ADD COLUMN IF NOT EXISTS is_verified BOOLEAN DEFAULT false;

-- 2. Crear índice para optimizar queries
CREATE INDEX IF NOT EXISTS idx_usuarios_is_verified 
ON public.usuarios(is_verified) 
WHERE is_verified = true;

-- 3. Verificar tu usuario actual y marcarlo como verificado
-- Primero obtener tu user_id de auth.users
DO $$
DECLARE
    admin_user_id UUID;
BEGIN
    -- Obtener el UUID del usuario admin desde auth.users
    SELECT id INTO admin_user_id 
    FROM auth.users 
    WHERE email = 'soporte.merqora@gmail.com';
    
    -- Si el usuario existe en auth.users
    IF admin_user_id IS NOT NULL THEN
        -- Verificar si existe en la tabla usuarios
        IF EXISTS (SELECT 1 FROM public.usuarios WHERE user_id = admin_user_id) THEN
            -- Ya existe, solo actualizar
            UPDATE public.usuarios
            SET is_verified = true
            WHERE user_id = admin_user_id;
            
            RAISE NOTICE 'Usuario admin actualizado con is_verified = true';
        ELSE
            -- No existe, crear el registro
            INSERT INTO public.usuarios (user_id, username, email, is_verified)
            VALUES (
                admin_user_id,
                'admin',
                'soporte.merqora@gmail.com',
                true
            );
            
            RAISE NOTICE 'Usuario admin creado con is_verified = true';
        END IF;
    ELSE
        RAISE NOTICE 'Usuario con email soporte.merqora@gmail.com no encontrado en auth.users';
    END IF;
END $$;

-- 4. Verificar el resultado
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.is_verified,
    au.email as auth_email,
    u.created_at
FROM public.usuarios u
LEFT JOIN auth.users au ON u.user_id = au.id
WHERE u.email = 'soporte.merqora@gmail.com' 
   OR au.email = 'soporte.merqora@gmail.com';
