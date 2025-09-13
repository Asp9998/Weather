package com.example.weather.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weather.screens.about.AboutScreen
import com.example.weather.screens.main.MainScreen
import com.example.weather.screens.main.MainViewModel
import com.example.weather.screens.search.SearchScreen
import com.example.weather.screens.settings.SettingsScreen

@Composable
fun WeatherNavigation() {
    val navController = rememberNavController()

    NavHost(navController,
        startDestination = WeatherScreens.MainScreen.name

    ){

        composable(WeatherScreens.MainScreen.name){
            val mainViewModel = hiltViewModel<MainViewModel>()

            MainScreen(
                navController = navController,
                mainViewModel = mainViewModel,
            )
        }

        composable(WeatherScreens.SearchScreen.name){
            SearchScreen(navController = navController)
        }

        composable(WeatherScreens.AboutScreen.name){
            AboutScreen(navController = navController)
        }

        composable(WeatherScreens.SettingsScreen.name){
            SettingsScreen(navController = navController)
        }
    }
}