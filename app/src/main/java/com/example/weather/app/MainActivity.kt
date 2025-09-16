package com.example.weather.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.weather.ui.navigation.WeatherNavigation
import com.example.weather.ui.feature.main.AppGateViewModel
import com.example.weather.core.design.theme.WeatherTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appGateVM: AppGateViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ res ->
        val granted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true || res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if(granted) checkLocationSettings()
        else appGateVM.set(g = AppGateViewModel.LocationGate.Denied)
    }

    private fun checkLocationSettings() {
        val req = LocationSettingsRequest.Builder()
            .addLocationRequest(
                LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 0).build()
            )
            .build()

        settingsClient.checkLocationSettings(req)
            .addOnSuccessListener {
                appGateVM.set(AppGateViewModel.LocationGate.Granted)
            }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    val intent = IntentSenderRequest.Builder(e.resolution).build()
                    enableLocationLauncher.launch(intent)
                } else {
                    appGateVM.set(AppGateViewModel.LocationGate.Disabled)
                }
            }
    }

    private val settingsClient by lazy { LocationServices.getSettingsClient(this) }

    private val enableLocationLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        // User accepted/declined the system "Enable Location" dialog
        if (result.resultCode == RESULT_OK) {
            appGateVM.set(AppGateViewModel.LocationGate.Granted)
        } else {
            appGateVM.set(AppGateViewModel.LocationGate.Disabled)
        }
    }

    override fun onResume() {
        super.onResume()
        // If user turned things off in system settings, re-evaluate on resume
        reEvaluateGate()
    }

    private fun ensureLocationReady() {
        if (!hasAnyLocationPermission()) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        } else {
            checkLocationSettings()
        }
    }

    private fun reEvaluateGate() {
        when {
            !hasAnyLocationPermission() -> appGateVM.set(AppGateViewModel.LocationGate.Denied)
            else -> checkLocationSettings()
        }
    }

    private fun hasAnyLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ensureLocationReady()
            WeatherApp(appGateVM = appGateVM)
        }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@Composable
fun WeatherApp(appGateVM: AppGateViewModel) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = false // set to true if you're using a light background

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }

    WeatherTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFADD3EE), // Bottom color
                            Color(0xFF5491A8), // Middle Color
                            Color(0xFF3F9AC0), // Top color
                        )
                    )
                )
        ) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 0.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
            ) {
                    WeatherNavigation(appGateVM = appGateVM)
            }
        }
    }
}


