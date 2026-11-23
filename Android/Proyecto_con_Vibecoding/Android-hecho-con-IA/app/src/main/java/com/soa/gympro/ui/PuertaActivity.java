package com.soa.gympro.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.soa.gympro.R;
import com.soa.gympro.config.Config;
import com.soa.gympro.data.SessionManager;
import com.soa.gympro.mqtt.AperturaPayload;
import com.soa.gympro.mqtt.EstadoMqtt;
import com.soa.gympro.mqtt.MqttManager;

/** Permite abrir la puerta del gimnasio publicando en el topic de apertura. */
public class PuertaActivity extends AppCompatActivity {

    /** Tiempo mínimo entre pedidos, para evitar envíos repetidos por doble tap. */
    private static final long ENFRIAMIENTO_MS = 3000;

    private final Handler main = new Handler(Looper.getMainLooper());
    private SessionManager sesion;
    private Button btnAbrir;
    private TextView tvEstado;
    private boolean ocupado = false;
    private boolean conectado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puerta);
        setTitle(R.string.menu_puerta);
        sesion = new SessionManager(this);
        btnAbrir = findViewById(R.id.btnAbrir);
        tvEstado = findViewById(R.id.tvEstadoPuerta);

        MqttManager.getInstance().getEstado().observe(this, estado -> {
            conectado = estado == EstadoMqtt.CONECTADO;
            actualizarBoton();
        });
        btnAbrir.setOnClickListener(v -> abrir());
    }

    private void abrir() {
        if (ocupado) return;
        ocupado = true; // se bloquea ya mismo, antes de que llegue la respuesta
        actualizarBoton();

        String payload = AperturaPayload.crear(sesion.getDni(), sesion.getNombre(),
                System.currentTimeMillis());
        MqttManager.getInstance().publicar(Config.TOPIC_ABRIR_PUERTA, payload,
                new MqttManager.PublicacionListener() {
                    @Override
                    public void onExito() {
                        tvEstado.setText(R.string.puerta_enviado);
                        liberarLuego();
                    }

                    @Override
                    public void onError(Throwable causa) {
                        tvEstado.setText(R.string.puerta_error);
                        liberarLuego();
                    }
                });
    }

    private void liberarLuego() {
        main.postDelayed(() -> {
            ocupado = false;
            actualizarBoton();
        }, ENFRIAMIENTO_MS);
    }

    /** Habilitado solo con cuota al día, MQTT conectado y sin un envío en curso. */
    private void actualizarBoton() {
        boolean cuota = sesion.isCuotaAlDia();
        btnAbrir.setEnabled(cuota && conectado && !ocupado);
        if (ocupado) return; // no pisamos el mensaje de resultado
        if (!cuota) tvEstado.setText(R.string.puerta_sin_cuota);
        else if (!conectado) tvEstado.setText(R.string.puerta_sin_mqtt);
        else tvEstado.setText(R.string.puerta_lista);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        main.removeCallbacksAndMessages(null);
    }
}
