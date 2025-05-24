package com.example.weather.screens.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.weather.components.CommonTextField
import com.example.weather.data.DataOrException
import com.example.weather.model.FavoriteModel.Favorite
import com.example.weather.model.GeoCoadingApiModels.CityName
import com.example.weather.model.GeoCoadingApiModels.CityNameItem
import com.example.weather.navigation.WeatherScreens
import com.example.weather.screens.favorite.FavoriteViewModel
import com.example.weather.utils.AppColor
import com.example.weather.widgets.WeatherAppBar

@Composable
fun SearchScreen(navController: NavController,
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

                    CityColumn(cityResult = cityResult, favoriteViewModel, navController)
            }
        }
    }
}

@Composable
fun CityColumn(
    cityResult: DataOrException<CityName, Boolean, Exception>,
    favoriteViewModel: FavoriteViewModel,
    navController: NavController
) {
    Surface(modifier = Modifier.padding(vertical = 10.dp),
        shape = RoundedCornerShape(25.dp),
        color = AppColor.contentBackground
    ) {
        cityResult.data?.let { cityList ->
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(cityList.size) { index ->

                    CityDetails(cityItem = cityList[index],
                        favoriteViewModel = favoriteViewModel,
                        navController = navController,
                        isLast = index == cityList.size-1
                    )

                }
            }
        }
    }
}

@Composable
fun CityDetails(cityItem: CityNameItem,
                isLast: Boolean,
                favoriteViewModel: FavoriteViewModel,
                navController: NavController) {
    Surface(
        color = Color.Transparent
    ) {
        Column (verticalArrangement = Arrangement.Center){

            Column (modifier = Modifier.fillMaxWidth()
                .clickable {
                    val cityLat = cityItem.lat
                    val cityLon = cityItem.lon
                    val city = cityItem.name.substring(0,1).uppercase() + cityItem.name.substring(1)

                    val alreadyFavorite = favoriteViewModel.favList.value.any { it.city == city }

                    if (!alreadyFavorite) {
                        favoriteViewModel.addFavorite(Favorite(lat = cityLat, lon = cityLon, city = city))
                    }

                    Log.d("ADD", "SearchScreen: $city")
                    navController.navigate(WeatherScreens.MainScreen.name+"/$cityLat/$cityLon/$city"){
                        popUpTo(0)
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
    modifier: Modifier = Modifier,
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

