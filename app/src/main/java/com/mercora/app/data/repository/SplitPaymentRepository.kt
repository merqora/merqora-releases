package com.mercora.app.data.repository

import android.util.Log
import com.mercora.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object SplitPaymentRepository {
    private const val TAG = "SplitPaymentRepo"
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun processCardPayment(
        token: String,
        paymentMethodId: String,
        transactionAmount: Double,
        installments: Int,
        payerEmail: String,
        orderId: String,
        issuerId: Int? = null,
        payerIdentification: PayerIdentification? = null,
        description: String? = null
    ): Result<SplitPaymentResult> = withContext(Dispatchers.IO) {
        try {
            val functionUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/process-card-payment"

            val requestBody = buildJsonObject {
                put("token", token)
                put("payment_method_id", paymentMethodId)
                put("transaction_amount", transactionAmount)
                put("installments", installments)
                put("payer_email", payerEmail)
                put("order_id", orderId)
                issuerId?.let { put("issuer_id", it) }
                payerIdentification?.let {
                    put("payer_identification", buildJsonObject {
                        put("type", it.type)
                        put("number", it.number)
                    })
                }
                description?.let { put("description", it) }
            }

            Log.d(TAG, "Procesando pago con split para orden $orderId")

            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url(functionUrl)
                .post(requestBody.toString().toByteArray()
                    .toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: throw Exception("Respuesta vacÃ­a")
            Log.d(TAG, "Respuesta process-card-payment: $responseText")

            if (!response.isSuccessful) {
                val errorResponse = try {
                    json.decodeFromString<ErrorResponse>(responseText)
                } catch (_: Exception) { null }
                return@withContext Result.failure(
                    Exception(errorResponse?.error ?: "Error procesando pago (${response.code})")
                )
            }

            val result = json.decodeFromString<SplitPaymentResult>(responseText)
            Log.d(TAG, "âœ… Pago ${result.id} estado: ${result.status}")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error en processCardPayment: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun createPreference(
        orderId: String,
        items: List<CartRepository.CartItem>,
        payerEmail: String? = null
    ): Result<PreferenceResult> = withContext(Dispatchers.IO) {
        try {
            val functionUrl = "${BuildConfig.SUPABASE_URL}/functions/v1/create-mp-preference"

            val requestBody = buildJsonObject {
                put("order_id", orderId)
                put("items", buildJsonArray {
                    items.forEach { cartItem ->
                        add(buildJsonObject {
                            put("id", cartItem.post.id)
                            put("title", cartItem.post.title.ifEmpty { cartItem.post.producto.titulo.ifEmpty { "Producto" } })
                            put("quantity", cartItem.quantity)
                            put("unit_price", cartItem.post.producto.precio)
                            put("currency_id", "UYU")
                            cartItem.post.images.firstOrNull()?.let { put("picture_url", it) }
                            cartItem.selectedColor?.let { put("description", "Color: $it") }
                        })
                    }
                })
                payerEmail?.let { put("payer_email", it) }
            }

            val okHttpClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder()
                .url(functionUrl)
                .post(requestBody.toString().toByteArray()
                    .toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseText = response.body?.string() ?: throw Exception("Respuesta vacÃ­a")
            Log.d(TAG, "Respuesta create-mp-preference: $responseText")

            val result = json.decodeFromString<PreferenceResult>(responseText)
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error creando preferencia: ${e.message}")
            Result.failure(e)
        }
    }
}

@Serializable
data class SplitPaymentResult(
    val id: Long,
    val status: String,
    @SerialName("status_detail") val statusDetail: String? = null,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("payment_type_id") val paymentTypeId: String? = null,
    val installments: Int = 1,
    @SerialName("transaction_amount") val transactionAmount: Double = 0.0,
    @SerialName("currency_id") val currencyId: String = "UYU",
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("date_approved") val dateApproved: String? = null,
    @SerialName("split_info") val splitInfo: SplitInfo? = null,
    val card: CardInfo? = null,
    @SerialName("transaction_details") val transactionDetails: TransactionDetails? = null
)

@Serializable
data class SplitInfo(
    val total: Double = 0.0,
    val commission: Double = 0.0,
    @SerialName("commission_pct") val commissionPct: Double = 0.0,
    @SerialName("seller_net") val sellerNet: Double = 0.0,
    @SerialName("sponsor_id") val sponsorId: String = ""
)

@Serializable
data class CardInfo(
    @SerialName("first_six_digits") val firstSixDigits: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null
)

@Serializable
data class TransactionDetails(
    @SerialName("net_received_amount") val netReceivedAmount: Double? = null,
    @SerialName("total_paid_amount") val totalPaidAmount: Double? = null,
    @SerialName("installment_amount") val installmentAmount: Double? = null
)

@Serializable
data class PreferenceResult(
    @SerialName("preference_id") val preferenceId: String = "",
    @SerialName("init_point") val initPoint: String = "",
    @SerialName("marketplace_fee") val marketplaceFee: Double? = null,
    @SerialName("sponsor_id") val sponsorId: String? = null
)

@Serializable
data class PayerIdentification(
    val type: String,
    val number: String
)

@Serializable
data class ErrorResponse(val error: String)

private fun ByteArray.toRequestBody(contentType: okhttp3.MediaType): okhttp3.RequestBody {
    return okhttp3.RequestBody.create(contentType, this)
}
