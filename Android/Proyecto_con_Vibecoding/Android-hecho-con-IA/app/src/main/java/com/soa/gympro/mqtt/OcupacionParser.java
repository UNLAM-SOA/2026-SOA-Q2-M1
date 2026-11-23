package com.soa.gympro.mqtt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Interpreta el payload del topic de ocupación:
 * {@code { "cantidadPersonasActual": <int o null> }}.
 * No depende de Android para poder probarse con tests unitarios.
 */
public final class OcupacionParser {

    private static final String CAMPO = "cantidadPersonasActual";

    private OcupacionParser() { }

    /**
     * @return la cantidad de personas (null o campo ausente se toma como 0, los
     *         negativos se llevan a 0), o {@code null} si el JSON es inválido.
     */
    public static Integer parsear(String payload) {
        if (payload == null || payload.trim().isEmpty()) return null;
        try {
            JsonElement raiz = JsonParser.parseString(payload);
            if (!raiz.isJsonObject()) return null;
            JsonObject obj = raiz.getAsJsonObject();
            JsonElement valor = obj.get(CAMPO);
            if (valor == null || valor.isJsonNull()) return 0;
            return Math.max(0, valor.getAsInt());
        } catch (RuntimeException e) {
            // JsonSyntaxException, NumberFormatException, UnsupportedOperationException...
            return null;
        }
    }
}
