package com.example.weather.ui.feature.favourite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.local.db.entity.FavouriteEntity
import com.example.weather.domain.repository.WeatherDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(private val weatherRepository: WeatherDbRepository) : ViewModel() {

    private val _favList = MutableStateFlow<List<FavouriteEntity>>(emptyList())
    val favList = _favList.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.observeAll().distinctUntilChanged()
                .collect { listOfFav ->
                    if(listOfFav.isEmpty()){
                        Log.d("FAVORITES", "ListOfFavorites: No favorites till now")
                    }
                    else{
                        _favList.value = listOfFav
                        Log.d("FAVORITES", "ListOfFavorites: ${favList.value}")
                    }

            }
        }
    }

    fun upsert(favorite: FavouriteEntity) = viewModelScope.launch {
        weatherRepository.upsert(favorite)
    }

    fun delete(locationId: String) = viewModelScope.launch {
        weatherRepository.delete(locationId)
    }
}