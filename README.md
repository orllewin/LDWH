# LDWH: Long Distance Walk Helper

A project that generates Android apps that help plan destinations and stopping points along long distance hiking paths, it also acts as a back-up to physical maps and gives you confidence you're on track. The key feature is a simple calculation that measures distances along the path that was missing in other apps. 

## Build instructions

* Install [Android Studio](https://developer.android.com/studio)
* Get a Mapbox token: [docs.mapbox.com/help/getting-started/access-tokens](https://docs.mapbox.com/help/getting-started/access-tokens/)
* Create a `mapbox.properties` file in the root of the project and add your Mapbox access token:
```
MAPBOX_ACCESS_TOKEN=YourAccessTokenHere
```

## Contributing

Choose a task, or suggest something else, let [@oppen@merveilles.town](https://merveilles.town/@oppen) know so they don't start work on the same thing.

### Outstanding tasks/features
* Initial coordinates and zoom should show entire route
* Improve location permissions flow
* Allow users adding their own Mapbox tokens in-app 
* Add sunset time to ui based on user location
* In-app ability to add waymarkers with notes (for hotel/hostel bookings etc)

## Adding a path
* The route should prepared in the [GeoJson](https://geojson.org/) format and composed of a series of `LineString` segments. There are several websites that can convert from the older but more common [.gpx format](https://en.wikipedia.org/wiki/GPS_Exchange_Format) to GeoJson. 
* Create a new [build variant Flavour](https://developer.android.com/build/build-variants). Edit the `productFlavors` section in `/app/build.gradle.kts` file, and the route in alphabetical order.
* Add a new source set directory matching the flavour name under `/app/src`, just copy/paste one of the others
* Add the GeoJson to `app/src/NEW_FLAVOUR_NAME/res/raw` with the filename `route.geojson` 
* Add a new `ic_launcher_foreground.xml` vector icon to `app/src/NEW_FLAVOUR_NAME/res/drawable` - see guide below

## Generating a new app icon
//todo
