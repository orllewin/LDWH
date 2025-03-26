package com.orllewin.ldwh.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orllewin.ldwh.GeoJson
import com.orllewin.ldwh.MapStyle
import com.orllewin.ldwh.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LdwhTopBar(
    title: String,
    subsection: GeoJson.Subsection,
    onNextStyle: () -> Unit,
    onNextElevation: () -> Unit,
    onClose: () -> Unit){

    var menuShowing by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        title = {
            when {
                subsection.hasSubsection() -> {
                    Column {
                        Text(
                            modifier = Modifier.padding(0.dp),
                            style = TextStyle(
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Bottom,
                                    trim = LineHeightStyle.Trim.None
                                )
                            ),
                            text = "${subsection.distanceKm}km",
                            fontSize = 22.sp
                        )
                        Text(
                            modifier = Modifier.padding(0.dp),
                            text = "${subsection.distanceMiles}miles",
                            style = TextStyle(
                                lineHeightStyle = LineHeightStyle(
                                    alignment = LineHeightStyle.Alignment.Top,
                                    trim = LineHeightStyle.Trim.None
                                )
                            ),
                            fontSize = 12.sp
                        )
                    }

                }
                else -> Text(text = title)
            }
        },
        navigationIcon = {
            if(subsection.hasSubsection()){
                IconButton(
                    onClick = { onClose() },
                ) {
                  Icon(Icons.Default.Close, "Cancel")
                }
            }
        },
        actions = {
            IconButton(
                onClick = {
                    onNextStyle()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.layers),
                    contentDescription = null
                )
            }
            IconButton(
                onClick = {
                    onNextElevation()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.elevation),
                    contentDescription = null
                )
            }
            IconButton(onClick = {
                menuShowing = !menuShowing
            }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Settings"
                )
            }
//            DropdownMenu(
//                expanded = menuShowing,
//                onDismissRequest = { menuShowing = false },
//            ) {
//                DropdownMenuItem(
//                    text = {
//                        Text("Style: Standard")
//                    },
//                    onClick = {
//                        onStyleChange(MapStyle.Default)
//                        menuShowing = false
//                    },
//                )
//                DropdownMenuItem(
//                    text = {
//                        Text("Style: Outdoors")
//                    },
//                    onClick = {
//                        onStyleChange(MapStyle.Outdoors)
//                        menuShowing = false
//                    },
//                )
//                DropdownMenuItem(
//                    text = {
//                        Text("Style: Terrain")
//                    },
//                    onClick = {
//                        onStyleChange(MapStyle.Terrain)
//                        menuShowing = false
//                    },
//                )
//                DropdownMenuItem(
//                    text = {
//                        Text("Style: Terrain x2")
//                    },
//                    onClick = {
//                        onStyleChange(MapStyle.TerrainExaggerated)
//                        menuShowing = false
//                    },
//                )
//                DropdownMenuItem(
//                    text = {
//                        Text("Style: North Star")
//                    },
//                    onClick = {
//                        onStyleChange(MapStyle.NorthStar)
//                        menuShowing = false
//                    },
//                )
//
//            }
        },
    )
}