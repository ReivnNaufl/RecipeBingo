package com.unluckygbs.recipebingo.data.dataclass

data class RecipesID(
    val id: Int,
    val amount: Int
)

data class DailyEatsFS(
    val date: String,
    val recipesID: List<RecipesID>,
    val totalNutrition: List<Nutrient>?
)

data class RecipeFS(
    val id: Int,
    val image: String,
    val name: String,
    val extendedIngredient: List<RecipeIngredient>,
    val analyzedInstruction: List<AnalyzedInstruction>,
    val nutrition: List<Nutrient>
)