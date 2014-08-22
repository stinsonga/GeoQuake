package com.geo.GeoQuake;

import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GeoQuakeDB extends SQLiteOpenHelper{
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "GeoQuake";
    private static final String TABLE_NAME = "quakes";
    private static final String QUAKE_TYPE = "quake_type";
    private static final String QUAKE_DATA = "quake_data";

    public GeoQuakeDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (id INTEGER PRIMARY KEY," + QUAKE_TYPE + " TEXT " + QUAKE_DATA + " TEXT)");
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(QUAKE_TYPE, quake_type);
        cv.put(QUAKE_DATA, quake_data);
        db.insert(TABLE_NAME, null, cv);
        db.close();
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
}
