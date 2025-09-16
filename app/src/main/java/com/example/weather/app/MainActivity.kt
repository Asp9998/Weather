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

//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//
//    private val appGateVM: AppGateViewModel by viewModels()
//    private val settingsClient by lazy { LocationServices.getSettingsClient(this) }
//
//    // --- Permission result ---
//    private val permissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
//            val granted = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
//                    res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
//            if (granted) {
//                // Continue interactively to settings (may show resolution dialog)
//                checkLocationSettings(interactive = true)
//            } else {
//                appGateVM.set(AppGateViewModel.LocationGate.Denied)
//            }
//        }
//
//    // --- Resolution dialog result (when interactive only) ---
//    private val enableLocationLauncher =
//        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                appGateVM.set(AppGateViewModel.LocationGate.Granted)
//            } else {
//                appGateVM.set(AppGateViewModel.LocationGate.Disabled)
//            }
//        }
////
////    // ─────────────────────────────────────────────────────────────────────────────
////    // Lifecycle
////    // ─────────────────────────────────────────────────────────────────────────────
//
//    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            WeatherApp(
//                appGateVM = appGateVM,
//                onRequestLocationSetup = ::startLocationSetup // UI can call to prompt user
//            )
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        // Passive re-evaluation: no dialogs, no permission prompts
//        refreshGate()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // User might have toggled system settings while app was backgrounded
//        refreshGate()
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // Public entry for UI to request interactive location setup
//    // ─────────────────────────────────────────────────────────────────────────────
//
//    private fun startLocationSetup() {
//        if (!hasAnyLocationPermission()) {
//            // Interactive: ask once when user taps a button
//            permissionLauncher.launch(
//                arrayOf(
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                )
//            )
//        } else {
//            // Interactive: may show resolution dialog if settings are off
//            checkLocationSettings(interactive = true)
//        }
//    }
//
//    // ─────────────────────────────────────────────────────────────────────────────
//    // Gate management
//    // ─────────────────────────────────────────────────────────────────────────────
//
//    /** Passive check used onStart/onResume: never shows dialogs, never prompts. */
//    private fun refreshGate() {
//        if (!hasAnyLocationPermission()) {
//            appGateVM.set(AppGateViewModel.LocationGate.Denied)
//            return
//        }
//        // Check settings without resolution; just classify state
//        checkLocationSettings(interactive = false)
//    }
//
//    /** Checks device location settings. Interactive path may show a resolution dialog. */
//    private fun checkLocationSettings(interactive: Boolean) {
//        val req = LocationSettingsRequest.Builder()
//            .addLocationRequest(
//                LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, /*interval*/ 0).build()
//            )
//            .build()
//
//        settingsClient.checkLocationSettings(req)
//            .addOnSuccessListener {
//                appGateVM.set(AppGateViewModel.LocationGate.Granted)
//            }
//            .addOnFailureListener { e ->
//                if (interactive && e is ResolvableApiException) {
//                    val intent = IntentSenderRequest.Builder(e.resolution).build()
//                    enableLocationLauncher.launch(intent)
//                } else {
//                    // Non-interactive path or non-resolvable failure → Disabled
//                    appGateVM.set(AppGateViewModel.LocationGate.Disabled)
//                }
//            }
//    }
//
//    private fun hasAnyLocationPermission(): Boolean {
//        val fine = ContextCompat.checkSelfPermission(
//            this, Manifest.permission.ACCESS_FINE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//        val coarse = ContextCompat.checkSelfPermission(
//            this, Manifest.permission.ACCESS_COARSE_LOCATION
//        ) == PackageManager.PERMISSION_GRANTED
//        return fine || coarse
//    }
//}
//
//@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
//@Composable
//fun WeatherApp(
//    appGateVM: AppGateViewModel,
//    onRequestLocationSetup: () -> Unit
//) {
//    val systemUiController = rememberSystemUiController()
//    val useDarkIcons = false
//
//    SideEffect {
//        systemUiController.setSystemBarsColor(
//            color = Color.Transparent,
//            darkIcons = useDarkIcons
//        )
//    }
//
//    WeatherTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            Color(0xFFADD3EE),  // Bottom
//                            Color(0xFF5491A8),  // Middle
//                            Color(0xFF3F9AC0),  // Top
//                        )
//                    )
//                )
//        ) {
//            Surface(
//                color = Color.Transparent,
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Transparent)
//            ) {
//                // Pass the callback into your navigation graph so any screen
//                // (e.g., a "Location is off" banner) can trigger the interactive flow.
//                WeatherNavigation(
//                    appGateVM = appGateVM,
//                    onRequestLocationSetup = onRequestLocationSetup
//                )
//            }
//        }
//    }
//}

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
//                Column(
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
                    WeatherNavigation(appGateVM = appGateVM)
//                }
            }
        }
    }
}


