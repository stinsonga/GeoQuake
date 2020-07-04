package com.geo.geoquake.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gstinson on 15-03-02.
 */
public class FeatureCollection {

    final String TAG = FeatureCollection.class.getSimpleName();

    //metadata
    protected JSONObject metadata;
    protected long generated;
    protected String url;
    protected String title;
    protected String api;
    protected int count;
    protected int status;

    //setup features list
    protected ArrayList<Feature> features = new ArrayList<>();

    //BBOX
    protected JSONArray bbox;



    public FeatureCollection(){

    }

    public FeatureCollection(String jsonString){
        try{
            //Log.d("FeatureCollection json", jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            //setup arrays
            this.metadata = jsonObject.getJSONObject("metadata");
            if(jsonObject.has("bbox")) {
                this.bbox = jsonObject.getJSONArray("bbox");
            }
            JSONArray featuresArray = jsonObject.getJSONArray("features");
            for(int i = 0; i < featuresArray.length(); i++){
                features.add(new Feature(featuresArray.getJSONObject(i)));
            }
            //Log.i("FeatureCollection", "size " + features.size());
            //setup metadata
            if(jsonObject.has("generated")) {
                this.generated = jsonObject.getLong("generated");
            }
            if(jsonObject.has("url")) {
                this.url = jsonObject.getString("url");
            }
            if(jsonObject.has("title")) {
                this.title = jsonObject.getString("title");
            }
            if(jsonObject.has("api")) {
                this.api = jsonObject.getString("api");
            }
            if(jsonObject.has("count")) {
                this.count = jsonObject.getInt("count");
            }
            if(jsonObject.has("status")) {
                this.status = jsonObject.getInt("status");
            }

        } catch (JSONException je){
            Log.e(TAG, "FeatureCollection derp: " + (je.getMessage() == null ? "Unknown error" : je.getMessage()));
        }

    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public long getGenerated() {
        return generated;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getApi() {
        return api;
    }

    public int getCount() {
        return count;
    }

    public int getStatus() {
        return status;
    }

    public ArrayList<Feature> getFeatures() {
        return features;
    }

    public JSONArray getBbox() {
        return bbox;
    }
}
