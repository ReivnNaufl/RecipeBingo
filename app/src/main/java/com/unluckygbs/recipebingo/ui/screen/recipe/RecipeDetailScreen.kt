package com.unluckygbs.recipebingo.ui.screen.recipe

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unluckygbs.recipebingo.data.entity.RecipeEntity
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.unluckygbs.recipebingo.R
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel

@Composable
fun RecipeDetailScreen(
    recipeViewModel: RecipeViewModel,
    recipeId: Int,
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    context: Context,
    ingredientViewModel: IngredientViewModel
) {
    val recipeById by recipeViewModel.recipeById.collectAsState()
    val isBookmarked by recipeViewModel.isBookmarked.collectAsState()

    LaunchedEffect(recipeId) {
        if (recipeViewModel.isRecipeExist(recipeId)) {
            recipeViewModel.getRecipeByIdLocal(recipeId)
            recipeViewModel.observeBookmarkStatus(recipeId)
        } else {
            recipeViewModel.getRecipeById(recipeId)
            recipeViewModel.observeBookmarkStatus(recipeId)
        }
    }

    RecipeDetailScreenContent(
        recipeById = recipeById,
        onBackClick = onBackClick,
        onSaveClick = onSaveClick,
        nutritionTrackerViewModel = nutritionTrackerViewModel,
        recipeViewModel = recipeViewModel,
        context = context,
        isBookmarked = isBookmarked,
        ingredientViewModel = ingredientViewModel
    )
}

@Composable
fun RecipeDetailScreenContent(
    onBackClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    recipeById: RecipeEntity?,
    isBookmarked: Boolean,
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    recipeViewModel: RecipeViewModel,
    context: Context,
    ingredientViewModel: IngredientViewModel
) {
    val translatedIngredients by recipeViewModel.translatedIngredients.observeAsState()
    val translatedSteps by recipeViewModel.translatedSteps.observeAsState()
    val translatedNutrition by recipeViewModel.translatedNutrition.observeAsState()
    val isTranslating by recipeViewModel.isTranslating.collectAsState()
    var isTranslated by remember { mutableStateOf(false) }

    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Ingredients & Nutrition", "Steps")

    Log.d("NUTRITION", "Nutrition: ${recipeById?.nutrition}")

    if (recipeById == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 80.dp) // Space for FAB
        ) {
            // Header with back button and recipe image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFFE0E0E0))
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.material_symbols_arrow_back),
                            contentDescription = "Back"
                        )
                    }

                    if (recipeById.image.isNotEmpty()) {
                        AsyncImage(
                            model = recipeById.image,
                            contentDescription = "Recipe Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .align(Alignment.Center)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .align(Alignment.Center)
                                .background(Color.Gray, shape = RoundedCornerShape(16.dp))
                        )
                    }
                }
            }

            // Title
            item {
                Text(
                    text = recipeById.title,
                    style = TextStyle(
                        textIndent = TextIndent(firstLine = 17.sp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }


            item { Divider() }

            // Translate Button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        if (!isTranslated) {
                            recipeViewModel.translateRecipeDetails(
                                ingredients = recipeById.extendedIngredient.map { it.original },
                                steps = recipeById.analyzedInstruction.flatMap { it.steps }.map { it.step },
                                nutrition = recipeById.nutrition.map { "${it.name}: ${it.amount} ${it.unit}" }
                            )
                        }
                        isTranslated = !isTranslated
                    }) {
                        Icon(painter = painterResource(R.drawable.material_symbols_translate), contentDescription = "Translate")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isTranslated) "Tampilkan Asli" else "Terjemahkan")
                    }
                }
            }

            item { Divider() }

            // TabRow
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF00C853)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                            }
                        )
                    }
                }
            }

            // Content with Box and LazyColumn
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Adjust height as needed
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (selectedTabIndex) {
                            0 -> {
                                // Ingredients and Nutrition Page
                                if (isTranslating) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                } else {
                                    // Ingredients section
                                    item {
                                        SectionWithDots(
                                            title = "Ingredients",
                                            items = if (isTranslated)
                                                translatedIngredients ?: emptyList()
                                            else
                                                recipeById.extendedIngredient.map { it.original }
                                        )
                                    }
                                    item { Divider() }
                                    // Nutrition section
                                    item {
                                        NutritionTableSection(
                                            title = "Nutrition",
                                            items = if (isTranslated)
                                                translatedNutrition ?: emptyList()
                                            else
                                                recipeById.nutrition.map { "${it.name}: ${it.amount} ${it.unit}" }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // Steps Page
                                if (isTranslating) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                } else {
                                    item {
                                        SectionWithNumbers(
                                            title = "Steps",
                                            items = if (isTranslated)
                                                translatedSteps ?: emptyList()
                                            else
                                                recipeById.analyzedInstruction.flatMap { it.steps }.map { it.step }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BookmarkButton(
                isBookmarked = isBookmarked,
                onSaveClick = {
                    val updatedRecipe = recipeById.copy(isBookmarked = !isBookmarked)
                    recipeViewModel.updateOrInsertRecipe(updatedRecipe, changeBookmark = true)
                }
            )

            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirm Add") },
                    text = { Text("Add this recipe to today's nutrition log?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            val insertedData = RecipeEntity(
                                id = recipeById.id,
                                title = recipeById.title,
                                image = recipeById.image,
                                isBookmarked = false,
                                nutrition = recipeById.nutrition,
                                extendedIngredient = recipeById.extendedIngredient,
                                analyzedInstruction = recipeById.analyzedInstruction
                            )
                            recipeViewModel.updateOrInsertRecipe(insertedData, changeBookmark = false)
                            nutritionTrackerViewModel.insertRecipe(insertedData)

                            Toast.makeText(context, "Recipe Added to Daily Eats!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            FloatingActionButton(
                onClick = { showDialog = true },
                backgroundColor = Color(0xFF00C853),
                shape = RoundedCornerShape(50),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eat")
                }
            }
        }
    }
}

@Composable
fun BookmarkButton(
    modifier: Modifier = Modifier,
    isBookmarked: Boolean,
    onSaveClick: () -> Unit
) {
    Log.d("BookmarkButton", "isBookmarked: $isBookmarked")
    FloatingActionButton(
        onClick = {
            Log.d("BookmarkButton", "Clicked, toggling bookmark")
            onSaveClick()
        },
        backgroundColor = Color(0xFF00C853),
        modifier = modifier
    ) {
        Icon(
            painter = if (isBookmarked) painterResource(R.drawable.tdesign_bookmark_filled) else painterResource(R.drawable.material_symbols_bookmark_outline),
            contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark"
        )
    }
}

@Composable
fun SectionWithDots(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00C853), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun SectionWithNumbers(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NutritionTableSection(title: String, items: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title, style = MaterialTheme.typography.subtitle1)
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Name", modifier = Modifier.weight(3f), style = MaterialTheme.typography.body2)
            Text("Amount", modifier = Modifier.weight(2f), style = MaterialTheme.typography.body2)
        }

        Divider()

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
                Text(name, modifier = Modifier.weight(3f))
                Text(amount, modifier = Modifier.weight(2f))
            }
            Divider()
        }
    }
}
