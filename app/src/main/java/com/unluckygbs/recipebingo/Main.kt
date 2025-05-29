package com.unluckygbs.recipebingo


import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
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
import com.unluckygbs.recipebingo.ui.screen.auth.OTPAuthScreen
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
import com.unluckygbs.recipebingo.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun Main(modifier: Modifier = Modifier, authViewModel: AuthViewModel,context: Context, startDestination: String, onOnboardingFinished: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val ingredientRepository = IngredientRepository(
        AppDatabase.getDatabase(context).ingredientDao(),
        firestore = firestore,
        userId = authViewModel.getCurrentUserUid() ?: ""
    )
    val dailyEatsRepository = DailyEatsRepository(
        dailyEatsDao = AppDatabase.getDatabase(context).dailyEatsDao(),
        recipeDao = AppDatabase.getDatabase(context).recipeDao(),
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
        recipeRepository.syncBookmarkedFromFirestore()
        dailyEatsRepository.syncDailyEatsFromFirestore()
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
        composable("otpAuth") {
            OTPAuthScreen(navController = navController, authViewModel = authViewModel)
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
                context = context,
                ingredientViewModel = ingredientViewModel
            )
        }
        composable("edit_profile") {
            EditProfileScreen(navController = navController, authViewModel = authViewModel)
        }

    })
}

@OptIn(ExperimentalFoundationApi::class)
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
        NavItem("Home", NavIcon.Drawable(R.drawable.home)),
        NavItem("Search", NavIcon.Drawable(R.drawable.search)),
        NavItem("Stock", NavIcon.Drawable(R.drawable.stock)),
        NavItem("Track", NavIcon.Drawable(R.drawable.dailyeats)),
        NavItem("Profile", NavIcon.Drawable(R.drawable.profile))
    )

    val selectedIndex by mainViewModel.selectedIndex
    val pagerState = rememberPagerState(
        initialPage = selectedIndex,
        pageCount = { navItemList.size }
    )

    val scope = rememberCoroutineScope()

    // Sinkronisasi swipe -> ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest {
            if (mainViewModel.selectedIndex.value != it) {
                mainViewModel.setSelectedIndex(it)
            }
        }
    }

    // Sinkronisasi ViewModel -> swipe
    LaunchedEffect(selectedIndex) {
        if (pagerState.currentPage != selectedIndex) {
            pagerState.animateScrollToPage(selectedIndex)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFF5F5F5),
                contentColor = Color.Black
            ) {
                navItemList.forEachIndexed { index, navItem ->
                    val isSelected = selectedIndex == index

                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF00C853) else Color.Transparent,
                        animationSpec = tween(durationMillis = 300)
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = tween(durationMillis = 300)
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            mainViewModel.setSelectedIndex(index)
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer { scaleX = scale; scaleY = scale }
                                    .background(backgroundColor, shape = CircleShape)
                                    .padding(horizontal = if (isSelected) 12.dp else 0.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    NavIconView(navItem.icon, navItem.label, if (isSelected) 24.dp else 18.dp, if (isSelected) Color.White else Color.Gray)
                                    AnimatedVisibility(visible = isSelected) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = navItem.label,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        },
                        label = {},
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(if (isSelected) 1.5f else 1f)
                            .widthIn(min = if (isSelected) 100.dp else 40.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { page ->
            ContentScreen(
                selectedIndex = page,
                navController = navController,
                authViewModel = authViewModel,
                ingredientViewModel = ingredientViewModel,
                recipeViewModel = recipeViewModel,
                nutritionTrackerViewModel = nutritionTrackerViewModel
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedTabContent(
    selectedIndex: Int,
    navController: NavController,
    authViewModel: AuthViewModel,
    ingredientViewModel: IngredientViewModel,
    recipeViewModel: RecipeViewModel,
    nutritionTrackerViewModel: NutritionTrackerViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = selectedIndex,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally { fullWidth -> fullWidth } + fadeIn() with
                        slideOutHorizontally { fullWidth -> -fullWidth } + fadeOut()
            } else {
                slideInHorizontally { fullWidth -> -fullWidth } + fadeIn() with
                        slideOutHorizontally { fullWidth -> fullWidth } + fadeOut()
            }
        },
        label = "TabContentTransition",
        modifier = modifier
    ) { targetIndex ->
        ContentScreen(
            selectedIndex = targetIndex,
            navController = navController,
            authViewModel = authViewModel,
            ingredientViewModel = ingredientViewModel,
            recipeViewModel = recipeViewModel,
            nutritionTrackerViewModel = nutritionTrackerViewModel
        )
    }
}



@Composable
fun NavIconView(icon: NavIcon, contentDescription: String, size: Dp, tint: Color) {
    when (icon) {
        is NavIcon.Vector -> Icon(
            imageVector = icon.image,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
        is NavIcon.Drawable -> Icon(
            painter = painterResource(id = icon.resId),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
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
        4 -> ProfileScreen(modifier, navController, authViewModel, recipeViewModel, ingredientViewModel, nutritionTrackerViewModel)
    }
}

