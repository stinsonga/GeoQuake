package com.geo.geoquake.models;

import android.content.Context;
import android.content.SharedPreferences;

import com.geo.geoquake.App;
import com.geo.geoquake.GeoQuakeDB;

import java.util.Objects;

/**
 * Created by George Stinson on 2017-03-28.
 */

public class Prefs {

    public static final String QUAKE_PREFS = "quake_prefs";

    private static Prefs prefs;
    private final SharedPreferences sharedPreferences;

    public static Prefs getInstance() {
        if(prefs == null) {
            prefs = new Prefs();
        }
        return prefs;
    }

    private Prefs() {
        this.sharedPreferences = Objects.requireNonNull(App.Companion.getAppContext())
                .getSharedPreferences(QUAKE_PREFS, Context.MODE_PRIVATE);
    }

    public void setRefreshLimiter() {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putLong(KEYS.REFRESH_LIMITER, GeoQuakeDB.getTime());
        e.apply();
    }

    public long getRefreshLimiter() {
        return sharedPreferences.getLong(KEYS.REFRESH_LIMITER, 0);
    }

    public void setCacheTime(String cacheTime) {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString(KEYS.CACHE_TIME, cacheTime);
        e.apply();
    }

    public String getCacheTime() {
        return sharedPreferences.getString(KEYS.CACHE_TIME, "300000");
    }

    public void setSource(int source) {
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putInt(KEYS.SOURCE, source);
        e.apply();
    }

    public int getSource() {
        return sharedPreferences.getInt(KEYS.SOURCE, 0);
    }

    private static class KEYS {
        private static final String REFRESH_LIMITER = "refresh_limiter";
        private static final String CACHE_TIME = "cache_time";
        private static final String SOURCE = "source";
    }
}
