package com.soa.gympro.data.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Pago de cuota de un socio (por ahora son datos de ejemplo). */
@Entity(tableName = "pagos")
public class Pago {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String dni;
    public double monto;
    /** Fecha en milisegundos desde epoch. */
    public long fecha;
    public String concepto;

    public Pago() { }
}
