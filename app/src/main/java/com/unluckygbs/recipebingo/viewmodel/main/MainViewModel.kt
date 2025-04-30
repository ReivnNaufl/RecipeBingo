package com.unluckygbs.recipebingo.viewmodel.main

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class MainViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val SELECTED_INDEX_KEY = "selected_index"
    }

    // Menyimpan selectedIndex sebagai state
    var selectedIndex: MutableState<Int> = mutableStateOf(
        savedStateHandle[SELECTED_INDEX_KEY] ?: 0
    )
        private set

    fun setSelectedIndex(index: Int) {
        selectedIndex.value = index
        savedStateHandle[SELECTED_INDEX_KEY] = index
    }
}
