package com.unluckygbs.recipebingo

import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unluckygbs.recipebingo.screen.HomeScreen
import com.unluckygbs.recipebingo.screen.IngredientScreen
import com.unluckygbs.recipebingo.screen.LoginScreen
import com.unluckygbs.recipebingo.screen.NutritionTrackerScreen
import com.unluckygbs.recipebingo.screen.ProfileScreen
import com.unluckygbs.recipebingo.screen.RegisterScreen
import com.unluckygbs.recipebingo.screen.SearchRecipeScreen

@Composable
fun Main(modifier: Modifier = Modifier, authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginScreen(modifier,navController,authViewModel)
        }
        composable("register"){
            RegisterScreen(modifier,navController,authViewModel)
        }
        composable("home"){
            App(modifier,navController,authViewModel)
        }
    })
}

@Composable
fun App(modifier: Modifier = Modifier,navController: NavController, authViewModel: AuthViewModel) {

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
        ContentScreen(modifier = Modifier.padding(innerPadding), navController = navController, authViewModel = authViewModel,selectedIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier,navController: NavController, authViewModel: AuthViewModel,selectedIndex : Int) {
    when(selectedIndex){
        0 -> HomeScreen(modifier,navController,authViewModel)
        1 -> SearchRecipeScreen(modifier,navController,authViewModel)
        2 -> IngredientScreen(modifier,navController,authViewModel)
        3 -> NutritionTrackerScreen(modifier,navController,authViewModel)
        4 -> ProfileScreen(modifier,navController,authViewModel)
    }
}