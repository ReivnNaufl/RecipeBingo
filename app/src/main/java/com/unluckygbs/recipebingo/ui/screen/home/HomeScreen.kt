package com.unluckygbs.recipebingo.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.unluckygbs.recipebingo.ui.screen.ingredient.AvailableIngredientsScreen
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel,
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    ingredientViewModel: IngredientViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.unAuthenticated) {
            navController.navigate("login")
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HomeDetail(navController, recipeViewModel, nutritionTrackerViewModel,ingredientViewModel)

        TextButton(onClick = {
            authViewModel.signout(context)
        }) {
            Text(text = "Log Out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDetail(
    navController: NavController,
    recipeViewModel: RecipeViewModel,
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    ingredientViewModel: IngredientViewModel
) {
    val recipe by recipeViewModel.recipe.observeAsState(emptyList())
    val ingredients by ingredientViewModel.availableIngredients.observeAsState(emptyList())
    val datePickerState = rememberDatePickerState()
    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()
    val recommended by recipeViewModel.recommendedRecipes.observeAsState(emptyList())


    LaunchedEffect(selectedDate) {
        nutritionTrackerViewModel.loadDailyEats(selectedDate)
    }

    val totalNutrition by nutritionTrackerViewModel.totalNutrition
    val calorie = totalNutrition?.find { it.name == "Calories" }
    val totalCalories = calorie?.amount?.toFloat() ?: 0
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Ingredients") },
                navigationIcon = {
                    IconButton(onClick = { /* Profile click */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Bookmark click */ }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bookmark")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recipes section
            item {
                SectionTitle(title = "Recipes")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(5) {
                        CircleRecipeItem(label = "Label")
                    }
                }
            }

            // Today's Recipes
            item {
                SectionTitle(title = "Today's Recipes")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(3) { index ->
                        RectangleRecipeItem(rank = "${index + 1}st")
                    }
                }
            }

            // Your Ingredients
            item {
                SectionTitle(title = "Your Ingredients")
                if (ingredients.isEmpty()) {
                    Text(
                        text = "No ingredients found.",
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(ingredients) { ingredient ->
                            IngredientItem(name = ingredient.name, image = ingredient.image)
                        }
                    }
                }
            }

            // Today's Nutrition
            item {
                SectionTitle(title = "Today's Nutrition")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularNutritionProgress(kcal = totalCalories.toFloat())
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
    }
}

@Composable
fun CircleRecipeItem(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
fun RectangleRecipeItem(rank: String) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray)
    ) {
        Text(
            text = rank,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun IngredientItem(name: String,image:String) {
    val imageUrl = "https://img.spoonacular.com/ingredients_250x250/$image"
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray)
        ){
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 12.sp)
    }
}

@Composable
fun CircularNutritionProgress(kcal: Float) {
    val progress = (kcal / 2400f).coerceIn(0f, 1f) // contoh: dari 2000 Kcal target harian
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(120.dp),
            color = Color.Green,
            strokeWidth = 8.dp
        )
        Text(
            text = "$kcal\nKcal",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
