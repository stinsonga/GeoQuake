package com.geo.GeoQuake;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
     * @return true if the network connection is ok, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        boolean wifiOnly = context.getSharedPreferences(QUAKE_PREFS, Context.MODE_PRIVATE).getBoolean(WIFI_ONLY, false);
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiOnly) {
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

    public static void changeCache(int position, SharedPreferences sp, String[] cacheArray) {
        SharedPreferences.Editor editor = sp.edit();
        switch (position) {
            //Crude fix for the fact that the spinner prompt doesn't seem to work, or perhaps
            //I don't get precisely how it is supposed to work, if at all.
            case 0:
                break;
            case 1:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            case 2:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            case 3:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            case 4:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            case 5:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            case 6:
                editor.putString(CACHE_TIME, cacheArray[position]);
                editor.apply();
                break;
            default:
                break;
        }
    }

    /**
     * This towering method could use some honing. Might want to switch to just using static class vars for this, instead
     * of accessing Resources all the time.
     *
     * @return a string representing the proper fragment to pass to the URL string
     */
    public static String getURLFrag(int quakeSelection, int durationSelection, Context context) {
        if (durationSelection == 0) {
            if (quakeSelection == 0) {
                return context.getString(R.string.significant_hour);
            } else if (quakeSelection == 1) {
                return context.getString(R.string._4_5_hour);
            } else if (quakeSelection == 2) {
                return context.getString(R.string._2_5_hour);
            } else if (quakeSelection == 3) {
                return context.getString(R.string._1_0_hour);
            } else if (quakeSelection == 4) {
                return context.getString(R.string.all_hour);
            }
        } else if (durationSelection == 1) {
            if (quakeSelection == 0) {
                return context.getString(R.string.significant_day);
            } else if (quakeSelection == 1) {
                return context.getString(R.string._4_5_day);
            } else if (quakeSelection == 2) {
                return context.getString(R.string._2_5_day);
            } else if (quakeSelection == 3) {
                return context.getString(R.string._1_0_day);
            } else if (quakeSelection == 4) {
                return context.getString(R.string.all_day);
            }
        } else if (durationSelection == 2) {
            if (quakeSelection == 0) {
                return context.getString(R.string.significant_week);
            } else if (quakeSelection == 1) {
                return context.getString(R.string._4_5_week);
            } else if (quakeSelection == 2) {
                return context.getString(R.string._2_5_week);
            } else if (quakeSelection == 3) {
                return context.getString(R.string._1_0_week);
            } else if (quakeSelection == 4) {
                return context.getString(R.string.all_week);
            }
        }

        /*
        Removed the past month option, due to OOM issues. Code is left here, for a future optimization update when it may be enabled again.

         */
//        else if(durationSelection == 3){
//            if(quakeSelection == 0){
//                return mContext.getString(R.string.significant_month);
//            }else if(quakeSelection == 1){
//                return mContext.getString(R.string._4_5_month);
//            }else if(quakeSelection == 2){
//                return mContext.getString(R.string._2_5_month);
//            }else if(quakeSelection == 3){
//                return mContext.getString(R.string._1_0_month);
//            }else if(quakeSelection == 4){
//                return mContext.getString(R.string.all_month);
//            }
//        }
        return context.getString(R.string.significant_week);

    }

    /**
     * Generates the appropriate toast, depending on the anticipated time of the request.
     */
    public static void fireToast(int duration, int quake, Context context) {
        Toast toast;
        if (duration == 2 && quake == 4) {
            toast = Toast.makeText(context, context.getString(R.string.loading_data_long), Toast.LENGTH_LONG);
        } else {
            toast = Toast.makeText(context, context.getString(R.string.loading_data), Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    /**
     * Generates the long toast message
     */
    public static void connectToast(Context context) {
        Toast toast;
        toast = Toast.makeText(context, context.getResources().getString(R.string.no_network), Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * We'll use this method to check a row's timestamp vs the current time, to determine whether
     * or not to overwrite
     *
     * @param timeStamp
     * @return
     */
    public static boolean isExpired(long timeStamp, Context context) {
        SharedPreferences sp = context.getSharedPreferences(Utils.QUAKE_PREFS, Context.MODE_PRIVATE);
        //default cache time set at 5 minutes (300000 ms)
        if (GeoQuakeDB.getTime() - timeStamp > Long.parseLong(sp.getString(Utils.CACHE_TIME, "300000"))) {
            return true; //need to refresh data
        } else {
            return false; //data still good, keep it
        }
    }


}
