package com.geo.GeoQuake.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by George Stinson on 2017-08-21.
 */
@Entity(tableName = "quakes")
public class GeoQuake {

    //Table name
    public static final String TABLE_NAME = "quakes";
    //Columns
    public static final String QUAKE_SOURCE = "quake_source";
    public static final String QUAKE_STRENGTH_TYPE = "quake_strength_type";
    public static final String QUAKE_PERIOD_TYPE = "quake_period_type";
    public static final String QUAKE_DATA = "quake_data";
    public static final String QUAKE_DATE = "quake_date";

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = QUAKE_SOURCE)
    private String quakeSource;
    @ColumnInfo(name = QUAKE_STRENGTH_TYPE)
    private String quakeStrengthType;
    @ColumnInfo(name = QUAKE_PERIOD_TYPE)
    private String quakePeriodType;
    @ColumnInfo(name = QUAKE_DATA)
    private String quakeData;
    @ColumnInfo(name = QUAKE_DATE)
    private String quakeDate;

    public GeoQuake(String quakeSource, String quakeStrengthType, String quakePeriodType, String quakeData, String quakeDate) {
        this.quakeSource = quakeSource;
        this.quakeStrengthType = quakeStrengthType;
        this.quakePeriodType = quakePeriodType;
        this.quakeData = quakeData;
        this.quakeDate = quakeDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuakeSource() {
        return quakeSource;
    }

    public void setQuakeSource(String quakeSource) {
        this.quakeSource = quakeSource;
    }

    public String getQuakeStrengthType() {
        return quakeStrengthType;
    }

    public void setQuakeStrengthType(String quakeStrengthType) {
        this.quakeStrengthType = quakeStrengthType;
    }

    public String getQuakePeriodType() {
        return quakePeriodType;
    }

    public void setQuakePeriodType(String quakePeriodType) {
        this.quakePeriodType = quakePeriodType;
    }

    public String getQuakeData() {
        return quakeData;
    }

    public void setQuakeData(String quakeData) {
        this.quakeData = quakeData;
    }

    public String getQuakeDate() {
        return quakeDate;
    }

    public void setQuakeDate(String quakeDate) {
        this.quakeDate = quakeDate;
    }
}
