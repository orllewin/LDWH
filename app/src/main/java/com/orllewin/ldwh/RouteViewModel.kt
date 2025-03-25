package com.orllewin.ldwh

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RouteViewModel : ViewModel() {

    private val _routeFlow: MutableStateFlow<List<Point>> = MutableStateFlow(listOf())
    var routeFlow = _routeFlow.asStateFlow()

    private val _routeConfigFlow: MutableStateFlow<GeoJson.RouteConfig?> = MutableStateFlow(null)
    var routeConfigFlow = _routeConfigFlow.asStateFlow()

    private val _mapViewPortStateFlow: MutableStateFlow<MapViewportState> = MutableStateFlow(MapViewportState())
    var mapViewportStateFlow = _mapViewPortStateFlow.asStateFlow()

    private val _subsectionFlow: MutableStateFlow<GeoJson.Subsection> = MutableStateFlow(GeoJson.Subsection.empty())
    var subsectionFlow = _subsectionFlow.asStateFlow()

    private val geoJson = GeoJson()

    fun getRoute(context: Context) {
        viewModelScope.launch(Dispatchers.Main){
            geoJson.initialiseRoute(context)
            _routeFlow.emit(geoJson.routePoints())

            val routeConfig = geoJson.routeConfig()
            _routeConfigFlow.emit(routeConfig)

            routeConfig?.let{ config ->
                _mapViewPortStateFlow.emit(
                    MapViewportState(
                        CameraState (
                            Point.fromLngLat(config.longitude, config.latitude),
                            EdgeInsets(0.0, 0.0, 0.0, 0.0),
                            config.zoom,
                            0.0,
                            0.0
                        )
                    )
                )
            }
        }
    }

    fun handlePoint(point: Point) {
        viewModelScope.launch {
            val subsection: GeoJson.Subsection = geoJson.handlePoint(point)
            _subsectionFlow.emit(subsection)
        }
    }

    fun clearSubsection(){
        geoJson.clearSubsection()
        viewModelScope.launch {
            _subsectionFlow.emit(GeoJson.Subsection.empty())
        }
    }
}