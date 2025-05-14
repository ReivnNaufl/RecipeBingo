package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.RecipeById
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