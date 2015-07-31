package com.geo.GeoQuake;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoQuakeDB extends SQLiteOpenHelper{
    private static final int DB_VERSION = 17;
    private static final long DAY_MILLISECONDS = 86400000;
    private static final String DB_NAME = "GeoQuake";
    private static final String TABLE_NAME = "quakes";
    //Columns
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

    public void setData(String quakeStrength, String quakeDuration, String data) {
        //Log.i("database activity", "setData()");
        //Log.i("setData date(approx)", ""+getTime());
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_STRENGTH_TYPE, quakeStrength);
        cv.put(QUAKE_PERIOD_TYPE, quakeDuration);
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, ""+getTime());
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }

    public String getData(String quakeStrength, String quakeDuration) {
        //Log.i("database activity", "getData()");
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        //TODO: use query method here... rawQuery is a bit fugly. Just look at it.
        Cursor rowCursor = db.rawQuery("SELECT "+QUAKE_DATA+" FROM " + TABLE_NAME + " WHERE " + QUAKE_STRENGTH_TYPE
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

    public String getDateColumn(String quakeStrength, String quakeDuration) {
        //Log.i("database activity", "getDateColumn()");
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";
        //TODO: use query method here
        Cursor rowCursor = db.rawQuery("SELECT "+QUAKE_DATE+" FROM " + TABLE_NAME + " WHERE " + QUAKE_STRENGTH_TYPE
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

    public void updateData(String quakeStrength, String quakeDuration, String data) {
        //Log.i("database activity", "updateData()");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_DATA, data);
        cv.put(QUAKE_DATE, ""+getTime());
        String where = QUAKE_STRENGTH_TYPE + "=? AND " + QUAKE_PERIOD_TYPE + " =?";
        String[] value = {quakeStrength, quakeDuration};
        db.update(TABLE_NAME, cv, where, value);
        db.close();
    }

    /**
     * A simple method to get the current time in millis
     *
     * @return The current milliseconds converted to a String
     */
    public static long getTime(){
        Date d = new Date();
        return d.getTime();
    }

    /**
     * Time comparisons specifically for the refresh limiter
     */
    public static boolean checkRefreshLimit(long currentTime, long previousTime){
        return (currentTime - previousTime) >= Utils.REFRESH_LIMITER_TIME;
    }


}
