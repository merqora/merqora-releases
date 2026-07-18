# ConfiguraciÃ³n FCM v1 API para Mercora

## Pasos para configurar notificaciones push seguras

### 1. Crear Service Account en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto **mercora-app**
3. Haz clic en âš™ï¸ â†’ **Project settings**
4. Ve a la pestaÃ±a **Service accounts**
5. Haz clic en **"Generate new private key"**
6. Descarga el archivo JSON (ej: `mercora-app-firebase-adminsdk-xxxxx.json`)

### 2. Extraer las credenciales del JSON

Abre el archivo JSON descargado. Necesitas estos valores:

```json
{
  "project_id": "mercora-app",
  "client_email": "firebase-adminsdk-xxxxx@mercora-app.iam.gserviceaccount.com",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQ...(muy largo)...xxxxx=\n-----END PRIVATE KEY-----\n"
}
```

### 3. Configurar Secrets en Supabase

1. Ve a [Supabase Dashboard](https://supabase.com/dashboard)
2. Selecciona tu proyecto
3. Ve a **Settings** â†’ **Edge Functions**
4. En la secciÃ³n **Secrets**, agrega:

| Nombre | Valor |
|--------|-------|
| `FIREBASE_PROJECT_ID` | `mercora-app` |
| `FIREBASE_CLIENT_EMAIL` | `firebase-adminsdk-xxxxx@mercora-app.iam.gserviceaccount.com` |
| `FIREBASE_PRIVATE_KEY` | El contenido completo de `private_key` (con `-----BEGIN...` y `-----END...`) |

**âš ï¸ IMPORTANTE para `FIREBASE_PRIVATE_KEY`:**
- Copia el valor COMPLETO incluyendo `-----BEGIN PRIVATE KEY-----` y `-----END PRIVATE KEY-----`
- MantÃ©n los `\n` como estÃ¡n en el JSON

### 4. Desplegar la Edge Function

Desde la terminal, en el directorio del proyecto:

```bash
# Instalar Supabase CLI si no lo tienes
npm install -g supabase

# Login
supabase login

# Vincular proyecto
supabase link --project-ref xyrpmmnegzjkbysoocpc

# Desplegar la funciÃ³n
supabase functions deploy send-fcm-v1
```

### 5. Ejecutar el SQL Fix

Ejecuta el archivo `SUPABASE_FCM_PUSH_FIX.sql` en Supabase SQL Editor para arreglar el problema de tipos UUID/TEXT.

### 6. Probar

1. Abre admin-web
2. Ve a "Test Notificaciones"
3. Haz clic en "Dar like (enviar notificaciÃ³n)"
4. La notificaciÃ³n deberÃ­a llegar al dispositivo aunque la app estÃ© cerrada

---

## Troubleshooting

### Error "Firebase credentials not configured"
- Verifica que los 3 secrets estÃ©n configurados en Supabase Edge Functions

### Error "OAuth error"
- Verifica que `FIREBASE_PRIVATE_KEY` estÃ© completo y sin modificaciones
- AsegÃºrate de que la Service Account tenga permisos de Cloud Messaging

### Error "No hay tokens FCM"
- Verifica que el usuario destino tenga la app instalada y haya dado permisos de notificaciones
- Revisa la tabla `fcm_tokens` en Supabase para ver si hay tokens activos

### La notificaciÃ³n no llega con app cerrada
- Verifica que el canal de notificaciones `mercora_notifications` estÃ© configurado en la app Android
- Revisa que Firebase Cloud Messaging estÃ© habilitado en el proyecto
