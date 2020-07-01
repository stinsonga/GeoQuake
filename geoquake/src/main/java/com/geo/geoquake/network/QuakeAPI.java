package com.geo.geoquake.network;

import android.content.Context;

import com.geo.geoquake.BuildConfig;
import com.geo.geoquake.R;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QuakeAPI {

    private static final HttpLoggingInterceptor.Level HTTP_LOG_LEVEL = HttpLoggingInterceptor.Level.HEADERS;
    private static final long CONNECTION_TIMEOUT = 20;
    private static final long READ_WRITE_TIMEOUT = 30;
    private QuakeService quakeService;

    public QuakeAPI(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.usgs_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build();

        quakeService = retrofit.create(QuakeService.class);
    }

    public OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.SECONDS);
        if(BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.level(HTTP_LOG_LEVEL);
            builder.addInterceptor(logging);
        }
        return builder.build();
    }

    /**
     *
     * @param config String to concatenate to the base url, contains intensity/timeline for quake list
     * @return
     */
    public Call<ResponseBody> getUSGSQuakes(String config) { return quakeService.getUSGSQuakes(config); }

}
