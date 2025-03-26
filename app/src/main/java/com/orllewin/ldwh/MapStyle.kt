package com.orllewin.ldwh

sealed class MapStyle {
    object Default: MapStyle()
    object Satellite: MapStyle()
    object Outdoors: MapStyle()
    object NorthStar: MapStyle()
}