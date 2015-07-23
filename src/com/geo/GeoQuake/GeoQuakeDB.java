package com.geo.GeoQuake;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GeoQuakeDB extends SQLiteOpenHelper{
    private static final int DB_VERSION = 6;
    private static final long DAY_MILLISECONDS = 86400000;
    private static final String DB_NAME = "GeoQuake";
    private static final String TABLE_NAME = "quakes";
    //Other columns to be defined!
    private static final String QUAKE_STRENGTH_TYPE = "quake_strength_type";
    private static final String QUAKE_PERIOD_TYPE = "quake_period_type";
    private static final String QUAKE_DATA = "quake_data";
    private static final String QUAKE_DATE = "quake_date";


    public GeoQuakeDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY," + QUAKE_STRENGTH_TYPE + " TEXT, " + QUAKE_PERIOD_TYPE + " TEXT, " + QUAKE_DATA + " TEXT, " +QUAKE_DATE+ " TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void setData(String strength_type, String period_type, String data) {
        Log.i("database activity", "setData()");
        Log.i("setData date(approx)", getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_STRENGTH_TYPE, strength_type);
        cv.put(QUAKE_PERIOD_TYPE, period_type);
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, getTime());
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public String getData(String query1, String query2) {
        Log.i("database activity", "getData()");
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        //TODO: use query method here... rawQuery is a bit fugly. Just look at it.
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + QUAKE_STRENGTH_TYPE
                + "='" + query1 + "' AND " + QUAKE_PERIOD_TYPE + "='" + query2 + "'", null);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString(2);
            } while (cursor.moveToNext());

        }
        cursor.close();
        return result;
    }

    public String getDateColumn(String query1, String query2) {
        Log.i("database activity", "getDateColumn()");
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + QUAKE_STRENGTH_TYPE
                + "='" + query1 + "' AND " + QUAKE_PERIOD_TYPE + "='" + query2 + "'", null);
        if (cursor.moveToFirst()) {
            do {
                result = cursor.getString(3);
            } while (cursor.moveToNext());

        }
        cursor.close();
        return result;
    }

    public void updateData(String query1, String query2, String name, String data) {
        Log.i("database activity", "updateData()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, getTime());
        String where = QUAKE_STRENGTH_TYPE + "=?";
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

    /**
     * Time comparisons specifically for the refresh limiter
     */
    public static boolean checkRefreshLimit(long currentTime, long previousTime){
        return (currentTime - previousTime) >= Utils.REFRESH_LIMITER_TIME;
    }


}
