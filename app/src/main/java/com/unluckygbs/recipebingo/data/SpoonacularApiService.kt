package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.SpoonacularApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.BuildConfig
import com.unluckygbs.recipebingo.data.dataclass.SearchIngredient
import com.unluckygbs.recipebingo.data.dataclass.SearchRecipe

interface SpoonacularApiService {
    @GET("recipes/complexSearch?")
    suspend fun getRecipeData(
        @Query("apiKey") apiKey: String,
        @Query("query") query: String,
        @Query("number") number: Int = 3,
        @Query("metaInformation") metaInformation: Boolean = true,
    ): SearchRecipe
    @GET("food/ingredients/search?")
    suspend fun getIngredientData(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 3,
        @Query("query") query: String,
        @Query("metaInformation") metaInformation: Boolean = true,
    ): SearchIngredient
}

