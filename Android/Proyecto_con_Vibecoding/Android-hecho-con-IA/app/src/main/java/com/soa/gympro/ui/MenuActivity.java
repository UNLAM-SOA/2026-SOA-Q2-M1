package com.soa.gympro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.soa.gympro.R;
import com.soa.gympro.data.SessionManager;
import com.soa.gympro.mqtt.MqttManager;

/** Menú principal: saludo, estado de cuota, estado MQTT y accesos. */
public class MenuActivity extends AppCompatActivity {

    private SessionManager sesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sesion = new SessionManager(this);
        setContentView(R.layout.activity_menu);

        ((TextView) findViewById(R.id.tvSaludo))
                .setText(getString(R.string.menu_saludo, sesion.getNombre()));

        TextView tvCuota = findViewById(R.id.tvCuota);
        boolean alDia = sesion.isCuotaAlDia();
        tvCuota.setText(alDia ? R.string.cuota_al_dia : R.string.cuota_vencida);
        tvCuota.setTextColor(getColor(alDia ? R.color.ocupacion_baja : R.color.ocupacion_alta));

        // Aseguramos la conexión (por ejemplo, tras un login recién hecho)
        MqttManager mqtt = MqttManager.getInstance();
        mqtt.conectar();

        // El observer del LiveData se desregistra solo con el ciclo de vida de la activity
        TextView tvMqtt = findViewById(R.id.tvEstadoMqtt);
        mqtt.getEstado().observe(this, estado ->
                tvMqtt.setText(getString(R.string.mqtt_estado_formato, getString(estado.textoRes))));

        findViewById(R.id.btnOcupacion).setOnClickListener(
                v -> startActivity(new Intent(this, OcupacionActivity.class)));
        findViewById(R.id.btnPuerta).setOnClickListener(
                v -> startActivity(new Intent(this, PuertaActivity.class)));
        findViewById(R.id.btnPagos).setOnClickListener(
                v -> startActivity(new Intent(this, PagosActivity.class)));
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        MqttManager.getInstance().desconectar();
        sesion.cerrar();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
