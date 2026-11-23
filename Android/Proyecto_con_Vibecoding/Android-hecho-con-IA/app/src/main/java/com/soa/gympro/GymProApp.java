package com.soa.gympro;

import android.app.Application;

import com.soa.gympro.data.SessionManager;
import com.soa.gympro.mqtt.MqttManager;

/** Application: inicializa componentes globales (MQTT compartido). */
public class GymProApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MqttManager.init(this);
        // Si ya hay sesión guardada, nos conectamos al broker desde el arranque
        if (new SessionManager(this).haySesion()) {
            MqttManager.getInstance().conectar();
        }
    }
}
