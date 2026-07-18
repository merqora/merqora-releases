# Mercado Pago Marketplace + Split Payments — Architecture Guide

## Overview

Vinzay acts as a **Mercado Pago Marketplace**. When a buyer pays:

1. MP creates the payment using the **seller's access_token** (obtained via OAuth)
2. MP **automatically splits** the payment: seller receives `(total - commission)`, Vinzay receives the commission
3. **Vinzay NEVER custodies the money**. Funds flow directly: Buyer → MP → Seller + Vinzay

## Key Concepts

### Split Payments (`application_fee`)
- The `POST /v1/payments` API call includes `application_fee` (commission amount) and `sponsor_id` (seller's MP user ID)
- MP automatically routes the split — no manual disbursement needed
- Both the seller and the platform receive their portion directly from MP

### OAuth Flow
- Sellers connect their MP accounts via OAuth → Vinzay stores their `access_token` (encrypted)
- The `access_token` is used server-side only (never exposed to the client)
- Token exchange happens via Edge Function (keeps `client_secret` secure)

## Files Modified/Created

### Database
- `supabase/migrations/007_marketplace_split_payments.sql` — New tables + functions

### Edge Functions (Server)
| File | Purpose |
|------|---------|
| `mercadopago-oauth-exchange/index.ts` | Exchange OAuth code for tokens (server-side) |
| `process-card-payment/index.ts` | Checkout API + Split Payments via seller's token |
| `create-mp-preference/index.ts` | Checkout Pro fallback with marketplace_fee |
| `mp-webhook/index.ts` | Webhook handler with signature validation, no manual disbursement |
| ~~`transfer-to-seller/index.ts`~~ | Eliminada — Split Payments reemplaza completamente |

### Android App
| File | Purpose |
|------|---------|
| `MercadoPagoOAuthRepository.kt` | OAuth flow management |
| `MercadoPagoConnectScreen.kt` | UI for connecting/disconnecting MP account |
| `SplitPaymentRepository.kt` | Client-side API for creating split payments |
| `SellerBalanceScreen.kt` | Visual-only balance screen (money lives in MP) |
| `CardPaymentRepository.kt` | Updated to pass `sellerId` to Edge Function |
| `CardPaymentForm.kt` | Updated with `sellerId` parameter |
| `CheckoutScreen.kt` | No more `creditSeller()` calls |

## Setup Instructions

### 1. Mercado Pago Application Setup
1. Register Vinzay as a **Marketplace application** at https://www.mercadopago.com.uy/developers
2. Get `client_id` and `client_secret`
3. Get `access_token` for the platform account (for webhook queries)
4. Configure `redirect_uri` for OAuth: `vinzay://mp-oauth/callback`

### 2. Environment Variables (Supabase Edge Functions)
```
MERCADOPAGO_ACCESS_TOKEN=<platform_access_token>
MERCADOPAGO_CLIENT_ID=<mp_client_id>
MERCADOPAGO_CLIENT_SECRET=<mp_client_secret>
MERCADOPAGO_REDIRECT_URI=vinzay://mp-oauth/callback
MERCADOPAGO_WEBHOOK_SECRET=<webhook_secret_for_signature_validation>
MERCADOPAGO_SANDBOX=true
TOKEN_ENCRYPTION_KEY=<32_char_key_for_encrypting_tokens>
```

### 3. Android Build Config
In `gradle.properties`:
```
MERCADOPAGO_CLIENT_ID=<mp_client_id>
MP_PUBLIC_KEY=<mp_public_key>
SUPABASE_URL=<supabase_url>
SUPABASE_ANON_KEY=<supabase_anon_key>
R2_PUBLIC_URL=<r2_public_url>
CLOUDINARY_CLOUD_NAME=<cloud_name>
IMAGEKIT_URL_ENDPOINT=<imagekit_url>
```

### 4. Deploy Edge Functions
```bash
supabase functions deploy mercadopago-oauth-exchange --no-verify-jwt
supabase functions deploy process-card-payment --no-verify-jwt
supabase functions deploy create-mp-preference --no-verify-jwt
supabase functions deploy mp-webhook --no-verify-jwt
```

### 5. Configure MP Webhook URL
Set the webhook URL in your MP application dashboard:
`https://<supabase_project>.supabase.co/functions/v1/mp-webhook`

## Commission Structure
- Default: **10%** of transaction amount
- Min fee: **$5 UYU**
- Max fee: **$5,000 UYU**
- Configurable via `platform_settings` table (key: `commission_percentage`)

## Security
- Access tokens are encrypted at rest using `TOKEN_ENCRYPTION_KEY`
- Webhook signatures are validated using `X-Signature` header (HMAC-SHA256)
- Idempotency keys prevent duplicate payment processing
- Client never sees seller's access_token
- `creditSeller()` is deprecated — all disbursement is handled by MP automatically
