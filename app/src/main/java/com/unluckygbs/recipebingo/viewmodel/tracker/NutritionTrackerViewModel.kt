package com.unluckygbs.recipebingo.viewmodel.tracker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.unluckygbs.recipebingo.data.dataclass.Nutrient
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import com.unluckygbs.recipebingo.data.entity.DailyEatsEntity
import com.unluckygbs.recipebingo.data.entity.DailyEatsWithRecipes
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.data.repository.DailyEatsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NutritionTrackerViewModel(
    private val dailyEatsRepository: DailyEatsRepository
) : ViewModel() {
    private val _dailyEats = mutableStateOf<DailyEatsWithRecipes?>(null)
    val dailyEats: State<DailyEatsWithRecipes?> get() = _dailyEats

    private val _totalNutrition = mutableStateOf<List<Nutrient>?>(null)
    val totalNutrition: State<List<Nutrient>?> get() = _totalNutrition

    suspend fun loadDailyEats(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        _dailyEats.value = dailyEatsRepository.getDailyEatsWithRecipes(dateString)
        _totalNutrition.value = _dailyEats.value?.dailyEats?.totalNutrition
    }


    fun insertRecipe(recipe: RecipeEntity) {
        viewModelScope.launch {
            dailyEatsRepository.insertSingleRecipe(recipe)
        }
    }

    suspend fun deleteDailyEats(dailyEatsEntity: DailyEatsEntity) {
        dailyEatsRepository.deleteDailyEats(dailyEatsEntity)
    }

    fun clearAll() {
        viewModelScope.launch {
            dailyEatsRepository.clearAll()
        }
    }

}
