package com.geo.GeoQuake.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contained within a FeatureCollection
 *
 */
public class Feature {

    protected String type;
    protected String id;
    //base object for properties, but we'll break it up in here
    protected JSONObject propertiesJson;
    //base object for geometry, which we'll break up here
    protected JSONObject geometry;
    protected double longitude;
    protected double latitude;
    protected double depth;
    protected Properties properties;

    public Feature(JSONObject jsonObject){
        try{
            this.type = jsonObject.getString("type");
            this.id = jsonObject.getString("id");
            this.propertiesJson = jsonObject.getJSONObject("properties");
            this.geometry = jsonObject.getJSONObject("geometry");
            this.longitude = geometry.getJSONArray("coordinates").getDouble(0);
            this.latitude = geometry.getJSONArray("coordinates").getDouble(1);
            this.depth = geometry.getJSONArray("coordinates").getDouble(2);
            this.properties = new Properties(propertiesJson);
        } catch(JSONException je){
            Log.e("Feature object except", je.getMessage());
        }
    }

    public Properties getProperties(){
        return properties;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public JSONObject getPropertiesJson() {
        return propertiesJson;
    }

    public JSONObject getGeometry() {
        return geometry;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getDepth() {
        return depth;
    }

    public class Properties {
        protected double mag;
        protected String place;
        protected long time;
        protected long updated;
        protected int tz;
        protected String url;
        protected String detail;
        protected int felt;
        protected double cdi;
        protected double mmi;
        protected String alert;
        protected String status;
        protected int tsunami;
        protected int sig;
        protected String net;
        protected String code;
        protected String ids;
        protected String sources;
        protected String types;
        protected int nst;
        protected double dmin;
        protected double rms;
        protected double gap;
        protected String magType;
        protected String type;

        public double getMag() {
            return mag;
        }

        public String getPlace() {
            return place;
        }

        public long getTime() {
            return time;
        }

        public long getUpdated() {
            return updated;
        }

        public int getTz() {
            return tz;
        }

        public String getUrl() {
            return url;
        }

        public String getDetail() {
            return detail;
        }

        public int getFelt() {
            return felt;
        }

        public double getCdi() {
            return cdi;
        }

        public double getMmi() {
            return mmi;
        }

        public String getAlert() {
            return alert;
        }

        public String getStatus() {
            return status;
        }

        public int getTsunami() {
            return tsunami;
        }

        public int getSig() {
            return sig;
        }

        public String getNet() {
            return net;
        }

        public String getCode() {
            return code;
        }

        public String getIds() {
            return ids;
        }

        public String getSources() {
            return sources;
        }

        public String getTypes() {
            return types;
        }

        public int getNst() {
            return nst;
        }

        public double getDmin() {
            return dmin;
        }

        public double getRms() {
            return rms;
        }

        public double getGap() {
            return gap;
        }

        public String getMagType() {
            return magType;
        }

        public String getType() {
            return type;
        }

        public Properties(JSONObject jsonObject){
            try{
                this.mag = jsonObject.getDouble("mag");
                this.place = jsonObject.getString("place");
                this.time  = jsonObject.getLong("time");
                this.updated = jsonObject.getLong("updated");
                this.tz = jsonObject.getInt("tz");
                this.url = jsonObject.getString("url");
                this.detail = jsonObject.getString("detail");
                this.felt = jsonObject.getInt("felt");
                this.cdi = jsonObject.getDouble("cdi");
                this.mmi = jsonObject.getDouble("mmi");
                this.alert = jsonObject.getString("alert");
                this.status = jsonObject.getString("status");
                this.tsunami = jsonObject.getInt("tsunami");
                this.sig = jsonObject.getInt("sig");
                this.net = jsonObject.getString("net");
                this.code = jsonObject.getString("code");
                this.ids = jsonObject.getString("ids");
                this.sources = jsonObject.getString("source");
                this.types = jsonObject.getString("types");
                this.nst = jsonObject.getInt("nst");
                this.dmin = jsonObject.getDouble("dmin");
                this.rms = jsonObject.getDouble("rms");
                this.gap = jsonObject.getDouble("gap");
                this.magType = jsonObject.getString("magType");
                this.type = jsonObject.getString("type");

            }catch (JSONException je){
                //TODO handle it
            }
        }
    }

}
