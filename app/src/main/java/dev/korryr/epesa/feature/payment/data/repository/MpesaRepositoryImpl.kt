package dev.korryr.epesa.feature.payment.data.repository

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.korryr.epesa.BuildConfig
import dev.korryr.epesa.feature.payment.data.remote.MpesaApiService
import dev.korryr.epesa.feature.payment.data.remote.dto.StkPushRequest
import dev.korryr.epesa.feature.payment.domain.model.PaymentResult
import dev.korryr.epesa.feature.payment.domain.repository.MpesaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MpesaRepositoryImpl @Inject constructor(
    private val api: MpesaApiService,
    @ApplicationContext private val context: Context
) : MpesaRepository {

    private val basicAuth: String by lazy {
        val raw = "${BuildConfig.CONSUMER_KEY}:${BuildConfig.CONSUMER_SECRET}"
        "Basic ${Base64.encodeToString(raw.toByteArray(), Base64.NO_WRAP)}"
    }

    override suspend fun initiateStkPush(
        phoneNumber: String,
        amount: String
    ): Result<PaymentResult> = withContext(Dispatchers.IO) {
        runCatching {
            val token = api.getAccessToken(basicAuth).accessToken

            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val rawPassword = "${BuildConfig.BUSINESS_SHORT_CODE}${BuildConfig.PASSKEY}$timestamp"
            val password = Base64.encodeToString(rawPassword.toByteArray(), Base64.NO_WRAP)

            val phone = formatPhone(phoneNumber)
            val amountInt = amount.toDouble().toInt().toString()

            Timber.d("STK Push → phone=$phone, amount=$amountInt")

            val response = api.initiateStkPush(
                authorization = "Bearer $token",
                request = StkPushRequest(
                    businessShortCode = BuildConfig.BUSINESS_SHORT_CODE,
                    password          = password,
                    timestamp         = timestamp,
                    transactionType   = "CustomerPayBillOnline",
                    amount            = amountInt,
                    partyA            = phone,
                    partyB            = BuildConfig.BUSINESS_SHORT_CODE,
                    phoneNumber       = phone,
                    callBackUrl       = BuildConfig.CALLBACK_URL,
                    accountReference  = "Epesa",
                    transactionDesc   = "Payment via Epesa"
                )
            )

            if (response.responseCode != "0") error(response.responseDescription)

            PaymentResult(
                transactionId   = response.checkoutRequestId,
                customerMessage = response.customerMessage
            )
        }
    }

    private fun formatPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }.trimStart('0')
        return if (digits.startsWith("254")) digits else "254$digits"
    }
}