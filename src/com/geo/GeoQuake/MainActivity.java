package com.geo.GeoQuake;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.geo.GeoQuake.adapters.TabPagerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IDataCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getSimpleName();
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

    @Bind(R.id.tab_layout)
    TabLayout mTabLayout;

    @Bind(R.id.view_pager)
    ViewPager mViewPager;

    boolean mAsyncUnderway = false;

    FeatureCollection mFeatureCollection;
    GeoQuakeDB mGeoQuakeDB;
    QuakeData mQuakeData;
    Toolbar mToolbar;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    double mUserLatitude = 0.0;
    double mUserLongitude = 0.0;
    boolean mHasUserLocation;
    boolean mDrawerIsOpen;

    int mCurrentTabPosition;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        buildGoogleApiClient();

        mDrawerLayout.addDrawerListener(drawerListener);
        mViewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(), this));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(onTabSelectedListener);

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
        if(mDrawerIsOpen) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
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
                    if(mHasUserLocation) {
                        ((TabPagerAdapter) mViewPager.getAdapter()).moveCamera(mUserLatitude, mUserLongitude);
                    }
                    if (mHasUserLocation) {
                        ((TabPagerAdapter) mViewPager.getAdapter()).sortByProximity(mUserLatitude, mUserLongitude);
                    }
                if(mCurrentTabPosition == 1) {
                  Toast.makeText(this, this.getResources().getString(R.string.sorting_by_proximity)
                , Toast.LENGTH_SHORT).show();
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
        menu.findItem(R.id.action_location).setVisible(mHasUserLocation);
        menu.findItem(R.id.action_search).setVisible(mCurrentTabPosition == 1);
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
            getAndHandleLocation();
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
                    getAndHandleLocation();
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        //TODO: more permission handling?
                        return;
                    }

                } else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
            //case 1111:
                //other
        }
    }

    public void getAndHandleLocation() {
        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (location != null) {
                Log.i(TAG, "Location found " + location.getLatitude() + " " + location.getLongitude());
                mUserLatitude = location.getLatitude();
                mUserLongitude = location.getLongitude();
                mHasUserLocation = true;
                invalidateOptionsMenu();
                if(mHasUserLocation) {
                    ((TabPagerAdapter) mViewPager.getAdapter()).moveCamera(mUserLatitude, mUserLongitude);
                }
            } else {
                Log.i(TAG, "No location.");
            }
        } catch (SecurityException se) {
            Log.i(TAG, "SecurityException when fetching location");
            mHasUserLocation = false;
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
            refreshCurrentFragment(mFeatureCollection);
        } else {
            Utils.fireToast(mDurationSelection, mStrengthSelection, this);
            mQuakeData = new QuakeData(this.getString(R.string.usgs_url),
                    mDurationSelection, mStrengthSelection, this, this);
            Log.i(TAG, "fetching data... await callback");
            mQuakeData.fetchData(this);
        }
    }

    /**
     * Refresh the current fragment with new data
     *
     * @param featureCollection FeatureCollection that will be sent to the fragment
     */
    public void refreshCurrentFragment(FeatureCollection featureCollection) {
        ((TabPagerAdapter) mViewPager.getAdapter()).updateFragments(featureCollection,
                mHasUserLocation, mUserLatitude, mUserLongitude);
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback(FeatureCollection featureCollection) {
        Log.i(TAG, "got callback, set data " + featureCollection.count);
        //update map with data
        mFeatureCollection = featureCollection; //mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        setLoadingFinishedView();
        refreshCurrentFragment(featureCollection);
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
            mDrawerIsOpen = true;
            Utils.hideKeyboard(drawerView);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            mDrawerIsOpen = false;
        }

        @Override
        public void onDrawerStateChanged(int newState) {

        }
    };

    TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            mCurrentTabPosition = tab.getPosition();
            invalidateOptionsMenu();
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

}
