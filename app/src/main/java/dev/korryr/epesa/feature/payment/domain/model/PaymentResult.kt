package dev.korryr.epesa.feature.payment.domain.model

data class PaymentResult(
    val transactionId: String,
    val customerMessage: String
)
