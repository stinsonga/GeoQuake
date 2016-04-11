package com.geo.GeoQuake;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

public class QuakeService extends Service {

    public static final String TAG = "QuakeService";
    double mUserLatitude = 0.0;
    double mUserLongitude = 0.0;


    public QuakeService() {
        setUserLocation();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed!");
    }

    public void setUserLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location;
        try {
            location = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(new Criteria(), false));
            if (location != null) {
                mUserLatitude = location.getLatitude();
                mUserLongitude = location.getLongitude();
            } else {
                //TODO: Handle scenarios with no user location
            }
        } catch (SecurityException se) {
            Log.e(TAG, se.getMessage());
        }

    }
}
