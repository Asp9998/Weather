package com.example.weather.screens.splash

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weather.R
import com.example.weather.navigation.WeatherScreens
import com.example.weather.utils.UserPreferences
import kotlinx.coroutines.delay

@Composable
fun WeatherSplashScreen(navController: NavController){

//    Log.d("LATESTSAVEDCITY", "WeatherSplashScreen: BEFORECALl ")

    val cityInfo = UserPreferences.getLatestSearchedCity(LocalContext.current)

    val city = cityInfo[0] as String // City name
    val cityLat = cityInfo[1] as Double// City Lat
    val cityLon = cityInfo[2] as Double// City Lon

//    Log.d("LATESTSAVEDCITY", "WeatherSplashScreen: city:$cityInfo")

    val scale = remember {
        Animatable(initialValue = 0f)
    }


    LaunchedEffect(key1 = true, block = {
        scale.animateTo(targetValue = 0.6f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(8f).getInterpolation(it)
                }
            )
        )
        delay(300)
        navController.navigate(WeatherScreens.MainScreen.name+"/$cityLat/$cityLon/$city"){
            popUpTo(0)
        }
    })


    Surface(modifier = Modifier
        .padding(15.dp)
        .size(330.dp)
        .scale(scale.value),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, color = Color.White)

    ) {
        Column(modifier = Modifier.padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
            ) {
            Image(painter = painterResource(R.drawable.sun),
                contentDescription = "Sunny Icon",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(95.dp))
            Text(text = "Find the sun?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
                )
        }
    }
}