package com.soa.gympro.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Base de datos local (Room). */
@Database(entities = {Usuario.class, Ingreso.class, Pago.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /** Un solo hilo para las operaciones de escritura/lectura de la base. */
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static volatile AppDatabase instancia;

    public abstract UsuarioDao usuarioDao();

    public abstract IngresoDao ingresoDao();

    public abstract PagoDao pagoDao();

    public static AppDatabase getInstance(Context ctx) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(ctx.getApplicationContext(),
                            AppDatabase.class, "gympro.db").build();
                }
            }
        }
        return instancia;
    }
}
