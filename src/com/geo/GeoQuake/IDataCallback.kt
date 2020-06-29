package com.geo.GeoQuake

import com.geo.GeoQuake.models.Earthquake
import java.util.*

/**
 * Created by gstinson on 15-06-29.
 */
interface IDataCallback {
    fun dataCallBack(earthquakes: ArrayList<Earthquake>)
    fun asyncUnderway()
}
