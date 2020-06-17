package com.geo.GeoQuake.models;

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

    ArrayList<Earthquake> earthquakes;

    public CanadaQuakes(String json) {
        earthquakes = new ArrayList<>();
       try {
           JSONObject jsonObject = new JSONObject(json);
           Iterator<String> keysIterator = jsonObject.keys();
           List<String> keysList = new ArrayList<>();
           while(keysIterator.hasNext()) {
               String key = (String) keysIterator.next();
               keysList.add(key);
           }
           for (String s: keysList) {
               if(!s.equals("metadata")) {
                   Earthquake earthquake = new Earthquake();
                   JSONObject quakeItem = jsonObject.getJSONObject(s);
                   if(quakeItem.has("origin_time")) {
                       earthquake.setTimeString(processTimeString(quakeItem.getString("origin_time")));
                   }
                   if(quakeItem.has("magnitude")) {
                       earthquake.setMag(quakeItem.getDouble("magnitude"));
                   }
                   if(quakeItem.has("location")) {
                       JSONObject locationObject = quakeItem.getJSONObject("location");
                       earthquake.setPlace(locationObject.getString("en"));
                   }
                   if(quakeItem.has("geoJSON")) {
                       JSONObject geoJSONObject = quakeItem.getJSONObject("geoJSON");
                       JSONArray coords = geoJSONObject.getJSONArray("coordinates");
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
