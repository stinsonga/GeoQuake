package com.geo.geoquake;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.geo.geoquake.models.CanadaQuakes;
import com.geo.geoquake.models.Earthquake;
import com.geo.geoquake.models.Feature;
import com.geo.geoquake.models.FeatureCollection;
import com.geo.geoquake.models.Prefs;

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
     * @return a string representing the proper fragment to pass to the URL string
     */
    public static String getURLFrag(int quakeSource, int quakeSelection, int durationSelection, Context context) {
        if(quakeSource == 0) {
            return getUSGSString(context, durationSelection, quakeSelection);
        } else if(quakeSource == 1) {
            //Canada won't filter by magnitude, just date
            return getCanadaQuakeString(context, durationSelection);
        }
        return "";
    }

    /**
     * Get USGS string for use in requests
     * @param context
     * @param durationSelection
     * @param quakeSelection
     * @return
     */
    private static String getUSGSString(Context context, int durationSelection, int quakeSelection) {
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
    }

    /**
     *
     * @param context
     * @param durationSelection
     * @return
     */
    private static String getCanadaQuakeString(Context context, int durationSelection) {
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

    /**
     * Checking to see if there is already data stored, and if it should be used
     *
     * @return true if we need to refresh data
     */
    public static boolean needToRefreshData(GeoQuakeDB geoQuakeDB, int quakeSource, int quakeType, int quakeDuration) {
        //first check to see if results are empty
        if (!geoQuakeDB.getData("" + quakeSource, "" + quakeType, "" + quakeDuration).isEmpty()) {
            //is the data too old?
            //return false, we'll use existing data
            return Utils.isExpired(Long.parseLong(geoQuakeDB.getDateColumn("" + quakeSource,
                    "" + quakeType, "" + quakeDuration)));
        } else {
            //data is empty...need to fetch it
            return true;
        }
    }


}
