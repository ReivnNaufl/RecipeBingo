package com.unluckygbs.recipebingo.data.dataclass

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

data class RandomRecipeResponse(
    @SerializedName("recipes") val randomResults: List<Recipe> = emptyList()
)

data class RecipeByIngredients(
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

data class RecipeIngredient(
    @SerializedName("name") val name: String,
    @SerializedName("original") val original: String,
    @SerializedName("measures") val measures: IngredientMeasure,
)
{
    constructor() : this("", "", IngredientMeasure(IngredientMetric(0.0, "")))
}

data class IngredientMeasure(
    @SerializedName("metric") val metric: IngredientMetric,
)
{
    constructor() : this(IngredientMetric(0.0, ""))
}

data class IngredientMetric(
    @SerializedName("amount") val amount: Double,
    @SerializedName("unitLong") val unitLong: String,
)
{
    constructor() : this(0.0, "")
}

data class RecipeById(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("extendedIngredients") val extendedIngredients: List<RecipeIngredient>,
    @SerializedName("nutrition") val nutrition :Nutrition,
    @SerializedName("analyzedInstructions") val analyzedInstruction: List<AnalyzedInstruction>
)

data class Nutrition(
    @SerializedName("nutrients") val nutrient: List<Nutrient>
)

data class Nutrient(
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("unit") val unit: String,
)
{
    constructor() : this("", 0.0, "")
}


data class AnalyzedInstruction(
    @SerializedName("name") val name: String?,
    @SerializedName("steps") val steps: List<InstructionStep>
)
{
    constructor() : this("", emptyList())
}

data class InstructionStep(
    @SerializedName("number") val number: Int,
    @SerializedName("step") val step: String
)
{
    constructor() : this(0, "")
}

data class ConversionResult(
    @SerializedName("sourceAmount") val sourceAmount: Double,
    @SerializedName("sourceUnit") val sourceUnit: String,
    @SerializedName("targetAmount") val targetAmount: Double,
    @SerializedName("targetUnit") val targetUnit: String
)