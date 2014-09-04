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
    /*
    * @stuff Data sent from the view to be written to DB
    *
    */
    public void setData(String quake_type, String quake_data){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_TYPE, quake_type);
        cv.put(QUAKE_DATA, quake_data);
        cv.put(QUAKE_DATE, dateFormat.format(date));
        db.insert(TABLE_NAME, null, cv);
    }
    public ArrayList<String> getData(){
        ArrayList<String> result=new ArrayList<String>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if(cursor.moveToFirst()){
            do{
                result.add(cursor.getString(1));
            }while(cursor.moveToNext());
        }
        return result;
    }

    /*
      Checking to see if any rows exist of that quake type

     */
    public boolean checkExists(String quakeType){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " +QUAKE_TYPE+ "='" +quakeType+"'", null);
        int count = 0;
        if(null != cursor) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        if(count==0){
            return true;
        }else if(count == 1 && isOutOfDate(quakeType)){
            return true;
        }else{
            return false;
        }
    }

    /*

    Is the row out of date by enough to refresh?

     */
    public boolean isOutOfDate(String quakeType){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT "+QUAKE_DATE+" FROM " + TABLE_NAME + " WHERE " +QUAKE_TYPE+ "='" +quakeType+"'", null);
        //TODO: logic to check dates
        try{
            Date storedDate = sdf.parse(cursor.getString(3));
            if(storedDate.getTime() - date.getTime() >= DAY_MILLISECONDS){
                return true;
            }
        }catch (ParseException pe){
            Log.e("Parse Exception", pe.getMessage());
        }

        return false;
    }
}
