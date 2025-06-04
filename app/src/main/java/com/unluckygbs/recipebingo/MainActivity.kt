package com.unluckygbs.recipebingo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.database.AppDatabase
import com.unluckygbs.recipebingo.data.repository.UserRepository
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.ui.theme.RecipeBingoTheme
import com.unluckygbs.recipebingo.util.DataStoreManager
import com.unluckygbs.recipebingo.util.TranslatorHelper
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModelFactory
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val firestore = FirebaseFirestore.getInstance()
        val userDao = AppDatabase.getDatabase(this).userDao()
        val userRepository = UserRepository(userDao, firestore)
        val authViewModel: AuthViewModel by viewModels {
            AuthViewModelFactory(userRepository)
        }
        authViewModel.syncUserProfileIfOnline(this)
        val ingredientRepository = IngredientRepository(AppDatabase.getDatabase(this).ingredientDao(), firestore = firestore, userId = authViewModel.getCurrentUserUid() ?: "")
        val dataStoreManager = DataStoreManager(applicationContext)

        val translator: TranslatorHelper = TranslatorHelper()

        lifecycleScope.launch {
            translator.downloadModel()
        }

        setContent {
            val isFirstLaunch by produceState<Boolean?>(initialValue = null) {
                dataStoreManager.isFirstLaunch.collect {
                    value = it
                }
            }

            if (isFirstLaunch == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val startDestination = if (isFirstLaunch == true) "onboarding" else "Start"

                RecipeBingoTheme {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Main(
                            modifier = Modifier.padding(innerPadding),
                            authViewModel = authViewModel,
                            context = this,
                            startDestination = startDestination,
                            onOnboardingFinished = {
                                lifecycleScope.launch {
                                    dataStoreManager.setFirstLaunchDone()
                                }
                            },
                            translator
                        )
                    }
                }
            }
        }
    }
}

