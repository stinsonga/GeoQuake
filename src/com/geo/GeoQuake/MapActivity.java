package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.support.v4.widget.DrawerLayout;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.util.HashMap;


public class MapActivity extends Activity implements AdapterView.OnItemSelectedListener, IDataCallback {
    Context mContext;
    Bundle mBundle;
    HashMap<String, String> markerInfo = new HashMap<String, String>();
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    boolean mRefreshMap = true;
    boolean mAsyncUnderway = false;

    LinearLayout mDrawerLinearLayout;
    DrawerLayout mDrawerLayout;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    Spinner mCacheTimeSpinner;
    CheckBox mActionBarCheckbox;
    CheckBox mWifiCheckbox;

    private GoogleMap mMap;
    GeoQuakeDB mGeoQuakeDB;

    FeatureCollection mFeatureCollection;
    QuakeData mQuakeData;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity_layout);

        mContext = getApplicationContext();
        mBundle = new Bundle();
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mGeoQuakeDB = new GeoQuakeDB(mContext);

        //Side Nav Begin
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinearLayout = (LinearLayout) findViewById(R.id.drawer_root);
        mDrawerLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
            }
        });
        mActionBarCheckbox = (CheckBox) findViewById(R.id.actionbar_toggle_checkbox);
        mActionBarCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case (R.id.quake_type_spinner):
                mStrengthSelection = mQuakeTypeSpinner.getSelectedItemPosition();
                break;
            case (R.id.duration_type_spinner):
                mDurationSelection = mDurationTypeSpinner.getSelectedItemPosition();
                break;
            case (R.id.cache_spinner):
                Utils.changeCache(mCacheTimeSpinner.getSelectedItemPosition(), mSharedPreferences,
                        getResources().getStringArray(R.array.cache_values));
                break;
            default:
                break;

        }
    }

    /**
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.map_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mDrawerLayout.closeDrawers();
                if(!mAsyncUnderway){
                    if (GeoQuakeDB.checkRefreshLimit(Long.parseLong(GeoQuakeDB.getTime()),
                            mSharedPreferences.getLong(Utils.REFRESH_LIMITER, 0))) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putLong(Utils.REFRESH_LIMITER, Long.parseLong(GeoQuakeDB.getTime()));
                        editor.apply();
                        mRefreshMap = true;
                        networkCheckFetchData();
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                    }

                } else{
                    Toast.makeText(mContext, getResources().getString(R.string.wait_for_loading), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_settings:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
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
    }

    /**
     * Checking the network before we bother trying to grab data
     */
    public void networkCheckFetchData() {
        if (Utils.checkNetwork(mContext)) {
            fetchData();
        } else {
            if(!mGeoQuakeDB.getData(""+mStrengthSelection, ""+mDurationSelection).isEmpty()){
                mFeatureCollection = new FeatureCollection(mGeoQuakeDB
                        .getData("" + mStrengthSelection, "" + mDurationSelection));
                mAsyncUnderway = false;
                Toast.makeText(mContext, getResources().getString(R.string.using_saved), Toast.LENGTH_SHORT).show();
                if (mRefreshMap) {
                    placeMarkers();
                    //A bit of a hack/fix for initial load, where the bastard spams several callbacks
                    if(mFeatureCollection.getFeatures().size() > 0){
                        mRefreshMap = false;
                    }
                }
            } else {
                Utils.connectToast(mContext);
            }
        }
    }

    /**
     * We're not using this method right now. That's how it goes.
     *
     * @return
     */
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
    private void placeMarkers() {
        mMap.clear();
        try {
            if (mFeatureCollection != null) {
                for (Feature feature : mFeatureCollection.getFeatures()) {
                    LatLng coords = new LatLng(feature.getLatitude(), feature.getLongitude());
                    BitmapDescriptor quakeIcon;
                    if (feature.getProperties().getMag() <= 1.00) {
                        quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake1);
                    } else if (feature.getProperties().getMag() <= 2.50) {
                        quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake2);
                    } else if (feature.getProperties().getMag() <= 4.50) {
                        quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake3);
                    } else {
                        quakeIcon = BitmapDescriptorFactory.fromResource(R.drawable.quake4);
                    }

                    Marker m = mMap.addMarker(new MarkerOptions().icon(quakeIcon).position(coords).title(feature.getProperties().getPlace()).snippet(getResources().getString(R.string.magnitude) + feature.getProperties().getMag()));
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

    /**
     * Send the request to the QuakeData class to grab new data
     */
    private void fetchData() {
        Utils.fireToast(mDurationTypeSpinner.getSelectedItemPosition(), mQuakeTypeSpinner.getSelectedItemPosition(), mContext);
        mQuakeData = new QuakeData(mContext.getString(R.string.usgs_url),
                mDurationTypeSpinner.getSelectedItemPosition(),
                mQuakeTypeSpinner.getSelectedItemPosition(), this, mContext);
        mQuakeData.fetchData(mContext);
    }


    /**
     * @param id The map marker id
     * @return URL value in hashmap
     */
    private String getURLFromMarker(String id) {
        return markerInfo.get(id);
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback(){
        //update map with data
        mFeatureCollection = mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        if (mRefreshMap) {
            placeMarkers();
            //A bit of a hack/fix for initial load, where the bastard spams several callbacks
            if(mFeatureCollection.getFeatures().size() > 0){
                mRefreshMap = false;
            }
        }
    }

    /**
     * Lets the activity know that an async data call is underway
     */
    @Override
    public void asyncUnderway(){
        mAsyncUnderway = true;
    }

    /**
     *
     * @param config
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("config", "landscape");
        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("config", "portrait");
        }
    }

    /**
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getBundle("mBundle");
    }

}
