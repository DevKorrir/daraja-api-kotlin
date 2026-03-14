package dev.korryr.epesa.feature.payment.data.remote

import dev.korryr.epesa.feature.payment.data.remote.dto.AccessTokenResponse
import dev.korryr.epesa.feature.payment.data.remote.dto.StkPushRequest
import dev.korryr.epesa.feature.payment.data.remote.dto.StkPushResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface MpesaApiService {

    @GET("oauth/v1/generate?grant_type=client_credentials")
    suspend fun getAccessToken(
        @Header("Authorization") authorization: String
    ): AccessTokenResponse

    @POST("mpesa/stkpush/v1/processrequest")
    suspend fun initiateStkPush(
        @Header("Authorization") authorization: String,
        @Body request: StkPushRequest
    ): StkPushResponse
}
