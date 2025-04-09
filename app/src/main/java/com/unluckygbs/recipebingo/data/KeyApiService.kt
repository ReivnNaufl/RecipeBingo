package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.KeyApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.BuildConfig


interface KeyApiService {
    @GET("key?key=p4UnluckyGBS")
    suspend fun getapikey(
        @Query("type") type: String = "SEARCH33",
    ): KeyApiResponse
}