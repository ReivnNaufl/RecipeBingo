package com.unluckygbs.recipebingo.data.dataclass

import android.icu.text.CaseMap.Title
import com.google.gson.annotations.SerializedName
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity

data class SpoonacularApiResponse(
    val message: String,
    val error: Int
)

data class SearchIngredient(
    @SerializedName("results") val results: List<Ingredient>
)

data class SearchRecipe(
    @SerializedName("results") val results: List<Recipe> = emptyList()
)

data class Ingredient(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("aisle") val aisle: String,
    @SerializedName("possibleUnits") val possibleUnits: List<String>,
    )

fun Ingredient.toEntity(): IngredientEntity {
    val isPcs = aisle.lowercase() == "produce" || name.lowercase() == "egg"
    val unit = if (isPcs) "pcs" else "g"
    val quantity = if (isPcs) 1.0 else 100.0

    return IngredientEntity(
        id = this.id,
        name = this.name,
        quantity = quantity,
        unit = unit,
        image = this.image
    )
}

data class Recipe(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String
)
