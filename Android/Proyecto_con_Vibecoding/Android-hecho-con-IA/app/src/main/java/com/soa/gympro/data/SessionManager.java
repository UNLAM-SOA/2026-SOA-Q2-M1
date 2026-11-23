package com.soa.gympro.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda la sesión del socio en SharedPreferences. */
public class SessionManager {

    private static final String PREFS = "sesion";
    private static final String K_DNI = "dni";
    private static final String K_NOMBRE = "nombre";
    private static final String K_CUOTA = "cuotaAlDia";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void guardar(String dni, String nombre, boolean cuotaAlDia) {
        prefs.edit().putString(K_DNI, dni).putString(K_NOMBRE, nombre)
                .putBoolean(K_CUOTA, cuotaAlDia).apply();
    }

    public boolean haySesion() {
        return prefs.getString(K_DNI, null) != null;
    }

    public String getDni() { return prefs.getString(K_DNI, null); }

    public String getNombre() { return prefs.getString(K_NOMBRE, ""); }

    public boolean isCuotaAlDia() { return prefs.getBoolean(K_CUOTA, false); }

    public void cerrar() { prefs.edit().clear().apply(); }
}
