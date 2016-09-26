package com.geo.GeoQuake;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IDataCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    SharedPreferences mSharedPreferences;
    Bundle mBundle;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.options_root)
    RelativeLayout mDrawerLinearLayout;

    @Bind(R.id.quake_type_spinner)
    Spinner mQuakeTypeSpinner;

    @Bind(R.id.duration_type_spinner)
    Spinner mDurationTypeSpinner;

    @Bind(R.id.cache_spinner)
    Spinner mCacheTimeSpinner;

//    @Bind(R.id.wifi_checkbox)
//    CheckBox mWifiCheckbox;

    @Bind(R.id.loading_overlay)
    RelativeLayout mLoadingOverlay;

    boolean mAsyncUnderway = false;

    FeatureCollection mFeatureCollection;
    GeoQuakeDB mGeoQuakeDB;
    QuakeData mQuakeData;
    QuakeMapFragment mMapFragment;
    ListFragment mListFragment;
    Toolbar mToolbar;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    int mSelectedFragment = 0;
    boolean isFirstLoad = true;

    double mUserLatitude = 0.0;
    double mUserLongitude = 0.0;
    boolean mHasUserLocation;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        buildGoogleApiClient();

        mDrawerLayout.addDrawerListener(drawerListener);

        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolbar);

        //set toolbar options
        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            //note that the custom navigation(home) logo is set in xml: toolbar_layout
            ab.setDisplayShowHomeEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowCustomEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        mBundle = new Bundle();
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mGeoQuakeDB = new GeoQuakeDB(this);

        mMapFragment = QuakeMapFragment.newInstance();
        mListFragment = ListFragment.newInstance();

        mDurationTypeSpinner.setOnItemSelectedListener(spinnerListener);
        mQuakeTypeSpinner.setOnItemSelectedListener(spinnerListener);
        mQuakeTypeSpinner.setSelection(4); //default selection
        mCacheTimeSpinner.setOnItemSelectedListener(spinnerListener);
//        mWifiCheckbox.setOnCheckedChangeListener(checkListener);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        checkNetworkFetchData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        //for potential use with larger data sets
        super.onLowMemory();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    /**
     * @param outState Bundle whose out state needs to be saved
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     * @param savedInstanceState Bundle to be restored from saved state
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getBundle("mBundle");
    }

    /**
     * @param menu from superclass
     * @return Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                break;

            case R.id.action_list:
                mSelectedFragment = 1;
                fm.beginTransaction().replace(R.id.fragment_container, mListFragment).addToBackStack("map").commit();
                invalidateOptionsMenu();
                break;
            case R.id.action_map_view:
                mSelectedFragment = 0;
                onBackPressed();
                invalidateOptionsMenu();
                break;
            case R.id.action_refresh:
                if (!mAsyncUnderway) {
                    if (GeoQuakeDB.checkRefreshLimit(GeoQuakeDB.getTime(),
                            mSharedPreferences.getLong(Utils.REFRESH_LIMITER, 0))) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putLong(Utils.REFRESH_LIMITER, GeoQuakeDB.getTime());
                        editor.apply();
                        checkNetworkFetchData();
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.wait_for_loading), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_location:
                if (mSelectedFragment == 0) {
                    if(mHasUserLocation) {
                        mMapFragment.moveCameraToUserLocation(mUserLatitude, mUserLongitude);
                    }
                } else {
                    if (mHasUserLocation) {
                        mListFragment.sortByProximity(mUserLatitude, mUserLongitude);
                    }
                }
                break;
            case R.id.action_info:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (mSelectedFragment == 0) {
            menu.findItem(R.id.action_map_view).setVisible(false);
            menu.findItem(R.id.action_list).setVisible(true);
        } else {
            menu.findItem(R.id.action_map_view).setVisible(true);
            menu.findItem(R.id.action_list).setVisible(false);
        }

        menu.findItem(R.id.action_location).setVisible(mHasUserLocation);

        return super.onPrepareOptionsPanel(view, menu);
    }

    public void checkNetworkFetchData() {
        if (Utils.checkNetwork(this)) {
            fetchData();
        } else {
            Utils.connectToast(this);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                Location location = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (location != null) {
                    Log.i(TAG, "Location found " + location.getLatitude() + " " + location.getLongitude());
                    mUserLatitude = location.getLatitude();
                    mUserLongitude = location.getLongitude();
                    mHasUserLocation = true;
                    invalidateOptionsMenu();
                    if (mSelectedFragment == 0) {
                        if(mHasUserLocation) {
                            mMapFragment.moveCameraToUserLocation(mUserLatitude, mUserLongitude);
                        }
                    }
                } else {
                    Log.i(TAG, "No location.");
                }
            } catch (SecurityException se) {
                Log.i(TAG, "SecurityException when fetching location");
                mHasUserLocation = false;
            }
        } else {
            Log.i(TAG, "No permission for ACCESS_FINE_LOCATION");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 99);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 99: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    buildGoogleApiClient();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public FeatureCollection getFeatures() {
        return mFeatureCollection;
    }

    /**
     * Send the request to the QuakeData class to grab new data
     */
    private void fetchData() {

        if (!mGeoQuakeDB.getData("" + mStrengthSelection, "" + mDurationSelection).isEmpty() &&
                !Utils.isExpired(Long.parseLong(mGeoQuakeDB.getDateColumn("" + mStrengthSelection, ""
                        + mDurationSelection)), this)) {
            mFeatureCollection = new FeatureCollection(mGeoQuakeDB.getData("" + mStrengthSelection, "" + mDurationSelection));
            Log.i(TAG, "no need for new data, setup fragment");
            if (isFirstLoad) {
                Log.i(TAG, "firstLoad, set map fragment");
                isFirstLoad = false;
                fireMapFragment();
            } else {
                refreshCurrentFragment(mFeatureCollection);
            }
        } else {
            Utils.fireToast(mDurationSelection, mStrengthSelection, this);
            mQuakeData = new QuakeData(this.getString(R.string.usgs_url),
                    mDurationSelection, mStrengthSelection, this, this);
            Log.i(TAG, "fetching data... await callback");
            mQuakeData.fetchData(this);
        }
    }

    /**
     * Essentially the default behaviour when we first enter the app
     */
    public void fireMapFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mMapFragment)
                .addToBackStack("stack").commit();
    }


    /**
     * Refresh the current fragment with new data
     *
     * @param featureCollection FeatureCollection that will be sent to the fragment
     */
    public void refreshCurrentFragment(FeatureCollection featureCollection) {
        if (mSelectedFragment == 0) {
            mMapFragment.onUpdateData(featureCollection);
            if(mHasUserLocation) {
                mMapFragment.userLocationMarker(mUserLatitude, mUserLongitude);
            }
        } else {
            mListFragment.onUpdateData(featureCollection);
        }
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback(FeatureCollection featureCollection) {
        Log.i(TAG, "got callback, set data");
        //update map with data
        mFeatureCollection = featureCollection; //mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        setLoadingFinishedView();

        if (isFirstLoad) {
            Log.i(TAG, "firstLoad, set map fragment");
            isFirstLoad = false;
            fireMapFragment();
        } else {
            refreshCurrentFragment(featureCollection);
        }
    }

    /**
     * Lets the activity know that an async data call is underway
     */
    @Override
    public void asyncUnderway() {
        mAsyncUnderway = true;
        setLoadingView();
    }

    public void setLoadingView() {
        runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.VISIBLE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
        });
    }

    public void setLoadingFinishedView() {
        runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.GONE);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().show();
                }
            }
        });
    }

    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
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

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(View drawerView) {
            Utils.hideKeyboard(drawerView);
        }

        @Override
        public void onDrawerClosed(View drawerView) {

        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

}
