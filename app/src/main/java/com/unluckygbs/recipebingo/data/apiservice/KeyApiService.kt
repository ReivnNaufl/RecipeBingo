package com.unluckygbs.recipebingo.data.apiservice

import com.unluckygbs.recipebingo.data.dataclass.KeyApiResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface KeyApiService {
    @GET("key?key=p4UnluckyGBS")
    suspend fun getapikey(
        @Query("type") type: String = "SEARCH3",
    ): KeyApiResponse

    @POST("otp/request")
    suspend fun postOtpRequest(
        @Query("key") key: String = "p4UnluckyGBS",
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("otp/verify")
    suspend fun postOtpVerification(
        @Query("key") key: String = "p4UnluckyGBS",
        @Body body: Map<String, String>
    ): Response<ResponseBody>
}