package com.geo.GeoQuake.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.geo.GeoQuake.entities.GeoQuake

/**
 * Created by George Stinson on 2017-08-21.
 */
@Dao
interface GeoQuakeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setData(quake: GeoQuake)

}
