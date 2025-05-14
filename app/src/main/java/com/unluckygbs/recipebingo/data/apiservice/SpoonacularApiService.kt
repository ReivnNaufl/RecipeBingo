package com.unluckygbs.recipebingo.data.apiservice

import com.unluckygbs.recipebingo.data.dataclass.ConversionResult
import com.unluckygbs.recipebingo.data.dataclass.RandomRecipeResponse
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.data.dataclass.RecipeByIngredients
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

    @GET("recipes/complexSearch")
    suspend fun findRecipesByIngredients(
        @Query("apiKey") apiKey: String,
        @Query("includeIngredients") ingredients: String,
        @Query("minCalories") minCalories: Int? = null,
        @Query("maxCalories") maxCalories: Int? = null,
        @Query("minProtein") minProtein: Int? = null,
        @Query("maxProtein") maxProtein: Int? = null,
        @Query("minSugar") minSugar: Int? = null,
        @Query("maxSugar") maxSugar: Int? = null,
        @Query("minFat") minFat: Int? = null,
        @Query("maxFat") maxFat: Int? = null,
        @Query("number") number: Int = 3,
    ): RecipeByIngredients

    @GET("recipes/convert")
    suspend fun convertIngredientAmount(
        @Query("apiKey") apiKey: String,
        @Query("ingredientName") ingredientName: String,
        @Query("sourceAmount") sourceAmount: Double,
        @Query("sourceUnit") sourceUnit: String,
        @Query("targetUnit") targetUnit: String? = "grams"
    ): ConversionResult
}

