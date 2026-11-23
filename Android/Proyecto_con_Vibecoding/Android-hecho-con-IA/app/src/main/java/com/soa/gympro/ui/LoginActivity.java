package com.soa.gympro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.soa.gympro.R;
import com.soa.gympro.api.ApiClient;
import com.soa.gympro.api.ApiErrorParser;
import com.soa.gympro.api.LoginResponse;
import com.soa.gympro.data.SessionManager;
import com.soa.gympro.data.db.AppDatabase;
import com.soa.gympro.data.db.Ingreso;
import com.soa.gympro.data.db.Usuario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla de inicio de sesión (DNI y contraseña). */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilDni, tilClave;
    private TextInputEditText etDni, etClave;
    private TextView tvError;
    private Button btnIngresar;
    private ProgressBar progreso;
    private SessionManager sesion;
    private Call<LoginResponse> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sesion = new SessionManager(this);

        // Si ya hay sesión guardada, vamos directo al menú
        if (sesion.haySesion()) {
            irAlMenu();
            return;
        }

        setContentView(R.layout.activity_login);
        tilDni = findViewById(R.id.tilDni);
        tilClave = findViewById(R.id.tilClave);
        etDni = findViewById(R.id.etDni);
        etClave = findViewById(R.id.etClave);
        tvError = findViewById(R.id.tvErrorLogin);
        btnIngresar = findViewById(R.id.btnIngresar);
        progreso = findViewById(R.id.progreso);

        btnIngresar.setOnClickListener(v -> intentarLogin());
        findViewById(R.id.tvIrRegistro).setOnClickListener(
                v -> startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void intentarLogin() {
        String dni = textoDe(etDni);
        String clave = textoDe(etClave);
        tilDni.setError(null);
        tilClave.setError(null);
        tvError.setVisibility(View.GONE);

        boolean valido = true;
        if (dni.isEmpty()) {
            tilDni.setError(getString(R.string.error_dni_vacio));
            valido = false;
        }
        if (clave.isEmpty()) {
            tilClave.setError(getString(R.string.error_clave_vacia));
            valido = false;
        }
        if (!valido) return;

        cargando(true);
        llamada = ApiClient.getService().login(dni, clave);
        llamada.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> r) {
                cargando(false);
                if (r.isSuccessful() && r.body() != null && r.body().ok) {
                    loginExitoso(dni, r.body());
                } else {
                    String msg = ApiErrorParser.mensajeDeError(r);
                    mostrarError(msg != null ? msg : getString(R.string.login_error_generico));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                if (call.isCanceled()) return;
                cargando(false);
                mostrarError(ApiErrorParser.mensajeDeFalla(LoginActivity.this, t));
            }
        });
    }

    private void loginExitoso(String dni, LoginResponse resp) {
        String nombre = resp.nombre != null ? resp.nombre : dni;
        sesion.guardar(dni, nombre, resp.estado);

        // Registramos el ingreso en la base local (fuera del hilo principal)
        AppDatabase db = AppDatabase.getInstance(this);
        AppDatabase.EXECUTOR.execute(() -> {
            Usuario u = new Usuario();
            u.dni = dni;
            u.nombre = nombre;
            db.usuarioDao().guardar(u);
            Ingreso in = new Ingreso();
            in.dni = dni;
            in.nombre = nombre;
            in.fechaHora = System.currentTimeMillis();
            db.ingresoDao().insertar(in);
        });
        irAlMenu();
    }

    private void irAlMenu() {
        startActivity(new Intent(this, MenuActivity.class));
        finish();
    }

    private void mostrarError(String mensaje) {
        tvError.setText(mensaje);
        tvError.setVisibility(View.VISIBLE);
    }

    private void cargando(boolean si) {
        progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        btnIngresar.setEnabled(!si);
    }

    private static String textoDe(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llamada != null) llamada.cancel();
    }
}
