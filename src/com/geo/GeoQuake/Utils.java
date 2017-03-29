package com.geo.GeoQuake;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.geo.GeoQuake.models.Earthquake;
import com.geo.GeoQuake.models.Feature;
import com.geo.GeoQuake.models.FeatureCollection;
import com.geo.GeoQuake.models.Prefs;

import java.util.ArrayList;

/**
 * Created by gaius on 15-03-05.
 */
public class Utils {

    public static final long REFRESH_LIMITER_TIME = 10000;

    /**
     * @return true if the network connection is ok, false otherwise
     */
    public static boolean checkNetwork(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = cm.getActiveNetworkInfo();

            return (network.getType() == ConnectivityManager.TYPE_WIFI
                    || network.getType() == ConnectivityManager.TYPE_MOBILE) && network.isConnected();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void changeCache(int position, String[] cacheArray) {
        switch (position) {
            case 0:
                break;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                Prefs.getInstance().setCacheTime(cacheArray[position]);
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
    public static String getURLFrag(int quakeSource, int quakeSelection, int durationSelection, Context context) {
        if(quakeSource == 0) {
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
            //Unreachable with current UI options... may return soon
            else if (durationSelection == 3) {
                if (quakeSelection == 0) {
                    return context.getString(R.string.significant_month);
                } else if (quakeSelection == 1) {
                    return context.getString(R.string._4_5_month);
                } else if (quakeSelection == 2) {
                    return context.getString(R.string._2_5_month);
                } else if (quakeSelection == 3) {
                    return context.getString(R.string._1_0_month);
                } else if (quakeSelection == 4) {
                    return context.getString(R.string.all_month);
                }
            }
            return context.getString(R.string.significant_week);
        } else if(quakeSource == 1) {
            //Canada won't filter by magnitude, just date
            if(durationSelection == 0) {
                return context.getString(R.string.canada_7_days);
            } else if(durationSelection == 1) {
                return context.getString(R.string.canada_30_days);
            } else if(durationSelection == 2) {
                return context.getString(R.string.canada_365_days);
            }
        }
        return "";
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
     * @param timeStamp current timestamp
     * @param context   from the activity that calls this method
     * @return boolean representing whether or not our DB row is expired
     */
    public static boolean isExpired(long timeStamp, Context context) {
        //default cache time set at 5 minutes (300000 ms)
        return (GeoQuakeDB.getTime() - timeStamp > Long.parseLong(Prefs.getInstance().getCacheTime()));
    }

    public static void hideKeyboard(View view) {
        if(view == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showKeyboard(View view) {
        if(view == null) return;
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public ArrayList<Earthquake> convertFeatureModel(FeatureCollection featureCollection) {
        ArrayList<Earthquake> quakes = new ArrayList<Earthquake>();
        for(Feature f : featureCollection.getFeatures()) {
            quakes.add(new Earthquake(f.getLatitude(), f.getLongitude(), f.getProperties().getMag(),
                    f.getProperties().getPlace(), f.getProperties().getTime(), f.getProperties().getUrl()));
        }
        return quakes;
    }


}
