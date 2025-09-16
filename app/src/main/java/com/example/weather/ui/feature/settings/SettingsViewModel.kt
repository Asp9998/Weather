package com.example.weather.ui.feature.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.db.entity.UnitEntity
import com.example.weather.domain.repository.WeatherDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel@Inject constructor(private val weatherRepository: WeatherDbRepository) : ViewModel() {
    private val _unitList = MutableStateFlow<List<UnitEntity>>(emptyList())
    val unitList = _unitList.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getUnit().distinctUntilChanged()
                .collect { listOfUnit ->
                    if(listOfUnit.isEmpty()){
                        Log.d("FAVORITES", "ListOfFavorites: No Unit yet")
                    }
                    else{
                        _unitList.value = listOfUnit
                        Log.d("FAVORITES", "ListOfFavorites: ${unitList.value}")
                    }

                }
        }
    }

    fun getUnit() = viewModelScope.launch { weatherRepository.getUnit() }

    fun addUnit(unit: UnitEntity) = viewModelScope.launch { weatherRepository.addUnit(unit) }

    fun removeUnit(unit: UnitEntity) = viewModelScope.launch { weatherRepository.removeUnit(unit) }

    fun deleteAll() = viewModelScope.launch { weatherRepository.deleteAll() }
}