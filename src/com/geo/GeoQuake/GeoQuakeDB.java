package com.geo.GeoQuake;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class GeoQuakeDB extends SQLiteOpenHelper {
    private static final int DB_VERSION = 19;
    private static final String DB_NAME = "GeoQuake";
    private static final String TABLE_NAME = "quakes";
    //Columns
    private static final String QUAKE_SOURCE = "quake_source";
    private static final String QUAKE_STRENGTH_TYPE = "quake_strength_type";
    private static final String QUAKE_PERIOD_TYPE = "quake_period_type";
    private static final String QUAKE_DATA = "quake_data";
    private static final String QUAKE_DATE = "quake_date";


    public GeoQuakeDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Initializing the db
     *
     * @param db SQLiteDatabase object to create
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY," +QUAKE_SOURCE+ " TEXT,"+ QUAKE_STRENGTH_TYPE + " TEXT, " + QUAKE_PERIOD_TYPE + " TEXT, " + QUAKE_DATA + " TEXT, " + QUAKE_DATE + " TEXT)");
    }

    /**
     * When upgrading the db, stuff needed logic here. By default, it merely drops the table,
     * and calls onCreate()
     *
     * @param db         SQLiteDatabase object to upgrade
     * @param oldVersion represents old version number
     * @param newVersion represents new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME); //we'll just drop the table for now
        onCreate(db);
    }

    /**
     * Initial db insert for a given set of params
     *
     * @param quakeStrength originates from the Spinner in the activity
     * @param quakeDuration originates from the Spinner in the activity
     * @param data          new data for DB insert
     */
    public void setData(String source, String quakeStrength, String quakeDuration, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_SOURCE, source);
        cv.put(QUAKE_STRENGTH_TYPE, quakeStrength);
        cv.put(QUAKE_PERIOD_TYPE, quakeDuration);
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, "" + getTime());
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    /**
     * Updating data that has already been set
     *
     * @param quakeStrength originates from the Spinner in the activity
     * @param quakeDuration originates from the Spinner in the activity
     * @param data          new data to be used in DB update
     */
    public void updateData(String source, String quakeStrength, String quakeDuration, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, "" + getTime());
        String where = QUAKE_SOURCE +"=? AND " + QUAKE_STRENGTH_TYPE + "=? AND " + QUAKE_PERIOD_TYPE + " =?";
        String[] value = {source, quakeStrength, quakeDuration};
        db.update(TABLE_NAME, cv, where, value);
        db.close();
    }

    /**
     * Fetching the data column for the selected row(s)
     *
     * @param quakeStrength originates from the Spinner in the activity
     * @param quakeDuration originates from the Spinner in the activity
     * @return data column value
     */
    public String getData(String source, String quakeStrength, String quakeDuration) {
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        //TODO: use query method here... rawQuery is a bit fugly. Just look at it.
        Cursor rowCursor = db.rawQuery("SELECT " + QUAKE_DATA + " FROM " + TABLE_NAME + " WHERE " +QUAKE_SOURCE +"='"+source+"' AND " + QUAKE_STRENGTH_TYPE
                + "='" + quakeStrength + "' AND " + QUAKE_PERIOD_TYPE + "='" + quakeDuration + "'", null);
        if (rowCursor.moveToFirst()) {
            do {
                result = rowCursor.getString(0);
            } while (rowCursor.moveToNext());

        }
        rowCursor.close();
        db.close();
        return result;
    }

    /**
     * Fetching the date column for the selected row(s)
     *
     * @param quakeStrength originates from the Spinner in the activity
     * @param quakeDuration originates from the Spinner in the activity
     * @return date column value
     */
    public String getDateColumn(String source, String quakeStrength, String quakeDuration) {
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        //TODO: use query method here
        Cursor rowCursor = db.rawQuery("SELECT " + QUAKE_DATE + " FROM " + TABLE_NAME + " WHERE " +QUAKE_SOURCE +"='"+source+"' AND " + QUAKE_STRENGTH_TYPE
                + "='" + quakeStrength + "' AND " + QUAKE_PERIOD_TYPE + "='" + quakeDuration + "'", null);
        if (rowCursor.moveToFirst()) {
            do {
                result = rowCursor.getString(0);
            } while (rowCursor.moveToNext());

        }
        //Log.i("getDateColumn() Result", result);
        rowCursor.close();
        db.close();
        return result;
    }


    /**
     * A simple method to get the current time in millis
     *
     * @return The current milliseconds converted to a String
     */
    public static long getTime() {
        Date d = new Date();
        return d.getTime();
    }

    /**
     * Time comparisons specifically for the refresh limiter
     */
    public static boolean checkRefreshLimit(long currentTime, long previousTime) {
        return (currentTime - previousTime) >= Utils.REFRESH_LIMITER_TIME;
    }


}
