package com.geo.GeoQuake;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class to be used for handling data fetching
 */
public class QuakeData {

    protected String usgsUrl;
    protected FeatureCollection mFeatureCollection = new FeatureCollection();
    protected IDataCallback mDataCallback;
    protected int mQuakeType;
    protected int mQuakeDuration;
    protected Context mContext;
    GeoQuakeDB mGeoQuakeDB;

    /**
     * Constructor... takes only the url for USGS, which should be a resource
     *
     * @param usgsUrl url being used to get quake data
     */
    public QuakeData(String usgsUrl, int quakeDuration, int quakeType, IDataCallback dataCallback, Context context) {
        this.usgsUrl = usgsUrl;
        this.mQuakeDuration = quakeDuration;
        this.mQuakeType = quakeType;
        this.mDataCallback = dataCallback;
        this.mContext = context;
        this.mGeoQuakeDB = new GeoQuakeDB(context);
    }
    /**
     * @param context needed for call to processData
     * @return
     */
    public void fetchData(Context context) {
        processData(context);
    }

    /**
     * @param context passing in context here, needed for calls within method
     */
    private void processData(final Context context) {
        if (needToRefreshData()) {
            try {
                new AsyncTask<URL, Void, FeatureCollection>() {
                    @Override
                    protected FeatureCollection doInBackground(URL... params) {
                        try {
                            mDataCallback.asyncUnderway();
                            return getJSON(new URL(usgsUrl + Utils.getURLFrag(
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
                    protected void onPostExecute(FeatureCollection featureCollection) {
                        super.onPostExecute(featureCollection);
                        mFeatureCollection = featureCollection;
                        Log.i(QuakeData.class.getSimpleName(), "onPostExecute " + featureCollection.count);
                        mDataCallback.dataCallback(mFeatureCollection);
                    }
                }.execute(new URL(usgsUrl + Utils.getURLFrag(mQuakeType,
                        mQuakeDuration, context)));
            } catch (MalformedURLException me) {
                Log.e(me.getMessage(), "URL Problem...");

            }
        } else {
            //no need to refresh, so we send them back the persisted data
            mFeatureCollection = new FeatureCollection(mGeoQuakeDB.getData("" + mQuakeType, "" + mQuakeDuration));
            mDataCallback.dataCallback(mFeatureCollection);
        }
    }

    /**
     * @param url The url we'll use to fetch the data
     * @return A JSONObject containing the requested data
     */
    private FeatureCollection getJSON(URL url) {
        try {
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connect.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String dataResponse = "";
            String currentStream;
            while ((currentStream = bufferedReader.readLine()) != null)
                dataResponse += currentStream;
            if (mGeoQuakeDB.getData("" + mQuakeType, "" + mQuakeDuration).isEmpty()) {
                mGeoQuakeDB.setData("" + mQuakeType, "" + mQuakeDuration, dataResponse);
            } else {
                mGeoQuakeDB.updateData("" + mQuakeType, "" + mQuakeDuration, dataResponse);
            }
            return new FeatureCollection(dataResponse);
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
        if (!mGeoQuakeDB.getData("" + mQuakeType, "" + mQuakeDuration).isEmpty()) {
            //is the data too old?
            if (Utils.isExpired(Long.parseLong(mGeoQuakeDB.getDateColumn("" + mQuakeType, "" + mQuakeDuration)), mContext)) {
                return true;
            } else {
                //use existing data set, and return false
                mFeatureCollection = new FeatureCollection(mGeoQuakeDB.getData("" + mQuakeType, "" + mQuakeDuration));
                return false;
            }
        } else {
            //data is empty...need to fetch it
            return true;
        }
    }

}
