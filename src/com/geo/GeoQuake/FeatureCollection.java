package com.geo.GeoQuake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by gstinson on 15-03-02.
 */
public class FeatureCollection {

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
            JSONObject jsonObject = new JSONObject(jsonString);
            //setup arrays
            this.metadata = jsonObject.getJSONObject("metadata");
            this.bbox = jsonObject.getJSONArray("bbox");
            JSONArray featuresArray = jsonObject.getJSONArray("features");
            for(int i = 0; i < featuresArray.length(); i++){
                features.add(new Feature(featuresArray.getJSONObject(i)));
            }
            //setup metadata
            this.generated = jsonObject.getLong("generated");
            this.url = jsonObject.getString("url");
            this.title = jsonObject.getString("title");
            this.api = jsonObject.getString("api");
            this.count = jsonObject.getInt("count");
            this.status = jsonObject.getInt("status");

        } catch (JSONException je){

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
