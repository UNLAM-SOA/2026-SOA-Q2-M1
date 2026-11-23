package com.soa.gympro.data.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Socio que inició sesión en este dispositivo. */
@Entity(tableName = "usuarios")
public class Usuario {
    @PrimaryKey
    @NonNull
    public String dni = "";
    public String nombre;

    public Usuario() { }
}
