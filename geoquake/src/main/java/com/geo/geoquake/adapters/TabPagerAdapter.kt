package com.geo.geoquake.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.geo.geoquake.ListFragment
import com.geo.geoquake.QuakeMapFragment
import com.geo.geoquake.models.Earthquake
import com.geo.geoquake.R

import java.util.*

/**
 * Created by George Stinson on 2016-09-27.
 */

class TabPagerAdapter(fm: FragmentManager, internal var context: Context)
    : androidx.fragment.app.FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    internal var mMapFragment: QuakeMapFragment = QuakeMapFragment.newInstance()
    internal var mListFragment: ListFragment = ListFragment.newInstance()

    override fun getItem(position: Int): Fragment {
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
        return when (position) {
            0 -> context.getString(R.string.map_tab)
            1 -> context.getString(R.string.list_tab)
            else -> context.getString(R.string.list_tab)
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
