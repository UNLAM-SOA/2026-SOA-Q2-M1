package com.soa.gympro.api;

import com.soa.gympro.config.Config;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/** Singleton que construye el cliente Retrofit con timeouts razonables. */
public final class ApiClient {

    private static ApiService servicio;

    private ApiClient() { }

    public static synchronized ApiService getService() {
        if (servicio == null) {
            OkHttpClient http = new OkHttpClient.Builder()
                    .connectTimeout(Config.HTTP_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .readTimeout(Config.HTTP_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .writeTimeout(Config.HTTP_TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
                    .build();
            servicio = new Retrofit.Builder()
                    .baseUrl(Config.API_BASE_URL)
                    .client(http)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ApiService.class);
        }
        return servicio;
    }
}
