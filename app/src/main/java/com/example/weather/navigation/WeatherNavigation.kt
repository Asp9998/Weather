package com.example.weather.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weather.screens.about.AboutScreen
//import com.example.weather.screens.favorite.FavoriteScreen
import com.example.weather.screens.main.MainScreen
import com.example.weather.screens.main.MainViewModel
import com.example.weather.screens.search.SearchScreen
import com.example.weather.screens.settings.SettingsScreen
import com.example.weather.screens.splash.WeatherSplashScreen

@Composable
fun WeatherNavigation() {
    val navController = rememberNavController()

    NavHost(navController,
        startDestination = WeatherScreens.SplashScreen.name

    ){
        composable(WeatherScreens.SplashScreen.name){
            WeatherSplashScreen(navController = navController)
        }

        val route = WeatherScreens.MainScreen.name
        composable(route = "$route/{cityLat}/{cityLon}/{city}",
            arguments = listOf(
                navArgument (name = "cityLat"){ type = NavType.StringType },
                navArgument (name = "cityLon"){ type = NavType.StringType },
                navArgument (name = "city"){ type = NavType.StringType }

            )){ navBack ->

            val latString = navBack.arguments?.getString("cityLat")
            val lonString = navBack.arguments?.getString("cityLon")
            val cityName = navBack.arguments?.getString("city")

            if (latString == null || lonString == null || cityName == null) {
                Log.e("NavError", "Missing one or more arguments.")
                return@composable
            }

            val cityLat = latString.toDoubleOrNull() ?: 0.0
            val cityLon = lonString.toDoubleOrNull() ?: 0.0

            val mainViewModel = hiltViewModel<MainViewModel>()

            MainScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                city = cityName,
                cityLat = cityLat,
                cityLon = cityLon
            )
        }

        composable(WeatherScreens.SearchScreen.name){
            SearchScreen(navController = navController)
        }

        composable(WeatherScreens.FavoriteScreen.name){
//            FavoriteScreen()
        }

        composable(WeatherScreens.AboutScreen.name){
            AboutScreen(navController = navController)
        }

        composable(WeatherScreens.SettingsScreen.name){
            SettingsScreen(navController = navController)
        }
    }
}