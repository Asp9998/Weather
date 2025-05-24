package com.example.weather.screens.favorite

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.model.FavoriteModel.Favorite
import com.example.weather.repository.WeatherDbRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(private val weatherRepository: WeatherDbRepository) : ViewModel() {

    private val _favList = MutableStateFlow<List<Favorite>>(emptyList())
    val favList = _favList.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            weatherRepository.getAllFavorite().distinctUntilChanged()
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

    fun getAllFavorite() = viewModelScope.launch {
        weatherRepository.getAllFavorite()
    }

    fun addFavorite(favorite: Favorite) = viewModelScope.launch {
        weatherRepository.addFavorite(favorite)
    }

    fun removeFavorite(favorite: Favorite) = viewModelScope.launch {
        weatherRepository.removeFavorite(favorite)
    }
}