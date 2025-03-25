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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotation
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.LongValue
import com.mapbox.maps.extension.compose.style.MapStyle
import com.mapbox.maps.extension.compose.style.StringValue
import com.mapbox.maps.extension.compose.style.sources.generated.rememberRasterDemSourceState
import com.mapbox.maps.extension.compose.style.terrain.generated.TerrainState
import com.mapbox.maps.extension.compose.style.terrain.generated.rememberTerrainState
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.orllewin.ldwh.ui.components.LdwhTopBar
import com.orllewin.ldwh.ui.theme.LDWHTheme
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Language
import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val viewModel: RouteViewModel by viewModels()

    @Language("JSON")
    private val STYLE = """
{
  "version": 8,
  "name": "Mapbox Terrain tileset v2",
  "sources": {
    "mapbox-terrain": {
      "type": "vector",
      "url": "mapbox://mapbox.mapbox-terrain-v2"
    }
  },
  "layers": [
    {
      "id": "background",
      "type": "background",
      "paint": {"background-color": "#4444ff"}
    },
    {
      "id": "landcover",
      "source": "mapbox-terrain",
      "source-layer": "landcover",
      "type": "fill",
      "paint": {
        "fill-color": "rgba(66,251,100, 0.3)",
        "fill-outline-color": "rgba(66,251,100, 1)"
      }
    },
    {
      "id": "hillshade",
      "source": "mapbox-terrain",
      "source-layer": "hillshade",
      "type": "fill",
      "paint": {
        "fill-color": "rgba(66,251,100, 0.3)",
        "fill-outline-color": "rgba(66,251,100, 1)"
      }
    },
    {
      "id": "contour",
      "source": "mapbox-terrain",
      "source-layer": "contour",
      "type": "line",
      "paint": {
        "line-color": "#ffffff"
      }
    }
  ]
}
"""

    @OptIn(MapboxExperimental::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel.getRoute(this)

        setContent {

            val routeState by remember(viewModel) {
                viewModel.routeFlow
            }.collectAsState()

            val routeConfigState by remember(viewModel) {
                viewModel.routeConfigFlow
            }.collectAsState()

            val mapViewportState by remember(viewModel) {
                viewModel.mapViewportStateFlow
            }.collectAsState()

            val subsectionState: GeoJson.Subsection by remember(viewModel) {
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

            //terrain
            val rasterDemSourceState = rememberRasterDemSourceState().apply {
                url = StringValue("mapbox://mapbox.mapbox-terrain-v2")
                tileSize = LongValue(514L)
            }

            val customTerrainState = rememberTerrainState(rasterDemSourceState) {
                exaggeration = DoubleValue(4.0)
            }

            var currentTerrainState by rememberSaveable(stateSaver = TerrainState.Saver) {
                mutableStateOf(customTerrainState)
            }

            val aMarker: Painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.a_marker))
            val bMarker: Painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.b_marker))
            val startMarker: Painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.start_marker))
            val endMarker: Painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.end_marker))
            val loopMarker: Painter = rememberVectorPainter(ImageVector.vectorResource(R.drawable.start_end_marker))

            LDWHTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        LdwhTopBar(
                            title = routeConfigState?.name ?: "",
                            subsection = subsectionState
                        ){
                            viewModel.clearSubsection()
                        }
                    },
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
//                            style = {
//                                MapStyle(style = STYLE)
//                            },
//                            style = {
//                                MapStyle(style = Style.OUTDOORS)
//                            },
                            style = { MapStyle(style = "mapbox://styles/orllewin/cm8no21bu002r01qv7nxshea8") },
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

                            if(routeState.isNotEmpty()) {
                                routeConfigState?.let{ config ->
                                    if(config.loop){
                                        val loopMarker = rememberIconImage(
                                            key = routeState.first(),
                                            painter = loopMarker
                                        )
                                        PointAnnotation(point = routeState.first()) {
                                            iconImage = loopMarker
                                            this.iconAnchor = IconAnchor.BOTTOM
                                        }
                                    }else{
                                        val startMarker = rememberIconImage(
                                            key = routeState.first(),
                                            painter = startMarker
                                        )
                                        PointAnnotation(point = routeState.first()) {
                                            iconImage = startMarker
                                            this.iconAnchor = IconAnchor.BOTTOM
                                        }
                                        val endMarker = rememberIconImage(
                                            key = routeState.first(),
                                            painter = endMarker
                                        )
                                        PointAnnotation(point = routeState.last()) {
                                            iconImage = endMarker
                                            this.iconAnchor = IconAnchor.BOTTOM
                                        }
                                    }
                                }
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
                                    painter = aMarker
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
                                            painter = bMarker
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