package com.orllewin.ldwh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.MapOptions
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.animation.CameraAnimatorType
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.orllewin.ldwh.ui.theme.LDWHTheme
import kotlinx.coroutines.launch
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val viewModel: RouteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.getRoute(this)

        setContent {

            val routeState by remember(viewModel) {
                viewModel.routeFlow
            }.collectAsState()

            val subsectionState by remember(viewModel) {
                viewModel.subsectionFlow
            }.collectAsState()


            val context = LocalContext.current

            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var permissionRequestCount by remember {
                mutableStateOf(1)
            }
            var showMap by remember {
                mutableStateOf(false)
            }
            var showRequestPermissionButton by remember {
                mutableStateOf(false)
            }

            var blueMarker: Painter = painterResource(R.drawable.ic_blue_marker)

            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    zoom(10.0)
                    center(Point.fromLngLat(53.826, -2.422))
                    pitch(0.0)
                    bearing(0.0)
                }
            }

            LDWHTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = {
                                mapViewportState.transitionToFollowPuckState()
                            }
                        ) {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                                contentDescription = "Locate button"
                            )
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(snackbarHostState)
                    },
                    bottomBar = {
                        if(subsectionState.hasSubsection()){
                            Row(
                                modifier = Modifier.height(75.dp).fillMaxWidth().background(MaterialTheme.colorScheme.secondary),
                                horizontalArrangement = Arrangement.Absolute.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "${subsectionState.distanceMiles}m | ${subsectionState.distanceKm}km",
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 25.sp
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    RequestLocationPermission(
                        requestCount = permissionRequestCount,
                        onPermissionDenied = {
                            scope.launch {
                                snackbarHostState.showSnackbar("You need to accept location permissions.")
                            }
                            showRequestPermissionButton = true
                        },
                        onPermissionReady = {
                            showRequestPermissionButton = false
                            showMap = true
                        }
                    )
                    if (showMap) {

                        MapboxMap(
                            Modifier
                                .fillMaxSize()
                                .padding(top = innerPadding.calculateTopPadding()),
                            mapViewportState = mapViewportState,
                            onMapClickListener = { point ->

                                return@MapboxMap true
                            },
                            onMapLongClickListener = { point ->
                                viewModel.handlePoint(point)
                                return@MapboxMap true
                            }
                        ) {
                            MapEffect(Unit) { mapView ->
                                mapView.location.updateSettings {
                                    locationPuck = createDefault2DPuck(withBearing = true)
                                    puckBearingEnabled = true
                                    puckBearing = PuckBearing.HEADING
                                    enabled = true
                                }
                                mapViewportState.transitionToFollowPuckState()
                            }
                            /*
                        MapEffect(Unit) { mapView ->
                            // Use mapView to access the Mapbox Maps APIs not in the Compose extension.
                            // Changes inside `MapEffect` may conflict with Compose states.
                            // For example, to enable debug mode:
                            mapView.debugOptions = setOf(
                                MapViewDebugOptions.TILE_BORDERS,
                                MapViewDebugOptions.PARSE_STATUS,
                                MapViewDebugOptions.TIMESTAMPS,
                                MapViewDebugOptions.COLLISION,
                                MapViewDebugOptions.STENCIL_CLIP,
                                MapViewDebugOptions.DEPTH_BUFFER,
                                MapViewDebugOptions.MODEL_BOUNDS,
                                MapViewDebugOptions.TERRAIN_WIREFRAME,
                            )
                        }

                         */

                            PolylineAnnotation(
                                points = routeState
                            ) {
                                lineColor = Color(0xfffc4f08)
                                lineWidth = 4.0
                            }

                            //Subsection------------------------------------------------------------
                            if (subsectionState.hasSubsection()) {
                                PolylineAnnotation(
                                    points = subsectionState.subsection ?: listOf()
                                ) {
                                    lineColor = Color(0xFF08A7FC)
                                    lineWidth = 5.0
                                }
                            }

                            subsectionState.startPoint?.let { startPoint ->
                                val marker = rememberIconImage(
                                    key = startPoint,
                                    painter = blueMarker
                                )
                                PointAnnotation(point = startPoint) {
                                    iconImage = marker
                                    this.iconAnchor = IconAnchor.BOTTOM
                                    interactionsState.onClicked {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "First",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        interactionsState.onClicked {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Second",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            true
                                        }
                                        true
                                    }
                                }

                                subsectionState.endPoint?.let { endPoint ->
                                    val endMarker =
                                        rememberIconImage(
                                            key = endPoint,
                                            painter = painterResource(R.drawable.ic_blue_marker)
                                        )
                                    PointAnnotation(point = endPoint) {
                                        iconImage = endMarker
                                        this.iconAnchor = IconAnchor.BOTTOM
                                        interactionsState.onClicked {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "First",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            interactionsState.onClicked {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Second",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                true
                                            }
                                            true
                                        }
                                    }
                                }
                            }

                            if (showRequestPermissionButton) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(modifier = Modifier.align(Alignment.Center)) {
                                        Button(
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            onClick = {
                                                permissionRequestCount += 1
                                            }
                                        ) {
                                            Text("Request permission again ($permissionRequestCount)")
                                        }
                                        Button(
                                            modifier = Modifier.align(Alignment.CenterHorizontally),
                                            onClick = {
                                                context.startActivity(
                                                    Intent(
                                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                        Uri.fromParts("package", packageName, null)
                                                    )
                                                )
                                            }
                                        ) {
                                            Text("Show App Settings page")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LDWHTheme {
        Greeting("Android")
    }
}