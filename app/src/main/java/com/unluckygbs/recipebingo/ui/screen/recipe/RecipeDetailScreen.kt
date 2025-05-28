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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
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
        if (recipeViewModel.isRecipeExist(recipeId)){
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


    Log.d("NUTRITION", "Nutrition: ${recipeById?.nutrition}")

    if (recipeById == null) {
        // Tampilkan indikator loading atau teks error
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
                .padding(bottom = 80.dp) // Menambahkan padding untuk memberi ruang bagi FAB
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
                            imageVector = Icons.Default.ArrowBack,
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
                        // Placeholder jika gambar tidak tersedia
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
                    style = TextStyle(textIndent = TextIndent(firstLine = 17.sp)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
            }

            item { Divider() }

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

                // Steps section
                val steps = recipeById.analyzedInstruction.flatMap { it.steps }.map { it.step }
                item {
                    SectionWithDots(
                        title = "Steps",
                        items = if (isTranslated)
                            translatedSteps ?: emptyList()
                        else
                            steps
                    )
                }

                item { Divider() }

                // Nutrition section
                val nutrients = recipeById.nutrition.map { "${it.name}: ${it.amount} ${it.unit}" }
                item {
                    SectionWithDots(
                        title = "Nutrition",
                        items = if (isTranslated)
                            translatedNutrition ?: emptyList()
                        else
                            nutrients
                    )
                }
            }
        }

        // Bottom buttons tetap di bawah layar
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
            var showSecondDialog by remember { mutableStateOf(false) }
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

                            showSecondDialog = true
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

            if (showSecondDialog) {
                AlertDialog(
                    onDismissRequest = { showSecondDialog = false },
                    title = { Text("Substract Ingredient?") },
                    text = { Text("Substract Ingredient based on this recipe?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSecondDialog = false

                            Toast.makeText(context, "Ingredient Substracted!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Substract")
                        }

                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    },
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
            imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
