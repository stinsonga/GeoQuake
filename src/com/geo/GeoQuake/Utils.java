package com.geo.GeoQuake;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by gaius on 15-03-05.
 */
public class Utils {

    //SharedPrefs Constants
    public static final String QUAKE_PREFS = "quake_prefs";
    public static final String CACHE_LIMITER = "cache_limiter";
    public static final String REFRESH_LIMITER = "refresh_limiter";
    public static final String WIFI_ONLY = "wifi_only";
    public static final String CACHE_TIME = "cache_time";

    //Quake types
    public static final String HOUR_ALL = "hour_all";
    public static final String HOUR_1 = "hour_1";
    public static final String HOUR_25 = "hour_25";
    public static final String HOUR_45 = "hour_45";
    public static final String HOUR_SIG = "hour_significant";

    public static final String DAY_ALL = "day_all";
    public static final String DAY_1 = "day_1";
    public static final String DAY_25 = "day_25";
    public static final String DAY_45 = "day_45";
    public static final String DAY_SIG = "day_significant";

    public static final String WEEK_ALL = "week_all";
    public static final String WEEK_1 = "week_1";
    public static final String WEEK_25 = "week_25";
    public static final String WEEK_45 = "week_45";
    public static final String WEEK_SIG = "week_significant";

    public static final String MONTH_ALL = "month_all";
    public static final String MONTH_1 = "month_1";
    public static final String MONTH_25 = "month_25";
    public static final String MONTH_45 = "month_45";
    public static final String MONTH_SIG = "month_significant";

    public static final String[] ALL_TYPES_ARRAY = {HOUR_ALL, HOUR_1, HOUR_25, HOUR_45, HOUR_SIG, DAY_ALL, DAY_1,
            DAY_25, DAY_45, DAY_SIG, WEEK_ALL, WEEK_1, WEEK_25, WEEK_45, WEEK_SIG};

    public static final long REFRESH_LIMITER_TIME = 10000;

    /**
     *
     * @return true if the network connection is ok, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        boolean wifiOnly = context.getSharedPreferences(QUAKE_PREFS, Context.MODE_PRIVATE).getBoolean(WIFI_ONLY, false);
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if(wifiOnly){
                if (wifi.isConnected()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (wifi.isConnected() || mobile.isConnected()) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void changeCache(int position, SharedPreferences sp, String[] cacheArray){
        SharedPreferences.Editor editor = sp.edit();
        switch(position) {
            //Crude fix for the fact that the spinner prompt doesn't seem to work, or perhaps
            //I don't get precisely how it is supposed to work, if at all.
            case 0:
                Log.i("Cache not changed", "Default prompt");
                break;
            case 1:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "5 minutes, 300000");
                break;
            case 2:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "10 minutes, 600000");
                break;
            case 3:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "15 minutes, 900000");
                break;
            case 4:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "30 minutes, 1800000");
                break;
            case 5:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "60 minutes, 3600000");
                break;
            case 6:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                Log.i("Cache changed", "1 day, 86400000");
                break;
            default:
                break;
        }
    }

}
