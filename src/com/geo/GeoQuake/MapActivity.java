package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.support.v4.widget.DrawerLayout;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class MapActivity extends Activity implements View.OnClickListener{
    Context mContext;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    Button mOptButton;
    Button mOptButtonOff;
    RelativeLayout mOptsHolder;
    HashMap<String, String> markerInfo = new HashMap<String, String>();

    private GoogleMap mMap;
    GeoQuakeDB geoQuakeDB;

    FeatureCollection mFeatureCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_layout);

        mContext = getApplicationContext();
        geoQuakeDB = new GeoQuakeDB(mContext);

        mQuakeTypeSpinner = (Spinner) findViewById(R.id.quake_type_spinner);
        ArrayAdapter<CharSequence> quakeTypeAdapter = ArrayAdapter.createFromResource(this, R.array.quake_types, android.R.layout.simple_spinner_dropdown_item);
        mQuakeTypeSpinner.setAdapter(quakeTypeAdapter);
        mQuakeTypeSpinner.setSelection(4);

        mDurationTypeSpinner = (Spinner) findViewById(R.id.duration_type_spinner);
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this, R.array.duration_types, android.R.layout.simple_spinner_dropdown_item);
        mDurationTypeSpinner.setAdapter(durationAdapter);
        mDurationTypeSpinner.setSelection(0);

        mOptButton = (Button) findViewById(R.id.opt_button);
        mOptButtonOff = (Button) findViewById(R.id.opt_button_off);
        mOptsHolder = (RelativeLayout) findViewById(R.id.opts_holder);

        if (checkNetwork()) {
            setUpMap();
            processJSON();
        } else {
            connectToast();
        }

        mDurationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (checkNetwork()) {
                    processJSON();
                } else {
                    connectToast();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mQuakeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (checkNetwork()) {
                    processJSON();
                } else {
                    connectToast();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }


    @Override
    public void onClick(View view){
        switch(view.getId()){
            case (R.id.opt_button):
                mOptButton.setVisibility(View.GONE);
                mOptsHolder.setVisibility(View.VISIBLE);
                getActionBar().show();
                break;
            case(R.id.opt_button_off):
                mOptsHolder.setVisibility(View.GONE);
                mOptButton.setVisibility(View.VISIBLE);
                getActionBar().hide();
                break;
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (checkNetwork()) {
//            setUpMap();
//            processJSON();
//        } else {
//            connectToast();
//        }
    }

    public GoogleMapOptions mapOptions() {
        GoogleMapOptions opts = new GoogleMapOptions();
        opts.mapType(GoogleMap.MAP_TYPE_HYBRID).compassEnabled(true);
        return opts;
    }

    /**
     * Contains the basics for setting up the map.
     */
    private void setUpMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.gmap))
                    .getMap();
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener(){
                    @Override
                    public void onMyLocationChange(Location arg0){
                        LatLng latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, getApplicationContext().getResources().getInteger(R.integer.zoom_level));
                        mMap.animateCamera(cameraUpdate);
                        mMap.setOnMyLocationChangeListener(null);

                    }
                });

                UiSettings settings = mMap.getUiSettings();
                settings.setCompassEnabled(true);
                settings.setMyLocationButtonEnabled(true);
            }
        }
    }

    /*
        USGC JSON Objects have four keys: features, type, bbox, metadata

        Within features are the main data points of interest for this application: type, properties, geometry, id

     */
    private void placeMarkers(){
        mMap.clear();
        try {
            if(mFeatureCollection != null){
                for(Feature feature : mFeatureCollection.getFeatures()){
                    LatLng coords = new LatLng(feature.getLatitude(), feature.getLongitude());
                    Marker m = mMap.addMarker(new MarkerOptions().position(coords).title(feature.getProperties().getPlace()).snippet(getResources().getString(R.string.magnitude)+feature.getProperties().getMag()));
                    markerInfo.put(m.getId(), feature.getProperties().getUrl());

                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            Intent intent = new Intent(MapActivity.this, WebInfoActivity.class);
                            intent.putExtra("url", getURLFromMarker(marker.getId()));
                            startActivity(intent);


                        }
                    });
                }
            }
        } catch (Exception me) {
            me.printStackTrace();
        }
    }

    private void processJSON() {
        fireToast();
        try {
            new AsyncTask<URL, Void, FeatureCollection>() {
                @Override
                protected FeatureCollection doInBackground(URL... params) {
                    try {
                        return getJSON(new URL(mContext.getString(R.string.usgs_url) + getURLFrag()));
                    } catch (MalformedURLException me) {
                        return null;
                    }

                }

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(FeatureCollection featureCollection) {
                    super.onPostExecute(featureCollection);
                    mFeatureCollection = featureCollection;
                    placeMarkers();

                }
            }.execute(new URL(mContext.getString(R.string.usgs_url) + getURLFrag()));
        } catch (MalformedURLException me) {
            Log.e(me.getMessage(), "URL Problem...");

        }
    }

    /**
     *
     * @param url The url we'll use to fetch the data
     * @return A JSONObject containing the requested data
     */
    private FeatureCollection getJSON(URL url) {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        try {
            response = client.execute(new HttpGet(url.toURI()));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                return new FeatureCollection(out.toString());
            } else {
                response.getEntity().getContent().close();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This towering method could use some honing
     *
     * @return a string representing the proper fragment to pass to the URL string
     */
    private String getURLFrag() {
        int quakeSelection = mQuakeTypeSpinner.getSelectedItemPosition();
        int durationSelection = mDurationTypeSpinner.getSelectedItemPosition();
        if (durationSelection == 0) {
            if (quakeSelection == 0) {
                return mContext.getString(R.string.significant_hour);
            } else if (quakeSelection == 1) {
                return mContext.getString(R.string._4_5_hour);
            } else if (quakeSelection == 2) {
                return mContext.getString(R.string._2_5_hour);
            } else if (quakeSelection == 3) {
                return mContext.getString(R.string._1_0_hour);
            } else if (quakeSelection == 4) {
                return mContext.getString(R.string.all_hour);
            }
        } else if (durationSelection == 1) {
            if (quakeSelection == 0) {
                return mContext.getString(R.string.significant_day);
            } else if (quakeSelection == 1) {
                return mContext.getString(R.string._4_5_day);
            } else if (quakeSelection == 2) {
                return mContext.getString(R.string._2_5_day);
            } else if (quakeSelection == 3) {
                return mContext.getString(R.string._1_0_day);
            } else if (quakeSelection == 4) {
                return mContext.getString(R.string.all_day);
            }
        } else if (durationSelection == 2) {
            if (quakeSelection == 0) {
                return mContext.getString(R.string.significant_week);
            } else if (quakeSelection == 1) {
                return mContext.getString(R.string._4_5_week);
            } else if (quakeSelection == 2) {
                return mContext.getString(R.string._2_5_week);
            } else if (quakeSelection == 3) {
                return mContext.getString(R.string._1_0_week);
            } else if (quakeSelection == 4) {
                return mContext.getString(R.string.all_week);
            }
        }

        /*
        Removed the past month option, due to OOM issues. Code is left here, for a future optimization update when it may be enabled again.

         */
//        else if(durationSelection == 3){
//            if(quakeSelection == 0){
//                return mContext.getString(R.string.significant_month);
//            }else if(quakeSelection == 1){
//                return mContext.getString(R.string._4_5_month);
//            }else if(quakeSelection == 2){
//                return mContext.getString(R.string._2_5_month);
//            }else if(quakeSelection == 3){
//                return mContext.getString(R.string._1_0_month);
//            }else if(quakeSelection == 4){
//                return mContext.getString(R.string.all_month);
//            }
//        }
        return mContext.getString(R.string.significant_week);

    }

    /**
     * Generates the appropriate toast, depending on the anticipated time of the request.
     */
    private void fireToast() {
        Toast toast;
        if (mDurationTypeSpinner.getSelectedItemPosition() == 2 && mQuakeTypeSpinner.getSelectedItemPosition() == 4) {
            toast = Toast.makeText(mContext, mContext.getString(R.string.loading_data_long), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(mContext, mContext.getString(R.string.loading_data), Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * Generates the long toast message
     */
    private void connectToast() {
        Toast toast;
        toast = Toast.makeText(mContext, getResources().getString(R.string.no_network), Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     *
     * @return true if the network connection is ok, false otherwise
     */
    private boolean checkNetwork() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.isConnected() || mobile.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     *
     * @param id The map marker id
     * @return URL value in hashmap
     */
    private String getURLFromMarker(String id){
        return markerInfo.get(id);
    }

}
