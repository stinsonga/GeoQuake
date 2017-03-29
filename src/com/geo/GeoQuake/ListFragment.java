package com.geo.GeoQuake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.geo.GeoQuake.adapters.QuakeAdapter;
import com.geo.GeoQuake.models.Earthquake;
import com.geo.GeoQuake.models.Feature;
import com.geo.GeoQuake.models.FeatureCollection;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by gstinson on 2014-08-25.
 */
public class ListFragment extends Fragment implements IDataCallback {
    private static final String TAG = ListFragment.class.getSimpleName();

    @Bind(R.id.quakeListView)
    RecyclerView mQuakeListView;

    QuakeAdapter mQuakeListAdapter;
    Bundle mBundle;

    ArrayList<Earthquake> mEarthquakes = new ArrayList<Earthquake>();

    @Bind(R.id.search_bar)
    LinearLayout mSearchBar;

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

    /**
     * The listener that handles changes in the query text box.
     * <p>
     * In the case of a non-empty string, a search is conducted within the list of Features
     */
    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (!StringUtils.isEmpty(query)) {
                doSearch();
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };


    final QuakeAdapter.OnQuakeItemClickedListener onQuakeItemClickedListener = new QuakeAdapter.OnQuakeItemClickedListener() {
        @Override
        public void onQuakeClicked(Earthquake earthquake) {
            Intent intent = new Intent(mContext, WebInfoActivity.class);
            intent.putExtra("url", earthquake.getUrl());
            startActivity(intent);

        }

        @Override
        public void onQuakeLongClick(Earthquake earthquake) {
            Intent intent = new Intent(mContext, WebInfoActivity.class);
            intent.putExtra("url", earthquake.getUrl());
            startActivity(intent);

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);

        mBundle = new Bundle();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        ButterKnife.bind(this, view);

        mQuakeListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mQuakeListAdapter = new QuakeAdapter(onQuakeItemClickedListener);
        mQuakeListView.setAdapter(mQuakeListAdapter);
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
            if (((MainActivity) getActivity()).getEarthquakes() != null && ((MainActivity) getActivity()).getEarthquakes().size() > 0) {
                mEarthquakes = ((MainActivity) getActivity()).getEarthquakes();
                setupList(mEarthquakes);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                handleSearchBar();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void handleSearchBar() {
        if (mSearchBar.getVisibility() == View.VISIBLE) {
            mSearchBar.setVisibility(View.GONE);
            mSearchView.setIconified(true);
            mSearchView.clearFocus();
            Utils.hideKeyboard(mSearchBar);
        } else {
            mSearchBar.setVisibility(View.VISIBLE);
            mSearchView.setIconified(false);
            mSearchView.requestFocus();
            Utils.showKeyboard(mSearchBar);
        }
    }

    /**
     * This sorts the list and sets the adapter
     */
    public void setupList(final ArrayList<Earthquake> earthquakes) {
        if (earthquakes != null && mContext != null) {
            Log.i(TAG, "setupList, with size: " + earthquakes.size());
            mQuakeCountTextView.setText(String.format(mContext.getString(R.string.quake_count), mEarthquakes.size()));
            mQuakeListAdapter.setQuakeList(earthquakes);
            if (earthquakes.size() == 0) {
                Toast.makeText(mContext, mContext.getString(R.string.empty_list)
                        , Toast.LENGTH_LONG).show();
            }
        } else {
            Log.i(TAG, "Null context " + (mContext == null));
        }
    }

    /**
     * Sorting a feature collection.
     *
     * @param earthquakes ArrayList of earthquakes
     */
    public void basicSort(ArrayList<Earthquake> earthquakes) {
        Collections.sort(earthquakes, new Comparator<Earthquake>() {
            @Override
            public int compare(Earthquake lhs, Earthquake rhs) {
                //Using Double's compare method makes this pretty straightforward.
                return Double.compare(rhs.getMag(), lhs.getMag());
            }
        });
    }

    /**
     * Sorting list by distance from user
     */
    public void sortByProximity(double latitude, double longitude) {
        ArrayList<Earthquake> proximityList = new ArrayList<>();
        TreeMap<Float, Earthquake> proximityMap = new TreeMap<>();
        for (Earthquake earthquake : mEarthquakes) {
            float[] results = new float[3];
            Location.distanceBetween(latitude, longitude, earthquake.getLatitude(),
                    earthquake.getLongitude(), results);
            proximityMap.put(results[0] / 1000, earthquake);
        }
        for (Map.Entry<Float, Earthquake> entry : proximityMap.entrySet()) {
            proximityList.add(entry.getValue());
        }
        mEarthquakes.clear();
        mEarthquakes = proximityList;
        setupList(mEarthquakes);
    }

    /**
     * Crude indeed. But it works 'enough'
     */
    public void doSearch() {
        ArrayList<Earthquake> searchEarhquakes = new ArrayList<>();
        String searchTerm = mSearchView.getQuery().toString();
        for (Earthquake earthquake : mEarthquakes) {
            //For "expected" input, this should handle cases
            if (earthquake.getPlace() != null
                    && earthquake.getPlace().toLowerCase().contains(searchTerm)) {
                searchEarhquakes.add(earthquake);
            }
        }

        if (searchEarhquakes.size() == 0) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.empty_search_list)
                    , Toast.LENGTH_LONG).show();
        } else {
            mQuakeCountTextView.setText(String.format(getActivity().getString(R.string.quake_count), mEarthquakes.size()));
            mSearchView.setQuery("", false);
            //close keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            setupList(searchEarhquakes);
        }

    }

    @Override
    public void asyncUnderway() {
        //unused
    }

    @Override
    public void dataCallBack(ArrayList<Earthquake> earthquakes) {
        mEarthquakes = earthquakes;
        basicSort(mEarthquakes);
        setupList(mEarthquakes);
    }

    /**
     * Called from activity on refresh
     */
    public void onUpdateData(ArrayList<Earthquake> earthquakes) {
        mEarthquakes = earthquakes;
        if (mEarthquakes != null) {
            basicSort(mEarthquakes);
            setupList(mEarthquakes);
        }
    }

}