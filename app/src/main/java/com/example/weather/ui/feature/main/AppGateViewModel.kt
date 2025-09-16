package com.example.weather.ui.feature.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppGateViewModel @Inject constructor () : ViewModel() {

    enum class LocationGate {Granted, Denied, Disabled }

    private val _gat = MutableStateFlow(LocationGate.Denied)
    val gat : StateFlow<LocationGate> =  _gat

    fun set(g: LocationGate) {
        _gat.value = g
    }

}