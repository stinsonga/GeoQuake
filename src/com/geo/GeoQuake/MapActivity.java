package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.support.v4.widget.DrawerLayout;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
//TODO: deal with deprecated imports
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


public class MapActivity extends Activity implements AdapterView.OnItemSelectedListener{
    Context mContext;
    HashMap<String, String> markerInfo = new HashMap<String, String>();
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    boolean mRefreshMap = true;

    DrawerLayout mDrawerLayout;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    Spinner mCacheTimeSpinner;
    CheckBox mActionBarCheckbox;
    CheckBox mWifiCheckbox;

    private GoogleMap mMap;
    GeoQuakeDB geoQuakeDB;

    FeatureCollection mFeatureCollection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_layout);

        mContext = getApplicationContext();
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        geoQuakeDB = new GeoQuakeDB(mContext);

        //Side Nav Begin
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mActionBarCheckbox = (CheckBox) findViewById(R.id.actionbar_toggle_checkbox);
        mActionBarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    getActionBar().hide();
                } else {
                    getActionBar().show();
                }
            }
        });

        mWifiCheckbox = (CheckBox) findViewById(R.id.wifi_checkbox);
        mWifiCheckbox.setChecked(mSharedPreferences.getBoolean(Utils.WIFI_ONLY, false));
        mWifiCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPreferencesEditor = mSharedPreferences.edit();
                mSharedPreferencesEditor.putBoolean(Utils.WIFI_ONLY, isChecked);
                mSharedPreferencesEditor.apply();
                Log.i("wifi_refresh pref changed:", ""+isChecked);
            }
        });
        mCacheTimeSpinner = (Spinner) findViewById(R.id.cache_spinner);

        mQuakeTypeSpinner = (Spinner) findViewById(R.id.quake_type_spinner);
        ArrayAdapter<CharSequence> quakeTypeAdapter = ArrayAdapter.createFromResource(this, R.array.quake_types, android.R.layout.simple_spinner_dropdown_item);
        mQuakeTypeSpinner.setAdapter(quakeTypeAdapter);
        mQuakeTypeSpinner.setSelection(4);

        mDurationTypeSpinner = (Spinner) findViewById(R.id.duration_type_spinner);
        ArrayAdapter<CharSequence> durationAdapter = ArrayAdapter.createFromResource(this, R.array.duration_types, android.R.layout.simple_spinner_dropdown_item);
        mDurationTypeSpinner.setAdapter(durationAdapter);
        mDurationTypeSpinner.setSelection(0);

        mDurationTypeSpinner.setOnItemSelectedListener(this);
        mQuakeTypeSpinner.setOnItemSelectedListener(this);
        mCacheTimeSpinner.setOnItemSelectedListener(this);
        //Side Nav End

        setUpMap();
        networkCheckFetchData();

    }

    /**
     * Side-nav onItemSelect handler
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        switch(parent.getId()){
            case(R.id.quake_type_spinner):case(R.id.duration_type_spinner):
                mRefreshMap = true;
                networkCheckFetchData();
                break;
            case(R.id.cache_spinner):
                Utils.changeCache(mCacheTimeSpinner.getSelectedItemPosition(), mSharedPreferences,
                        getResources().getStringArray(R.array.cache_values));
                break;
            default:
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (GeoQuakeDB.checkRefreshLimit(Long.parseLong(GeoQuakeDB.getTime()),
                        mSharedPreferences.getLong(Utils.REFRESH_LIMITER, 0))) {
                    Log.i("ok to refresh?", "YES");
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putLong(Utils.REFRESH_LIMITER, Long.parseLong(GeoQuakeDB.getTime()));
                    editor.apply();
                    mRefreshMap = true;
                    networkCheckFetchData();
                } else {
                    Log.i("ok to refresh?", "NO");
                    Toast.makeText(mContext, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                    Log.i("can refresh again at: ", ""+mSharedPreferences.getLong(Utils.REFRESH_LIMITER,
                            0)+Utils.REFRESH_LIMITER_TIME);
                    Log.i("current time: ", GeoQuakeDB.getTime());
                }
                break;
            case R.id.action_settings:
                if(mDrawerLayout.isDrawerOpen(Gravity.LEFT)){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else{
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            case R.id.action_list:
                Intent intent = new Intent(this, ListQuakes.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return false;
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

    public void networkCheckFetchData(){
        if (Utils.checkNetwork(mContext)) {
            fetchData();
        } else {
            Utils.connectToast(mContext);
        }
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
        if (Utils.checkNetwork(mContext)) {
            if (mMap == null) {
                mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.gmap))
                        .getMap();
                if (mMap != null) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    mMap.setMyLocationEnabled(true);
                    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location arg0) {
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

        } else {
            Utils.connectToast(mContext);
        }
    }

    /**
     * The method that does the work of placing the markers on the map. Yes.
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

    private void fetchData() {
        Utils.fireToast(mDurationTypeSpinner.getSelectedItemPosition(), mQuakeTypeSpinner.getSelectedItemPosition(), mContext);
        try {
            new AsyncTask<URL, Void, FeatureCollection>() {
                @Override
                protected FeatureCollection doInBackground(URL... params) {
                    try {
                        return getJSON(new URL(mContext.getString(R.string.usgs_url) + Utils.getURLFrag(
                                mQuakeTypeSpinner.getSelectedItemPosition(),
                                mDurationTypeSpinner.getSelectedItemPosition(), mContext)));
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
                    if(mRefreshMap){
                        placeMarkers();
                        mRefreshMap = false;
                    }

                }
            }.execute(new URL(mContext.getString(R.string.usgs_url) + Utils.getURLFrag(mQuakeTypeSpinner.getSelectedItemPosition(),
            mDurationTypeSpinner.getSelectedItemPosition(), mContext)));
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
     *
     * @param id The map marker id
     * @return URL value in hashmap
     */
    private String getURLFromMarker(String id){
        return markerInfo.get(id);
    }

}
