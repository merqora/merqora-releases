package com.rendly.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * CARD PAYMENT MODELS - Checkout API de Mercado Pago
 * ═══════════════════════════════════════════════════════════════════════════════
 * 
 * Modelos para el flujo de pago con tarjeta usando Checkout API.
 * Permite tokenizar tarjetas y procesar pagos directamente.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════
 */

/**
 * Datos de la tarjeta para tokenización
 */
@Serializable
data class CardData(
    @SerialName("card_number") val cardNumber: String,
    @SerialName("expiration_month") val expirationMonth: Int,
    @SerialName("expiration_year") val expirationYear: Int,
    @SerialName("security_code") val securityCode: String,
    @SerialName("cardholder") val cardholder: Cardholder
)

@Serializable
data class Cardholder(
    val name: String,
    val identification: CardholderIdentification
)

@Serializable
data class CardholderIdentification(
    val type: String = "CI", // CI para Uruguay, DNI para Argentina
    val number: String
)

/**
 * Token de tarjeta generado por MP
 */
@Serializable
data class CardToken(
    val id: String,
    @SerialName("public_key") val publicKey: String? = null,
    @SerialName("first_six_digits") val firstSixDigits: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null,
    @SerialName("expiration_month") val expirationMonth: Int? = null,
    @SerialName("expiration_year") val expirationYear: Int? = null,
    @SerialName("cardholder") val cardholder: CardholderResponse? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("date_last_updated") val dateLastUpdated: String? = null,
    @SerialName("date_due") val dateDue: String? = null,
    @SerialName("luhn_validation") val luhnValidation: Boolean? = null,
    @SerialName("live_mode") val liveMode: Boolean? = null,
    val status: String? = null
)

@Serializable
data class CardholderResponse(
    val name: String? = null,
    val identification: CardholderIdentification? = null
)

/**
 * Método de pago disponible
 */
@Serializable
data class PaymentMethodInfo(
    val id: String,
    val name: String,
    @SerialName("payment_type_id") val paymentTypeId: String,
    val status: String? = null,
    @SerialName("secure_thumbnail") val secureThumbnail: String? = null,
    val thumbnail: String? = null,
    @SerialName("deferred_capture") val deferredCapture: String? = null,
    val settings: List<PaymentMethodSetting>? = null,
    @SerialName("additional_info_needed") val additionalInfoNeeded: List<String>? = null,
    @SerialName("min_allowed_amount") val minAllowedAmount: Double? = null,
    @SerialName("max_allowed_amount") val maxAllowedAmount: Double? = null,
    @SerialName("accreditation_time") val accreditationTime: Int? = null,
    @SerialName("financial_institutions") val financialInstitutions: List<FinancialInstitution>? = null,
    @SerialName("processing_modes") val processingModes: List<String>? = null
)

@Serializable
data class PaymentMethodSetting(
    @SerialName("card_number") val cardNumber: CardNumberSetting? = null,
    val bin: BinSetting? = null,
    @SerialName("security_code") val securityCode: SecurityCodeSetting? = null
)

@Serializable
data class CardNumberSetting(
    val length: Int? = null,
    val validation: String? = null
)

@Serializable
data class BinSetting(
    val pattern: String? = null,
    @SerialName("installments_pattern") val installmentsPattern: String? = null,
    @SerialName("exclusion_pattern") val exclusionPattern: String? = null
)

@Serializable
data class SecurityCodeSetting(
    val length: Int? = null,
    @SerialName("card_location") val cardLocation: String? = null,
    val mode: String? = null
)

@Serializable
data class FinancialInstitution(
    val id: String? = null,
    val description: String? = null
)

/**
 * Banco emisor de la tarjeta
 */
@Serializable
data class Issuer(
    val id: String,
    val name: String,
    @SerialName("secure_thumbnail") val secureThumbnail: String? = null,
    val thumbnail: String? = null,
    @SerialName("processing_mode") val processingMode: String? = null,
    @SerialName("merchant_account_id") val merchantAccountId: String? = null
)

/**
 * Opciones de cuotas
 */
@Serializable
data class InstallmentsResponse(
    @SerialName("payment_method_id") val paymentMethodId: String,
    @SerialName("payment_type_id") val paymentTypeId: String,
    val issuer: Issuer? = null,
    @SerialName("payer_costs") val payerCosts: List<PayerCost>
)

@Serializable
data class PayerCost(
    val installments: Int,
    @SerialName("installment_rate") val installmentRate: Double,
    @SerialName("discount_rate") val discountRate: Double? = null,
    @SerialName("reimbursement_rate") val reimbursementRate: Double? = null,
    val labels: List<String>? = null,
    @SerialName("installment_rate_collector") val installmentRateCollector: List<String>? = null,
    @SerialName("min_allowed_amount") val minAllowedAmount: Double? = null,
    @SerialName("max_allowed_amount") val maxAllowedAmount: Double? = null,
    @SerialName("recommended_message") val recommendedMessage: String? = null,
    @SerialName("installment_amount") val installmentAmount: Double,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("payment_method_option_id") val paymentMethodOptionId: String? = null
)

/**
 * Request para procesar pago
 */
@Serializable
data class ProcessPaymentRequest(
    @SerialName("transaction_amount") val transactionAmount: Double,
    val token: String,
    val description: String,
    val installments: Int,
    @SerialName("payment_method_id") val paymentMethodId: String,
    @SerialName("issuer_id") val issuerId: String? = null,
    val payer: PayerInfo,
    @SerialName("external_reference") val externalReference: String? = null,
    @SerialName("binary_mode") val binaryMode: Boolean = false,
    val metadata: Map<String, String>? = null
)

@Serializable
data class PayerInfo(
    val email: String,
    val identification: CardholderIdentification? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
)

/**
 * Respuesta del procesamiento de pago
 */
@Serializable
data class PaymentResponse(
    val id: Long,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("date_approved") val dateApproved: String? = null,
    @SerialName("date_last_updated") val dateLastUpdated: String? = null,
    @SerialName("operation_type") val operationType: String? = null,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
    @SerialName("payment_type_id") val paymentTypeId: String? = null,
    val status: String, // approved, rejected, pending, in_process
    @SerialName("status_detail") val statusDetail: String? = null,
    @SerialName("currency_id") val currencyId: String? = null,
    val description: String? = null,
    @SerialName("live_mode") val liveMode: Boolean? = null,
    @SerialName("external_reference") val externalReference: String? = null,
    @SerialName("transaction_amount") val transactionAmount: Double? = null,
    val installments: Int? = null,
    @SerialName("transaction_details") val transactionDetails: TransactionDetails? = null,
    val card: CardResponse? = null,
    @SerialName("statement_descriptor") val statementDescriptor: String? = null
)

@Serializable
data class PayerResponse(
    val email: String? = null,
    val identification: CardholderIdentification? = null,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null
)

@Serializable
data class TransactionDetails(
    @SerialName("net_received_amount") val netReceivedAmount: Double? = null,
    @SerialName("total_paid_amount") val totalPaidAmount: Double? = null,
    @SerialName("overpaid_amount") val overpaidAmount: Double? = null,
    @SerialName("installment_amount") val installmentAmount: Double? = null
)

@Serializable
data class CardResponse(
    val id: String? = null,
    @SerialName("first_six_digits") val firstSixDigits: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null,
    @SerialName("expiration_month") val expirationMonth: Int? = null,
    @SerialName("expiration_year") val expirationYear: Int? = null,
    @SerialName("date_created") val dateCreated: String? = null,
    @SerialName("date_last_updated") val dateLastUpdated: String? = null,
    val cardholder: CardholderResponse? = null
)

/**
 * Tipo de tarjeta detectado por BIN
 */
enum class CardType(
    val displayName: String,
    val icon: String, // Nombre del icono/recurso
    val cvvLength: Int,
    val cardNumberLength: Int
) {
    VISA("Visa", "visa", 3, 16),
    MASTERCARD("Mastercard", "mastercard", 3, 16),
    AMEX("American Express", "amex", 4, 15),
    OCA("OCA", "oca", 3, 16),
    DINERS("Diners Club", "diners", 3, 14),
    CABAL("Cabal", "cabal", 3, 16),
    UNKNOWN("Tarjeta", "card", 3, 16)
}

/**
 * Detectar tipo de tarjeta por los primeros dígitos (BIN)
 */
fun detectCardType(cardNumber: String): CardType {
    val cleanNumber = cardNumber.replace(" ", "").replace("-", "")
    if (cleanNumber.isEmpty()) return CardType.UNKNOWN
    
    return when {
        // Visa: empieza con 4
        cleanNumber.startsWith("4") -> CardType.VISA
        
        // Mastercard: 51-55 o 2221-2720
        cleanNumber.length >= 2 && (
            cleanNumber.substring(0, 2).toIntOrNull() in 51..55 ||
            (cleanNumber.length >= 4 && cleanNumber.substring(0, 4).toIntOrNull() in 2221..2720)
        ) -> CardType.MASTERCARD
        
        // Amex: 34 o 37
        cleanNumber.length >= 2 && cleanNumber.substring(0, 2) in listOf("34", "37") -> CardType.AMEX
        
        // OCA (Uruguay): 542991 o 549941 o 6042
        cleanNumber.length >= 6 && (
            cleanNumber.startsWith("542991") ||
            cleanNumber.startsWith("549941") ||
            cleanNumber.startsWith("6042")
        ) -> CardType.OCA
        
        // Diners: 300-305, 36, 38
        cleanNumber.length >= 2 && (
            cleanNumber.startsWith("36") ||
            cleanNumber.startsWith("38") ||
            (cleanNumber.length >= 3 && cleanNumber.substring(0, 3).toIntOrNull() in 300..305)
        ) -> CardType.DINERS
        
        // Cabal: 604(2xx) o 589657 o 627170
        cleanNumber.length >= 6 && (
            cleanNumber.startsWith("589657") ||
            cleanNumber.startsWith("627170") ||
            cleanNumber.startsWith("6042")
        ) -> CardType.CABAL
        
        else -> CardType.UNKNOWN
    }
}

/**
 * Formatear número de tarjeta con espacios
 */
fun formatCardNumber(number: String, cardType: CardType = CardType.UNKNOWN): String {
    val clean = number.replace(" ", "").replace("-", "")
    
    return when (cardType) {
        CardType.AMEX -> {
            // Formato AMEX: 4-6-5
            buildString {
                clean.forEachIndexed { index, c ->
                    if (index == 4 || index == 10) append(" ")
                    if (index < 15) append(c)
                }
            }
        }
        else -> {
            // Formato estándar: 4-4-4-4
            buildString {
                clean.forEachIndexed { index, c ->
                    if (index > 0 && index % 4 == 0) append(" ")
                    if (index < 16) append(c)
                }
            }
        }
    }
}

/**
 * Validar número de tarjeta con algoritmo de Luhn
 */
fun isValidCardNumber(number: String): Boolean {
    val clean = number.replace(" ", "").replace("-", "")
    if (clean.length < 13 || clean.length > 19) return false
    if (!clean.all { it.isDigit() }) return false
    
    var sum = 0
    var alternate = false
    
    for (i in clean.length - 1 downTo 0) {
        var digit = clean[i].digitToInt()
        
        if (alternate) {
            digit *= 2
            if (digit > 9) digit -= 9
        }
        
        sum += digit
        alternate = !alternate
    }
    
    return sum % 10 == 0
}

/**
 * Validar fecha de expiración
 */
fun isValidExpirationDate(month: Int, year: Int): Boolean {
    if (month < 1 || month > 12) return false
    
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
    val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    
    val expYear = if (year > 2000) year % 100 else year
    
    return when {
        expYear > currentYear -> true
        expYear == currentYear && month >= currentMonth -> true
        else -> false
    }
}

/**
 * Validar CVV
 */
fun isValidCVV(cvv: String, cardType: CardType): Boolean {
    val expectedLength = cardType.cvvLength
    return cvv.length == expectedLength && cvv.all { it.isDigit() }
}
