package com.unluckygbs.recipebingo.data.apiservice

import com.unluckygbs.recipebingo.data.dataclass.SpoonacularApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.data.dataclass.SearchIngredient

interface SpoonacularApiService {
    @GET("recipes/complexSearch?")
    suspend fun getRecipeData(
        @Query("apiKey") apiKey: String,
        @Query("limit") limit: Int = 3,
    ): SpoonacularApiResponse
    @GET("food/ingredients/search?")
    suspend fun getIngredientData(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 3,
        @Query("query") query: String,
        @Query("metaInformation") metaInformation: Boolean = true,
    ): SearchIngredient
}

