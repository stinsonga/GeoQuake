package com.geo.GeoQuake.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

import com.geo.GeoQuake.models.Earthquake
import com.geo.GeoQuake.models.FeatureCollection
import com.geo.GeoQuake.ListFragment
import com.geo.GeoQuake.QuakeMapFragment
import com.geo.GeoQuake.R

import java.util.ArrayList

/**
 * Created by George Stinson on 2016-09-27.
 */

class TabPagerAdapter(fm: androidx.fragment.app.FragmentManager, internal var context: Context) : androidx.fragment.app.FragmentPagerAdapter(fm) {
    internal var mMapFragment: QuakeMapFragment
    internal var mListFragment: ListFragment

    init {
        this.mMapFragment = QuakeMapFragment.newInstance()
        this.mListFragment = ListFragment.newInstance()
    }

    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        when (position) {
            0 -> return mMapFragment
            1 -> return mListFragment
            else -> return mListFragment
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return context.getString(R.string.map_tab)
            1 -> return context.getString(R.string.list_tab)
            else -> return context.getString(R.string.list_tab)
        }
    }

    fun updateFragments(earthquakes: ArrayList<Earthquake>, hasUserLocation: Boolean, latitude: Double, longitude: Double) {
        mMapFragment.onUpdateData(earthquakes)
        mListFragment.onUpdateData(earthquakes)
        if (hasUserLocation) {
            mMapFragment.userLocationMarker(latitude, longitude)
        }
    }

    fun moveCamera(latitude: Double, longitude: Double) {
        mMapFragment.moveCameraToUserLocation(latitude, longitude)
    }

    fun sortByProximity(latitude: Double, longitude: Double) {
        mListFragment.sortByProximity(latitude, longitude)
    }
}
