@file:DependsOn(
    "org.json:json:20250107"
)

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.math.BigDecimal
import kotlin.system.exitProcess

/**
 * Find the central point for a long distance path by averaging the latitude and longitude values
 * @param path to geojson file
 */
if(args.isEmpty()){
    println("Usage: kotlin centre_point.main.kts ../path/to/file.geojson")
    println("eg. kotlin centre_point.main.kts ../app/src/pennine_way/res/raw/route.geojson")
    exitProcess(1)
}

val file = File(args.first())

if(!file.exists()){
    println("Can't find file at ${file.path}")
    exitProcess(1)
}

val json = JSONObject(file.readText())

if(json.getString("type") != "FeatureCollection"){
    println("GeoJson must be a FeatureCollection")
    exitProcess(1)
}

val features: JSONArray = json.getJSONArray("features")

var coordinateCount = 0
var latitudeSum = 0f
var longitudeSum = 0f

val featureCount = features.length()
repeat(featureCount){ featureIndex ->
    println("Processing feature ${featureIndex + 1} of $featureCount")
    val feature = features.getJSONObject(featureIndex)
    val featureGeometry = feature.getJSONObject("geometry")
    if(featureGeometry.getString("type") == "LineString"){
        val featureCoordinates = featureGeometry.getJSONArray("coordinates")
        repeat(featureCoordinates.length()){ coordinateIndex ->
            val coordinatePair = featureCoordinates.getJSONArray(coordinateIndex)
            val longitude = coordinatePair[0] as BigDecimal
            val latitude = coordinatePair[1] as BigDecimal

            coordinateCount++

            latitudeSum+=latitude.toFloat()
            longitudeSum+=longitude.toFloat()
        }
    }
}

val centreLatitude = latitudeSum/coordinateCount
val centreLongitude = longitudeSum/coordinateCount

println("\n\nCentre coordinate: $centreLatitude,$centreLongitude")
println("https://www.google.com/maps/search/$centreLatitude,+$centreLongitude\n\n")
