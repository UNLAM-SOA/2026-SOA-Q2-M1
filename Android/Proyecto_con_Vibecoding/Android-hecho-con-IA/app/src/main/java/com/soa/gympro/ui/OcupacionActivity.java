package com.soa.gympro.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.soa.gympro.R;
import com.soa.gympro.config.Config;
import com.soa.gympro.mqtt.MqttManager;
import com.soa.gympro.mqtt.OcupacionParser;

/** Muestra la ocupación del gimnasio en tiempo real (topic de ocupación). */
public class OcupacionActivity extends AppCompatActivity {

    private static final String TAG = "OcupacionActivity";

    private TextView tvCantidad, tvDetalle, tvAforo;
    private LinearProgressIndicator barra;

    private final MqttManager.MensajeListener listener = (topic, payload) -> {
        Integer cantidad = OcupacionParser.parsear(payload);
        if (cantidad == null) {
            Log.e(TAG, "Payload de ocupación inválido: " + payload);
            return;
        }
        mostrar(cantidad);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocupacion);
        setTitle(R.string.menu_ocupacion);
        tvCantidad = findViewById(R.id.tvCantidad);
        tvDetalle = findViewById(R.id.tvDetalle);
        tvAforo = findViewById(R.id.tvAforo);
        barra = findViewById(R.id.barraOcupacion);
        barra.setMax(Config.AFORO_MAXIMO);
        tvAforo.setText(getString(R.string.ocupacion_aforo, Config.AFORO_MAXIMO));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Si ya llegó un valor, el manager lo entrega de inmediato
        MqttManager.getInstance().agregarListener(Config.TOPIC_OCUPACION, listener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MqttManager.getInstance().quitarListener(Config.TOPIC_OCUPACION, listener);
    }

    private void mostrar(int cantidad) {
        tvCantidad.setText(String.valueOf(cantidad));
        tvDetalle.setText(R.string.ocupacion_personas);
        barra.setProgress(Math.min(cantidad, Config.AFORO_MAXIMO));

        // Verde < 60 %, amarillo < 85 %, rojo a partir de ahí
        double porcentaje = (double) cantidad / Config.AFORO_MAXIMO;
        int color = porcentaje < 0.6 ? R.color.ocupacion_baja
                : porcentaje < 0.85 ? R.color.ocupacion_media : R.color.ocupacion_alta;
        barra.setIndicatorColor(getColor(color));
        tvCantidad.setTextColor(getColor(color));
    }
}
