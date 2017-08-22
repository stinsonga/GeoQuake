package com.geo.GeoQuake.entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by George Stinson on 2017-08-21.
 */
@Entity(tableName = "quakes")
public class GeoQuake {

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo(name = "quake_source")
    private String quakeSource;
    @ColumnInfo(name = "quake_strength_type")
    private String quakeStrengthType;
    @ColumnInfo(name = "quake_period_type")
    private String quakePeriodType;
    @ColumnInfo(name = "quake_data")
    private String quakeData;
    @ColumnInfo(name = "quake_date")
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
