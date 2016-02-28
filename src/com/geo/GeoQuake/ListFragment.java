package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import butterknife.Bind;

/**
 * Created by gstinson on 2014-08-25.
 */
public class ListFragment extends Fragment implements IDataCallback {

    @Bind(R.id.quakeListView)
    ListView mQuakeListView;

    QuakeListAdapter mQuakeListAdapter;
    SharedPreferences mSharedPreferences;
    SharedPreferences.Editor mSharedPreferencesEditor;
    Context mContext;
    Bundle mBundle;
    boolean mRefreshList = true;
    boolean mAsyncUnderway = false;

    GeoQuakeDB mGeoQuakeDB;

    FeatureCollection mFeatureCollection;
    ArrayList<Feature> mFeatureList;
    QuakeData mQuakeData;

    LinearLayout mSearchBar;
    EditText mSearchEditText;
    TextView mQuakeCountTextView;
    ImageView mSearchImageButton;
    ImageView mProximityImageButton;
    Button mAboutButton;

    int mStrengthSelection = 4;
    int mDurationSelection = 0;

    double mUserLatitude = 0.0;
    double mUserLongitude = 0.0;

    @Bind(R.id.loading_overlay)
    RelativeLayout mLoadingOverlay;

    @Bind(R.id.progress_counter)
    ProgressBar mLoadingProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mSharedPreferences = getActivity().getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        mContext = getActivity().getApplicationContext();
        mBundle = new Bundle();
        mGeoQuakeDB = new GeoQuakeDB(mContext);

        //grab intent values, if any
        Intent intent = getActivity().getIntent();
        mStrengthSelection = intent.getIntExtra("quake_strength", 4);
        mDurationSelection = intent.getIntExtra("quake_duration", 0);


        setupLocation();
        networkCheckFetchData();

    }

    public void setupLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location;
        try {
             location = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(new Criteria(), false));
            if (location != null) {
                mProximityImageButton.setVisibility(View.VISIBLE);
                mUserLatitude = location.getLatitude();
                mUserLongitude = location.getLongitude();
            } else {
                mProximityImageButton.setVisibility(View.GONE);
            }
        } catch (SecurityException se) {
            mProximityImageButton.setVisibility(View.GONE);
        }

    }

    /**
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
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     * Checking the network before we bother trying to grab data
     */
    public void networkCheckFetchData() {
        if (Utils.checkNetwork(mContext)) {
            fetchData();
        } else {
            if (!mGeoQuakeDB.getData("" + mStrengthSelection, "" + mDurationSelection).isEmpty()) {
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
        if (!mGeoQuakeDB.getData("" + mStrengthSelection, "" + mDurationSelection).isEmpty() &&
                !Utils.isExpired(Long.parseLong(mGeoQuakeDB.getDateColumn("" + mStrengthSelection, ""
                        + mDurationSelection)), mContext)) {
            mFeatureCollection = new FeatureCollection(mGeoQuakeDB.getData("" + mStrengthSelection, "" + mDurationSelection));
            basicSort(mFeatureCollection);
            mFeatureList = mFeatureCollection.getFeatures();
            setupList();
        } else {
//            Utils.fireToast(mDurationTypeSpinner.getSelectedItemPosition(), mQuakeTypeSpinner.getSelectedItemPosition(), mContext);
//            mQuakeData = new QuakeData(mContext.getString(R.string.usgs_url),
//                    mDurationTypeSpinner.getSelectedItemPosition(),
//                    mQuakeTypeSpinner.getSelectedItemPosition(), this, mContext);
            mQuakeData.fetchData(mContext);
        }

    }

    /**
     * Interface callback when fetching data
     */
    @Override
    public void dataCallback() {
        mFeatureCollection = mQuakeData.getFeatureCollection();
        mAsyncUnderway = false;
        basicSort(mFeatureCollection);
        mFeatureList = mFeatureCollection.getFeatures();
        setupList();
        setLoadingFinishedView();
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
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.VISIBLE);
                getActivity().getActionBar().hide();
            }
        });
    }

    public void setLoadingFinishedView() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mLoadingOverlay.setVisibility(View.GONE);
                getActivity().getActionBar().show();
            }
        });
    }

    /**
     * This sorts the list and sets the adapter
     */
    public void setupList() {
        if (mFeatureList != null) {
            mQuakeCountTextView.setText(String.format(mContext.getResources().getString(R.string.quake_count), mFeatureList.size()));
            //TODO: This could use some cleaning up
            mQuakeListAdapter = new QuakeListAdapter(mContext, mFeatureList);
            mQuakeListView.setAdapter(mQuakeListAdapter);
            mQuakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), WebInfoActivity.class);
                    intent.putExtra("url", mFeatureList.get(position).getProperties().getUrl());
                    startActivity(intent);
                }
            });
            if (mFeatureList.size() == 0) {
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
    public void basicSort(FeatureCollection featureCollection) {
        Collections.sort(featureCollection.getFeatures(), new Comparator<Feature>() {
            @Override
            public int compare(Feature lhs, Feature rhs) {
                //Using Double's compare method makes this pretty straightforward.
                return Double.compare(rhs.getProperties().getMag(), lhs.getProperties().getMag());
            }
        });


    }

    /**
     * Sorting list by distance from user
     */
    public void sortByProximity() {
        Toast.makeText(mContext, mContext.getResources().getString(R.string.sorting_by_proximity)
                , Toast.LENGTH_SHORT).show();
        ArrayList<Feature> proximityList = new ArrayList<>();
        TreeMap<Float, Feature> proximityMap = new TreeMap<>();
        for (Feature feature : mFeatureList) {
            float[] results = new float[3];
            Location.distanceBetween(mUserLatitude, mUserLongitude, feature.getLatitude(),
                    feature.getLongitude(), results);
            proximityMap.put(results[0] / 1000, feature);
        }
        for (Map.Entry<Float, Feature> entry : proximityMap.entrySet()) {
            proximityList.add(entry.getValue());
        }
        mFeatureList.clear();
        mFeatureList = proximityList;
        setupList();
    }

    /**
     * Crude indeed. This won't be very robust if we're getting into unicode,
     * but we can pretty well assume that the USGS data isn't giving us any
     * oddball characters in the result list
     */
    public void doSearch(View view) {
        ArrayList<Feature> searchFeatures = new ArrayList<>();
        String searchTerm = mSearchEditText.getText().toString();
        for (Feature feature : mFeatureList) {
            //For "expected" input, this should handle cases
            if (feature.properties.getPlace().toLowerCase().contains(searchTerm)) {
                searchFeatures.add(feature);
            }
        }

        if (searchFeatures.size() == 0) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_search_list)
                    , Toast.LENGTH_LONG).show();
        } else {
            mQuakeCountTextView.setText(String.format(mContext.getResources().getString(R.string.quake_count), mFeatureList.size()));
            mFeatureList.clear(); //is this needed?
            mFeatureList = searchFeatures;
            mSearchEditText.setText("");
            mSearchBar.setVisibility(View.GONE);
            mSearchImageButton.setVisibility(View.VISIBLE);
            //close keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        }
        setupList();
    }

}