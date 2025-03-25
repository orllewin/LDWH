package com.orllewin.ldwh.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.orllewin.ldwh.GeoJson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LdwhTopBar(title: String, subsection: GeoJson.Subsection, onClose: () -> Unit){
    TopAppBar(
        title = {
            when {
                subsection.hasSubsection() -> Text("${subsection.distanceMiles}m | ${subsection.distanceKm}km")
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
        }
    )
}