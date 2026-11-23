package com.soa.gympro.mqtt;

import androidx.annotation.StringRes;

import com.soa.gympro.R;

/** Estado de la conexión MQTT compartida. */
public enum EstadoMqtt {
    CONECTANDO(R.string.mqtt_conectando),
    CONECTADO(R.string.mqtt_conectado),
    RECONECTANDO(R.string.mqtt_reconectando),
    DESCONECTADO(R.string.mqtt_desconectado);

    @StringRes
    public final int textoRes;

    EstadoMqtt(int textoRes) {
        this.textoRes = textoRes;
    }
}
