# Configuración FCM v1 API para Rendly

## Pasos para configurar notificaciones push seguras

### 1. Crear Service Account en Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto **rendly-app**
3. Haz clic en ⚙️ → **Project settings**
4. Ve a la pestaña **Service accounts**
5. Haz clic en **"Generate new private key"**
6. Descarga el archivo JSON (ej: `rendly-app-firebase-adminsdk-xxxxx.json`)

### 2. Extraer las credenciales del JSON

Abre el archivo JSON descargado. Necesitas estos valores:

```json
{
  "project_id": "rendly-app",
  "client_email": "firebase-adminsdk-xxxxx@rendly-app.iam.gserviceaccount.com",
  "private_key": "-----BEGIN PRIVATE KEY-----\nMIIEvQ...(muy largo)...xxxxx=\n-----END PRIVATE KEY-----\n"
}
```

### 3. Configurar Secrets en Supabase

1. Ve a [Supabase Dashboard](https://supabase.com/dashboard)
2. Selecciona tu proyecto
3. Ve a **Settings** → **Edge Functions**
4. En la sección **Secrets**, agrega:

| Nombre | Valor |
|--------|-------|
| `FIREBASE_PROJECT_ID` | `rendly-app` |
| `FIREBASE_CLIENT_EMAIL` | `firebase-adminsdk-xxxxx@rendly-app.iam.gserviceaccount.com` |
| `FIREBASE_PRIVATE_KEY` | El contenido completo de `private_key` (con `-----BEGIN...` y `-----END...`) |

**⚠️ IMPORTANTE para `FIREBASE_PRIVATE_KEY`:**
- Copia el valor COMPLETO incluyendo `-----BEGIN PRIVATE KEY-----` y `-----END PRIVATE KEY-----`
- Mantén los `\n` como están en el JSON

### 4. Desplegar la Edge Function

Desde la terminal, en el directorio del proyecto:

```bash
# Instalar Supabase CLI si no lo tienes
npm install -g supabase

# Login
supabase login

# Vincular proyecto
supabase link --project-ref xyrpmmnegzjkbysoocpc

# Desplegar la función
supabase functions deploy send-fcm-v1
```

### 5. Ejecutar el SQL Fix

Ejecuta el archivo `SUPABASE_FCM_PUSH_FIX.sql` en Supabase SQL Editor para arreglar el problema de tipos UUID/TEXT.

### 6. Probar

1. Abre admin-web
2. Ve a "Test Notificaciones"
3. Haz clic en "Dar like (enviar notificación)"
4. La notificación debería llegar al dispositivo aunque la app esté cerrada

---

## Troubleshooting

### Error "Firebase credentials not configured"
- Verifica que los 3 secrets estén configurados en Supabase Edge Functions

### Error "OAuth error"
- Verifica que `FIREBASE_PRIVATE_KEY` esté completo y sin modificaciones
- Asegúrate de que la Service Account tenga permisos de Cloud Messaging

### Error "No hay tokens FCM"
- Verifica que el usuario destino tenga la app instalada y haya dado permisos de notificaciones
- Revisa la tabla `fcm_tokens` en Supabase para ver si hay tokens activos

### La notificación no llega con app cerrada
- Verifica que el canal de notificaciones `rendly_notifications` esté configurado en la app Android
- Revisa que Firebase Cloud Messaging esté habilitado en el proyecto
