@file:OptIn(ExperimentalMaterial3Api::class)

package com.unluckygbs.recipebingo.screen.ingredient

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import coil.compose.rememberAsyncImagePainter
import com.unluckygbs.recipebingo.data.dataclass.toEntity


@Composable
fun SearchIngredientScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    ingredientViewModel: IngredientViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val ingredients by ingredientViewModel.ingredients.observeAsState(emptyList())
    val isLoading by ingredientViewModel.loading.observeAsState(false)
    val errorMessage by ingredientViewModel.errorMessage.observeAsState()

    val availabelIngredient by ingredientViewModel.availableIngredients.observeAsState(emptyList())


    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.unAuthenticated) {
            navController.navigate("login")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Ingredient") },
                navigationIcon = {
                    IconButton(onClick = { /* Profile click */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Bookmark */ }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Bookmark")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFA6E9C2), shape = RoundedCornerShape(50))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (searchQuery.isNotBlank()) {
                        ingredientViewModel.fetchIngredients(searchQuery)
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                errorMessage != null -> {
                    Text("Error: $errorMessage", color = Color.Red)
                }

                ingredients.isEmpty() -> {
                    Text("No ingredients found", color = Color.Gray)
                }

                else -> {
                    ingredients.forEach { ingredient ->
                        val isAdded = availabelIngredient.any { it.name.equals(ingredient.name, ignoreCase = true) }

                        IngredientResultItem(
                            ingredient = ingredient,
                            isAdded = isAdded,
                            onAddClick = {
                                if (!isAdded) {
                                    ingredientViewModel.insertIngredient(it.toEntity())
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}



@Composable
fun IngredientResultItem(
    ingredient: Ingredient,
    isAdded: Boolean,
    onAddClick: (Ingredient) -> Unit
) {
    val IngredientResultImage = "https://img.spoonacular.com/ingredients_250x250/${ingredient.image}"

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Gambar bahan
            Image(
                painter = rememberAsyncImagePainter(model = IngredientResultImage),
                contentDescription = ingredient.name,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Nama bahan
            Text(
                text = ingredient.name,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )

            // Tombol Add
            if (isAdded) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA6E9C2)),
                    shape = RoundedCornerShape(50),
                    enabled = false
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Added", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Added", color = Color.White)
                }
            } else {
                Button(
                    onClick = { onAddClick(ingredient) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = Color.White)
                }
            }
        }
    }
}
