package com.geo.GeoQuake.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

import com.geo.GeoQuake.entities.GeoQuake

/**
 * Created by George Stinson on 2017-08-21.
 */
@Dao
interface GeoQuakeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setData(quake: GeoQuake)

}
