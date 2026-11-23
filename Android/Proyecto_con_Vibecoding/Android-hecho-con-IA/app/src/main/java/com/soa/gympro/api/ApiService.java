package com.soa.gympro.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/** Endpoints HTTP expuestos por el backend Node-RED. */
public interface ApiService {

    @GET("login")
    Call<LoginResponse> login(@Query("dni") String dni, @Query("clave") String clave);

    @POST("registrar")
    Call<RegistroResponse> registrar(@Body RegistroRequest body);
}
