package com.unluckygbs.recipebingo.viewmodel.ingredient

import androidx.lifecycle.*
import com.unluckygbs.recipebingo.data.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import kotlinx.coroutines.launch
import com.unluckygbs.recipebingo.BuildConfig
import com.unluckygbs.recipebingo.data.KeyClient
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.repository.IngredientRepository

class IngredientViewModel(private val ingredientRepository: IngredientRepository) : ViewModel() {

    private val _ingredients = MutableLiveData<List<Ingredient>>(emptyList())
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchIngredients(query: String) {
        viewModelScope.launch {
            _loading.value = true
            _errorMessage.value = null
            try {
                val keyResponse = KeyClient.apiService.getapikey()
                val apiKey = keyResponse.key

                val response = SpoonacularClient.apiService.getIngredientData(
                    apiKey = apiKey,
                    query = query
                )
                _ingredients.value = response.results
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun insertIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            try {
                ingredientRepository.insert(ingredient)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan: ${e.message}"
            }
        }
    }

    val availableIngredients: LiveData<List<IngredientEntity>> = ingredientRepository.allIngredients

    fun clearError() {
        _errorMessage.value = null
    }
}

