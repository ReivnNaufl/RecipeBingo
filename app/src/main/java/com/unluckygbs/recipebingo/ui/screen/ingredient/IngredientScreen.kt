package com.unluckygbs.recipebingo.ui.screen.ingredient

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.unluckygbs.recipebingo.data.dataclass.Ingredient
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.unluckygbs.recipebingo.R


@Composable
fun IngredientScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
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
        AvailableIngredientsScreen(navController, ingredientViewModel)

        TextButton(onClick = {
            authViewModel.signout(context)
        }) {
            Text(text = "Log Out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableIngredientsScreen(
    navController: NavController,
    ingredientViewModel: IngredientViewModel
) {
    val ingredients by ingredientViewModel.availableIngredients.observeAsState(emptyList())

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var editingIngredient by remember { mutableStateOf<IngredientEntity?>(null) }
    var quantity by remember { mutableStateOf(0.0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Ingredients") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("searchingredient") },
                containerColor = Color(0xFF4CAF50)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (ingredients.isEmpty()) {
                item {
                    Text("No ingredients found.", color = Color.Gray)
                }
            } else {
                items(ingredients) { ingredient ->
                    SwipeToDeleteIngredientItem(
                        name = ingredient.name,
                        quantity = "${ingredient.quantity} ${ingredient.unit}",
                        image = ingredient.image,
                        onDeleteConfirmed = {
                            ingredientViewModel.deleteIngredient(ingredient)
                        },
                        onClick = {
                            editingIngredient = ingredient
                            quantity = ingredient.quantity
                            scope.launch { sheetState.show() }
                        }
                    )
                }
            }
        }

        if (editingIngredient != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    editingIngredient = null
                    scope.launch { sheetState.hide() }
                },
                sheetState = sheetState
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Edit ${editingIngredient?.name} Quantity", fontSize = 18.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = { if (quantity > 0) quantity-- }) {
                            Icon(painter = painterResource(R.drawable.ic_baseline_minus), contentDescription = "Decrease", tint = Color(0xFF4CAF50))
                        }
                        OutlinedTextField(
                            value = quantity.toString(),
                            onValueChange = {
                                quantity = it.toDoubleOrNull() ?: 0.0
                            },
                            label = { Text("Quantity") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.width(100.dp)
                        )

                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFF4CAF50))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            editingIngredient?.let {
                                ingredientViewModel.updateIngredient(it.copy(quantity = quantity))
                                scope.launch { sheetState.hide() }
                                editingIngredient = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Edit")
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientItem(name: String, quantity: String, image: String) {
    val imageUrl = "https://img.spoonacular.com/ingredients_250x250/$image"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp
            )

            Text(
                text = quantity,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteIngredientItem(
    name: String,
    quantity: String,
    image: String,
    onDeleteConfirmed: () -> Unit,
    onClick: () -> Unit // Tambahkan ini
) {
    val dismissState = rememberDismissState()
    val openDialog = remember { mutableStateOf(false) }

    val dismissed = dismissState.isDismissed(DismissDirection.EndToStart)
    if (dismissed) {
        openDialog.value = true
        LaunchedEffect(dismissed) {
            dismissState.reset()
        }
    }

    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
            title = { Text("Delete Ingredient") },
            text = { Text("Are you sure you want to delete \"$name\"?") },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    onDeleteConfirmed()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp) // Tinggi minimum berdasarkan Card
                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissContent = {
            Box(modifier = Modifier.clickable { onClick() }) {
                IngredientItem(name = name, quantity = quantity, image = image)
            }
        }
    )
}
