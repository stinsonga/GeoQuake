package com.geo.GeoQuake

import com.geo.GeoQuake.models.Earthquake
import com.geo.GeoQuake.models.FeatureCollection

import java.util.ArrayList

/**
 * Created by gstinson on 15-06-29.
 */
interface IDataCallback {
    fun dataCallBack(earthquakes: ArrayList<Earthquake>)
    fun asyncUnderway()
}
