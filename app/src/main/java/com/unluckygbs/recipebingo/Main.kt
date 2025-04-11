package com.unluckygbs.recipebingo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.screen.ingredient.SearchIngredientScreen
import com.unluckygbs.recipebingo.screen.home.HomeScreen
import com.unluckygbs.recipebingo.screen.ingredient.IngredientScreen
import com.unluckygbs.recipebingo.screen.auth.LoginScreen
import com.unluckygbs.recipebingo.screen.tracker.NutritionTrackerScreen
import com.unluckygbs.recipebingo.screen.profile.ProfileScreen
import com.unluckygbs.recipebingo.screen.auth.RegisterScreen
import com.unluckygbs.recipebingo.screen.recipe.SearchRecipeScreen
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModelFactory
import com.unluckygbs.recipebingo.viewmodel.ingredient.RecipeViewModel

@Composable
fun Main(modifier: Modifier = Modifier, authViewModel: AuthViewModel,ingredientRepository: IngredientRepository) {
    val navController = rememberNavController()
    val ingredientViewModel: IngredientViewModel = viewModel(
        factory = IngredientViewModelFactory(ingredientRepository)
    )
    val recipeViewModel: RecipeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginScreen(modifier,navController,authViewModel,ingredientViewModel)
        }
        composable("register"){
            RegisterScreen(modifier,navController,authViewModel)
        }
        composable("home"){
            App(modifier,navController,authViewModel,ingredientViewModel, recipeViewModel)
        }
        composable("searchingredient") {
            SearchIngredientScreen(modifier,navController,authViewModel,ingredientViewModel)
        }
    })
}

@Composable
fun App(modifier: Modifier = Modifier,navController: NavController, authViewModel: AuthViewModel,ingredientViewModel: IngredientViewModel, recipeViewModel: RecipeViewModel) {

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Search", Icons.Default.Search),
        NavItem("Stock", Icons.Default.Add),
        NavItem("Track",Icons.Default.Check),
        NavItem("Profile", Icons.Default.AccountCircle)
    )

    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed{index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = {
                            Icon(imageVector = navItem.icon, "icon")
                               },
                        label = {
                            Text(text = navItem.label)
                        }
                    )

                }
            }
        }
        ) { innerPadding ->
        ContentScreen(modifier = Modifier.padding(innerPadding), navController = navController, authViewModel = authViewModel,selectedIndex, ingredientViewModel = ingredientViewModel, recipeViewModel = recipeViewModel)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, selectedIndex : Int,ingredientViewModel: IngredientViewModel, recipeViewModel: RecipeViewModel) {
    when(selectedIndex){
        0 -> HomeScreen(modifier,navController,authViewModel)
        1 -> SearchRecipeScreen(modifier,navController,authViewModel,recipeViewModel)
        2 -> IngredientScreen(modifier,navController,authViewModel,ingredientViewModel)
        3 -> NutritionTrackerScreen(modifier,navController,authViewModel)
        4 -> ProfileScreen(modifier,navController,authViewModel)
    }
}