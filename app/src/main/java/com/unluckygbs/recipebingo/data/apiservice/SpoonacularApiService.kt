package com.unluckygbs.recipebingo.data.apiservice

import com.unluckygbs.recipebingo.data.dataclass.RandomRecipeResponse
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.data.dataclass.SpoonacularApiResponse
import retrofit2.http.GET
import retrofit2.http.Query
import com.unluckygbs.recipebingo.data.dataclass.SearchIngredient
import com.unluckygbs.recipebingo.data.dataclass.SearchRecipe
import retrofit2.http.Path

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
    @GET("recipes/random?")
    suspend fun getRandomRecipeData(
        @Query("apiKey") apiKey: String,
        @Query("number") number: Int = 3,
        @Query("metaInformation") metaInformation: Boolean = true
    ): RandomRecipeResponse
    @GET("recipes/{id}/information")
    suspend fun getRecipeById(
        @Path("id") id:Int,
        @Query("apiKey") apiKey: String,
        @Query("metaInformation") metaInformation: Boolean = true,
        @Query("includeNutrition") includeNutrition: Boolean = true,
    ): RecipeById
}

