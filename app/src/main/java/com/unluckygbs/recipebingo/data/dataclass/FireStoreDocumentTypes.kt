package com.unluckygbs.recipebingo.data.dataclass

import androidx.annotation.Keep

@Keep
data class RecipesID(
    val id: Int,
    val amount: Int
)
{
    constructor() : this(0, 0)
}

@Keep
data class DailyEatsFS(
    val date: String,
    val recipesID: List<RecipesID>,
    val totalNutrition: List<Nutrient>?
)
{
    constructor() : this("9999-9-9", emptyList(), emptyList())
}

@Keep
data class RecipeFS(
    val id: Int,
    val image: String,
    val name: String,
    val extendedIngredient: List<RecipeIngredient>,
    val analyzedInstruction: List<AnalyzedInstruction>,
    val nutrition: List<Nutrient>
)
{
    constructor() : this(0, "", "", emptyList(), emptyList(), emptyList())
}