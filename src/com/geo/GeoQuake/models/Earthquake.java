package com.geo.GeoQuake.models;

/**
 * Created by George Stinson on 2016-10-24.
 */

public class Earthquake {

    protected String place;
    protected String url;
    protected double mag;
    protected double latitude;
    protected double longitude;
    protected long time;

    public Earthquake(double latitude, double longitude, double mag, String place, long time, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.mag = mag;
        this.place = place;
        this.time = time;
        this.url = url;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getMag() {
        return mag;
    }

    public String getPlace() {
        return place;
    }

    public long getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }
}
