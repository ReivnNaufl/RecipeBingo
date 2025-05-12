package com.unluckygbs.recipebingo.ui.screen.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.unluckygbs.recipebingo.data.entity.IngredientEntity
import com.unluckygbs.recipebingo.ui.screen.ingredient.AvailableIngredientsScreen
import com.unluckygbs.recipebingo.ui.screen.ingredient.IngredientItem
import com.unluckygbs.recipebingo.ui.screen.ingredient.SwipeToDeleteIngredientItem
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeBookmarkScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    recipeViewModel: RecipeViewModel,
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
        BookmarkedRecipe(navController,recipeViewModel)

        TextButton(onClick = {
            authViewModel.signout(context)
        }) {
            Text(text = "Log Out")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkedRecipe(
    navController: NavController,
    recipeViewModel: RecipeViewModel
) {
    val bookmarkedRecipes by recipeViewModel.bookmarkedRecipes.collectAsState()
    val loading by recipeViewModel.loading.observeAsState(false)
    val errorMessage by recipeViewModel.errorMessage.observeAsState()

    LaunchedEffect(Unit) {
        recipeViewModel.getAllBookmarkedRecipes()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarked Recipe") },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle profile click */ }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (bookmarkedRecipes.isEmpty()) {
                Text("No Recipe Bookmarked.", color = Color.Gray)
            } else {
                bookmarkedRecipes.forEach { recipe ->
                    SwipeToDeleteBookmarkedItem(
                        name = recipe.title,
                        image = recipe.image,
                        onDeleteConfirmed = {
                            recipeViewModel.removeBookmark(recipe)
                        },
                        onClick = {
                            navController.navigate("detailedrecipe/${recipe.id}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeToDeleteBookmarkedItem(
    name: String,
    image: String,
    onDeleteConfirmed: () -> Unit,
    onClick: () -> Unit
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
            title = { Text("Remove Bookmark") }, // Ubah teks untuk konteks bookmark
            text = { Text("Are you sure you want to remove \"$name\" from bookmarks?") },
            confirmButton = {
                TextButton(onClick = {
                    openDialog.value = false
                    onDeleteConfirmed()
                }) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    androidx.compose.material.SwipeToDismiss(
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
                BookmarkedItem(name = name, imageUrl = image)
            }
        }
    )
}

@Composable
fun BookmarkedItem(name: String, imageUrl: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp) // Padding sama dengan Box
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
        }
    }
}