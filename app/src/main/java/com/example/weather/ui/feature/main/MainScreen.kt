package com.example.weather.ui.feature.main

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weather.data.remote.dto.onecall.Weather
import com.example.weather.ui.navigation.WeatherScreens
import com.example.weather.core.util.formatDecimals
import com.example.weather.core.design.components.WeatherAppBar
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weather.core.util.DataOrException
import com.example.weather.presentation.WeatherVisualSpec
import com.example.weather.ui.feature.settings.SettingsViewModel
import com.example.weather.core.util.AppColor
import androidx.compose.animation.Crossfade


@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    appGateVM: AppGateViewModel = hiltViewModel(),
    lat: Double?,
    lon: Double?,
    label: String?
) {


    // background
    val vm: MainViewModel = hiltViewModel()
    val spec by vm.visualSpec.collectAsStateWithLifecycle()


    val gate by appGateVM.gat.collectAsStateWithLifecycle()
    val unitFromDB = settingsViewModel.unitList.collectAsStateWithLifecycle().value
    val unit = remember { mutableStateOf("metric") }
    val cityName by mainViewModel.cityName.collectAsStateWithLifecycle()


    LaunchedEffect(unitFromDB) {
        unitFromDB.firstOrNull()?.unit?.split(" ")?.firstOrNull()?.lowercase()?.let {
            if (it == "metric" || it == "imperial") unit.value = it
        }
    }

    // If args exist, we're in “selection mode”
    val selectionMode = lat != null && lon != null

    // Bind exactly once depending on mode
    LaunchedEffect(selectionMode, lat, lon, label, unit.value) {
        if (selectionMode) {
            mainViewModel.lockToSelection(true)                          // <-- prevent GPS take-over
            mainViewModel.observeLocation(lat, lon, unit.value, labelIfKnown = label ?: "")
        } else {
            mainViewModel.lockToSelection(false)
            mainViewModel.bindFromLastLocationOrCurrent(unit.value)      // your normal cold-start path
        }
    }

    // Only auto-bind to live location if NOT in selection mode
    LaunchedEffect(gate, unit.value, selectionMode) {
        if (!selectionMode && gate == AppGateViewModel.LocationGate.Granted) {
            mainViewModel.observeCurrentLocation(unit.value)
        }
    }


    val uiState = mainViewModel.ui
//    Log.d("WeatherCached", "MainScreen: ${uiState.data} ")
    LaunchedEffect(unit.value, gate) {
        if (gate == AppGateViewModel.LocationGate.Granted && uiState.data != null) {
            mainViewModel.refresh(force = true) // SWR revalidate without flicker
        }
    }



    when {
        uiState.data != null -> {
            Box(Modifier.fillMaxSize()) {
                spec?.let { WeatherBackground(it) }
                Column {
                    MainDrawer(
                        weather = uiState.data!!,
                        navController = navController,
                        cityName = cityName ?: "—",
                        isMetric = unit.value == "metric"
                    )
                }
            }
        }

        uiState.loading == true -> Box(Modifier.fillMaxSize())

        // If no cache exists at all, fall back to search/onboarding
        uiState.data == null &&
                (gate == AppGateViewModel.LocationGate.Denied ||
                        gate == AppGateViewModel.LocationGate.Disabled) -> {
            FallbackBlock(
                state = uiState,
                onSearch = { navController.navigate(WeatherScreens.SearchScreen.name) }
            )
        }

        uiState.e != null -> Box(Modifier.fillMaxSize()) { /* error UI */ }

        else -> Box(Modifier.fillMaxSize())
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
fun WeatherBackground(spec: WeatherVisualSpec?) {
    val palette = spec?.palette ?: listOf(Color(0xFF177DC7), Color(0xFF05425B))

    // Crossfade between palettes; use the lambda parameter
    Crossfade(
        targetState = palette,
        label = "bg"
    ) { animatedPalette ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(animatedPalette))
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
                        Text(text = "${formatDecimals(data.current?.temp ?: 0.0)}°",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColor.text
                        )

                        // Nature of the day
                        Text(text = data.current?.weather?.get(0)?.main ?: "Sunny",
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



@Composable
private fun FallbackBlock(
    state: DataOrException<Weather, Boolean, Exception>,
    onSearch: () -> Unit
) {
    when {
        state.loading == true -> CircularProgressIndicator()
        state.data != null -> {
            // We have a cached/home-city weather; show it
            MainContent(data = state.data!!, isMetric = true) // or pass isMetric
        }
        else -> {
            Log.d("FallBack", "FallbackBlock: Error occurred ")
        }
    }
}



