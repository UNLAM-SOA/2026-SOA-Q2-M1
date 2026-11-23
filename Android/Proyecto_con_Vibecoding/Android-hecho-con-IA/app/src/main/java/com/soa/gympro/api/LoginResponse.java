package com.soa.gympro.api;

/** Respuesta 200 de GET /login. */
public class LoginResponse {
    public boolean ok;
    public String nombre;
    /** true si el socio tiene la cuota al día. */
    public boolean estado;
}
