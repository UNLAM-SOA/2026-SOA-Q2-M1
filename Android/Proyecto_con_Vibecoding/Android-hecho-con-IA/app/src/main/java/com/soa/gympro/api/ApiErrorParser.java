package com.soa.gympro.api;

import android.content.Context;

import com.google.gson.Gson;
import com.soa.gympro.R;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.Response;

/** Convierte errores de red y cuerpos de error JSON en mensajes para el usuario. */
public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() { }

    /** Mensaje para una falla de red (sin conexión, timeout, etc.). */
    public static String mensajeDeFalla(Context ctx, Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return ctx.getString(R.string.error_timeout);
        }
        if (t instanceof IOException) {
            return ctx.getString(R.string.error_sin_conexion);
        }
        return ctx.getString(R.string.error_desconocido);
    }

    /** Extrae el campo "error" del cuerpo de una respuesta no exitosa; null si no hay. */
    public static String mensajeDeError(Response<?> respuesta) {
        return extraerError(respuesta.errorBody());
    }

    static String extraerError(ResponseBody cuerpo) {
        if (cuerpo == null) return null;
        try {
            ErrorResponse e = GSON.fromJson(cuerpo.string(), ErrorResponse.class);
            return e != null ? e.error : null;
        } catch (Exception ex) {
            return null;
        }
    }

    /** Intenta leer el cuerpo de error como RegistroResponse (caso HTTP 500). */
    public static RegistroResponse registroDeError(Response<?> respuesta) {
        ResponseBody cuerpo = respuesta.errorBody();
        if (cuerpo == null) return null;
        try {
            return GSON.fromJson(cuerpo.string(), RegistroResponse.class);
        } catch (Exception ex) {
            return null;
        }
    }
}
