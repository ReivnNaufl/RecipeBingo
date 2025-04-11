package com.unluckygbs.recipebingo.viewmodel.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.data.client.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import kotlinx.coroutines.launch

class RecipeViewModel : ViewModel() {

    private val _recipe = MutableLiveData<List<Recipe>>(emptyList())
    val recipe: LiveData<List<Recipe>> = _recipe

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

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
}
