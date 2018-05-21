package com.geo.GeoQuake;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.geo.GeoQuake.models.Earthquake;
import com.geo.GeoQuake.models.FeatureCollection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Class to be used for handling data fetching
 */
public class QuakeData {

    protected static final String TAG = QuakeData.class.getSimpleName();
    private String usgsUrl;
    protected FeatureCollection mFeatureCollection = new FeatureCollection();
    private ArrayList<Earthquake> mEarthquakes;
    private IDataCallback mDataCallback;
    private int mQuakeSource;
    private int mQuakeType;
    private int mQuakeDuration;
    private Context mContext;
    private GeoQuakeDB mGeoQuakeDB;

    /**
     * Constructor... takes only the url for USGS, which should be a resource
     *
     * @param usgsUrl url being used to get quake data
     */
    public QuakeData(String usgsUrl, int quakeSource, int quakeDuration, int quakeType, IDataCallback dataCallback, Context context) {
        this.usgsUrl = usgsUrl;
        this.mQuakeSource = quakeSource;
        this.mQuakeDuration = quakeDuration;
        this.mQuakeType = quakeType;
        this.mDataCallback = dataCallback;
        this.mContext = context;
        this.mGeoQuakeDB = new GeoQuakeDB(context);
        this.mEarthquakes = new ArrayList<Earthquake>();
    }
    /**
     * @param context needed for call to processData
     * @return
     */
    public void fetchData(Context context) {
        if(needToRefreshData()) {
            processData(context);
        } else {
            //no need to refresh, so we send them back the persisted data
            mEarthquakes = Utils.convertModelBySource(mQuakeSource, mGeoQuakeDB.getData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration));
            mDataCallback.dataCallBack(mEarthquakes);
        }
    }

    /**
     * @param context passing in context here, needed for calls within method
     */
    private void processData(final Context context) {
            try {
                new AsyncTask<URL, Void, ArrayList<Earthquake>>() {
                    @Override
                    protected ArrayList<Earthquake> doInBackground(URL... params) {
                        try {
                            mDataCallback.asyncUnderway();
                            return getJSON(new URL(usgsUrl + Utils.getURLFrag(mQuakeSource,
                                    mQuakeType, mQuakeDuration, context)));
                        } catch (MalformedURLException me) {
                            return null;
                        }

                    }

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                    }

                    @Override
                    protected void onPostExecute(ArrayList<Earthquake> earthquakes) {
                        super.onPostExecute(earthquakes);
                        mEarthquakes = earthquakes;
                        //Log.i(QuakeData.class.getSimpleName(), "onPostExecute " + mEarthquakes.size());
                        mDataCallback.dataCallBack(mEarthquakes);
                    }
                }.execute(new URL(usgsUrl + Utils.getURLFrag(mQuakeSource, mQuakeType,
                        mQuakeDuration, context)));
            } catch (MalformedURLException me) {
                Log.e(me.getMessage(), "URL Problem...");
            }
    }

    /**
     * @param url The url we'll use to fetch the data
     * @return A JSONObject containing the requested data
     */
    private ArrayList<Earthquake> getJSON(URL url) {
        try {
            //Log.d(TAG, url.toString());
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connect.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String dataResponse = "";
            String currentStream;
            while ((currentStream = bufferedReader.readLine()) != null)
                dataResponse += currentStream;
            if (mGeoQuakeDB.getData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration).isEmpty()) {
                mGeoQuakeDB.setData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration, dataResponse);
            } else {
                mGeoQuakeDB.updateData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration, dataResponse);
            }
            //Log.d(TAG, dataResponse);
            return Utils.convertModelBySource(mQuakeSource, dataResponse);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Checking to see if there is already data stored, and if it should be used
     *
     * @return true if we need to refresh data
     */
    private boolean needToRefreshData() {
        //first check to see if results are empty
        if (!mGeoQuakeDB.getData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration).isEmpty()) {
            //is the data too old?
            if (Utils.isExpired(Long.parseLong(mGeoQuakeDB.getDateColumn("" + mQuakeSource,
                    "" + mQuakeType, "" + mQuakeDuration)))) {
                return true;
            } else {
                //use existing data set, and return false
                mEarthquakes= Utils.convertModelBySource(mQuakeSource, mGeoQuakeDB.getData("" + mQuakeSource, "" + mQuakeType, "" + mQuakeDuration));
                return false;
            }
        } else {
            //data is empty...need to fetch it
            return true;
        }
    }

}
