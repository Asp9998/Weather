package com.example.weather.core.design.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
//@Preview
@Composable
fun WeatherAppBar(
    title: String = "Mississauga",
    isMainScreen: Boolean = true,
    elevation: Dp = 0.dp,
    onSearchButtonClicked: () -> Unit = {},
    onMenuClicked: () -> Unit = {},
    onBackButtonClicked: () -> Unit = {}
){
        TopAppBar(modifier = Modifier.shadow(elevation = elevation)
            .background(Color.Transparent)
            .padding(start = 10.dp,
                end = if(!isMainScreen) 60.dp else 10.dp,
                bottom = 12.dp),
            title = {
                Text(text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            actions = {
                if(isMainScreen){
                    IconButton(onClick = {
                        onSearchButtonClicked.invoke()
                    }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "search icon",
                            tint = Color.White)
                    }
                } else{ Box {} }
            },
            navigationIcon = {
                IconButton(onClick = {
                    if(isMainScreen){
                        onMenuClicked.invoke()
                    }
                    else{
                        onBackButtonClicked.invoke()
                    }
                }) {
                    Icon(imageVector = if(isMainScreen) Icons.Rounded.Menu else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = if(isMainScreen) "menu icon" else "back icon",
                        tint = Color.White,
                    )
                }

            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
}
