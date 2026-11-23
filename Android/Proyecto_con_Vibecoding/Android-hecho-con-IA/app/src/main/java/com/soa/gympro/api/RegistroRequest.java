package com.soa.gympro.api;

/** Body de POST /registrar. */
public class RegistroRequest {
    public final String dni;
    public final String clave;
    public final String nombre;
    public final String apellido;

    public RegistroRequest(String dni, String clave, String nombre, String apellido) {
        this.dni = dni;
        this.clave = clave;
        this.nombre = nombre;
        this.apellido = apellido;
    }
}
