package com.soa.gympro.ui;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.soa.gympro.R;
import com.soa.gympro.data.SessionManager;
import com.soa.gympro.data.db.AppDatabase;
import com.soa.gympro.data.db.Pago;
import com.soa.gympro.data.db.PagoSeeder;

import java.util.List;

/** Historial de pagos del socio, leído de la base local. */
public class PagosActivity extends AppCompatActivity {

    private final PagoAdapter adapter = new PagoAdapter();
    private View tvVacio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagos);
        setTitle(R.string.pagos_titulo);

        RecyclerView lista = findViewById(R.id.listaPagos);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.setAdapter(adapter);
        tvVacio = findViewById(R.id.tvVacio);

        String dni = new SessionManager(this).getDni();
        AppDatabase db = AppDatabase.getInstance(this);
        // Room no se puede usar en el hilo principal
        AppDatabase.EXECUTOR.execute(() -> {
            PagoSeeder.sembrarSiHaceFalta(db, dni);
            List<Pago> pagos = db.pagoDao().pagosDe(dni);
            runOnUiThread(() -> {
                if (isDestroyed()) return;
                adapter.setPagos(pagos);
                tvVacio.setVisibility(pagos.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
}
