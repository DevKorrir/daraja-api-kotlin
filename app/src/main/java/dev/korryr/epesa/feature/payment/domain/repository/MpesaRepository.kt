package dev.korryr.epesa.feature.payment.domain.repository

import dev.korryr.epesa.feature.payment.domain.model.PaymentResult

interface MpesaRepository {
    suspend fun initiateStkPush(phoneNumber: String, amount: String): Result<PaymentResult>
}
