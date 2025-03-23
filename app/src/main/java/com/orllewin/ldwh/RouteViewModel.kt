package com.orllewin.ldwh

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {

    private val _routeFlow: MutableStateFlow<List<Point>> = MutableStateFlow(listOf())
    var routeFlow = _routeFlow.asStateFlow()

    private val _subsectionFlow: MutableStateFlow<GeoJson.Subsection> = MutableStateFlow(GeoJson.Subsection.empty())
    var subsectionFlow = _subsectionFlow.asStateFlow()

    private val geoJson = GeoJson()

    fun getRoute(context: Context) {
        viewModelScope.launch(Dispatchers.Main){
            geoJson.initialiseRoute(context)
            _routeFlow.emit(geoJson.routePoints())
        }
    }

    fun handlePoint(point: Point) {
        viewModelScope.launch {
            val subsection: GeoJson.Subsection = geoJson.handlePoint(point)
            _subsectionFlow.emit(subsection)
        }
    }
}