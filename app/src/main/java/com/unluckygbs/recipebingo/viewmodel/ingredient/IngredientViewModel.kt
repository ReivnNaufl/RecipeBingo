package com.unluckygbs.recipebingo.viewmodel.ingredient

import androidx.lifecycle.*
import com.unluckygbs.recipebingo.data.client.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import kotlinx.coroutines.launch
import com.unluckygbs.recipebingo.data.client.KeyClient
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel

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
                ingredientRepository.insertIngredient(ingredient)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan: ${e.message}"
            }
        }
    }

    fun deleteIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            try {
                ingredientRepository.deleteIngredient(ingredient)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan: ${e.message}"
            }
        }
    }

    fun updateIngredient(ingredient: IngredientEntity) {
        viewModelScope.launch {
            try {
                ingredientRepository.updateIngredientQuantity(ingredient)
            } catch (e: Exception) {
                _errorMessage.value = "Gagal menambahkan: ${e.message}"
            }
        }
    }

    fun syncFromFirestore() {
        viewModelScope.launch {
            ingredientRepository.syncFromFirestoreToRoom()
        }
    }

    val availableIngredients: LiveData<List<IngredientEntity>> = ingredientRepository.allIngredients

    fun clearError() {
        _errorMessage.value = null
    }
}

