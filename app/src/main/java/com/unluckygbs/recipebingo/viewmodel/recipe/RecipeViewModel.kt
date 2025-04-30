package com.unluckygbs.recipebingo.viewmodel.recipe

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.data.client.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.data.dataclass.RecipeById
import com.unluckygbs.recipebingo.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val ingredientRepository: IngredientRepository): ViewModel() {

    private val _recipe = MutableLiveData<List<Recipe>>(emptyList())
    val recipe: LiveData<List<Recipe>> = _recipe

    private val _recipebyid = MutableStateFlow<RecipeById?>(null)
    val recipeById: StateFlow<RecipeById?> = _recipebyid

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _recommendedRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val recommendedRecipes: LiveData<List<Recipe>> = _recommendedRecipes

    fun fetchRecipe(query: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val keyResponse = KeyClient.apiService.getapikey()
                val apiKey = keyResponse.key

                val response = SpoonacularClient.apiService.getRecipeData(
                    apiKey = apiKey,
                    query = query
                )
                _recipe.value = response.results
            } catch (e: Exception) {
                _errorMessage.value = "No Connection."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchRandomRecipes() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _recipe.value = emptyList()

            try {
                val keyResponse = KeyClient.apiService.getapikey()
                val apiKey = keyResponse.key

                val response = SpoonacularClient.apiService.getRandomRecipeData(
                    apiKey = apiKey,
                )

                _recommendedRecipes.value = response.randomResults
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recommended recipes."
            } finally {
                _loading.value = false
            }
        }
    }

    fun getRecipeById(Id: Int) {
        Log.d("DetailRecipeDebug", "Fetching recipe with id: $Id")
        viewModelScope.launch {
            _recipebyid.value = null
            _loading.value = true
            _errorMessage.value = null
            try {
                val keyResponse = KeyClient.apiService.getapikey()
                val apiKey = keyResponse.key

                val response = SpoonacularClient.apiService.getRecipeById(
                    apiKey = apiKey,
                    id = Id
                )
                Log.d("DetailRecipeDebug", "API response: $response")

                _recipebyid.value = response
            } catch (e: Exception) {
                Log.e("DetailRecipeDebug", "Error: ${e.localizedMessage}")
                _errorMessage.value = "No Connection."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchRecipeByAvailableIngredientswithNutrition(nutrients: Map<String, Int>) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _recipe.value = emptyList()

            try {
                val apiKey = KeyClient.apiService.getapikey().key
                val ingredients = ingredientRepository.getIncludeIngredientsQuery()

                val response = SpoonacularClient.apiService.findRecipesByIngredients(
                    apiKey = apiKey,
                    ingredients = ingredients,
                    minCalories = nutrients["minCalories"],
                    maxCalories = nutrients["maxCalories"],
                    minProtein = nutrients["minProtein"],
                    maxProtein = nutrients["maxProtein"],
                    minSugar = nutrients["minSugar"],
                    maxSugar = nutrients["maxSugar"],
                    minFat = nutrients["minFat"],
                    maxFat = nutrients["maxFat"]
                )

                _recipe.value = response.results
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch recipes."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchRecipeByNutritionOnly(nutrients: Map<String, Int>) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _recommendedRecipes.value = emptyList()

            try {
                val apiKey = KeyClient.apiService.getapikey().key

                val response = SpoonacularClient.apiService.findRecipesByIngredients(
                    apiKey = apiKey,
                    ingredients = "",
                    minCalories = nutrients["minCalories"],
                    maxCalories = nutrients["maxCalories"],
                    minProtein = nutrients["minProtein"],
                    maxProtein = nutrients["maxProtein"],
                    minSugar = nutrients["minSugar"],
                    maxSugar = nutrients["maxSugar"],
                    minFat = nutrients["minFat"],
                    maxFat = nutrients["maxFat"]
                )

                _recommendedRecipes.value = response.results
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch recipes by nutrition."
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetState() {
        _recipe.value = emptyList()
        _recommendedRecipes.value = emptyList()
        _errorMessage.value = null
        _loading.value = false
    }
}
