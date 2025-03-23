package com.orllewin.ldwh

import android.app.Application
import com.mapbox.common.MapboxOptions

class LDWHApp: Application() {

    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
    }
}