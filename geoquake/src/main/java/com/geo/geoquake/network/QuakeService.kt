package com.geo.geoquake.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface QuakeService {

    @GET("{config}")
    fun getUSGSQuakes(@Path("config") time: String): Call<ResponseBody>?
}