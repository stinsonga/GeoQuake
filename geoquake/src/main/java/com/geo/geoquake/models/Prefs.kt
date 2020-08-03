package com.geo.geoquake.models

import android.content.Context
import android.content.SharedPreferences
import com.geo.geoquake.App.Companion.appContext
import com.geo.geoquake.GeoQuakeDB
import java.util.*

/**
 * Created by George Stinson on 2017-03-28.
 */
class Prefs private constructor() {
    private val sharedPreferences: SharedPreferences
    fun setRefreshLimiter() {
        val e = sharedPreferences.edit()
        e.putLong(KEYS.REFRESH_LIMITER, GeoQuakeDB.getTime())
        e.apply()
    }

    val refreshLimiter: Long
        get() = sharedPreferences.getLong(KEYS.REFRESH_LIMITER, 0)

    var cacheTime: String?
        get() = sharedPreferences.getString(KEYS.CACHE_TIME, "300000")
        set(cacheTime) {
            val e = sharedPreferences.edit()
            e.putString(KEYS.CACHE_TIME, cacheTime)
            e.apply()
        }

    var source: Int
        get() = sharedPreferences.getInt(KEYS.SOURCE, 0)
        set(source) {
            val e = sharedPreferences.edit()
            e.putInt(KEYS.SOURCE, source)
            e.apply()
        }

    private object KEYS {
        const val REFRESH_LIMITER = "refresh_limiter"
        const val CACHE_TIME = "cache_time"
        const val SOURCE = "source"
    }

    companion object {
        const val QUAKE_PREFS = "quake_prefs"
        private var prefs: Prefs? = null
        @JvmStatic
        val instance: Prefs?
            get() {
                if (prefs == null) {
                    prefs = Prefs()
                }
                return prefs
            }
    }

    init {
        sharedPreferences = appContext!!.getSharedPreferences(QUAKE_PREFS, Context.MODE_PRIVATE)
    }
}