# LDWH: Long Distance Walk Helper

A project that generates Android apps that help plan destinations and stopping points along long distance hiking paths, it also acts as a back-up to physical maps and gives you confidence you're on track. The key feature is a simple calculation that measures distances along the path that was missing in various other apps. 

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
* <strike>Initial coordinates and zoom should show entire route</strike> - done.
* Improve location permissions flow
* Allow users adding their own Mapbox tokens in-app 
* Add sunset time to ui based on user location
* In-app ability to add waymarkers with notes (for hotel/hostel bookings etc)

## Adding a path
* The route should be prepared in the [GeoJson](https://geojson.org/) format and composed of a series of `LineString` segments. There are several websites that can convert from the older but more common [.gpx format](https://en.wikipedia.org/wiki/GPS_Exchange_Format) to GeoJson. 
* Create a new [build variant Flavour](https://developer.android.com/build/build-variants). Edit the `productFlavors` section in `/app/build.gradle.kts` file, and the route in alphabetical order.
* Add a new source set directory matching the flavour name under `/app/src`, just copy/paste one of the others
* Add the GeoJson to `app/src/NEW_FLAVOUR_NAME/res/raw` with the filename `route.geojson` 
* Add a new `routeConfig.json` to `app/src/NEW_FLAVOUR_NAME/res/raw`, copy one of the existing files then:
* Find the midpoint of the route for the initial map state, either eyeball it using some mapping software or run the kotlin script: `/tools/centre_point.main.kts`
* Add a new `ic_launcher_foreground.xml` vector icon to `app/src/NEW_FLAVOUR_NAME/res/drawable` - see guide below

## Generating a new app icon
* Create the new icon from the template ([app_icon_template.svg](./app_icon_template.svg)) in Figma/Inkscape/other. Typeface is Helvetica Neue, weight: Medium, size: 96. Use two characters if possible (Pennine Way = PW), three at most (Yorkshire Three Peaks = Y3P)
* Switch Android Studio's file explorer mode to 'Project' instead of 'Android'
* Find the flavour drawable directory: `src/NEW_FLAVOUR_NAME/res/drawable` **NOT** `src/main/res/drawable`
* Right-click > New > Image Asset and make sure Icon type is set to 'Launcher Icons (Adaptive and Legacy)'
* In the Options tab set Legacy Icon, Round Icon, Google Play Store Icon all to No.
* In Foreground Layer tab select the new SVG file using the Path file chooser, tap Next then Finish.
* Delete `ic_launcher_background.xml`, that's identical for all flavours, you just want the new `ic_launcher_foreground.xml` icon in the flavours `res/drawable` source set.
