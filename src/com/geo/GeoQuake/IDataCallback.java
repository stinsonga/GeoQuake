package com.geo.GeoQuake;

import com.geo.GeoQuake.models.FeatureCollection;

/**
 * Created by gstinson on 15-06-29.
 */
public interface IDataCallback {
    void dataCallback(FeatureCollection featureCollection);
    void asyncUnderway();
}
