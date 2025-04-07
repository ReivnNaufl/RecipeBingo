package com.unluckygbs.recipebingo.data.dataclass

import com.google.gson.annotations.SerializedName

data class SpoonacularApiResponse(
    val message: String,
    val error: Int
)

data class SearchIngredient(
    @SerializedName("results") val results: List<Ingredient>
)

data class Ingredient(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("aisle") val aisle: String,
    @SerializedName("possibleUnits") val possibleUnits: List<String>,
    )