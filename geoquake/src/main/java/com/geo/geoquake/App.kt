package com.geo.geoquake

import android.app.Application
import android.content.Context

/**
 * Created by George Stinson on 2017-03-28.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}
