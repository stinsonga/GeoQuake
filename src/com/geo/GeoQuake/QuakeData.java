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
     * @param usgsUrl
     */
    public QuakeData(String usgsUrl, int quakeDuration, int quakeType, IDataCallback dataCallback, Context context){
        this.usgsUrl = usgsUrl;
        this.mQuakeDuration = quakeDuration;
        this.mQuakeType = quakeType;
        this.mDataCallback = dataCallback;
        this.mContext = context;
        this.mGeoQuakeDB = new GeoQuakeDB(context);
    }

    /**
     *
     * @return
     */
    public FeatureCollection getFeatureCollection(){
        return mFeatureCollection;
    }

    /**
     *
     * @param context
     * @return
     */
    public void fetchData(Context context){
        processData(context);
    }

    /**
     *
     * @param context
     */
    private void processData(Context context) {
        if (checkForStoredData()) {
            //TODO: check for stored data, versus cache timer

        } else {
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
                        mDataCallback.dataCallback();
                    }
                }.execute(new URL(usgsUrl + Utils.getURLFrag(mQuakeType,
                        mQuakeDuration, context)));
            } catch (MalformedURLException me) {
                Log.e(me.getMessage(), "URL Problem...");

            }
        }
    }

    /**
     *
     * @param url The url we'll use to fetch the data
     * @return A JSONObject containing the requested data
     */
    private FeatureCollection getJSON(URL url) {
        try{
            HttpURLConnection connect = (HttpURLConnection) url.openConnection();
            InputStream inputStream = connect.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String dataResponse = "";
            String currentStream = "";
            while((currentStream = bufferedReader.readLine()) != null){
                dataResponse += currentStream;
            }
            return new FeatureCollection(dataResponse);
        } catch(IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * We'll use this method to check a row's timestamp vs the current time, to determine whether
     * or not to overwrite
     *
     * @param timeStamp
     * @return
     */
    private boolean compareDate(long timeStamp){
        return false; //TODO: fill 'er out
    }

    /**
     * Checking to see if there is already data stored, and if it should be used
     *
     * @return
     */
    private boolean checkForStoredData(){
        if(!mGeoQuakeDB.getData(""+mQuakeType, ""+mQuakeDuration).isEmpty()
                ){

        }
        return false;
    }

}
