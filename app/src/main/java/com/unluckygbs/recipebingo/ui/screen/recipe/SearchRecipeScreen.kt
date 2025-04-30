package com.unluckygbs.recipebingo.ui.screen.recipe

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.unluckygbs.recipebingo.data.dataclass.Recipe
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRecipeScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, recipeViewModel: RecipeViewModel) {

    val authState = authViewModel.authState.observeAsState()
    val recipe by recipeViewModel.recipe.observeAsState(emptyList())
    val isLoading by recipeViewModel.loading.observeAsState(false)
    val errorMessage by recipeViewModel.errorMessage.observeAsState()
    val recommended by recipeViewModel.recommendedRecipes.observeAsState(emptyList())

    var searchQuery by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val openBottomSheet = remember { mutableStateOf(false) }



    val _selectedFilters = remember { mutableStateOf<Set<String>>(setOf("without")) }
    val selectedFilters = _selectedFilters.value

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

    fun onFilterClick(filter: String) {
        val current = _selectedFilters.value.toMutableSet()
        if (filter == "with" || filter == "without") {
            current.remove("with")
            current.remove("without")
            current.add(filter)
        } else {
            if (current.contains(filter)) {
                current.remove(filter)
            } else {
                filterPairs[filter]?.let { pair -> current.remove(pair) }
                current.add(filter)
            }
        }
        _selectedFilters.value = current
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
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
                    Text("Search...",
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        if (searchQuery.isNotBlank()) {
                            recipeViewModel.fetchRecipe(searchQuery)
                        }
                    }, modifier = Modifier
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
                            RecipeResultItem(recipe)
                            {
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
                            RecipeResultItem(recipe)
                            {
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
        if (openBottomSheet.value){
            ModalBottomSheet(
                onDismissRequest = { openBottomSheet.value = false },
                sheetState = sheetState
            ){
                RecipeRecommendationBottomSheet(
                    selectedFilters = selectedFilters,
                    onFilterClick = ::onFilterClick,
                    onRecommendClick = {
                        openBottomSheet.value = false

                        // Inisialisasi map filter
                        val nutrientParams = mutableMapOf<String, Int>()

                        if ("Min Calorie" in selectedFilters) nutrientParams["minCalories"] = 10
                        if ("Max Calorie" in selectedFilters) nutrientParams["maxCalories"] = 100
                        if ("Min Protein" in selectedFilters) nutrientParams["minProtein"] = 10
                        if ("Max Protein" in selectedFilters) nutrientParams["maxProtein"] = 100
                        if ("Min Sugar" in selectedFilters) nutrientParams["minSugar"] = 10
                        if ("Max Sugar" in selectedFilters) nutrientParams["maxSugar"] = 100
                        if ("Min Fat" in selectedFilters) nutrientParams["minFat"] = 10
                        if ("Max Fat" in selectedFilters) nutrientParams["maxFat"] = 100

                        if ("with" in selectedFilters) {
                            recipeViewModel.fetchRecipeByAvailableIngredientswithNutrition(nutrientParams)
                        } else if (nutrientParams.isNotEmpty()) {
                            recipeViewModel.fetchRecipeByNutritionOnly(nutrientParams)
                        } else {
                            recipeViewModel.fetchRandomRecipes()
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun RecipeResultItem(
    recipe: Recipe,
    onClick: () -> Unit // tambahkan parameter onClick
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeRecommendationBottomSheet(
    selectedFilters: Set<String>,
    onFilterClick: (String) -> Unit,
    onRecommendClick: () -> Unit
) {
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
                    onClick = { onFilterClick(filter) }
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
                    onClick = { onFilterClick(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Button: Recommend me a recipe
        Button(
            onClick = onRecommendClick,
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
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = if (selected) {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFF00C853),
                contentColor = Color.White
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            )
        },
        border = ButtonDefaults.outlinedButtonBorder,
        modifier = Modifier.height(36.dp)
    ) {
        Text(text = text, fontSize = 12.sp)
    }
}