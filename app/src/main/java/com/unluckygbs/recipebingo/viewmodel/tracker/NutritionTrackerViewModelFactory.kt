package com.unluckygbs.recipebingo.viewmodel.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.unluckygbs.recipebingo.data.repository.DailyEatsRepository

class NutritionTrackerViewModelFactory(
    private val dailyEatsRepository: DailyEatsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionTrackerViewModel::class.java)) {
            return NutritionTrackerViewModel(dailyEatsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}