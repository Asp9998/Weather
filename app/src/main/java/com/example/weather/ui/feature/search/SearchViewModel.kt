package com.example.weather.ui.feature.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.core.util.DataOrException
import com.example.weather.data.remote.dto.geocoading.CityNameItem
import com.example.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(private val weatherRepository: WeatherRepository)
    : ViewModel()  {

    var citySearchResult by mutableStateOf(DataOrException<List<CityNameItem>, Boolean, Exception>())
        private set

    private val _query = MutableStateFlow("")
    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    init {
        viewModelScope.launch {
            _query
                .debounce(500) // wait 500ms after typing stops
                .filter { it.length > 2 } // optional: only search for 2+ characters
                .distinctUntilChanged()
                .collect { query ->
                    Log.d("SearchCitySolve", "Setting View model: Loading cities ")
                    citySearchResult = DataOrException(loading = true)
                    citySearchResult = try {
                        Log.d("SearchCitySolve", "Setting View model: trying to fetch cities")
                        val data = weatherRepository.getCities(query).data
                        DataOrException(data = data, loading = false)
                    } catch (e: Exception) {
                        Log.d("SearchCitySolve", "Setting View model: error: $e ")
                        DataOrException(e = e, loading = false)
                    }
                }
        }
    }
}