package com.geo.GeoQuake;

/**
 * Created by gstinson on 15-06-29.
 */
public interface IDataCallback {
    void dataCallback(FeatureCollection featureCollection);
    void asyncUnderway();
}
