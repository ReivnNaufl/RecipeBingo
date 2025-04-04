package com.unluckygbs.recipebingo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unluckygbs.recipebingo.screen.HomeScreen
import com.unluckygbs.recipebingo.screen.LoginScreen
import com.unluckygbs.recipebingo.screen.RegisterScreen

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
            HomeScreen(modifier,navController,authViewModel)
        }
    })
}