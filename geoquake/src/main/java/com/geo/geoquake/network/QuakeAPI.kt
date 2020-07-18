package com.geo.geoquake.network

import android.content.Context
import com.geo.geoquake.BuildConfig
import com.geo.geoquake.R
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class QuakeAPI(context: Context) {
    private val quakeService: QuakeService
    val okHttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            builder.readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            builder.writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS)
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HTTP_LOG_LEVEL
                builder.addInterceptor(logging)
            }
            return builder.build()
        }

    /**
     *
     * @param config String to concatenate to the base url, contains intensity/timeline for quake list
     * @return
     */
    fun getUSGSQuakes(config: String?): Call<ResponseBody>? {
        return quakeService.getUSGSQuakes(config!!)
    }

    companion object {
        private val HTTP_LOG_LEVEL = HttpLoggingInterceptor.Level.HEADERS
        private const val CONNECTION_TIMEOUT: Long = 20
        private const val READ_WRITE_TIMEOUT: Long = 30
    }

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(context.getString(R.string.usgs_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        quakeService = retrofit.create(QuakeService::class.java)
    }
}