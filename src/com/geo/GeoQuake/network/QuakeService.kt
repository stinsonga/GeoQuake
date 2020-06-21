package com.geo.GeoQuake.network

import com.geo.GeoQuake.models.Earthquake
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface QuakeService {

    @GET("{config}")
    fun getUSGSQuakes(@Path("config") time: String): Call<List<Earthquake?>>?
}