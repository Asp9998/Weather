package com.example.weather.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weather.data.DataOrException
import com.example.weather.model.GeoCoadingApiModels.CityName
import com.example.weather.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter


@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(private val weatherRepository: WeatherRepository)
    : ViewModel()  {

    var citySearchResult by mutableStateOf(DataOrException<CityName, Boolean, Exception>())
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
                    citySearchResult = DataOrException(loading = true)
                    citySearchResult = try {
                        val data = weatherRepository.getCities(query).data
                        DataOrException(data = data, loading = false)
                    } catch (e: Exception) {
                        DataOrException(e = e, loading = false)
                    }
                }
        }
    }
}