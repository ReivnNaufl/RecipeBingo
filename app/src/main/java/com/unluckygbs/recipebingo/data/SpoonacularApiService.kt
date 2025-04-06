package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.SpoonacularApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.BuildConfig

interface SpoonacularApiService {
    @GET("recipes/complexSearch?")
    suspend fun getRecipeData(
        @Query("apiKey") apiKey: String,
        @Query("limit") limit: Int = 3,
    ): SpoonacularApiResponse
}