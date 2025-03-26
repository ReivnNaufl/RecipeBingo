package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.BuildConfig

const val apiKey = BuildConfig.API_KEY

interface ApiService {
    @GET("recipes/complexSearch?apiKey=${apiKey}")
    suspend fun getRecipeData(
        @Query("limit") limit: Int = 250,
    ): ApiResponse
}