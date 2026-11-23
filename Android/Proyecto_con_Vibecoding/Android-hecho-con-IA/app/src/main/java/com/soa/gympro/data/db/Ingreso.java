package com.soa.gympro.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Registro de cada ingreso (login) a la app. */
@Entity(tableName = "ingresos")
public class Ingreso {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String dni;
    public String nombre;
    /** Fecha y hora en milisegundos desde epoch. */
    public long fechaHora;

    public Ingreso() { }
}
