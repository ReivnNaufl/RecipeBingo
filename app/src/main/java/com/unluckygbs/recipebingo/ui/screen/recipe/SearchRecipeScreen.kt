package com.unluckygbs.recipebingo.ui.screen.recipe

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unluckygbs.recipebingo.R
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    val recipe by recipeViewModel.recipe.observeAsState(emptyList())
    val isLoading by recipeViewModel.loading.observeAsState(false)
    val errorMessage by recipeViewModel.errorMessage.observeAsState()
    val recommended by recipeViewModel.recommendedRecipes.observeAsState(emptyList())

    var searchQuery by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openBottomSheet = remember { mutableStateOf(false) }
    val context = LocalContext.current

    val _selectedFilters = remember { mutableStateOf<Set<String>>(setOf("without")) }
    val selectedFilters = _selectedFilters.value
    val _filterValues = remember { mutableStateOf<Map<String, String>>(emptyMap()) } // Track filter values

    val filterPairs = mapOf(
        "Min Calorie" to "Max Calorie",
        "Max Calorie" to "Min Calorie",
        "Min Protein" to "Max Protein",
        "Max Protein" to "Min Protein",
        "Min Sugar" to "Max Sugar",
        "Max Sugar" to "Min Sugar",
        "Min Fat" to "Max Fat",
        "Max Fat" to "Min Fat"
    )
    val allFilters = listOf(
        "with",
        "without",
        "Min Calorie",
        "Max Calorie",
        "Min Protein",
        "Max Protein",
        "Min Sugar",
        "Max Sugar",
        "Min Fat",
        "Max Fat"
    )

    fun onFilterClick(filter: String) {
        val current = _selectedFilters.value.toMutableSet()
        if (filter == "with" || filter == "without") {
            current.remove("with")
            current.remove("without")
            if (filter == "with") {
                // Clear all nutrient filters and their values when "with" is selected
                current.removeAll(allFilters.filter { it != "with" && it != "without" })
                _filterValues.value = emptyMap()
            }
            current.add(filter)
        } else {
            if ("with" in current) return
            if (current.contains(filter)) {
                current.remove(filter)
                _filterValues.value = _filterValues.value.toMutableMap().apply {
                    remove(filter)
                }
            } else {
                current.add(filter)
            }
        }
        _selectedFilters.value = current
    }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.unAuthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        recipeViewModel.resetState()
        searchQuery = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Recipe") },
                actions = {
                    IconButton(onClick = { navController.navigate("bookmarkedrecipe") }) {
                        Icon(
                            painter = painterResource(R.drawable.material_symbols_bookmark_outline),
                            contentDescription = "Bookmark"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                placeholder = {
                    Text(
                        "Search...",
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                recipeViewModel.fetchRecipe(searchQuery)
                            }
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            recipeViewModel.fetchRecipe(searchQuery)
                            keyboardController?.hide()
                        }
                    }
                ),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFA8F0B8),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(50),
                textStyle = TextStyle(textIndent = TextIndent(firstLine = 17.sp)),
                modifier = Modifier
                    .width(330.dp)
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (recipe.isEmpty()) {
                Text("Or get a recommendation 👇", color = Color.Gray)
                if (recommended.isEmpty()) {
                    RecommendationButton(onClick = {
                        openBottomSheet.value = true
                    })
                }

                if (recommended.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommended) { recipe ->
                            RecipeResultItem(recipe) {
                                navController.navigate("detailedrecipe/${recipe.id}")
                            }
                        }

                        item(span = { GridItemSpan(3) }) {
                            Button(
                                onClick = { openBottomSheet.value = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D45B)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                Text("Try another recommendation", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items = recipe) { recipe ->
                            RecipeResultItem(recipe) {
                                navController.navigate("detailedrecipe/${recipe.id}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Or get a recommendation 👇", color = Color.Gray)
                    RecommendationButton(
                        onClick = { openBottomSheet.value = true },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
        if (openBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = { openBottomSheet.value = false },
                sheetState = sheetState
            ) {
                RecipeRecommendationBottomSheets(
                    selectedFilters = selectedFilters,
                    filterValues = _filterValues.value,
                    onFilterClick = ::onFilterClick,
                    onRecommendClick = { filterValues ->
                        openBottomSheet.value = false

                        // Initialize map for nutrient parameters
                        val nutrientParams = mutableMapOf<String, Int>()

                        if ("with" in selectedFilters) {
                            // Jika "with" dipilih, abaikan filterValues
                            if (selectedFilters.any { it != "with" && it != "without" }) {
                                Toast.makeText(
                                    context,
                                    "You cannot combine ingredient-based and nutrient-based filters.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                recipeViewModel.fetchRecipeByAvailableIngredients()
                            }
                        } else {
                            // Map filter names to API parameters using user-entered values
                            filterValues.forEach { (filter, value) ->
                                when (filter) {
                                    "Min Calorie" -> nutrientParams["minCalories"] = value.toIntOrNull() ?: 10
                                    "Max Calorie" -> nutrientParams["maxCalories"] = value.toIntOrNull() ?: 100
                                    "Min Protein" -> nutrientParams["minProtein"] = value.toIntOrNull() ?: 10
                                    "Max Protein" -> nutrientParams["maxProtein"] = value.toIntOrNull() ?: 100
                                    "Min Sugar" -> nutrientParams["minSugar"] = value.toIntOrNull() ?: 10
                                    "Max Sugar" -> nutrientParams["maxSugar"] = value.toIntOrNull() ?: 100
                                    "Min Fat" -> nutrientParams["minFat"] = value.toIntOrNull() ?: 10
                                    "Max Fat" -> nutrientParams["maxFat"] = value.toIntOrNull() ?: 100
                                }
                            }
                            if (nutrientParams.isNotEmpty()) {
                                recipeViewModel.fetchRecipeByNutrition(nutrientParams)
                            } else {
                                recipeViewModel.fetchRandomRecipes()
                            }
                        }
                    },
                    context = context
                )
            }
        }
    }
}

@Composable
fun RecipeResultItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF1F1F1))
            .padding(8.dp)
    ) {
        AsyncImage(
            model = recipe.image,
            contentDescription = recipe.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = recipe.title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RecommendationButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D45B)),
        shape = RoundedCornerShape(50),
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Text("Recommend me a recipe", color = Color.White)
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeRecommendationBottomSheets(
    selectedFilters: Set<String>,
    filterValues: Map<String, String>,
    onFilterClick: (String) -> Unit,
    onRecommendClick: (Map<String, String>) -> Unit,
    context: Context
) {
    // State to hold input values for each filter
    val localFilterValues = remember { mutableStateOf(filterValues) }
    // State to control dialog visibility and current filter being edited
    var showDialog by remember { mutableStateOf(false) }
    var currentFilter by remember { mutableStateOf<String?>(null) }
    var dialogInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Update localFilterValues when filterValues changes
    LaunchedEffect(filterValues) {
        localFilterValues.value = filterValues
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Drag Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .padding(bottom = 8.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(2.dp))
        )

        Text(
            text = "Recipe Recommendation",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Based on available ingredients
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isWithSelected = selectedFilters.contains("with")
                FilterChip(
                    text = "Based on available Ingredients",
                    selected = isWithSelected,
                    value = null,
                    onClick = {
                        onFilterClick(if (isWithSelected) "without" else "with")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Min Filters
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Min Calorie", "Min Protein", "Min Sugar", "Min Fat").forEach { filter ->
                FilterChip(
                    text = filter,
                    selected = selectedFilters.contains(filter),
                    value = localFilterValues.value[filter],
                    onClick = {
                        if (!selectedFilters.contains("with")) {
                            if (selectedFilters.contains(filter)) {
                                onFilterClick(filter) // Deselect if already selected
                            } else {
                                currentFilter = filter
                                dialogInput = localFilterValues.value[filter] ?: ""
                                showDialog = true
                            }
                        }
                    },
                    enabled = !selectedFilters.contains("with")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Max Filters
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Max Calorie", "Max Protein", "Max Sugar", "Max Fat").forEach { filter ->
                FilterChip(
                    text = filter,
                    selected = selectedFilters.contains(filter),
                    value = localFilterValues.value[filter],
                    onClick = {
                        if (!selectedFilters.contains("with")) {
                            if (selectedFilters.contains(filter)) {
                                onFilterClick(filter) // Deselect if already selected
                            } else {
                                currentFilter = filter
                                dialogInput = localFilterValues.value[filter] ?: ""
                                showDialog = true
                            }
                        }
                    },
                    enabled = !selectedFilters.contains("with")
                )
            }
        }

        // Dialog for entering filter value
        if (showDialog && currentFilter != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    dialogInput = ""
                    currentFilter = null
                },
                title = { Text("Enter $currentFilter") },
                text = {
                    OutlinedTextField(
                        value = dialogInput,
                        onValueChange = { value ->
                            if (value.all { it.isDigit() } || value.isEmpty()) {
                                dialogInput = value
                            }
                        },
                        label = { Text("Value") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }
                        ),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFFF1F1F1),
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (dialogInput.isNotBlank()) {
                                localFilterValues.value = localFilterValues.value.toMutableMap().apply {
                                    put(currentFilter!!, dialogInput)
                                }
                                onFilterClick(currentFilter!!) // Select the filter
                                showDialog = false
                                dialogInput = ""
                                currentFilter = null
                                keyboardController?.hide()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter a value.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            dialogInput = ""
                            currentFilter = null
                            keyboardController?.hide()
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Button: Recommend me a recipe
        Button(
            onClick = {
                // Validate inputs
                if (selectedFilters.contains("with")) {
                    // Jika "with" dipilih, abaikan semua filter min/max
                    if (selectedFilters.any { it != "with" && it != "without" }) {
                        Toast.makeText(
                            context,
                            "Cannot combine 'Based on available Ingredients' with nutrient filters.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onRecommendClick(emptyMap()) // Kirim map kosong untuk mengabaikan filter min/max
                    }
                } else {
                    // Validasi untuk filter min/max
                    val invalidInputs = selectedFilters.filter { it != "with" && it != "without" }
                        .any { localFilterValues.value[it].isNullOrBlank() }
                    if (invalidInputs) {
                        Toast.makeText(
                            context,
                            "Please enter values for all selected filters.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        onRecommendClick(localFilterValues.value)
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(50.dp)
        ) {
            Text(text = "Recommend me a recipe", color = Color.White, fontSize = 16.sp)
        }
    }
}
@Composable
fun FilterChip(
    text: String,
    selected: Boolean,
    value: String?,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = if (selected && enabled) {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (enabled) Color.Black else Color.Gray
            )
        },
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier.height(36.dp),
        enabled = enabled
    ) {
        Text(
            text = if (value != null && value.isNotBlank()) "$text: $value" else text,
            fontSize = 12.sp
        )
    }
}