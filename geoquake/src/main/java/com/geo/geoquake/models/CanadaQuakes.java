package com.geo.geoquake.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by George Stinson on 2017-03-28.
 */

public class CanadaQuakes {

    private final String MAG = "magnitude";
    private final String LOCATION = "location";
    private final String GEOJSON = "geoJSON";
    private final String COORDINATES = "coordinates";
    private final String ORIGIN_TIME = "origin_time";
    private final String METADATA = "metadata";
    ArrayList<Earthquake> earthquakes;

    public CanadaQuakes(String json) {
        earthquakes = new ArrayList<>();
       try {
           JSONObject jsonObject = new JSONObject(json);
           Iterator<String> keysIterator = jsonObject.keys();
           List<String> keysList = new ArrayList<>();
           while(keysIterator.hasNext()) {
               String key = keysIterator.next();
               keysList.add(key);
           }
           for (String s: keysList) {
               if(!s.equals(METADATA)) {
                   Earthquake earthquake = new Earthquake();
                   JSONObject quakeItem = jsonObject.getJSONObject(s);
                   if(quakeItem.has(ORIGIN_TIME)) {
                       earthquake.setTimeString(processTimeString(quakeItem.getString(ORIGIN_TIME)));
                   }
                   if(quakeItem.has(MAG)) {
                       earthquake.setMag(quakeItem.getDouble(MAG));
                   }
                   if(quakeItem.has(LOCATION)) {
                       JSONObject locationObject = quakeItem.getJSONObject(LOCATION);
                       earthquake.setPlace(locationObject.getString("en"));
                   }
                   if(quakeItem.has(GEOJSON)) {
                       JSONObject geoJSONObject = quakeItem.getJSONObject(GEOJSON);
                       JSONArray coords = geoJSONObject.getJSONArray(COORDINATES);
                       earthquake.setLatitude(coords.getDouble(0));
                       earthquake.setLongitude(coords.getDouble(1));
                   }
                   earthquake.setUrl("http://www.earthquakescanada.nrcan.gc.ca/index-en.php");
                   earthquake.setSource(Earthquake.CANADA);
                   earthquakes.add(earthquake);
               }
           }

       } catch(JSONException e) {
           Log.e("CanadaQuakes", "json error!");
       }
    }

    public ArrayList<Earthquake> getEarthquakes() {
        return earthquakes;
    }

    private String processTimeString(String dateTime) {
        String dateString = dateTime.substring(0, dateTime.indexOf("T"));
        String timeString = dateTime.substring(dateTime.indexOf("T")+1, dateTime.indexOf("+"));
        return dateString + " " + timeString;
    }
}
