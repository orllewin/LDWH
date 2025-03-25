package com.orllewin.ldwh

import android.content.Context
import android.location.Location
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

const val METERS_IN_MILE = 1609.344
const val METERS_IN_KILOMETER = 1000.00

class GeoJson {

    data class Subsection(
        val startPoint: Point? = null,
        val endPoint: Point? = null,
        val subsection: List<Point>? = null,
        val distanceMiles: String? = null,
        val distanceKm: String? = null
    ) {
        companion object {
            fun empty(): Subsection = Subsection()
        }

        fun isEmpty(): Boolean = startPoint == null && endPoint == null
        fun isNotEmpty(): Boolean = !isEmpty()
        fun hasSubsection(): Boolean = subsection != null
    }

    data class RouteConfig(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val zoom: Double,
        val loop: Boolean
    )

    private val routePoints = mutableListOf<Point>()
    private var routeConfig: RouteConfig? = null

    private enum class RouteStatus{
        IDLE, START_SET, END_SET
    }

    private var routeStatus = RouteStatus.IDLE

    fun initialiseRoute(context: Context){
        context.resources.openRawResource(R.raw.route).use { inputStream ->
            inputStream.reader().use { streamReader ->
                val routeJsonStr = streamReader.readText()
                val mapboxFeatureCollection = FeatureCollection.fromJson(routeJsonStr)
                mapboxFeatureCollection.features()?.forEach { feature ->
                    if(feature.geometry()?.type() == "LineString"){
                        routePoints.addAll(LineString.fromJson(feature.geometry()?.toJson()).coordinates())
                    }
                }
            }
        }

        context.resources.openRawResource(R.raw.route_config).use { inputStream ->
            inputStream.reader().use { streamReader ->
                val routeConfigJson = JSONObject(streamReader.readText())
                routeConfig = RouteConfig(
                    name = routeConfigJson.getString("name"),
                    latitude = routeConfigJson.getDouble("centreLatitude"),
                    longitude = routeConfigJson.getDouble("centreLongitude"),
                    zoom = routeConfigJson.getDouble("initialZoom"),
                    loop = routeConfigJson.getBoolean("loop")
                )
            }
        }
    }

    fun routePoints(): List<Point> = routePoints
    fun routeConfig(): RouteConfig? = routeConfig

    private var tappedStart: Point? = null
    private var startOnRoute: Point? = null
    private var tappedEnd: Point? = null
    private var endOnRoute: Point? = null

    fun handlePoint(point: Point): Subsection{
        when(routeStatus){
            RouteStatus.IDLE, RouteStatus.END_SET -> {
                //Set start point only
                tappedStart = point
                startOnRoute = findNearestPointOnRoute(point)
                tappedEnd = null
                endOnRoute = null

                routeStatus = RouteStatus.START_SET

                return Subsection(
                    startPoint = startOnRoute,
                )
            }
            RouteStatus.START_SET -> {
                //Emit full subsection including distance
                tappedEnd = point

                val terminalIndexes: Pair<Int, Int> = findNearestPointIndexesOnRoute(tappedStart!!, tappedEnd!!)


                val subsection = when {
                    terminalIndexes.first < terminalIndexes.second -> routePoints.subList(terminalIndexes.first, terminalIndexes.second + 1)
                    else -> routePoints.subList(terminalIndexes.second, terminalIndexes.first + 1)
                }

                var prevPoint = subsection.first()
                var totalMeters = 0f

                for (i in 1..subsection.size - 1) {
                    val point = subsection[i]
                    totalMeters = totalMeters + distance(point, prevPoint)
                    prevPoint = point
                }

                val miles = totalMeters / METERS_IN_MILE
                val kilometers = totalMeters / METERS_IN_KILOMETER

                routeStatus = RouteStatus.END_SET

                return Subsection(
                    startPoint = routePoints[terminalIndexes.first],
                    endPoint = routePoints[terminalIndexes.second],
                    subsection = subsection,
                    distanceMiles = "${BigDecimal(miles).setScale(1, RoundingMode.HALF_EVEN)}",
                    distanceKm = "${BigDecimal(kilometers).setScale(1, RoundingMode.HALF_EVEN)}",
                )
            }
        }
    }

    fun clearSubsection(){
        tappedStart = null
        startOnRoute = null
        tappedEnd = null
        endOnRoute = null
        routeStatus = RouteStatus.IDLE
    }

    private fun distance(ll1: Point, ll2: Point): Float {
        val dist = FloatArray(1)
        Location.distanceBetween(ll1.latitude(), ll1.longitude(), ll2.latitude(), ll2.longitude(), dist)
        return dist[0]
    }

    /**
     * Used when a we only have a start point - eg. when routeStatus is IDLE or END_SET
     */
    fun findNearestPointOnRoute(point: Point): Point {
        var closestDistance = Float.MAX_VALUE
        var closestIndex = -1

        routePoints.forEachIndexed { index, routePoint ->
            val distanceToRoutePoint = distance(point, routePoint)
            if (distanceToRoutePoint < closestDistance) {
                closestDistance = distanceToRoutePoint
                closestIndex = index
            }
        }

        return routePoints[closestIndex]
    }

    private fun findNearestPointIndexesOnRoute(startPoint: Point, endPoint: Point): Pair<Int, Int> {
        var distanceToStart = Float.MAX_VALUE
        var closestStartIndex = -1

        var distanceToEnd = Float.MAX_VALUE
        var closestEndIndex = -1

        routePoints.forEachIndexed { index, point ->
            val hereToStart = distance(point, startPoint)
            if(hereToStart < distanceToStart){
                distanceToStart = hereToStart
                closestStartIndex = index
            }

            val hereToEnd = distance(point, endPoint)
            if (hereToEnd < distanceToEnd) {
                distanceToEnd = hereToEnd
                closestEndIndex = index
            }
        }

        return Pair(closestStartIndex, closestEndIndex)
    }
}