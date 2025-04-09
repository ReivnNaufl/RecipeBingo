package com.unluckygbs.recipebingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.unluckygbs.recipebingo.data.database.AppDatabase
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.ui.theme.RecipeBingoTheme
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        val ingredientRepository = IngredientRepository(AppDatabase.getDatabase(this).ingredientDao())
        setContent {
            RecipeBingoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Main(modifier = Modifier.padding(innerPadding),authViewModel = authViewModel, ingredientRepository = ingredientRepository)
                }
            }
        }
    }
}

