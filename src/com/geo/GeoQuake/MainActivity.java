package com.geo.GeoQuake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements IDataCallback {
    private static final String TAG = "MainActivity";
    SharedPreferences mSharedPreferences;
    Bundle mBundle;

    @Bind(R.id.drawer_root)
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

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    int mSelectedFragment = 0;
    boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        return super.onMenuItemSelected(featureId, item);
//    }
//
//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getSupportFragmentManager();
        switch (item.getItemId()) {

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
                mDrawerLinearLayout.setVisibility(View.GONE);
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
            case R.id.action_settings:
                if (mDrawerLinearLayout.getVisibility() == View.VISIBLE) {
                    mDrawerLinearLayout.setVisibility(View.GONE);
                } else {
                    mDrawerLinearLayout.setVisibility(View.VISIBLE);
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
        return super.onPrepareOptionsPanel(view, menu);
    }

    public void checkNetworkFetchData() {

        if (Utils.checkNetwork(this)) {
            fetchData();
        } else {
            Utils.connectToast(this);
        }
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
       if(mSelectedFragment == 0) {
           mMapFragment.onUpdateData(featureCollection);
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
                if(getActionBar() != null) {
                    getActionBar().hide();
                }
            }
        });
    }

    public void setLoadingFinishedView() {
        runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.GONE);
                if(getActionBar() != null) {
                    getActionBar().show();
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

}
