package com.unluckygbs.recipebingo

import android.content.Context
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.unluckygbs.recipebingo.data.database.AppDatabase
import com.unluckygbs.recipebingo.data.repository.DailyEatsRepository
import com.unluckygbs.recipebingo.data.repository.RecipeRepository
import com.unluckygbs.recipebingo.repository.IngredientRepository
import com.unluckygbs.recipebingo.ui.screen.ingredient.SearchIngredientScreen
import com.unluckygbs.recipebingo.ui.screen.home.HomeScreen
import com.unluckygbs.recipebingo.ui.screen.ingredient.IngredientScreen
import com.unluckygbs.recipebingo.ui.screen.auth.LoginScreen
import com.unluckygbs.recipebingo.ui.screen.tracker.NutritionTrackerScreen
import com.unluckygbs.recipebingo.ui.screen.profile.ProfileScreen
import com.unluckygbs.recipebingo.ui.screen.auth.RegisterScreen
import com.unluckygbs.recipebingo.ui.screen.onboarding.OnboardingScreen
import com.unluckygbs.recipebingo.ui.screen.recipe.RecipeDetailScreen
import com.unluckygbs.recipebingo.ui.screen.recipe.SearchRecipeScreen
import com.unluckygbs.recipebingo.ui.screen.start.StartScreen
import com.unluckygbs.recipebingo.util.DataStoreManager
import com.unluckygbs.recipebingo.viewmodel.auth.AuthState
import com.unluckygbs.recipebingo.viewmodel.auth.AuthViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModel
import com.unluckygbs.recipebingo.viewmodel.ingredient.IngredientViewModelFactory
import com.unluckygbs.recipebingo.viewmodel.main.MainViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModel
import com.unluckygbs.recipebingo.viewmodel.recipe.RecipeViewModelFactory
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModel
import com.unluckygbs.recipebingo.viewmodel.tracker.NutritionTrackerViewModelFactory

@Composable
fun Main(modifier: Modifier = Modifier, authViewModel: AuthViewModel,context: Context, startDestination: String, onOnboardingFinished: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val ingredientRepository = IngredientRepository(
        AppDatabase.getDatabase(context).ingredientDao(),
        firestore = firestore,
        userId = authViewModel.getCurrentUserUid() ?: ""
    )
    val dailyEatsRepository = DailyEatsRepository(
        dao = AppDatabase.getDatabase(context).dailyEatsDao(),
        firestore = firestore,
        userId = authViewModel.getCurrentUserUid() ?: ""
    )
    val recipeRepository = RecipeRepository(
        dao = AppDatabase.getDatabase(context).recipeDao(),
        firestore = firestore,
        userId = authViewModel.getCurrentUserUid() ?: ""
    )

    val navController = rememberNavController()
    val ingredientViewModel: IngredientViewModel = viewModel(
        factory = IngredientViewModelFactory(ingredientRepository)
    )
    val recipeViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModelFactory(recipeRepository)
    )

    val nutritionTrackerViewModel: NutritionTrackerViewModel = viewModel(
        factory = NutritionTrackerViewModelFactory(dailyEatsRepository)
    )

    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        ingredientRepository.syncFromFirestoreToRoom()
    }

    NavHost(navController = navController, startDestination = startDestination, builder = {
        composable("Start"){
            StartScreen(modifier,navController,authViewModel,ingredientViewModel)
        }
        composable("onboarding") {
            OnboardingScreen(navController = navController, onFinish = {
                onOnboardingFinished()
                navController.navigate("Start") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("login"){
            LoginScreen(modifier,navController,authViewModel,ingredientViewModel)
        }
        composable("register"){
            RegisterScreen(modifier,navController,authViewModel)
        }
        composable("home"){
            App(modifier,navController,authViewModel,ingredientViewModel, recipeViewModel, nutritionTrackerViewModel = nutritionTrackerViewModel)
        }
        composable("searchingredient") {
            SearchIngredientScreen(modifier,navController,authViewModel,ingredientViewModel)
        }
        composable("detailedrecipe/{recipeId}") { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()


            LaunchedEffect(recipeId) {
                if (recipeId != null) {
                    recipeViewModel.getRecipeById(recipeId)
                }
            }

            val recipe by recipeViewModel.recipeById.collectAsState()


            RecipeDetailScreen(
                recipeById = recipe,
                onBackClick = { navController.popBackStack() },
                nutritionTrackerViewModel = nutritionTrackerViewModel,
                recipeViewModel = recipeViewModel,
                context = context
            )
        }
    })
}

@Composable
fun App(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    ingredientViewModel: IngredientViewModel,
    recipeViewModel: RecipeViewModel,
    mainViewModel: MainViewModel = viewModel(),
    nutritionTrackerViewModel: NutritionTrackerViewModel
) {

    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Search", Icons.Default.Search),
        NavItem("Stock", Icons.Default.Add),
        NavItem("Track",Icons.Default.Check),
        NavItem("Profile", Icons.Default.AccountCircle)
    )

    val selectedIndex by mainViewModel.selectedIndex


    Scaffold (
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed{index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { mainViewModel.setSelectedIndex(index) },
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
        ContentScreen(modifier = Modifier.padding(innerPadding), navController = navController, authViewModel = authViewModel,selectedIndex, ingredientViewModel = ingredientViewModel, recipeViewModel = recipeViewModel, nutritionTrackerViewModel = nutritionTrackerViewModel)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, selectedIndex : Int,ingredientViewModel: IngredientViewModel, recipeViewModel: RecipeViewModel, nutritionTrackerViewModel: NutritionTrackerViewModel) {
    when(selectedIndex){
        0 -> HomeScreen(modifier,navController,authViewModel)
        1 -> SearchRecipeScreen(modifier,navController,authViewModel,recipeViewModel)
        2 -> IngredientScreen(modifier,navController,authViewModel,ingredientViewModel)
        3 -> NutritionTrackerScreen(modifier,navController,authViewModel,nutritionTrackerViewModel)
        4 -> ProfileScreen(modifier,navController,authViewModel)
    }
}