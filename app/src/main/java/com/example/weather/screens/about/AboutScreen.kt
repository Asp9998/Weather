package com.example.weather.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.weather.R
import com.example.weather.widgets.WeatherAppBar

@Composable
fun AboutScreen(navController: NavHostController) {

    Scaffold(topBar = {WeatherAppBar(title = "About",
        isMainScreen = false,
        navController = navController){
        navController.popBackStack()
    }},
        containerColor = Color.Transparent

    ) { paddingValue ->

        Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {

            Column(modifier = Modifier.padding(paddingValue),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.about_app),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)

                Text(text = stringResource(R.string.api_used),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }


    }
}