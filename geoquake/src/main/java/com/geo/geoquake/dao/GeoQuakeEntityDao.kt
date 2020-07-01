package com.geo.geoquake.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.geo.geoquake.entities.GeoQuake

/**
 * Created by George Stinson on 2017-08-21.
 */
@Dao
interface GeoQuakeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setData(quake: GeoQuake)

}
