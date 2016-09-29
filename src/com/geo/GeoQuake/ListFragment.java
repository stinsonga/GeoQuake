package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by gstinson on 2014-08-25.
 */
public class ListFragment extends Fragment implements IDataCallback {
    private static final String TAG = ListFragment.class.getSimpleName();

    @Bind(R.id.quakeListView)
    ListView mQuakeListView;

    QuakeListAdapter mQuakeListAdapter;
    Bundle mBundle;

    GeoQuakeDB mGeoQuakeDB;

    FeatureCollection mFeatureCollection;
    ArrayList<Feature> mFeatureList;

    @Bind(R.id.search_view)
    SearchView mSearchView;

    @Bind(R.id.count_textview)
    TextView mQuakeCountTextView;

    Context mContext;

    public static ListFragment newInstance() {
        return new ListFragment();
    }

    public ListFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);

        mBundle = new Bundle();
        mGeoQuakeDB = new GeoQuakeDB(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        ButterKnife.bind(this, view);

        mSearchView.setOnQueryTextListener(queryTextListener);
        mSearchView.setQueryHint(getActivity().getString(R.string.search_hint));
        mSearchView.setQuery(getActivity().getString(R.string.search_hint), false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (Utils.checkNetwork(getActivity())) {
            if (((MainActivity) getActivity()).getFeatures() != null && ((MainActivity) getActivity()).getFeatures().getFeatures().size() > 0) {
                mFeatureList = ((MainActivity) getActivity()).getFeatures().getFeatures();
                setupList(mFeatureList);
            } else {
                ((MainActivity) getActivity()).checkNetworkFetchData();
            }
        } else {
            Utils.connectToast(getActivity());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;

    }

    /**
     * @param config Configuration passed in from superclass
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        //TODO: Possible actions for orientation change
    }

    /**
     * @param outState Bundle state to be saved onSaveInstanceState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("mBundle", mBundle);
    }

    /**
     * This sorts the list and sets the adapter
     */
    public void setupList(final ArrayList<Feature> features) {
        if (features != null && mContext != null) {
            Log.i(TAG, "setupList, with size: " + mFeatureList.size());
            mQuakeCountTextView.setText(String.format(mContext.getResources().getString(R.string.quake_count), mFeatureList.size()));
            //TODO: This could use some cleaning up
            mQuakeListAdapter = new QuakeListAdapter(mContext, features);
            mQuakeListView.setAdapter(mQuakeListAdapter);
            mQuakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(mContext, WebInfoActivity.class);
                    intent.putExtra("url", features.get(position).getProperties().getUrl());
                    startActivity(intent);
                }
            });
            if (features.size() == 0) {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.empty_list)
                        , Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Null context " + (mContext == null));
        }
    }

    /**
     * Sorting a feature collection.
     *
     * @param features ArrayList of Features
     */
    public void basicSort(ArrayList<Feature> features) {
        Collections.sort(features, new Comparator<Feature>() {
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
    public void sortByProximity(double latitude, double longitude) {
        ArrayList<Feature> proximityList = new ArrayList<>();
        TreeMap<Float, Feature> proximityMap = new TreeMap<>();
        for (Feature feature : mFeatureList) {
            float[] results = new float[3];
            Location.distanceBetween(latitude, longitude, feature.getLatitude(),
                    feature.getLongitude(), results);
            proximityMap.put(results[0] / 1000, feature);
        }
        for (Map.Entry<Float, Feature> entry : proximityMap.entrySet()) {
            proximityList.add(entry.getValue());
        }
        mFeatureList.clear();
        mFeatureList = proximityList;
        setupList(mFeatureList);
    }

    /**
     * Crude indeed. But it works 'enough'
     *
     */
    public void doSearch() {
        ArrayList<Feature> searchFeatures = new ArrayList<>();
        String searchTerm = mSearchView.getQuery().toString();
        for (Feature feature : mFeatureList) {
            //For "expected" input, this should handle cases
            if (feature.getProperties().getPlace() != null
                    && feature.getProperties().getPlace().toLowerCase().contains(searchTerm)) {
                searchFeatures.add(feature);
            }
        }

        if (searchFeatures.size() == 0) {
            Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.empty_search_list)
                    , Toast.LENGTH_LONG).show();
        } else {
            mQuakeCountTextView.setText(String.format(getActivity().getResources().getString(R.string.quake_count), mFeatureList.size()));
            mSearchView.setQuery("", false);
            //close keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            setupList(searchFeatures);
        }

    }

    /**
     * The listener that handles changes in the query text box.
     *
     * In the case of a non-empty string, a search is conducted within the list of Features
     *
     */
    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if(!StringUtils.isEmpty(query)) {
                doSearch();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public void asyncUnderway() {
        //unused
    }

    /**
     *
     * @param featureCollection a FeatureCollection passed by the parent activity
     */
    @Override
    public void dataCallback(FeatureCollection featureCollection) {
        mFeatureCollection = featureCollection;
        mFeatureList = featureCollection.getFeatures();
        basicSort(mFeatureList);
        setupList(mFeatureList);
    }

    /**
     * Called from activity on refresh
     *
     * @param featureCollection FeatureCollection passed in by calling Activity
     */
    public void onUpdateData(FeatureCollection featureCollection) {
        mFeatureCollection = featureCollection;
        if(featureCollection != null) {
            mFeatureList = featureCollection.getFeatures();
            basicSort(mFeatureList);
            setupList(mFeatureList);
        }
    }
}