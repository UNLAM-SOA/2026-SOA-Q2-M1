package com.soa.gympro.config;

import com.soa.gympro.BuildConfig;

/**
 * Único lugar de la app desde donde se leen los valores configurables.
 * Los valores reales se definen en app/build.gradle.kts (buildConfigField).
 */
public final class Config {

    private Config() { }

    /** URL base de la API HTTP (Node-RED). Debe terminar en "/". */
    public static final String API_BASE_URL = BuildConfig.API_BASE_URL;

    /** URI del broker MQTT por WebSocket seguro. */
    public static final String MQTT_BROKER_URI = BuildConfig.MQTT_BROKER_URI;

    /** Topic al que la app se suscribe para conocer la ocupación. */
    public static final String TOPIC_OCUPACION = BuildConfig.TOPIC_OCUPACION;

    /** Topic en el que la app publica para abrir la puerta. */
    public static final String TOPIC_ABRIR_PUERTA = BuildConfig.TOPIC_ABRIR_PUERTA;

    /** Aforo máximo usado para el indicador visual de ocupación. */
    public static final int AFORO_MAXIMO = BuildConfig.AFORO_MAXIMO;

    /** Timeout de las llamadas HTTP, en segundos. */
    public static final int HTTP_TIMEOUT_SEGUNDOS = 15;
}
