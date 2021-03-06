package com.geo.geoquake;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.geo.geoquake.adapters.TabPagerAdapter;
import com.geo.geoquake.models.Earthquake;
import com.geo.geoquake.models.Prefs;
import com.geo.geoquake.network.QuakeAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements IDataCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    protected Bundle mBundle;

    protected DrawerLayout mDrawerLayout;
    protected RelativeLayout mDrawerLinearLayout;
    protected Spinner mSourceTypeSpinner;
    protected Spinner mQuakeTypeSpinner;
    protected Spinner mDurationTypeSpinner;
    protected Spinner mCacheTimeSpinner;
    protected RelativeLayout mLoadingOverlay;
    protected TabLayout mTabLayout;
    protected ViewPager mViewPager;

    //    CheckBox mWifiCheckbox;

    protected ArrayList<Earthquake> mEarthquakes = new ArrayList<>();
    protected GeoQuakeDB mGeoQuakeDB;
    protected Toolbar mToolbar;

    protected int mSourceSelection = 0;
    protected int mStrengthSelection = 4;
    protected int mDurationSelection = 0;

    double mUserLatitude = 0.0;
    double mUserLongitude = 0.0;
    boolean mHasUserLocation;
    boolean mDrawerIsOpen;
    boolean mParametersAreChanged;

    int mCurrentTabPosition;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set UI elements
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLinearLayout = findViewById(R.id.options_root);
        mSourceTypeSpinner = findViewById(R.id.source_type_spinner);
        mQuakeTypeSpinner = findViewById(R.id.quake_type_spinner);
        mDurationTypeSpinner = findViewById(R.id.duration_type_spinner);
        mCacheTimeSpinner = findViewById(R.id.cache_spinner);
        mLoadingOverlay = findViewById(R.id.loading_overlay);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);

        buildGoogleApiClient();

        mDrawerLayout.addDrawerListener(drawerListener);
        mViewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(), this));
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.addOnTabSelectedListener(onTabSelectedListener);

        mToolbar = findViewById(R.id.action_bar);
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
        mGeoQuakeDB = new GeoQuakeDB(this);

        mSourceSelection = Prefs.getInstance().getSource();
        mSourceTypeSpinner.setSelection(mSourceSelection);
        mDurationTypeSpinner.setOnItemSelectedListener(spinnerListener);
        mQuakeTypeSpinner.setOnItemSelectedListener(spinnerListener);
        mQuakeTypeSpinner.setSelection(4); //default selection
        mCacheTimeSpinner.setOnItemSelectedListener(spinnerListener);
        mSourceTypeSpinner.setOnItemSelectedListener(spinnerListener);
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
        if (mDrawerIsOpen) {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     * @param savedInstanceState Bundle to be restored from saved state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
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
                doRefresh();
                break;
            case R.id.action_location:
                if (mHasUserLocation) {
                    ((TabPagerAdapter) Objects.requireNonNull(mViewPager.getAdapter())).moveCamera(mUserLatitude, mUserLongitude);
                }
                if (mHasUserLocation) {
                    ((TabPagerAdapter) mViewPager.getAdapter()).sortByProximity(mUserLatitude, mUserLongitude);
                }
                if (mCurrentTabPosition == 1) {
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_location).setVisible(mHasUserLocation);
        menu.findItem(R.id.action_search).setVisible(mCurrentTabPosition == 1);
        return super.onPrepareOptionsMenu(menu);
    }

    public void checkNetworkFetchData() {
        if (Utils.checkNetwork(this)) {
            fetchData();
        } else {
            Utils.connectToast(this);
        }
    }

    public void doRefresh() {
        if (GeoQuakeDB.checkRefreshLimit(GeoQuakeDB.getTime(),
                Prefs.getInstance().getRefreshLimiter())) {
            Prefs.getInstance().setRefreshLimiter();
            checkNetworkFetchData();
        } else {
            Toast.makeText(this, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
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
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 99) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                getAndHandleLocation();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //TODO: more permission handling?
                }

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void getAndHandleLocation() {
        try {
            LocationServices.getFusedLocationProviderClient(this).getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    Log.i(TAG, "Location found " + location.getLatitude() + " " + location.getLongitude());
                    mUserLatitude = location.getLatitude();
                    mUserLongitude = location.getLongitude();
                    mHasUserLocation = true;
                    invalidateOptionsMenu();
                    if (mHasUserLocation) {
                        ((TabPagerAdapter) Objects.requireNonNull(mViewPager.getAdapter())).moveCamera(mUserLatitude, mUserLongitude);
                    }
                } else {
                    Log.i(TAG, "No location.");
                }
            });
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

    public ArrayList<Earthquake> getEarthquakes() {
        return mEarthquakes;
    }

    /**
     * Send the request to the QuakeData class to grab new data
     */
    private void fetchData() {

        if (!mGeoQuakeDB.getData("" + mSourceSelection, "" + mStrengthSelection, "" + mDurationSelection).isEmpty() &&
                !Utils.isExpired(Long.parseLong(mGeoQuakeDB.getDateColumn("" + mSourceSelection, "" + mStrengthSelection, ""
                        + mDurationSelection)))) {
            mEarthquakes = Utils.convertModelBySource(mSourceSelection, mGeoQuakeDB.getData("" + mSourceSelection, "" + mStrengthSelection, "" + mDurationSelection));
            Log.i(TAG, "no need for new data, setup fragment");
            refreshCurrentFragment(mEarthquakes);
        } else {
            getNewData();
        }
    }

    private void getNewData() {
        if (Utils.needToRefreshData(mGeoQuakeDB, mSourceSelection, mStrengthSelection, mDurationSelection)) {
            QuakeAPI quakeAPI = new QuakeAPI(this);
            quakeAPI.getUSGSQuakes(Utils.getURLFrag(mSourceSelection, mStrengthSelection, mDurationSelection, this))
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            String jsonResponse;
                            if (response.isSuccessful()) {
                                try {
                                    assert response.body() != null;
                                    jsonResponse = response.body().string();

                                    //set or update DB, as needed
                                    if (mGeoQuakeDB.getData("" + mSourceSelection, "" + mStrengthSelection, "" + mDurationSelection).isEmpty()) {
                                        mGeoQuakeDB.setData("" + mSourceSelection, "" + mStrengthSelection, "" + mDurationSelection, jsonResponse);
                                    } else {
                                        mGeoQuakeDB.updateData("" + mSourceSelection, "" + mStrengthSelection, "" + mDurationSelection, jsonResponse);
                                    }

                                    dataCallBack(Utils.convertModelBySource(mSourceSelection, jsonResponse));
                                } catch (IOException e) {
                                    Log.e(TAG, "Failure in parsing response body: " + e.getMessage());
                                }
                            } else {
                                Log.e(TAG, "Issue in data response");
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e(TAG, "Failure " + t.getMessage());
                        }
                    });
        } else {
            //use existing data
            mEarthquakes = Utils.convertModelBySource(mSourceSelection,
                    mGeoQuakeDB.getData("" + mSourceSelection, ""
                            + mStrengthSelection, "" + mDurationSelection));
        }
    }

    /**
     * Refresh the current fragment with new data
     */
    public void refreshCurrentFragment(ArrayList<Earthquake> mEarthquakes) {
        ((TabPagerAdapter) Objects.requireNonNull(mViewPager.getAdapter())).updateFragments(mEarthquakes,
                mHasUserLocation, mUserLatitude, mUserLongitude);
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void dataCallBack(@NonNull ArrayList<Earthquake> earthquakes) {
        //update map with data
        mEarthquakes = earthquakes;
        setLoadingFinishedView();
        refreshCurrentFragment(mEarthquakes);
    }

    /**
     * Lets the activity know that an async data call is underway
     */
    @Override
    public void asyncUnderway() {
        setLoadingView();
    }

    public void setLoadingView() {
        runOnUiThread(() -> {
            mLoadingOverlay.setVisibility(View.VISIBLE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
        });
    }

    public void setLoadingFinishedView() {
        runOnUiThread(() -> {
            mLoadingOverlay.setVisibility(View.GONE);
            if (getSupportActionBar() != null) {
                getSupportActionBar().show();
            }
        });
    }

    public void getAlertDiagloBuilder(Context context) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setMessage(context.getResources().getString(R.string.parameters_changed))
                .setPositiveButton(context.getString(R.string.menu_refresh), (dialog, which) -> {
                    doRefresh();
                    mParametersAreChanged = false;
                })
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> {
                    //nuthin'
                    mParametersAreChanged = false;
                });
        alertBuilder.create();
        alertBuilder.show();
    }

    AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case (R.id.source_type_spinner):
                    switch (mSourceTypeSpinner.getSelectedItemPosition()) {
                        //USGS
                        case 0:
                            Prefs.getInstance().setSource(0);
                            mSourceSelection = 0;
                            mQuakeTypeSpinner.setVisibility(View.VISIBLE);
                            ArrayAdapter<String> usaAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, MainActivity.this.getResources().getStringArray(R.array.duration_types));
                            usaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mDurationTypeSpinner.setAdapter(usaAdapter);
                            mParametersAreChanged = true;
                            break;
                        //Canada
                        case 1:
                            Prefs.getInstance().setSource(1);
                            mSourceSelection = 1;
                            mQuakeTypeSpinner.setVisibility(View.GONE);
                            ArrayAdapter<String> canAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, MainActivity.this.getResources().getStringArray(R.array.canada_duration_types));
                            canAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            mDurationTypeSpinner.setAdapter(canAdapter);
                            mParametersAreChanged = true;
                            break;
                    }
                    break;
                case (R.id.quake_type_spinner):
                    if (mStrengthSelection != mQuakeTypeSpinner.getSelectedItemPosition()) {
                        mParametersAreChanged = true;
                        mStrengthSelection = mQuakeTypeSpinner.getSelectedItemPosition();
                    }
                    break;
                case (R.id.duration_type_spinner):
                    if (mDurationSelection != mDurationTypeSpinner.getSelectedItemPosition()) {
                        mDurationSelection = mDurationTypeSpinner.getSelectedItemPosition();
                        mParametersAreChanged = true;
                    }
                    break;
                case (R.id.cache_spinner):
                    Utils.changeCache(mCacheTimeSpinner.getSelectedItemPosition(),
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
        public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

        }

        @Override
        public void onDrawerOpened(@NonNull View drawerView) {
            mDrawerIsOpen = true;
            Utils.hideKeyboard(drawerView);
        }

        @Override
        public void onDrawerClosed(@NonNull View drawerView) {
            mDrawerIsOpen = false;
            if (mParametersAreChanged) {
                getAlertDiagloBuilder(MainActivity.this);
            }
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
