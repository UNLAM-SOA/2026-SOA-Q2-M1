package com.soa.gympro.util;

import static com.soa.gympro.util.Validaciones.Resultado.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ValidacionesTest {

    @Test
    public void dni() {
        assertEquals(OK, Validaciones.validarDni("12345678"));
        assertEquals(OK, Validaciones.validarDni(" 12345678 "));
        assertEquals(VACIO, Validaciones.validarDni(""));
        assertEquals(VACIO, Validaciones.validarDni(null));
        assertEquals(DNI_NO_NUMERICO, Validaciones.validarDni("12a45678"));
        assertEquals(DNI_LARGO, Validaciones.validarDni("123"));
        assertEquals(DNI_LARGO, Validaciones.validarDni("1234567890"));
    }

    @Test
    public void texto() {
        assertEquals(OK, Validaciones.validarTexto("Ana"));
        assertEquals(VACIO, Validaciones.validarTexto("   "));
        assertEquals(VACIO, Validaciones.validarTexto(null));
    }

    @Test
    public void clave() {
        assertEquals(OK, Validaciones.validarClave("secreto"));
        assertEquals(VACIO, Validaciones.validarClave(""));
        assertEquals(CLAVE_CORTA, Validaciones.validarClave("123"));
    }

    @Test
    public void confirmacion() {
        assertEquals(OK, Validaciones.validarConfirmacion("secreto", "secreto"));
        assertEquals(CLAVES_DISTINTAS, Validaciones.validarConfirmacion("secreto", "otra"));
        assertEquals(VACIO, Validaciones.validarConfirmacion("secreto", ""));
    }
}
