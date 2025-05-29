package com.unluckygbs.recipebingo.ui.screen.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.round
import androidx.compose.foundation.lazy.items

@Composable
fun NutritionTrackerScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    nutritionTrackerViewModel: NutritionTrackerViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

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
        NutritionTracker(nutritionTrackerViewModel,navController)

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
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    navController: NavController
) {
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    LaunchedEffect(selectedDate) {
        nutritionTrackerViewModel.loadDailyEats(selectedDate)
    }
    val dailyEats by nutritionTrackerViewModel.dailyEats
    val totalNutrition by nutritionTrackerViewModel.totalNutrition
    val calorie = totalNutrition?.find { it.name == "Calories" }
    val totalCalories = calorie?.amount?.toFloat() ?: 0



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Eats") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
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

        val eatenRecipe = dailyEats?.recipes

        if (eatenRecipe.isNullOrEmpty()) {
            Text("No foods logged.")
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF))
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(eatenRecipe) { food ->
                        RectangleRecipeItem(
                            rank = food.title,
                            imageUrl = food.image
                        ) {
                            navController.navigate("detailedrecipe/${food.id}")
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

        // Nutrients Summary
        Text("Nutrients", fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        progress = (totalCalories.toFloat() / 2400f).coerceIn(0f, 1f),
                        modifier = Modifier.size(120.dp),
                        color = Color(0xFF00C853), // Hijau terang
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "${totalCalories} Kcal",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

        Column {
            val nutrients = totalNutrition
            if (nutrients == null) {
                Text("No recipe eaten today")
            } else {
                    val filteredItems = nutrients
                        .filter { it.name != "Calories" }
                        .map { "${it.name}: ${it.amount} ${it.unit}" }
                    NutritionTableSection(
                        title = "Nutrition",
                        items = filteredItems
                    )

            }
        }
    }
}}}

@Composable
fun NutritionTableSection(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        androidx.compose.material.Text(
            text = title,
            style = androidx.compose.material.MaterialTheme.typography.subtitle1
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material.Text(
                "Name",
                modifier = Modifier.weight(3f),
                style = androidx.compose.material.MaterialTheme.typography.body2
            )
            androidx.compose.material.Text(
                "Amount",
                modifier = Modifier.weight(2f),
                style = androidx.compose.material.MaterialTheme.typography.body2
            )
        }

        androidx.compose.material.Divider()

        items.forEachIndexed { index, item ->
            val parts = item.split(":").map { it.trim() }
            val name = parts.getOrNull(0) ?: "-"
            val amount = parts.getOrNull(1) ?: "-"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material.Text(name, modifier = Modifier.weight(3f))
                androidx.compose.material.Text(amount, modifier = Modifier.weight(2f))
            }
            androidx.compose.material.Divider()
        }
    }
}

@Composable
fun RectangleRecipeItem(rank: String, imageUrl: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = rank,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            text = rank,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}
