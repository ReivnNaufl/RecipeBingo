package com.unluckygbs.recipebingo.viewmodel.tracker

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.LocalDate

// Data class for placeholder
data class ConsumedFood(
    val name: String,
    val carbohydrates: Int,
    val protein: Int,
    val fat: Int,
    val sugar: Int,
    val fiber: Int,
    val calories: Int,
    val date: LocalDate
)

class NutritionTrackerViewModel : ViewModel() {
    private val allFoods = listOf(
        ConsumedFood("Nasi Goreng", 40, 6, 10, 5, 2, 350, LocalDate.of(2025,4,23)),
        ConsumedFood("Ayam Bakar", 0, 30, 5, 0, 1, 250, LocalDate.of(2025,4,21)),
        ConsumedFood("Salad", 10, 2, 3, 2, 5, 100, LocalDate.of(2025,4,30))
    )

    private val _consumedFoods = mutableStateListOf<ConsumedFood>()
    val consumedFoods: List<ConsumedFood> get() = _consumedFoods

    fun loadFoodsForDate(date: LocalDate) {
        _consumedFoods.clear()
        _consumedFoods.addAll(allFoods.filter { it.date == date })
    }
}
