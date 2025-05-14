package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.DailyEatsFS
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.data.dataclass.RecipesID
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.DailyRecipeCrossRef
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity


fun RecipeById.toRecipeEntity(isBookmarked: Boolean): RecipeEntity {
    return RecipeEntity(
        id = this.id,
        title = this.title,
        image = this.image,
        extendedIngredient = this.extendedIngredients,
        nutrition = this.nutrition.nutrient,
        analyzedInstruction = this.analyzedInstruction,
        isBookmarked = isBookmarked
    )
}

fun List<IngredientEntity>.mergeQuantityChanges(listOfChanges: MutableList<IngredientEntity>): List<IngredientEntity> {
    val changesMap = listOfChanges.associateBy { it.id }

    return this.map { original ->
        changesMap[original.id]?.let { changed ->
            original.copy(quantity = changed.quantity)
        } ?: original
    }
}

fun DailyRecipeCrossRef.toRecipesID(): RecipesID{
    return RecipesID(
        id = this.id,
        amount = this.amount
    )
}

fun DailyEatsWithRecipes.toDailyEatsFS(crossRefs: List<DailyRecipeCrossRef>): DailyEatsFS{
    val recipesID: MutableList<RecipesID> = mutableListOf()
    crossRefs.forEach {crossRef ->
        recipesID.add(crossRef.toRecipesID())
    }
    return DailyEatsFS(
        date = this.dailyEats.date,
        recipesID = recipesID.toList(),
        totalNutrition = this.dailyEats.totalNutrition
    )
}