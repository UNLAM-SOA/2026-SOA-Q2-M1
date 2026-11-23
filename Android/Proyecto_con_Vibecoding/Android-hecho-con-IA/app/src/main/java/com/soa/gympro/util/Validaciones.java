package com.soa.gympro.util;

/** Validaciones del formulario de registro (sin dependencias de Android, testeable). */
public final class Validaciones {

    public static final int CLAVE_MIN = 6;
    public static final int DNI_MIN = 7;
    public static final int DNI_MAX = 9;

    /** Resultado de validar un campo. */
    public enum Resultado { OK, VACIO, DNI_NO_NUMERICO, DNI_LARGO, CLAVE_CORTA, CLAVES_DISTINTAS }

    private Validaciones() { }

    private static boolean vacio(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static Resultado validarDni(String dni) {
        if (vacio(dni)) return Resultado.VACIO;
        String d = dni.trim();
        for (int i = 0; i < d.length(); i++) {
            if (d.charAt(i) < '0' || d.charAt(i) > '9') return Resultado.DNI_NO_NUMERICO;
        }
        if (d.length() < DNI_MIN || d.length() > DNI_MAX) return Resultado.DNI_LARGO;
        return Resultado.OK;
    }

    public static Resultado validarTexto(String texto) {
        return vacio(texto) ? Resultado.VACIO : Resultado.OK;
    }

    public static Resultado validarClave(String clave) {
        if (clave == null || clave.isEmpty()) return Resultado.VACIO;
        if (clave.length() < CLAVE_MIN) return Resultado.CLAVE_CORTA;
        return Resultado.OK;
    }

    public static Resultado validarConfirmacion(String clave, String confirmacion) {
        if (confirmacion == null || confirmacion.isEmpty()) return Resultado.VACIO;
        return confirmacion.equals(clave) ? Resultado.OK : Resultado.CLAVES_DISTINTAS;
    }
}
