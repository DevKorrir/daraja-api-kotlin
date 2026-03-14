package dev.korryr.epesa.feature.payment.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StkPushResponse(
    @SerializedName("MerchantRequestID")   val merchantRequestId: String,
    @SerializedName("CheckoutRequestID")   val checkoutRequestId: String,
    @SerializedName("ResponseCode")        val responseCode: String,
    @SerializedName("ResponseDescription") val responseDescription: String,
    @SerializedName("CustomerMessage")     val customerMessage: String
)
