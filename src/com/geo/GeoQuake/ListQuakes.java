package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.*;
import android.widget.*;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gstinson on 2014-08-25.
 */
public class ListQuakes extends Activity implements AdapterView.OnItemSelectedListener, DataCallback {

    ListView mQuakeListView;
    QuakeListAdapter mQuakeListAdapter;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    Context mContext;

    DrawerLayout mDrawerLayout;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    Spinner mCacheTimeSpinner;
    CheckBox mActionBarCheckbox;
    CheckBox mWifiCheckbox;
    boolean mRefreshList = true;

    GeoQuakeDB geoQuakeDB;

    FeatureCollection mFeatureCollection;
    QuakeData mQuakeData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_quakes_layout);
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mContext = getApplicationContext();
        geoQuakeDB = new GeoQuakeDB(mContext);
        mQuakeListView = (ListView) findViewById(R.id.quakeListView);
        //Side Nav Begin
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

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

        networkCheckFetchData();


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_map_view:
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                break;
            case R.id.action_refresh:
                if (GeoQuakeDB.checkRefreshLimit(Long.parseLong(GeoQuakeDB.getTime()),
                        mSharedPreferences.getLong(Utils.REFRESH_LIMITER, 0))) {
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putLong(Utils.REFRESH_LIMITER, Long.parseLong(GeoQuakeDB.getTime()));
                    editor.apply();
                    mRefreshList = true;
                    networkCheckFetchData();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.refresh_warning), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_settings:
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case (R.id.quake_type_spinner):
            case (R.id.duration_type_spinner):
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

    /**
     * Checking the network before we bother trying to grab data
     */
    public void networkCheckFetchData() {
        if (Utils.checkNetwork(mContext)) {
            fetchData();
        } else {
            Utils.connectToast(mContext);
        }
    }

    private void fetchData() {
        Utils.fireToast(mDurationTypeSpinner.getSelectedItemPosition(), mQuakeTypeSpinner.getSelectedItemPosition(), mContext);
        mQuakeData = new QuakeData(mContext.getString(R.string.usgs_url),
                mDurationTypeSpinner.getSelectedItemPosition(),
                mQuakeTypeSpinner.getSelectedItemPosition(), this);
        mQuakeData.fetchData(mContext);
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback(){
        mFeatureCollection = mQuakeData.getFeatureCollection();
        setupList();
    }

    /**
     * This sorts the list and sets the adapter
     */
    public void setupList(){
        if (mFeatureCollection != null) {
            Collections.sort(mFeatureCollection.getFeatures(), new Comparator<Feature>() {
                @Override
                public int compare(Feature lhs, Feature rhs) {
                    //Using Double's compare method makes this pretty straightforward.
                    return Double.compare(lhs.getProperties().getMag(), rhs.getProperties().getMag());
                }
            });
            //TODO: This could use some cleaning up
            mQuakeListAdapter = new QuakeListAdapter(mContext, mFeatureCollection.getFeatures());
            mQuakeListView.setAdapter(mQuakeListAdapter);

            mQuakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ListQuakes.this, WebInfoActivity.class);
                    intent.putExtra("url", mFeatureCollection.getFeatures().get(position).getProperties().getUrl());
                    startActivity(intent);
                }
            });
        }
    }

}