package com.geo.GeoQuake;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import butterknife.Bind;

public class MainActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener, IDataCallback {

    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    Bundle mBundle;


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

    boolean mRefreshList = true;
    boolean mAsyncUnderway = false;

    GeoQuakeDB mGeoQuakeDB;
    QuakeMapFragment mMapFragment;
    ListFragment mListFragment;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    int mSelectedFragment = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBundle = new Bundle();
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mGeoQuakeDB = new GeoQuakeDB(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mMapFragment = new QuakeMapFragment();
        mListFragment = new ListFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mMapFragment).commit();

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
        switch (item.getItemId()) {

            case R.id.action_list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mListFragment).commit();
                mSelectedFragment = 1;
                invalidateOptionsMenu();
                break;
            case R.id.action_map_view:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mMapFragment).commit();
                mSelectedFragment = 0;
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
                        //networkCheckFetchData();
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
        if(mSelectedFragment == 0) {
            menu.findItem(R.id.action_map_view).setVisible(false);
            menu.findItem(R.id.action_list).setVisible(true);
        } else {
            menu.findItem(R.id.action_map_view).setVisible(true);
            menu.findItem(R.id.action_list).setVisible(false);
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public void dataCallback() {

    }

    @Override
    public void asyncUnderway() {

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
}
