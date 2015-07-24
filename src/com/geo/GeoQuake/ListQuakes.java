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
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gstinson on 2014-08-25.
 */
public class ListQuakes extends Activity implements AdapterView.OnItemSelectedListener, IDataCallback {

    ListView mQuakeListView;
    QuakeListAdapter mQuakeListAdapter;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    Context mContext;
    Bundle mBundle;

    LinearLayout mDrawerLinearLayout;
    DrawerLayout mDrawerLayout;
    Spinner mQuakeTypeSpinner;
    Spinner mDurationTypeSpinner;
    Spinner mCacheTimeSpinner;
    CheckBox mActionBarCheckbox;
    CheckBox mWifiCheckbox;
    boolean mRefreshList = true;
    boolean mAsyncUnderway = false;

    GeoQuakeDB mGeoQuakeDB;

    FeatureCollection mFeatureCollection;
    ArrayList<Feature> mFeatureList;
    QuakeData mQuakeData;

    LinearLayout mSearchBar;
    EditText mSearchEditText;
    Button mSearchButton;
    TextView mQuakeCountTextView;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_quakes_layout);
        mSharedPreferences = getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mContext = getApplicationContext();
        mBundle = new Bundle();
        mGeoQuakeDB = new GeoQuakeDB(mContext);
        mQuakeListView = (ListView) findViewById(R.id.quakeListView);
        //Side Nav Begin
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLinearLayout = (LinearLayout) findViewById(R.id.drawer_root);
        mDrawerLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
            }
        });
        mSearchBar = (LinearLayout) findViewById(R.id.search_bar);
        mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
        mSearchButton = (Button) findViewById(R.id.search_button);
        mQuakeCountTextView = (TextView) findViewById(R.id.count_textview);

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
        //TODO: Possible actions for orientation change
//        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            //Log.i("config", "landscape");
//        } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            //Log.i("config", "portrait");
//        }
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
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(0, 0);
                mSearchBar.setVisibility(View.VISIBLE);
                mSearchBar.requestFocus();
                break;
            case R.id.action_refresh:
                mDrawerLayout.closeDrawers();
                if(!mAsyncUnderway) {
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
                } else {
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
            default:
                break;
        }
        return false;
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
                basicSort(mFeatureCollection);
                mFeatureList = mFeatureCollection.getFeatures();
                setupList();
            } else {
                Utils.connectToast(mContext);
            }
        }
    }

    private void fetchData() {
        Utils.fireToast(mDurationTypeSpinner.getSelectedItemPosition(), mQuakeTypeSpinner.getSelectedItemPosition(), mContext);
        mQuakeData = new QuakeData(mContext.getString(R.string.usgs_url),
                mDurationTypeSpinner.getSelectedItemPosition(),
                mQuakeTypeSpinner.getSelectedItemPosition(), this, mContext);
        mQuakeData.fetchData(mContext);
    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback(){
        mFeatureCollection = mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        basicSort(mFeatureCollection);
        mFeatureList = mFeatureCollection.getFeatures();
        setupList();
    }

    /**
     * Lets the activity know that an async data call is underway
     */
    @Override
    public void asyncUnderway(){
        mAsyncUnderway = true;
    }

    /**
     * This sorts the list and sets the adapter
     */
    public void setupList(){
        if (mFeatureList != null) {
            mQuakeCountTextView.setText(String.format(mContext.getResources().getString(R.string.quake_count), mFeatureList.size()));
            //TODO: This could use some cleaning up
            mQuakeListAdapter = new QuakeListAdapter(mContext, mFeatureList);
            mQuakeListView.setAdapter(mQuakeListAdapter);
            mQuakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ListQuakes.this, WebInfoActivity.class);
                    intent.putExtra("url", mFeatureList.get(position).getProperties().getUrl());
                    startActivity(intent);
                }
            });
            if(mFeatureList.size() == 0){
                Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_list)
                        , Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Sorting a feature collection.
     *
     * @param featureCollection
     */
    public void basicSort(FeatureCollection featureCollection){
        Collections.sort(featureCollection.getFeatures(), new Comparator<Feature>() {
            @Override
            public int compare(Feature lhs, Feature rhs) {
                //Using Double's compare method makes this pretty straightforward.
                return Double.compare(rhs.getProperties().getMag(), lhs.getProperties().getMag());
            }
        });
    }

    /**
     *
     */
    public void sortByProximity(){
        //TODO: a possible sort by closest location
    }

    /**
     * Crude indeed. This won't be very robust if we're getting into unicode,
     * but we can pretty well assume that the USGS data isn't giving us any
     * oddball characters in the result list
     */
    public void doSearch(View view){
        ArrayList<Feature> searchFeatures = new ArrayList<>();
        String searchTerm = mSearchEditText.getText().toString();
        for(Feature feature : mFeatureList){
            //For "expected" input, this should handle cases
            if(feature.properties.getPlace().toLowerCase().contains(searchTerm)){
                searchFeatures.add(feature);
            }
        }

        if(searchFeatures.size() == 0){
            Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_search_list)
                    , Toast.LENGTH_LONG).show();
        }else{
            mQuakeCountTextView.setText(String.format(mContext.getResources().getString(R.string.quake_count), mFeatureList.size()));
            mFeatureList.clear(); //is this needed?
            mFeatureList = searchFeatures;
            mSearchEditText.setText("");
            mSearchBar.setVisibility(View.GONE);
            //close keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

        }
        setupList();
    }

}