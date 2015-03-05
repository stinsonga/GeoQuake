package com.geo.GeoQuake;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GeoQuakeDB extends SQLiteOpenHelper{
    private static final int DB_VERSION = 4;
    private static final long DAY_MILLISECONDS = 86400000;
    private static final String DB_NAME = "GeoQuake";
    private static final String TABLE_NAME = "quakes";
    //Other columns to be defined!
    private static final String QUAKE_TYPE = "quake_type";
    private static final String QUAKE_DATA = "quake_data";
    private static final String QUAKE_DATE = "quake_date";
    //Quake types
    private static final String HOUR_ALL = "hour_all";
    private static final String HOUR_1 = "hour_1";
    private static final String HOUR_25 = "hour_25";
    private static final String HOUR_45 = "hour_45";
    private static final String HOUR_SIG = "hour_significant";

    private static final String DAY_ALL = "day_all";
    private static final String DAY_1 = "day_1";
    private static final String DAY_25 = "day_25";
    private static final String DAY_45 = "day_45";
    private static final String DAY_SIG = "day_significant";

    private static final String WEEK_ALL = "week_all";
    private static final String WEEK_1 = "week_1";
    private static final String WEEK_25 = "week_25";
    private static final String WEEK_45 = "week_45";
    private static final String WEEK_SIG = "week_significant";

    private static final String MONTH_ALL = "month_all";
    private static final String MONTH_1 = "month_1";
    private static final String MONTH_25 = "month_25";
    private static final String MONTH_45 = "month_45";
    private static final String MONTH_SIG = "month_significant";

    public GeoQuakeDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY," +QUAKE_TYPE + " TEXT, " + QUAKE_DATA + " TEXT, " +QUAKE_DATE+ " TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void setData(String type, String data) {
        Log.i("setData date(approx)", getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_TYPE, type);
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, getTime());
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public String getData(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + QUAKE_TYPE + "='" + query + "'", null);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString(2);
            } while (cursor.moveToNext());

        }
        return result;
    }

    public String getDateColumn(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + QUAKE_TYPE + "='" + query + "'", null);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString(3);
            } while (cursor.moveToNext());

        }
        return result;
    }

    public void updateData(String name, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, getTime());
        String where = QUAKE_TYPE + "=?";
        String[] value = {name};
        db.update(TABLE_NAME, cv, where, value);
    }

    /**
     * A simple method to get the current time in millis
     *
     * @return The current milliseconds converted to a String
     */
    public static String getTime(){
        Date d = new Date();
        return ""+d.getTime();
    }

    /**
     * A method used to compare timestamps, in case we need to update the db
     *
     * @param time
     * @return
     */
    public static boolean compareTime(long prefTime, long time){
        return (Long.parseLong(getTime()) - time) > prefTime;
    }


}
