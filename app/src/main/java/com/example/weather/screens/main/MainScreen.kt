package com.example.weather.screens.main

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weather.data.DataOrException
import com.example.weather.model.OneCallApiModels.Weather
import com.example.weather.navigation.WeatherScreens
import com.example.weather.utils.formatDecimals
import com.example.weather.widgets.WeatherAppBar
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.weather.components.DrawerContent
import com.example.weather.components.FeelsLike
import com.example.weather.components.HourlyForecast
import com.example.weather.components.HumidityWindPressureRow
import com.example.weather.components.MoonPosition
import com.example.weather.components.SlidingDrawerLayout
import com.example.weather.components.SunPosition
import com.example.weather.components.WeeklyForecast
import com.example.weather.screens.settings.SettingsViewModel
import com.example.weather.utils.AppColor
import com.example.weather.utils.UserPreferences


@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
){

    val cityInfo = UserPreferences.getLatestSearchedCity(LocalContext.current)

    val cityName = cityInfo[0] as String
    val cityLat = cityInfo[1] as Double
    val cityLon = cityInfo[2] as Double

    // Unit management
    val unitFromDB = settingsViewModel.unitList.collectAsState().value

    val unit = remember {
        mutableStateOf("metric")
    }
    val isMetric = remember {
        mutableStateOf(false)
    }

    if(unitFromDB.isNotEmpty()){

        Log.d("mainScreen", "MainScreen: Inside of main Function")

        unit.value = unitFromDB[0].unit.split(" ")[0].lowercase()
        isMetric.value = unit.value == "metric"


        val weatherData = produceState(
            initialValue = DataOrException(loading = true)) {
            value = mainViewModel.getWeatherData(lat = cityLat, lon = cityLon, unit = unit.value)
        }.value

        Log.d("WeatherDetails", "MainScreen: ${weatherData.data}")

        if(weatherData.loading == true){
            Surface(modifier = Modifier.fillMaxSize(),
                color = Color.Transparent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
                }
            }
        } else if(weatherData.data != null){
            MainDrawer(
                weather = weatherData.data!!,
                navController = navController,
                cityName = cityName,
                isMetric = isMetric.value
            )

        }
    }
    else{
        Log.d("MainScreen", "Unit: unit is Empty")

        unit.value = "metric"
        isMetric.value = unit.value == "metric"


        val weatherData = produceState(
            initialValue = DataOrException(loading = true)) {
            value = mainViewModel.getWeatherData(lat = cityLat, lon = cityLon, unit = unit.value)
        }.value

        if(weatherData.loading == true){
            Surface(modifier = Modifier.fillMaxSize(),
                color = Color.Transparent) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        } else if(weatherData.data != null){
            MainDrawer(
                weather = weatherData.data!!,
                navController = navController,
                cityName = cityName,
                isMetric = isMetric.value
            )

        }

    }
}

@Composable
fun MainDrawer(
    weather: Weather,
    navController: NavController,
    cityName: String,
    isMetric: Boolean
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    SlidingDrawerLayout(
        drawerContent = {
            DrawerContent (navController) { screenName ->

                scope.launch {
                    drawerState.close()
                    if(screenName == "about"){
                        navController.navigate(WeatherScreens.AboutScreen.name)
                    }else  if(screenName == "settings"){
                        navController.navigate(WeatherScreens.SettingsScreen.name)
                    }
                }
            }
        },
        mainContent = { onMenuClicked ->
            MainScaffold(
                weather = weather,
                navController = navController,
                cityName = cityName,
                isMetric = isMetric,
                onMenuClicked = onMenuClicked
            )
        }
    )
}

@Composable
fun MainScaffold(weather: Weather,
                 navController: NavController,
                 cityName: String,
                 isMetric: Boolean,
                 onMenuClicked: () -> Unit = {}){

    Scaffold (topBar = {
        WeatherAppBar(title = cityName,
            navController = navController,
            onSearchButtonClicked = {
                navController.navigate(WeatherScreens.SearchScreen.name)
            },
            onMenuClicked = onMenuClicked,
            onBackButtonClicked = {
                navController.popBackStack()
            }
        )},
        containerColor = Color.Transparent
    ) { paddingValues ->
        MainContent(
            data = weather,
            modifier = Modifier.padding(paddingValues),
            isMetric = isMetric
        )
    }
}


@Composable
fun MainContent(data: Weather, modifier: Modifier = Modifier, isMetric: Boolean){

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {

        Surface (modifier = Modifier
            .fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            color = Color.Transparent
        ){
            Column (horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center){

                        // Temperature
                        Text(text = "${formatDecimals(data.current.temp)}°",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColor.text
                        )

                        // Nature of the day
                        Text(text = data.current.weather[0].main,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColor.text
                        )

            }
        }

        Column(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            FeelsLike(data = data)

            HourlyForecast(data = data)

            SunPosition(data = data)

            WeeklyForecast(data = data)

            HumidityWindPressureRow(data = data, isMetric = isMetric)

            MoonPosition(data = data)

        }
    }
}

