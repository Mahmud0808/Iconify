package com.drdisagree.iconify.features.xposed.lockscreen.location.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.utils.weather.WeatherConfig
import com.drdisagree.iconify.features.xposed.lockscreen.location.models.LocationBrowseItem
import com.drdisagree.iconify.features.xposed.lockscreen.location.states.LocationBrowseUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LocationBrowseViewModel @Inject constructor(
    @param:ApplicationContext val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationBrowseUiState())
    val uiState: StateFlow<LocationBrowseUiState> = _uiState.asStateFlow()

    init {
        // Debounce: react to query changes with a 750 ms delay
        viewModelScope.launch {
            _uiState
                .map { it.query }
                .distinctUntilChanged()
                .debounce(DEBOUNCE_MS)
                .collectLatest { query ->
                    if (query.isBlank()) return@collectLatest

                    _uiState.update { it.copy(isLoading = true) }

                    val results = fetchLocations(query)
                    _uiState.update { it.copy(locations = results, isLoading = false) }
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        if (newQuery.isEmpty()) {
            _uiState.update { LocationBrowseUiState() }
        } else {
            _uiState.update { it.copy(query = newQuery, isLoading = true) }
        }
    }

    fun saveLocation(item: LocationBrowseItem) {
        WeatherConfig.apply {
            setLocationId(context, item.lat.toString(), item.lon.toString())
            setLocationName(context, item.city)
        }
    }

    private suspend fun fetchLocations(input: String): List<LocationBrowseItem> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<LocationBrowseItem>()

            try {
                val lang = Locale.getDefault().language.replaceFirst("_".toRegex(), "-")
                val url = String.format(URL_PLACES, Uri.encode(input.trim()), lang)
                val response = java.net.URL(url).readText()
                val jsonResults = JSONObject(response).getJSONArray("geonames")
                val count = jsonResults.length()

                for (i in 0 until count) {
                    val result = jsonResults.getJSONObject(i)

                    val population =
                        if (result.has("population")) result.getInt("population") else 0
                    if (population == 0) continue

                    val city = result.getString("name")
                    val country = result.getString("countryName")
                    val countryId = result.getString("countryId")
                    val adminName =
                        if (result.has("adminName1")) result.getString("adminName1") else ""
                    val cityExt = (if (adminName.isEmpty()) "" else "$adminName, ") + country
                    val lat = result.getDouble("lat")
                    val lon = result.getDouble("lng")

                    val item = LocationBrowseItem(cityExt, countryId, city, lat, lon)
                    if (!results.contains(item)) {
                        results.add(item)
                        if (results.size == 5) break
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Received malformed location data input=$input", e)
            }

            results
        }

    companion object {
        private const val TAG = "LocationViewModel"
        private const val DEBOUNCE_MS = 750L
        private const val URL_PLACES =
            "https://secure.geonames.org/searchJSON?name_startsWith=%s&lang=%s&username=omnijaws&maxRows=20"
    }
}