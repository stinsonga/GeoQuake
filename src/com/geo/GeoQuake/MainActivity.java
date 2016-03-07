package com.geo.GeoQuake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.util.AsyncExecutor;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener, IDataCallback {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    Bundle mBundle;
    AsyncExecutor mAsyncRequest;

    @Bind(R.id.drawer_root)
    LinearLayout mDrawerLinearLayout;

    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.quake_type_spinner)
    Spinner mQuakeTypeSpinner;

    @Bind(R.id.duration_type_spinner)
    Spinner mDurationTypeSpinner;

    @Bind(R.id.cache_spinner)
    Spinner mCacheTimeSpinner;

    @Bind(R.id.actionbar_toggle_checkbox)
    CheckBox mActionBarCheckbox;

    @Bind(R.id.wifi_checkbox)
    CheckBox mWifiCheckbox;

    @Bind(R.id.loading_overlay)
    RelativeLayout mLoadingOverlay;

    boolean mRefreshList = true;
    boolean mAsyncUnderway = false;

    FeatureCollection mFeatureCollection;
    GeoQuakeDB mGeoQuakeDB;
    QuakeData mQuakeData;
    QuakeMapFragment mMapFragment;
    ListFragment mListFragment;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    int mSelectedFragment = 0;

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
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mMapFragment)
                .addToBackStack("stack").commit();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getBundle("mBundle");
    }

    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

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
                mDrawerLayout.closeDrawers();
                if (!mAsyncUnderway) {
                    if (GeoQuakeDB.checkRefreshLimit(GeoQuakeDB.getTime(),
                            mSharedPreferences.getLong(Utils.REFRESH_LIMITER, 0))) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putLong(Utils.REFRESH_LIMITER, GeoQuakeDB.getTime());
                        editor.apply();
                        mRefreshList = true;
                        if (Utils.checkNetwork(this)) {
                            fetchData();
                        } else {
                            Utils.connectToast(this);
                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.wait_for_loading), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_settings:
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
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
        } else {
            Utils.fireToast(mDurationSelection, mStrengthSelection, this);
            mQuakeData = new QuakeData(this.getString(R.string.usgs_url),
                    mDurationSelection, mStrengthSelection, this, this);
            mQuakeData.fetchData(this);
        }
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback() {
        //update map with data
        mFeatureCollection = mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        setLoadingFinishedView();
        EventBus.getDefault().post(new QuakeDataEvent(mQuakeData.getFeatureCollection()));
//        if (mSelectedFragment == 0) {
//            mMapFragment.updateData(mFeatureCollection);
//        } else {
//            mListFragment.updateData(mFeatureCollection);
//        }

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
                getActionBar().hide();
            }
        });
    }

    public void setLoadingFinishedView() {
        runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.GONE);
                getActionBar().show();
            }
        });
    }

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

    public class QuakeDataEvent {
        FeatureCollection mFeatureCollection;

        public QuakeDataEvent(FeatureCollection featureCollection) {
            this.mFeatureCollection = featureCollection;
        }

        public FeatureCollection getFeatureCollection() {
            return mFeatureCollection;
        }
    }


}
