package com.unluckygbs.recipebingo.ui.screen.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NutritionTrackerScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val nutritionTrackerViewModel: NutritionTrackerViewModel = viewModel()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.unAuthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NutritionTracker(nutritionTrackerViewModel)

        TextButton(onClick = {
            authViewModel.signout(context)
        }) {
            Text(text = "Log Out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionTracker(
    nutritionTrackerViewModel: NutritionTrackerViewModel
) {
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    LaunchedEffect(selectedDate) {
        nutritionTrackerViewModel.loadFoodsForDate(selectedDate)
    }

    val foods = nutritionTrackerViewModel.consumedFoods
    val totalCalories = foods.sumOf { it.calories }
    val totalCarbs = foods.sumOf { it.carbohydrates }
    val totalProtein = foods.sumOf { it.protein }
    val totalFat = foods.sumOf { it.fat }
    val totalSugar = foods.sumOf { it.sugar }
    val totalFiber = foods.sumOf { it.fiber }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title
        Text("Track Nutrition", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar shown directly
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation()
        ) {
            DatePicker(state = datePickerState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Consumed Foods
        Text("Consumed Foods on ${selectedDate}", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (foods.isEmpty()) {
            Text("No foods logged.")
        } else {
            foods.forEach { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(food.name, fontWeight = FontWeight.Bold)
                        Text("Carbs: ${food.carbohydrates}g, Protein: ${food.protein}g, Fat: ${food.fat}g")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Nutrients Summary
        Text("Nutrients", fontWeight = FontWeight.Bold)

        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(
                progress = (totalCalories / 2400f).coerceIn(0f, 1f),
                modifier = Modifier.size(120.dp),
                color = Color.Green,
                strokeWidth = 8.dp
            )
            Text("${totalCalories} Kcal", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column {
            Text("$totalCarbs gr of Carbohydrates")
            Text("$totalProtein gr of Proteins")
            Text("$totalFat gr of Fats")
            Text("$totalSugar gr of Sugar")
            Text("$totalFiber gr of Fibers")
        }
    }
}
