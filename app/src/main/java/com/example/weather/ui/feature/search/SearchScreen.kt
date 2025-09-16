package com.example.weather.ui.feature.search

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weather.core.design.components.CommonTextField
import com.example.weather.core.util.DataOrException
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.data.remote.dto.geocoading.CityNameItem
import com.example.weather.ui.navigation.WeatherScreen
import com.example.weather.ui.feature.favourite.FavoriteViewModel
import com.example.weather.ui.feature.main.MainViewModel
import com.example.weather.core.util.AppColor
import com.example.weather.core.design.components.WeatherAppBar
import kotlinx.coroutines.launch
import java.util.Locale

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun SearchScreen(navController: NavController,
                 mainViewModel: MainViewModel = hiltViewModel(),
                 favoriteViewModel: FavoriteViewModel = hiltViewModel(),
                 searchViewModel: SearchViewModel = hiltViewModel()
){
    Scaffold(
        topBar = {
            WeatherAppBar(title = "Search",
                isMainScreen = false,
                navController = navController){
                navController.popBackStack()
            }
        },
        containerColor = Color.Transparent
    ){ paddingValues ->

        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
        ){
                Column (verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ){
                    SearchBar(
                        onQueryChanged = {
                            searchViewModel.updateQuery(it)
                        }
                    )

                    // Lazy Column for list of cities

                    val cityResult = searchViewModel.citySearchResult

                    if (cityResult.loading == true) {
                        // Show a loading indicator
                        Text(text = "Loading...",
                            modifier = Modifier.padding(25.dp),
                            color = AppColor.text,
                            fontWeight = FontWeight.SemiBold)
                    }

                    CityColumn(
                        cityResult = cityResult, favoriteViewModel, navController = navController,
                        mainVM = mainViewModel,
                    )
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun CityColumn(
    cityResult: DataOrException<List<CityNameItem>, Boolean, Exception>,
    favoriteViewModel: FavoriteViewModel,
    mainVM: MainViewModel,
    navController: NavController
) {
    Surface(modifier = Modifier.padding(vertical = 10.dp),
        shape = RoundedCornerShape(25.dp),
        color = AppColor.contentBackground
    ) {
        cityResult.data?.let { cityList ->
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(cityList.size) { index ->

                    CityDetails(
                        cityItem = cityList[index],
                        favoriteViewModel = favoriteViewModel,
                        navController = navController,
                        isLast = index == cityList.size - 1,
                        mainVM = mainVM,
                    )

                }
            }
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun CityDetails(cityItem: CityNameItem,
                isLast: Boolean,
                mainVM: MainViewModel,
                favoriteViewModel: FavoriteViewModel,
                navController: NavController) {

    val scope = rememberCoroutineScope()

    Surface(
        color = Color.Transparent
    ) {
        Column (verticalArrangement = Arrangement.Center){

            Column (modifier = Modifier.fillMaxWidth()
                .clickable {
                    val lat = cityItem.lat
                    val lon = cityItem.lon
                    val label = makeLabel(cityItem.name, cityItem.country)
                    val id = locationKey(lat, lon)
                    val now = System.currentTimeMillis()

                    scope.launch {
                        // 1) Upsert favorite (idempotent)
                        favoriteViewModel.upsert(
                            FavouriteEntity(
                                locationId = id,
                                cityLabel = label,
                                cityLat = lat,
                                cityLon = lon,
                                lastViewedMs = now,
                                pinned = false
                            )
                        )

                        mainVM.addFavorite(lat = lat , lon = lon, label = label, unit = "metric")

                        navController.navigate("${WeatherScreen.MainScreen.base}?lat=$lat&lon=$lon&label=$label") {
                            popUpTo(WeatherScreen.MainScreen.base) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

            ){
                Text(text = cityItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColor.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 13.dp, bottom = 0.dp)
                )

                Text(text = cityItem.state +", "+ cityItem.country,
                    fontSize = 13.sp,
                    color = AppColor.textLight,
                    modifier = Modifier.fillMaxWidth()
                        .padding(start = 20.dp, bottom = 13.dp, top = 0.dp)
                )

            }

            if(!isLast){
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp),
                    thickness = 0.5.dp,
                    color = AppColor.SearchHDivider
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    onQueryChanged: (String) -> Unit,

){
    val searchQueryState = rememberSaveable {
        mutableStateOf("")
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val valid = remember(searchQueryState){
        searchQueryState.value.trim().isNotBlank()
    }

    Column {
        CommonTextField(
            valueState = searchQueryState,
            onQueryChanged = onQueryChanged,
            placeHolder = "Enter city",
            onAction = KeyboardActions{
                if(valid) return@KeyboardActions

                // hide keyboard
                keyboardController?.hide()
            }
        )
    }
}



private fun locationKey(lat: Double, lon: Double) =
    String.format(Locale.US, "%.4f,%.4f", lat, lon)

private fun makeLabel(name: String, country: String) =
    buildString {
        append(name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        append(", ").append(country.uppercase())
    }

