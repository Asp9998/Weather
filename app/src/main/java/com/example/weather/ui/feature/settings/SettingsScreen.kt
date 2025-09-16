package com.example.weather.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.weather.data.local.db.entity.UnitEntity
import com.example.weather.core.util.AppColor
import com.example.weather.core.design.components.WeatherAppBar

@Composable
fun SettingsScreen(navController: NavHostController, settingsViewModel: SettingsViewModel = hiltViewModel()) {

    val unitToggleState = remember {
        mutableStateOf(false)
    }
    val measurementUnits = listOf("Metric (C)", "Imperial (F)")

    val choiceFormDB = settingsViewModel.unitList.collectAsState().value

    val defaultChoice = if(choiceFormDB.isEmpty()) measurementUnits[0] else choiceFormDB[0].unit

    val choiceState = remember {
        mutableStateOf(defaultChoice)
    }

    Scaffold(
        topBar = {
            WeatherAppBar(title = "Settings",
                isMainScreen = false){
                navController.popBackStack()
            }
        },
        containerColor = Color.Transparent
    ){ paddingValues ->

        Surface(color = Color.Transparent) {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(text = "Change Units of Measurement",
                    color = AppColor.text, modifier = Modifier.padding(bottom = 10.dp))

                Surface(shape = RoundedCornerShape(CornerSize(10.dp)), color = AppColor.text) {

                    IconToggleButton(checked = !unitToggleState.value,
                        onCheckedChange = {
                            unitToggleState.value = !it
                            if(unitToggleState.value){ choiceState.value = "Imperial (F)"}
                            else { choiceState.value = "Metric (C)"}
                                          },
                        modifier = Modifier.width(180.dp)
                            .clip(shape = RoundedCornerShape(CornerSize(10.dp)))
                            .padding(5.dp)
                            .background(AppColor.text),
                    ) {
                        Text(text = if (unitToggleState.value) "Fahrenheit °F" else  "Celsius °c",
                            color = Color.Black,
                            fontSize = 22.sp)
                    }
                }

                    Button(onClick = {
                        settingsViewModel.deleteAll()
                        settingsViewModel.addUnit(UnitEntity(unit = choiceState.value))
                        navController.popBackStack()
                    },
                        modifier = Modifier.padding(top = 10.dp)
                            .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(CornerSize(20.dp)),
                        colors = ButtonColors(
                            containerColor = AppColor.contentBackground,
                            contentColor = AppColor.text,
                            disabledContainerColor = AppColor.contentBackground,
                            disabledContentColor = AppColor.text
                        )

                    ) {
                        Text(text = "save", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
            }
        }
    }
}