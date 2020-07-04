package com.geo.geoquake

import com.geo.geoquake.models.Earthquake
import java.util.*

/**
 * Created by gstinson on 15-06-29.
 */
interface IDataCallback {
    fun dataCallBack(earthquakes: ArrayList<Earthquake>)
    fun asyncUnderway()
}
