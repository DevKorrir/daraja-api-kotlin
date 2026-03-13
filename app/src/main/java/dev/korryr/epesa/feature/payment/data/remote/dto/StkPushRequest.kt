package dev.korryr.epesa.feature.payment.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StkPushRequest(
    @SerializedName("BusinessShortCode") val businessShortCode: String,
    @SerializedName("Password")          val password: String,
    @SerializedName("Timestamp")         val timestamp: String,
    @SerializedName("TransactionType")   val transactionType: String,
    @SerializedName("Amount")            val amount: String,
    @SerializedName("PartyA")            val partyA: String,
    @SerializedName("PartyB")            val partyB: String,
    @SerializedName("PhoneNumber")       val phoneNumber: String,
    @SerializedName("CallBackURL")       val callBackUrl: String,
    @SerializedName("AccountReference")  val accountReference: String,
    @SerializedName("TransactionDesc")   val transactionDesc: String
)
