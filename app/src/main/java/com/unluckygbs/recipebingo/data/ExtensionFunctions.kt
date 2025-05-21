package com.unluckygbs.recipebingo.data

import com.unluckygbs.recipebingo.data.dataclass.AnalyzedInstruction
import com.unluckygbs.recipebingo.data.dataclass.DailyEatsFS
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.data.dataclass.RecipeFS
import com.unluckygbs.recipebingo.data.dataclass.RecipeIngredient
import com.unluckygbs.recipebingo.data.dataclass.RecipesID
import com.unluckygbs.recipebingo.data.entity.DailyEatsEntity
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

fun List<Int>.toStringList(): List<String> {
    return this.map { it.toString() }
}

fun RecipeEntity.toRecipeFS(): RecipeFS {
    return RecipeFS(
        id = this.id,
        image = this.image,
        name = this.title,
        extendedIngredient = this.extendedIngredient,
        analyzedInstruction = this.analyzedInstruction,
        nutrition = this.nutrition
    )
}

fun RecipeFS.toRecipeEntity(bookmark: Boolean): RecipeEntity {
    return RecipeEntity(
        id = this.id,
        image = this.image,
        title = this.name,
        isBookmarked = bookmark,
        extendedIngredient = this.extendedIngredient,
        analyzedInstruction = this.analyzedInstruction,
        nutrition = this.nutrition
    )
}

fun DailyEatsFS.toDailyEatsEntity(): DailyEatsEntity {
    return DailyEatsEntity(
        date = this.date,
        totalNutrition = this.totalNutrition
    )
}

fun RecipesID.toCrossRef(date: String): DailyRecipeCrossRef {
    return DailyRecipeCrossRef(
        date = date,
        id = this.id,
        amount = this.amount
    )
}

fun List<RecipesID>.toDailyRecipeCrossRefList(date: String): List<DailyRecipeCrossRef> {
    return this.map { recipeId ->
        recipeId.toCrossRef(date)
    }
}

fun DailyEatsFS.toDailyRecipeCrossRefs(): List<DailyRecipeCrossRef> {
    return this.recipesID.toDailyRecipeCrossRefList(this.date)
}


fun List<DailyRecipeCrossRef>.extractIds(): List<Int> = this.map { it.id }