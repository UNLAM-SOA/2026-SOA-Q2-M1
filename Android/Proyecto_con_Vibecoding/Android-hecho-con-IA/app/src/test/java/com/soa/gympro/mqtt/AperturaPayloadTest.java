package com.soa.gympro.mqtt;

import static org.junit.Assert.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

public class AperturaPayloadTest {

    @Test
    public void contieneDniNombreYTimestamp() {
        String json = AperturaPayload.crear("12345678", "Ana", 1732200000000L);
        JsonObject o = JsonParser.parseString(json).getAsJsonObject();
        assertEquals("abrir", o.get("accion").getAsString());
        assertEquals("12345678", o.get("dni").getAsString());
        assertEquals("Ana", o.get("nombre").getAsString());
        assertEquals(1732200000000L, o.get("timestamp").getAsLong());
    }
}
