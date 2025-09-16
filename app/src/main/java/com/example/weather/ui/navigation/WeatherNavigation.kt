package com.example.weather.ui.navigation

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weather.ui.navigation.WeatherScreen
import com.example.weather.ui.navigation.WeatherScreens
import com.example.weather.ui.feature.about.AboutScreen
import com.example.weather.ui.feature.favourite.FavoriteViewModel
import com.example.weather.ui.feature.main.AppGateViewModel
import com.example.weather.ui.feature.main.MainScreen
import com.example.weather.ui.feature.main.MainViewModel
import com.example.weather.ui.feature.search.SearchScreen
import com.example.weather.ui.feature.search.SearchViewModel
import com.example.weather.ui.feature.settings.SettingsScreen

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun WeatherNavigation(
    appGateVM: AppGateViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController,
        startDestination = WeatherScreen.MainScreen.base

    ){

        composable(
            route = WeatherScreen.MainScreen.route,
            arguments = listOf(
                navArgument("lat")   { type = NavType.StringType;  nullable = true },
                navArgument("lon")   { type = NavType.StringType;  nullable = true },
                navArgument("label") { type = NavType.StringType;  nullable = true },
            )
        ) { backStackEntry ->
            val lat   = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lon   = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
            val label = backStackEntry.arguments?.getString("label")
            val mainViewModel = hiltViewModel<MainViewModel>()

            MainScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                appGateVM = appGateVM,
                lat = lat,
                lon = lon,
                label = label,
            )
        }

        composable(WeatherScreens.SearchScreen.name){
            val favoriteViewModel = hiltViewModel<FavoriteViewModel>()
            val searchViewModel = hiltViewModel<SearchViewModel>()
            val mainViewModel = hiltViewModel<MainViewModel>()

            SearchScreen(
                navController = navController,
                favoriteViewModel = favoriteViewModel,
                searchViewModel = searchViewModel,
                mainViewModel =  mainViewModel
            )
        }

        composable(WeatherScreens.AboutScreen.name){
            AboutScreen(navController = navController)
        }

        composable(WeatherScreens.SettingsScreen.name){
            SettingsScreen(navController = navController)
        }
    }
}