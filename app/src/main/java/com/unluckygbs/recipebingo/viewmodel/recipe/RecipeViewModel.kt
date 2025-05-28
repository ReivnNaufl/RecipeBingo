package com.unluckygbs.recipebingo.viewmodel.recipe

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.data.client.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.data.dataclass.RecipeIngredient
import com.unluckygbs.recipebingo.data.toRecipeEntity
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.repository.RecipeRepository
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.util.TranslatorHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.runtime.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    private val _subtractionMessage = MutableStateFlow<String?>(null)
    val subtractionMessage: StateFlow<String?> = _subtractionMessage.asStateFlow()

    private val _recipe = MutableLiveData<List<Recipe>>(emptyList())
    val recipe: LiveData<List<Recipe>> = _recipe

    private val _recipebyid = MutableStateFlow<RecipeEntity?>(null)
    val recipeById: StateFlow<RecipeEntity?> = _recipebyid

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _recommendedRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val recommendedRecipes: LiveData<List<Recipe>> = _recommendedRecipes

    private val _homeRamdomRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val homeRandomRecipes: LiveData<List<Recipe>> = _homeRamdomRecipes

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _bookmarkedRecipes = MutableStateFlow<List<RecipeEntity>>(emptyList())
    val bookmarkedRecipes: StateFlow<List<RecipeEntity>> = _bookmarkedRecipes.asStateFlow()

    private val _dailyRecipes = MutableLiveData<List<Recipe>>(emptyList())
    val dailyRecipes: LiveData<List<Recipe>> = _dailyRecipes

    private var lastUpdatedDate: LocalDate? = null

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating

    private val _translatedIngredients = MutableLiveData<List<String>>()
    val translatedIngredients: LiveData<List<String>> = _translatedIngredients

    private val _translatedSteps = MutableLiveData<List<String>>()
    val translatedSteps: LiveData<List<String>> = _translatedSteps

    private val _translatedNutrition = MutableLiveData<List<String>>()
    val translatedNutrition: LiveData<List<String>> = _translatedNutrition


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

    fun fetchHomeRandomRecipes() {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _recipe.value = emptyList()

            try {
                val keyResponse = KeyClient.apiService.getapikey("RAND5")
                val apiKey = keyResponse.key

                val response = SpoonacularClient.apiService.getRandomRecipeForHome(
                    apiKey = apiKey,
                )

                _homeRamdomRecipes.value = response.randomResults
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

                _recipebyid.value = response.toRecipeEntity(false)
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
                val apiKey = KeyClient.apiService.getapikey("SEARCH3").key
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

    fun fetchDailyRecipesOncePerDay(context: Context) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            _dailyRecipes.value = emptyList()

            try {
                val cached = recipeRepository.getShuffledDailyRecipes(context) ?: emptyList()
                if (cached.isNotEmpty()) {
                    _dailyRecipes.value = cached
                    return@launch
                }

                val apiKey = KeyClient.apiService.getapikey().key
                val ingredients = ingredientRepository.getIncludeIngredientsQuery()

                val response = SpoonacularClient.apiService.findDailyRecipes(
                    apiKey = apiKey,
                    ingredients = ingredients
                )

                val shuffled = response.results.shuffled().take(3)
                _dailyRecipes.value = shuffled
                recipeRepository.saveShuffledDailyRecipes(context, shuffled)

            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch daily recipes."
            } finally {
                _loading.value = false
            }
        }
    }

    fun translateRecipeDetails(
        ingredients: List<String>,
        steps: List<String>,
        nutrition: List<String>
    ) {
        viewModelScope.launch {
            _isTranslating.value = true

            val translator = TranslatorHelper()
            val translatedIngredients = ingredients.map { translator.translateText(it) ?: it }
            val translatedSteps = steps.map { translator.translateText(it) ?: it }
            val translatedNutrition = nutrition.map { translator.translateText(it) ?: it }

            _translatedIngredients.value = translatedIngredients
            _translatedSteps.value = translatedSteps
            _translatedNutrition.value = translatedNutrition

            _isTranslating.value = false
        }
    }

    fun resetState() {
        _recipe.value = emptyList()
        _recommendedRecipes.value = emptyList()
        _errorMessage.value = null
        _loading.value = false
    }

    fun insertSingleRecipe(recipeEntity: RecipeEntity) {
        viewModelScope.launch {
            recipeRepository.insertSingleRecipe(recipeEntity)
        }
    }

    fun updateOrInsertRecipe(recipeEntity: RecipeEntity, changeBookmark: Boolean) {
        viewModelScope.launch {
            recipeRepository.updateOrInsertRecipe(recipeEntity, changeBookmark)
            if (!recipeRepository.isRecipeExistsInFireStore(recipeEntity)) {
                recipeRepository.syncSingleRecipe(recipeEntity)
            }
            if (changeBookmark) {
                recipeRepository.syncRecipeBookmarkToFireStore()
            }
        }
    }


    suspend fun isRecipeExist(id: Int): Boolean {
        return recipeRepository.isRecipeExist(id)
    }

    suspend fun getRecipeByIdLocal(id: Int){
        _recipebyid.value = recipeRepository.getRecipeById(id)
    }

    suspend fun observeBookmarkStatus(recipeId: Int){
        viewModelScope.launch {
            recipeRepository.observeBookmarkStatus(recipeId).collect {isBookmarked ->
                _isBookmarked.value = isBookmarked ?: false
            }
        }
    }

    fun getAllBookmarkedRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllBookmarkedRecipes().collect { bookmarkedRecipes ->
                _bookmarkedRecipes.value = bookmarkedRecipes
            }
        }
    }

    fun removeBookmark(recipe: RecipeEntity) {
        viewModelScope.launch {
            val updatedRecipe = recipe.copy(isBookmarked = false)
            recipeRepository.updateOrInsertRecipe(updatedRecipe, changeBookmark = true)
        }
    }

    fun subtractIngredients(recipeIngredients: List<RecipeIngredient>) {
        viewModelScope.launch(Dispatchers.IO) {  // Run in background thread
            try {
                val message = ingredientRepository.subtractIngredient(recipeIngredients)
                _subtractionMessage.emit(message)  // Update StateFlow
            } catch (e: Exception) {
                _subtractionMessage.emit(e.message ?: "Unknown error")
            }
        }
    }

    fun insertRecipeToFireStore(recipe: RecipeEntity) {
        viewModelScope.launch {
            if (!recipeRepository.isRecipeExistsInFireStore(recipe)) {
                recipeRepository.syncSingleRecipe(recipe)
            }
        }
    }

    fun insertBookmarksToFireStore() {
        viewModelScope.launch {
            recipeRepository.syncRecipeBookmarkToFireStore()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            recipeRepository.deleteAll()
        }
    }

    fun clearSubtractMsg(){
        _subtractionMessage.value = null
    }
}
