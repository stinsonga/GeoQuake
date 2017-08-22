package com.geo.GeoQuake.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.geo.GeoQuake.entities.GeoQuake;

/**
 * Created by George Stinson on 2017-08-21.
 */
@Dao
public interface GeoQuakeEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void setData(GeoQuake quake);



}
