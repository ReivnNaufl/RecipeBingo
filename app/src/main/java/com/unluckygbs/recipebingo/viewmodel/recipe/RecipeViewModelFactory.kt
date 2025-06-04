package com.unluckygbs.recipebingo.viewmodel.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unluckygbs.recipebingo.data.repository.RecipeRepository
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.util.TranslatorHelper

class RecipeViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val translator: TranslatorHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            return RecipeViewModel(recipeRepository, ingredientRepository, translator) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}