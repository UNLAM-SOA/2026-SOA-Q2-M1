package com.soa.gympro.mqtt;

import com.google.gson.JsonObject;

/**
 * Payload que la app publica en el topic de apertura de puerta:
 * {@code {"accion":"abrir","dni":"...","nombre":"...","timestamp":<ms epoch>}}.
 */
public final class AperturaPayload {

    private AperturaPayload() { }

    public static String crear(String dni, String nombre, long timestampMs) {
        JsonObject o = new JsonObject();
        o.addProperty("accion", "abrir");
        o.addProperty("dni", dni);
        o.addProperty("nombre", nombre);
        o.addProperty("timestamp", timestampMs);
        return o.toString();
    }
}
