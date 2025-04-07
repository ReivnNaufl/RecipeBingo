package com.unluckygbs.recipebingo.viewmodel.ingredient

import androidx.lifecycle.*
import com.unluckygbs.recipebingo.data.SpoonacularClient
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import kotlinx.coroutines.launch
import com.unluckygbs.recipebingo.BuildConfig
import com.unluckygbs.recipebingo.data.KeyClient

class IngredientViewModel : ViewModel() {

    private val _ingredients = MutableLiveData<List<Ingredient>>()
    val ingredients: LiveData<List<Ingredient>> = _ingredients

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>()
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
}
