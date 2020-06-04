package com.geo.GeoQuake.models

/**
 * Created by George Stinson on 2016-10-24.
 */

class Earthquake {

    var place = ""
    var url: String = ""
    var mag: Double = 0.toDouble()
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    var time: Long = 0
    var timeString = ""
    var source: Int = 0

    constructor() {


    }

    constructor(latitude: Double, longitude: Double, mag: Double, place: String, time: Long, url: String) {
        this.latitude = latitude
        this.longitude = longitude
        this.mag = mag
        this.place = place
        this.time = time
        this.url = url
    }

    constructor(feature: Feature) {
        this.latitude = feature.getLatitude()
        this.longitude = feature.getLongitude()
        this.mag = feature.getProperties().getMag()
        this.place = feature.getProperties().getPlace()
        this.time = feature.getProperties().getTime()
        //FIXME: can't assume this will not be null
        this.url = feature.getProperties().getUrl()
        this.source = Earthquake.USA
    }

    companion object {

        val USA = 0
        val CANADA = 1
    }
}
