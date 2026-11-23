package com.soa.gympro.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.soa.gympro.config.Config;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

/**
 * Conexión MQTT única y compartida por toda la app.
 *
 * - Reconexión automática y resuscripción a los topics al reconectar.
 * - clientId persistente (SharedPreferences).
 * - Listeners por topic; todos los callbacks se entregan en el hilo principal.
 * - Guarda el último mensaje de cada topic para entregarlo a quien se registre tarde.
 *
 * Todo el estado mutable se toca solo desde el hilo principal.
 */
public final class MqttManager {

    private static final String TAG = "MqttManager";
    private static final String PREFS = "mqtt";
    private static final String K_CLIENT_ID = "clientId";
    private static final int QOS = 1;
    private static final long REINTENTO_MS = 5000;

    /** Escucha los mensajes de un topic (se invoca en el hilo principal). */
    public interface MensajeListener {
        void onMensaje(String topic, String payload);
    }

    /** Resultado de una publicación (se invoca en el hilo principal). */
    public interface PublicacionListener {
        void onExito();
        void onError(Throwable causa);
    }

    private static MqttManager instancia;

    private final Context contexto;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final MutableLiveData<EstadoMqtt> estado = new MutableLiveData<>(EstadoMqtt.DESCONECTADO);
    private final Map<String, Set<MensajeListener>> listeners = new HashMap<>();
    private final Map<String, String> ultimoMensaje = new HashMap<>();
    private final Set<String> topicsSuscriptos = new HashSet<>();

    private MqttAndroidClient cliente;
    private boolean conectando = false;
    private boolean deseaConexion = false;

    private MqttManager(Context ctx) {
        this.contexto = ctx.getApplicationContext();
    }

    public static synchronized void init(Context ctx) {
        if (instancia == null) instancia = new MqttManager(ctx);
    }

    public static synchronized MqttManager getInstance() {
        if (instancia == null) throw new IllegalStateException("MqttManager.init() no fue llamado");
        return instancia;
    }

    public LiveData<EstadoMqtt> getEstado() {
        return estado;
    }

    public boolean estaConectado() {
        return estado.getValue() == EstadoMqtt.CONECTADO;
    }

    // ---------------------------------------------------------------- conexión

    /** Inicia la conexión si todavía no está conectada ni conectándose. */
    public void conectar() {
        if (!enMain(this::conectar)) return;
        deseaConexion = true;
        if (conectando || (cliente != null && cliente.isConnected())) return;

        if (cliente == null) {
            cliente = new MqttAndroidClient(contexto, Config.MQTT_BROKER_URI,
                    obtenerClientId(), Ack.AUTO_ACK, null, false, 1000);
            cliente.setCallback(callback);
        }
        conectando = true;
        setEstado(EstadoMqtt.CONECTANDO);

        MqttConnectOptions opciones = new MqttConnectOptions();
        opciones.setAutomaticReconnect(true);
        opciones.setCleanSession(true);
        opciones.setKeepAliveInterval(30);
        opciones.setConnectionTimeout(10);
        try {
            cliente.connect(opciones, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    main.post(() -> conectando = false);
                    // connectComplete() se encarga de suscribir y actualizar el estado
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Falló la conexión inicial", exception);
                    main.post(() -> {
                        conectando = false;
                        if (!deseaConexion) return;
                        setEstado(EstadoMqtt.RECONECTANDO);
                        // La reconexión automática de Paho solo aplica tras una conexión exitosa
                        main.postDelayed(MqttManager.this::conectar, REINTENTO_MS);
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al conectar", e);
            conectando = false;
            setEstado(EstadoMqtt.DESCONECTADO);
        }
    }

    /** Cierra la conexión (al cerrar sesión). */
    public void desconectar() {
        if (!enMain(this::desconectar)) return;
        deseaConexion = false;
        main.removeCallbacksAndMessages(null);
        ultimoMensaje.clear();
        topicsSuscriptos.clear();
        if (cliente != null) {
            try {
                cliente.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Error al desconectar", e);
            }
            try {
                cliente.close();
            } catch (Exception e) {
                Log.w(TAG, "Error al cerrar el cliente", e);
            }
            cliente = null;
        }
        conectando = false;
        setEstado(EstadoMqtt.DESCONECTADO);
    }

    private final MqttCallbackExtended callback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Log.i(TAG, "Conectado (reconexión=" + reconnect + ")");
            main.post(() -> {
                conectando = false;
                setEstado(EstadoMqtt.CONECTADO);
                // Tras (re)conectar hay que volver a suscribirse
                topicsSuscriptos.clear();
                suscribirPendientes();
            });
        }

        @Override
        public void connectionLost(Throwable cause) {
            Log.w(TAG, "Conexión perdida", cause);
            main.post(() -> {
                topicsSuscriptos.clear();
                // Con automaticReconnect, Paho vuelve a intentar solo
                setEstado(deseaConexion ? EstadoMqtt.RECONECTANDO : EstadoMqtt.DESCONECTADO);
            });
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            final String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            main.post(() -> entregar(topic, payload));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) { }
    };

    // ------------------------------------------------------------ suscripción

    /**
     * Registra un listener para un topic. Si ya llegó un mensaje, se entrega al instante.
     * Llamar desde onStart().
     */
    public void agregarListener(@NonNull String topic, @NonNull MensajeListener listener) {
        if (!enMain(() -> agregarListener(topic, listener))) return;
        Set<MensajeListener> set = listeners.get(topic);
        if (set == null) {
            set = new CopyOnWriteArraySet<>();
            listeners.put(topic, set);
        }
        set.add(listener);

        String ultimo = ultimoMensaje.get(topic);
        if (ultimo != null) listener.onMensaje(topic, ultimo);
        suscribirPendientes();
    }

    /** Quita un listener. Llamar desde onStop(). */
    public void quitarListener(@NonNull String topic, @NonNull MensajeListener listener) {
        if (!enMain(() -> quitarListener(topic, listener))) return;
        Set<MensajeListener> set = listeners.get(topic);
        if (set != null) set.remove(listener);
    }

    /** Suscribe a la ocupación (siempre) y a cualquier topic con listeners registrados. */
    private void suscribirPendientes() {
        if (cliente == null || !cliente.isConnected()) return;
        Set<String> objetivo = new HashSet<>(listeners.keySet());
        objetivo.add(Config.TOPIC_OCUPACION);
        for (String topic : objetivo) {
            if (topicsSuscriptos.contains(topic)) continue;
            try {
                cliente.subscribe(topic, QOS, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken t) {
                        Log.i(TAG, "Suscripto a " + topic);
                    }

                    @Override
                    public void onFailure(IMqttToken t, Throwable e) {
                        Log.e(TAG, "Falló la suscripción a " + topic, e);
                        main.post(() -> topicsSuscriptos.remove(topic));
                    }
                });
                topicsSuscriptos.add(topic);
            } catch (Exception e) {
                Log.e(TAG, "Error al suscribirse a " + topic, e);
            }
        }
    }

    private void entregar(String topic, String payload) {
        ultimoMensaje.put(topic, payload);
        Set<MensajeListener> set = listeners.get(topic);
        if (set == null) return;
        for (MensajeListener l : set) l.onMensaje(topic, payload);
    }

    // ------------------------------------------------------------- publicación

    /** Publica un mensaje; el resultado se informa en el hilo principal. */
    public void publicar(String topic, String payload, PublicacionListener resultado) {
        if (!enMain(() -> publicar(topic, payload, resultado))) return;
        if (cliente == null || !cliente.isConnected()) {
            resultado.onError(new IllegalStateException("Sin conexión MQTT"));
            return;
        }
        try {
            cliente.publish(topic, payload.getBytes(StandardCharsets.UTF_8), QOS, false, null,
                    new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken t) {
                            main.post(resultado::onExito);
                        }

                        @Override
                        public void onFailure(IMqttToken t, Throwable e) {
                            main.post(() -> resultado.onError(e));
                        }
                    });
        } catch (Exception e) {
            resultado.onError(e);
        }
    }

    // ------------------------------------------------------------------ utils

    private void setEstado(EstadoMqtt nuevo) {
        estado.setValue(nuevo);
    }

    /** Si no estamos en el hilo principal, reenvía la tarea y devuelve false. */
    private boolean enMain(Runnable tarea) {
        if (Looper.myLooper() == Looper.getMainLooper()) return true;
        main.post(tarea);
        return false;
    }

    private String obtenerClientId() {
        SharedPreferences prefs = contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String id = prefs.getString(K_CLIENT_ID, null);
        if (id == null) {
            id = "gympro-" + UUID.randomUUID().toString().substring(0, 12);
            prefs.edit().putString(K_CLIENT_ID, id).apply();
        }
        return id;
    }
}
