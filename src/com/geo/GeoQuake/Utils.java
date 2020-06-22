package com.geo.GeoQuake;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.geo.GeoQuake.models.CanadaQuakes;
import com.geo.GeoQuake.models.Earthquake;
import com.geo.GeoQuake.models.Feature;
import com.geo.GeoQuake.models.FeatureCollection;
import com.geo.GeoQuake.models.Prefs;

import java.util.ArrayList;

/**
 * Created by gaius on 15-03-05.
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();
    public static final long REFRESH_LIMITER_TIME = 10000;

    /**
     * @return true if the network connection is ok, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = cm != null ? cm.getActiveNetworkInfo() : null;

            return network != null && (network.getType() == ConnectivityManager.TYPE_WIFI
                    || network.getType() == ConnectivityManager.TYPE_MOBILE) && network.isConnected();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void changeCache(int position, String[] cacheArray) {
        switch (position) {
            case 0:
            default:
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                Prefs.getInstance().setCacheTime(cacheArray[position]);
                break;
        }
    }

    /**
     * This towering method could use some honing. Might want to switch to just using static class vars for this, instead
     * of accessing Resources all the time.
     *
     * @return a string representing the proper fragment to pass to the URL string
     */
    public static String getURLFrag(int quakeSource, int quakeSelection, int durationSelection, Context context) {
        if(quakeSource == 0) {
            switch(durationSelection) {
                case 0:
                    switch(quakeSelection) {
                        case 0:
                        default:
                            return context.getString(R.string.significant_hour);
                        case 1:
                            return context.getString(R.string._4_5_hour);
                        case 2:
                            return context.getString(R.string._2_5_hour);
                        case 3:
                            return context.getString(R.string._1_0_hour);
                        case 4:
                            return context.getString(R.string.all_hour);
                    }
                case 1:
                    switch(quakeSelection) {
                        case 0:
                        default:
                            return context.getString(R.string.significant_day);
                        case 1:
                            return context.getString(R.string._4_5_day);
                        case 2:
                            return context.getString(R.string._2_5_day);
                        case 3:
                            return context.getString(R.string._1_0_day);
                        case 4:
                            return context.getString(R.string.all_day);
                    }
                case 2:
                    switch(quakeSelection) {
                        case 0:
                        default:
                            return context.getString(R.string.significant_week);
                        case 1:
                            return context.getString(R.string._4_5_week);
                        case 2:
                            return context.getString(R.string._2_5_week);
                        case 3:
                            return context.getString(R.string._1_0_week);
                        case 4:
                            return context.getString(R.string.all_week);
                    }
                case 3:
                    switch(quakeSelection) {
                        case 0:
                        default:
                            return context.getString(R.string.significant_month);
                        case 1:
                            return context.getString(R.string._4_5_month);
                        case 2:
                            return context.getString(R.string._2_5_month);
                        case 3:
                            return context.getString(R.string._1_0_month);
                        case 4:
                            return context.getString(R.string.all_month);
                    }
            }
            return context.getString(R.string.significant_week);
        } else if(quakeSource == 1) {
            //Canada won't filter by magnitude, just date
            switch(durationSelection) {
                case 0:
                default:
                    return context.getString(R.string.canada_7_days);
                case 1:
                    return context.getString(R.string.canada_30_days);
                case 2:
                    return context.getString(R.string.canada_365_days);
            }
        }
        return "";
    }

    /**
     * Generates the appropriate toast, depending on the anticipated time of the request.
     */
    public static void fireToast(int duration, int quake, Context context) {
        String toastText;
        int toastDuration;
        if (duration == 2 && quake == 4) {
            toastText = context.getString(R.string.loading_data_long);
            toastDuration = Toast.LENGTH_LONG;
        } else {
            toastText = context.getString(R.string.loading_data);
            toastDuration = Toast.LENGTH_SHORT;
        }
        Toast toast = Toast.makeText(context, toastText, toastDuration);
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
     * @param timeStamp current timestamp
     * @return boolean representing whether or not our DB row is expired
     */
    public static boolean isExpired(long timeStamp) {
        //default cache time set at 5 minutes (300000 ms)
        return (GeoQuakeDB.getTime() - timeStamp > Long.parseLong(Prefs.getInstance().getCacheTime()));
    }

    public static void hideKeyboard(View view) {
        if(view == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm == null) return;
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(View view) {
        if(view == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm == null) return;
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static ArrayList<Earthquake> convertModelBySource(int source, String json) {
        ArrayList<Earthquake> quakes = new ArrayList<>();
        if(source == 0) {
            FeatureCollection featureCollection = new FeatureCollection(json);
            for(Feature f : featureCollection.getFeatures()) {
                quakes.add(new Earthquake(f));
            }
            return quakes;
        } else if(source == 1) {
            CanadaQuakes canadaQuakes = new CanadaQuakes(json);
            return canadaQuakes.getEarthquakes();
        }
        return quakes;
    }


}
