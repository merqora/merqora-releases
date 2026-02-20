# 🔵 Integración Mercado Pago - Guía de Configuración

## Resumen

Esta guía explica cómo configurar la integración de Mercado Pago para pagos en Uruguay.

## Arquitectura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Android App   │────▶│ Supabase Edge   │────▶│  Mercado Pago   │
│  (CheckoutScreen│     │   Functions     │     │      API        │
│   + Custom Tab) │     │                 │     │                 │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                       │                       │
        │                       │                       │
        ▼                       ▼                       ▼
   Deep Link            Webhook (IPN)          Checkout Pro
   (Retorno)           (Notificaciones)         (Pago)
```

## Paso 1: Crear Cuenta de Desarrollador en Mercado Pago

1. Ve a: https://www.mercadopago.com.uy/developers/panel
2. Inicia sesión con tu cuenta de Mercado Pago Uruguay
3. Click en **"Crear aplicación"**
4. Nombre: `Rendly Uruguay`
5. Selecciona **"Checkout Pro"** como producto
6. Acepta términos y crea

### Obtener Credenciales

Una vez creada la aplicación, ve a **Credenciales** y copia:

- **Public Key**: `APP_USR-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`
- **Access Token**: `APP_USR-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-xxxxxx-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-xxxxxxxxx`

⚠️ **IMPORTANTE**: El Access Token es SECRETO. Nunca lo incluyas en el código del app.

### Credenciales de Prueba (Sandbox)

Para testing, Mercado Pago te da credenciales de prueba:
- Ve a la sección **"Credenciales de prueba"**
- Usa estas credenciales para el modo sandbox

## Paso 2: Ejecutar SQL en Supabase

Ejecuta el contenido de `SUPABASE_ORDERS_MERCADOPAGO.sql` en el SQL Editor de Supabase:

1. Ve a: https://supabase.com/dashboard/project/xyrpmmnegzjkbysoocpc/sql
2. Pega el contenido del archivo
3. Click "Run"

Esto crea las tablas:
- `orders` - Órdenes de compra
- `order_items` - Items de cada orden
- `payments` - Información de pagos
- `order_status_history` - Historial de estados
- `seller_stats` - Estadísticas de vendedores

## Paso 3: Configurar Edge Functions en Supabase

### 3.1 Configurar Secrets

En el dashboard de Supabase, ve a **Project Settings > Edge Functions** y agrega:

```bash
# Credenciales de Mercado Pago
MERCADOPAGO_ACCESS_TOKEN=APP_USR-tu-access-token-aqui

# Modo sandbox (true para pruebas, false para producción)
MERCADOPAGO_SANDBOX=true

# Scheme de la app para deep links
APP_DEEP_LINK_SCHEME=rendly
```

### 3.2 Desplegar Edge Functions

Desde la terminal en el directorio del proyecto:

```powershell
# Instalar Supabase CLI si no lo tienes
npm install -g supabase

# Login
supabase login

# Link al proyecto
supabase link --project-ref xyrpmmnegzjkbysoocpc

# Desplegar funciones
supabase functions deploy create-mp-preference --no-verify-jwt
supabase functions deploy mp-webhook --no-verify-jwt
```

### 3.3 Configurar Webhook en Mercado Pago

1. Ve a: https://www.mercadopago.com.uy/developers/panel/app/[TU_APP_ID]/webhooks
2. Click "Agregar webhook"
3. URL: `https://xyrpmmnegzjkbysoocpc.supabase.co/functions/v1/mp-webhook`
4. Eventos: Selecciona **"Pagos"**
5. Guarda

## Paso 4: Sincronizar Gradle

Después de los cambios en `build.gradle.kts`:

```powershell
cd c:\Users\Rodrigo\Documents\Rendly
.\gradlew --refresh-dependencies
```

Las nuevas dependencias agregadas:
- `io.github.jan-tennert.supabase:functions-kt` - Para llamar Edge Functions
- `androidx.browser:browser:1.7.0` - Para Custom Tabs (checkout)

## Paso 5: Probar la Integración

### Usuarios de Prueba

Mercado Pago te da usuarios de prueba para simular compras:

1. Ve a: https://www.mercadopago.com.uy/developers/panel/test-users
2. Crea 2 usuarios: uno como **comprador** y otro como **vendedor**
3. Usa el usuario comprador para simular pagos

### Tarjetas de Prueba (Uruguay)

| Tarjeta | Número | CVV | Vencimiento | Resultado |
|---------|--------|-----|-------------|-----------|
| Visa | 4509 9535 6623 3704 | 123 | 11/25 | Aprobado |
| Mastercard | 5031 7557 3453 0604 | 123 | 11/25 | Aprobado |
| Visa | 4000 0000 0000 0002 | 123 | 11/25 | Rechazado |

### Flujo de Prueba

1. Agrega productos al carrito
2. Ve a Checkout
3. Selecciona "Mercado Pago" como método de pago
4. Click "Pagar"
5. Se abre Custom Tab con el checkout de MP
6. Usa tarjeta de prueba
7. Después del pago, la app recibe el deep link:
   - Éxito: `rendly://payment/success?order_id=xxx`
   - Fallo: `rendly://payment/failure?order_id=xxx`
   - Pendiente: `rendly://payment/pending?order_id=xxx`

## Archivos Creados/Modificados

### Nuevos archivos:
- `supabase/functions/create-mp-preference/index.ts` - Crea preferencias de pago
- `supabase/functions/mp-webhook/index.ts` - Recibe notificaciones de pago
- `app/.../data/repository/MercadoPagoRepository.kt` - Cliente Android
- `app/.../data/model/Order.kt` - Modelos de órdenes
- `app/.../data/repository/OrderRepository.kt` - Gestión de órdenes
- `app/.../ui/screens/checkout/CheckoutScreen.kt` - Pantalla de checkout
- `SUPABASE_ORDERS_MERCADOPAGO.sql` - Esquema de base de datos

### Archivos modificados:
- `app/build.gradle.kts` - Nuevas dependencias
- `app/src/main/AndroidManifest.xml` - Deep links
- `app/.../MainActivity.kt` - Manejo de deep links
- `app/.../ui/components/CartModal.kt` - Navegación a checkout
- `app/.../ui/components/ProductPage.kt` - Mejoras varias

## Pasar a Producción

1. Cambia `MERCADOPAGO_SANDBOX=false` en Supabase secrets
2. Usa las credenciales de **producción** (no las de prueba)
3. Verifica que el webhook esté configurado correctamente
4. Prueba con una compra real de bajo monto

## Troubleshooting

### El checkout no abre
- Verifica que `androidx.browser:browser` esté en las dependencias
- Asegúrate de tener Chrome o un navegador compatible instalado

### El deep link no funciona
- Verifica el `AndroidManifest.xml` tenga el intent-filter correcto
- Asegúrate que `launchMode="singleTask"` esté en la MainActivity
- Prueba manualmente: `adb shell am start -d "rendly://payment/success"`

### Error en Edge Function
- Revisa los logs: `supabase functions logs create-mp-preference`
- Verifica que `MERCADOPAGO_ACCESS_TOKEN` esté configurado

### Webhook no recibe notificaciones
- Verifica que la URL del webhook sea correcta en el panel de MP
- Revisa los logs del webhook: `supabase functions logs mp-webhook`
- MP puede tardar unos segundos en enviar la notificación

## Soporte

- Documentación MP: https://www.mercadopago.com.uy/developers/es/docs
- API Reference: https://www.mercadopago.com.uy/developers/es/reference
- Supabase Edge Functions: https://supabase.com/docs/guides/functions
