package com.soa.gympro.data.db;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Carga pagos de ejemplo la primera vez que el socio abre el historial,
 * porque todavía no hay una pasarela de pagos real.
 */
public final class PagoSeeder {

    private PagoSeeder() { }

    /** Debe llamarse fuera del hilo principal. */
    public static void sembrarSiHaceFalta(AppDatabase db, String dni) {
        if (db.pagoDao().cantidadDe(dni) > 0) return;
        List<Pago> ejemplo = new ArrayList<>();
        String[] conceptos = {"Cuota mensual", "Cuota mensual", "Cuota mensual",
                "Inscripción anual", "Cuota mensual"};
        double[] montos = {25000, 25000, 25000, 15000, 25000};
        for (int i = 0; i < conceptos.length; i++) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MONTH, -i);
            Pago p = new Pago();
            p.dni = dni;
            p.concepto = conceptos[i];
            p.monto = montos[i];
            p.fecha = c.getTimeInMillis();
            ejemplo.add(p);
        }
        db.pagoDao().insertar(ejemplo);
    }
}
