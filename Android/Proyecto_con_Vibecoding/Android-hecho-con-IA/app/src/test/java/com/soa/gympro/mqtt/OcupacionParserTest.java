package com.soa.gympro.mqtt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class OcupacionParserTest {

    @Test
    public void valorNormal() {
        assertEquals(Integer.valueOf(12), OcupacionParser.parsear("{\"cantidadPersonasActual\": 12}"));
    }

    @Test
    public void valorNuloSeTomaComoCero() {
        assertEquals(Integer.valueOf(0), OcupacionParser.parsear("{\"cantidadPersonasActual\": null}"));
    }

    @Test
    public void campoAusenteSeTomaComoCero() {
        assertEquals(Integer.valueOf(0), OcupacionParser.parsear("{}"));
    }

    @Test
    public void negativoSeLlevaACero() {
        assertEquals(Integer.valueOf(0), OcupacionParser.parsear("{\"cantidadPersonasActual\": -3}"));
    }

    @Test
    public void jsonInvalidoDevuelveNull() {
        assertNull(OcupacionParser.parsear("esto no es json"));
        assertNull(OcupacionParser.parsear("{\"cantidadPersonasActual\": "));
        assertNull(OcupacionParser.parsear("[1,2]"));
        assertNull(OcupacionParser.parsear(""));
        assertNull(OcupacionParser.parsear(null));
    }

    @Test
    public void valorNoNumericoDevuelveNull() {
        assertNull(OcupacionParser.parsear("{\"cantidadPersonasActual\": \"muchas\"}"));
    }
}
