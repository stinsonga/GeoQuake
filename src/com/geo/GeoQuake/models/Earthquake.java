package com.geo.GeoQuake.models;

/**
 * Created by George Stinson on 2016-10-24.
 */

public class Earthquake {

    protected String place = "";
    protected String url;
    protected double mag;
    protected double latitude;
    protected double longitude;
    protected long time;
    protected String timeString = "";

    public Earthquake() {


    }

    public Earthquake(double latitude, double longitude, double mag, String place, long time, String url) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.mag = mag;
        this.place = place;
        this.time = time;
        this.url = url;
    }

    public Earthquake(Feature feature) {
        this.latitude = feature.getLatitude();
        this.longitude = feature.getLongitude();
        this.mag = feature.getProperties().getMag();
        this.place = feature.getProperties().getPlace();
        this.time = feature.getProperties().getTime();
        this.url = feature.getProperties().getUrl();
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

    public void setPlace(String place) {
        this.place = place;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMag(double mag) {
        this.mag = mag;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }
}
