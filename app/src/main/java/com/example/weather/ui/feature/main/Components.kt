package com.example.weather.ui.feature.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.weather.R
import com.example.weather.data.remote.dto.onecall.Daily
import com.example.weather.data.remote.dto.onecall.Hourly
import com.example.weather.data.remote.dto.onecall.Weather
import com.example.weather.ui.feature.favourite.FavoriteScreen
import com.example.weather.core.util.AppColor
import com.example.weather.core.util.formatDate
import com.example.weather.core.util.formatDateTime
import com.example.weather.core.util.formatDecimals
import kotlinx.coroutines.launch

@Composable
fun SlidingDrawerLayout(
    drawerContent: @Composable () -> Unit,
    mainContent: @Composable (onMenuClicked: () -> Unit) -> Unit
) {
    val drawerOpen = remember {
        mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val offsetX by animateDpAsState(
        targetValue = if (drawerOpen.value) 280.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 400, // adjust this value to speed up or slow down
            easing = FastOutSlowInEasing // optional: control the curve
        ),
        label = "MainContentOffset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Drawer content
        if (drawerOpen.value) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .windowInsetsPadding(WindowInsets.systemBars),
                shape = RoundedCornerShape(topEnd = 25.dp, bottomEnd = 25.dp),
                color = MaterialTheme.colorScheme.background.copy(0.1f)
            )
            {
                drawerContent()
            }
        }

        // Main content
        Box(
            modifier = Modifier
                .offset(x = offsetX)
                .fillMaxSize()
        ) {
            mainContent {
                scope.launch { drawerOpen.value = true }
            }
        }

        // Click outside to close drawer
        if (drawerOpen.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 280.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource() }
                    ) {
                        scope.launch { drawerOpen.value = false }
                    }
            )
        }
    }
}

@Composable
fun DrawerContent(navController: NavController, onItemClick: (String) -> Unit) {

    Surface(modifier = Modifier.background(Color.Transparent)
        .fillMaxHeight()
        .fillMaxWidth(2.3f/3f),
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(
            topEnd = 25.dp,
            bottomEnd = 25.dp
        )) {
        Column(modifier = Modifier.padding(15.dp)) {

            Row (modifier = Modifier.padding(5.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End

            ) {
                Icon(imageVector = Icons.Default.Info,
                    contentDescription = "about icon",
                    tint = AppColor.text,
                    modifier = Modifier.padding(10.dp)
                        .clickable{onItemClick("about")}
                )

                Icon(imageVector = Icons.Default.Settings,
                    contentDescription = "settings icon",
                    tint = AppColor.text,
                    modifier = Modifier.padding(10.dp)
                        .clickable{onItemClick("settings")}

                )
            }

            Row(modifier = Modifier.padding(10.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FavoriteScreen(navController = navController)
            }


        }
    }


}



@Composable
fun FeelsLike(data: Weather){

    Surface (modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        .fillMaxWidth(),
        color = Color.Transparent){

        Column (horizontalAlignment = Alignment.CenterHorizontally){

            Text(
                text = "Feel like ${formatDecimals(data.current?.feels_like ?: 0.0)}°",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColor.text
            )
        }
    }
}

@Composable
fun HourlyForecast(data: Weather) {
    Surface (modifier = Modifier.padding(5.dp).background(Color.Transparent),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 12.dp, start = 18.dp, end = 18.dp)
        ){
            items(items = data.hourly!!.take(24)){
                HourlyWeatherDetailsColumn(data = it)
            }
        }
    }
}

@Composable
fun HourlyWeatherDetailsColumn(data: Hourly) {
    val imageURL = "https://openweathermap.org/img/wn/${data.weather[0].icon}.png"

    Surface (modifier = Modifier
        .padding(start = 6.dp, end = 6.dp, top = 1.dp, bottom = 1.dp),
        color = Color.Transparent
    ){
        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {

            // time
            Text(text = "${formatDateTime(data.dt).split(":")[0]} ${formatDateTime(data.dt).split(" ")[1].lowercase()}",
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                fontSize = 11.sp,
                color = AppColor.HourlyForecastLow
            )

            // Icon
            WeatherStateImage(imageUrl = imageURL, modifier = Modifier.size(40.dp)
                .padding(top = 2.dp, bottom = 2.dp),
            )

            // Temp
            Text(text = "${formatDecimals(data.temp)}°",
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Humidity
            Row (modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically){
                Icon(painter = painterResource(id = R.drawable.humidity),
                    contentDescription = "humidity icon",
                    tint = AppColor.HourlyForecastLow,
                    modifier = Modifier
                        .size(11.dp)
                        .padding(end = 1.dp))
                Text(text = "${data.humidity}%",
                    fontSize = 11.sp,
                    color = AppColor.HourlyForecastLow)
            }
        }


    }
}

@Composable
fun SunPosition(data: Weather) {

    SunMoonPosition(title1 = "Sunrise",
        title2 = "Sunset",
        value1 = formatDateTime(data.current?.sunrise ?: 0).lowercase(),
        value2 = formatDateTime(data.current?.sunset ?: 0).lowercase())

}

@Composable
fun WeeklyForecast(data: Weather) {
    Surface (modifier = Modifier.padding(5.dp).background(Color.Transparent),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column (modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp)){
            data.daily!!.take(7).forEach{
                WeeklyWeatherDetailsRow(data = it)
            }
        }
    }
}

@Composable
fun WeeklyWeatherDetailsRow(data: Daily) {
    val imageURL = "https://openweathermap.org/img/wn/${data.weather!![0].icon }.png"

    Surface (modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)
        .padding(start = 25.dp, end = 25.dp, top = 1.dp, bottom = 1.dp),
        color = Color.Transparent
    ){
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {

            // Day
            Text(text =
                if(formatDate(data.dt).split(",")[0] == "Sun"){"Sunday"}
                else if (formatDate(data.dt).split(",")[0] == "Mon"){"Monday"}
                else if (formatDate(data.dt).split(",")[0] == "Tue"){"Tuesday"}
                else if (formatDate(data.dt).split(",")[0] == "Wed"){"Wednesday"}
                else if (formatDate(data.dt).split(",")[0] == "Thu"){"Thursday"}
                else if (formatDate(data.dt).split(",")[0] == "Fri"){"Friday"}
                else {"Saturday"},

                modifier = Modifier.width(110.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Humidity
            Row (modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(painter = painterResource(id = R.drawable.humidity),
                    contentDescription = "humidity icon",
                    tint = AppColor.weeklyForecastLow,
                    modifier = Modifier
                        .size(12.dp)
                        .padding(end = 3.dp))
                Text(text = "${data.humidity}%",
                    fontSize = 12.sp,
                    color = AppColor.weeklyForecastLow)
            }

            // icon
            WeatherStateImage(imageUrl = imageURL,
                modifier = Modifier.size(40.dp)
            )

            // max temp
            Text(text = "${formatDecimals(data.temp?.max ?: 0.0)}°",
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // min temp
            Text(text = "${formatDecimals(data.temp?.min ?: 0.0)}°",
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )


        }

    }
}

@Composable
fun HumidityWindPressureRow(data: Weather, isMetric: Boolean) {


    val uvIndexString = remember {
        mutableStateOf("")
    }

    val visibilityString = remember {
        mutableStateOf("")
    }

    val uvIndexInt = formatDecimals(data.current?.uvi ?: 0.0).toInt()

    val visibilityInt = data.current!!.visibility ?: 0

    uvIndexString.value = when (uvIndexInt) {
        in 0..2 -> "Low"
        in 3..6 -> "Moderate"
        in 7..8 -> "High"
        in 9..10 -> "Very High"
        else -> "Extreme"
    }

    visibilityString.value = when {
        visibilityInt > 10_000 -> "Excellent"
        visibilityInt in 6_000..10_000 -> "Good"
        visibilityInt in 4_000..5_999 -> "Moderate"
        visibilityInt in 1_000..3_999 -> "Poor"
        else -> "very Poor"
    }





    Column {

        ContainerStructure(icon1 = Icons.Default.WbSunny,
            icon1Des = "uvi icon",
            title1 = "UV index",
            value1 = uvIndexString.value,
            icon2 = Icons.Default.WaterDrop,
            icon2Des = "humidity icon",
            title2 = "Humidity",
            value2 = data.current.humidity.toString()+ "%")

        ContainerStructure(icon1 = Icons.Filled.Air,
            icon1Des = "wind icon",
            title1 = "Wind",
            value1 = data.current.wind_speed.toString() + if (isMetric) " m/s" else " mph" ,
            icon2 = Icons.Default.Thermostat,
            icon2Des = "dew point icon",
            title2 = "Dew Point",
            value2 = formatDecimals(data.current.dew_point ?: 0.0)+ "°")

        ContainerStructure(icon1 = Icons.Default.Compress,
            icon1Des = "pressure icon",
            title1 = "Pressure",
            value1 = data.current.pressure.toString() + " psi",
            icon2 = Icons.Default.Visibility,
            icon2Des = "visibility icon",
            title2 = "Visibility",
            value2 = visibilityString.value)

    }

}

@Composable
fun WeatherStateImage(imageUrl: String, modifier: Modifier) {
    Image(painter =  rememberImagePainter(imageUrl),
        contentDescription = "icon Image", modifier = modifier)
}

@Composable
fun MoonPosition(data: Weather){
    SunMoonPosition(title1 = "Moonrise",
        title2 = "Moonset",
        value1 = formatDateTime(data.daily?.get(0)?.moonrise ?: 0).lowercase(),
        value2 = formatDateTime(data.daily?.get(0)?.moonset ?: 0).lowercase())
}

@Composable
fun ContainerStructure(
    icon1: ImageVector,
    icon1Des: String,
    title1: String,
    value1: String,
    icon2: ImageVector,
    icon2Des: String,
    title2: String,
    value2: String

){
    Surface(modifier = Modifier.padding(5.dp).background(Color.Transparent)
        .height(90.dp),
        color = Color.Transparent

    ) {
        Row (modifier = Modifier.fillMaxWidth()
            .fillMaxHeight()
        ){

            Surface (modifier = Modifier
                .padding(end = 5.dp)
                .weight(1f)
                .background(Color.Transparent),
                shape = RoundedCornerShape(corner = CornerSize(25.dp)),
                color = MaterialTheme.colorScheme.background

            ) {

                Column (modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ){
                    Row (verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 25.dp, bottom = 8.dp)
                    ){
                        Icon(imageVector = icon1,
                            contentDescription = icon1Des,
                            tint = AppColor.ContainerStructureLow,
                            modifier = Modifier.size(20.dp).padding(end = 3.dp)
                        )
                        Text(text = title1,
                            fontSize = 15.sp,
                            color = AppColor.ContainerStructureLow,
                        )
                    }
                    Text( text = value1,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColor.text,
                        modifier = Modifier.padding(start = 25.dp)

                    )
                }
            }

            Surface (modifier = Modifier
                .background(Color.Transparent)
                .padding(start = 5.dp)
                .weight(1f),
                shape = RoundedCornerShape(corner = CornerSize(25.dp)),
                color = MaterialTheme.colorScheme.background

            ) {
                Column (modifier = Modifier.fillMaxWidth()
                    .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ){
                    Row (verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 25.dp, bottom = 8.dp)

                    ){
                        Icon(imageVector = icon2,
                            contentDescription = icon2Des,
                            tint = AppColor.ContainerStructureLow,
                            modifier = Modifier.size(20.dp).padding(end = 3.dp)
                        )
                        Text(text = title2,
                            fontSize = 15.sp,
                            color = AppColor.ContainerStructureLow,
                        )
                    }
                    Text( text = value2,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColor.text,
                        modifier = Modifier.padding(start = 25.dp)

                    )
                }
            }

        }
    }

}

@Composable
fun SunMoonPosition (title1: String,
                     title2: String,
                     value1: String,
                     value2: String

){
    Surface (modifier = Modifier.padding(5.dp).background(Color.Transparent),
        shape = RoundedCornerShape(25.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row (modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp)
            .height(100.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Column (modifier = Modifier.weight(1f)
                .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text( text = title1,
                    fontSize = 12.sp,
                    color = AppColor.SunPositionLow,
                    modifier = Modifier)
                Text(text = value1,
                    color = AppColor.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold)
            }

            VerticalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 30.dp))

            Column (modifier = Modifier.weight(1f)
                .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center

            ){
                Text( text = title2,
                    fontSize = 12.sp,
                    color = AppColor.SunPositionLow,
                    modifier = Modifier)
                Text(text = value2,
                    color = AppColor.text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold)

            }

        }
    }
}

