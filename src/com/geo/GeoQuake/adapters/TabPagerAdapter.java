package com.geo.GeoQuake.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.geo.GeoQuake.FeatureCollection;
import com.geo.GeoQuake.ListFragment;
import com.geo.GeoQuake.QuakeMapFragment;
import com.geo.GeoQuake.R;

/**
 * Created by George Stinson on 2016-09-27.
 */

public class TabPagerAdapter extends FragmentPagerAdapter {

    Context context;
    QuakeMapFragment mMapFragment;
    ListFragment mListFragment;

    public TabPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        this.mMapFragment = QuakeMapFragment.newInstance();
        this.mListFragment = ListFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new QuakeMapFragment();
            case 1:
            default:
                return new ListFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.map_tab);
            case 1:
            default:
                return context.getString(R.string.list_tab);
        }
    }

    public void updateFragments(Context context, FeatureCollection featureCollection, boolean hasUserLocation, double latitude, double longitude) {
        mMapFragment.onUpdateData(context, featureCollection);
        mListFragment.onUpdateData(context, featureCollection);
        if(hasUserLocation) {
            mMapFragment.userLocationMarker(context, latitude, longitude);
        }
    }

    public void moveCamera(Context context, double latitude, double longitude) {
        mMapFragment.moveCameraToUserLocation(context, latitude, longitude);
    }

    public void sortByProximity(Context context, double latitude, double longitude) {
        mListFragment.sortByProximity(context, latitude, longitude);
    }
}
