package com.unluckybgs.recipebingo.data

import com.unluckybgs.recipebingo.data.dataclass.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckybgs.recipebingo.BuildConfig

const val apiKey = BuildConfig.API_KEY

interface ApiService {
    @GET("recipes/complexSearch?apiKey=${apiKey}")
    suspend fun getRecipeData(
        @Query("limit") limit: Int = 250,
    ): ApiResponse
}