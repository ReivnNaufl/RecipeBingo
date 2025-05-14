package com.unluckygbs.recipebingo

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.unluckygbs.recipebingo.ui.screen.profile.EditProfileScreen
import com.unluckygbs.recipebingo.ui.screen.recipe.RecipeBookmarkScreen
import com.unluckygbs.recipebingo.ui.screen.recipe.RecipeDetailScreen
import com.unluckygbs.recipebingo.ui.screen.recipe.SearchRecipeScreen
import com.unluckygbs.recipebingo.ui.screen.start.StartScreen
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
        factory = RecipeViewModelFactory(recipeRepository, ingredientRepository)
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
        composable("bookmarkedrecipe"){
            RecipeBookmarkScreen(modifier,navController,authViewModel,recipeViewModel)
        }
        composable(
            route = "detailedrecipe/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt("recipeId") ?: 0 // Nilai default jika diperlukan
            RecipeDetailScreen(
                recipeId = recipeId,
                recipeViewModel = recipeViewModel,
                onBackClick = { navController.popBackStack() },
                nutritionTrackerViewModel = nutritionTrackerViewModel,
                context = context
            )
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, authViewModel = authViewModel)
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
        NavItem("Track", Icons.Default.Check),
        NavItem("Profile", Icons.Default.AccountCircle)
    )

    val selectedIndex by mainViewModel.selectedIndex

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF5F5F5), // Latar belakang abu-abu muda
                contentColor = Color.Black,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
                    )
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { mainViewModel.setSelectedIndex(index) },
                        icon = {
                            if (selectedIndex == index) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFF00C853),
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = navItem.icon,
                                            contentDescription = navItem.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp) // Ikon lebih besar untuk item yang dipilih
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = navItem.label,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            modifier = Modifier.wrapContentWidth()
                                        )
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp) // Ikon lebih kecil untuk item yang tidak dipilih
                                )
                            }
                        },
                        label = { /* Kosongkan label bawaan, kita atur di icon */ },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00C853),
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Color(0xFF00C853),
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(if (selectedIndex == index) 1.5f else 1f)
                            .widthIn(min = if (selectedIndex == index) 120.dp else 40.dp) // Tingkatkan lebar minimum untuk item yang dipilih
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            authViewModel = authViewModel,
            selectedIndex = selectedIndex,
            ingredientViewModel = ingredientViewModel,
            recipeViewModel = recipeViewModel,
            nutritionTrackerViewModel = nutritionTrackerViewModel
        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    selectedIndex: Int,
    ingredientViewModel: IngredientViewModel,
    recipeViewModel: RecipeViewModel,
    nutritionTrackerViewModel: NutritionTrackerViewModel
) {
    when (selectedIndex) {
        0 -> HomeScreen(modifier, navController, authViewModel, recipeViewModel, nutritionTrackerViewModel, ingredientViewModel)
        1 -> SearchRecipeScreen(modifier, navController, authViewModel, recipeViewModel)
        2 -> IngredientScreen(modifier, navController, authViewModel, ingredientViewModel)
        3 -> NutritionTrackerScreen(modifier, navController, authViewModel, nutritionTrackerViewModel)
        4 -> ProfileScreen(modifier, navController, authViewModel)
    }
}

