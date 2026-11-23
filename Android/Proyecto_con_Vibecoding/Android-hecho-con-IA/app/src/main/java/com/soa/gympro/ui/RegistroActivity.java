package com.soa.gympro.ui;

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
import com.soa.gympro.api.RegistroRequest;
import com.soa.gympro.api.RegistroResponse;
import com.soa.gympro.util.Validaciones;
import com.soa.gympro.util.Validaciones.Resultado;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla de registro de un nuevo socio. */
public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilDni, tilNombre, tilApellido, tilClave, tilConfirmar;
    private TextInputEditText etDni, etNombre, etApellido, etClave, etConfirmar;
    private TextView tvMensaje;
    private Button btnRegistrar;
    private ProgressBar progreso;
    private Call<RegistroResponse> llamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        tilDni = findViewById(R.id.tilDni);
        tilNombre = findViewById(R.id.tilNombre);
        tilApellido = findViewById(R.id.tilApellido);
        tilClave = findViewById(R.id.tilClave);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etDni = findViewById(R.id.etDni);
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etClave = findViewById(R.id.etClave);
        etConfirmar = findViewById(R.id.etConfirmar);
        tvMensaje = findViewById(R.id.tvMensaje);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progreso = findViewById(R.id.progreso);
        btnRegistrar.setOnClickListener(v -> intentarRegistro());
    }

    private void intentarRegistro() {
        String dni = texto(etDni), nombre = texto(etNombre), apellido = texto(etApellido);
        String clave = etClave.getText() == null ? "" : etClave.getText().toString();
        String confirmar = etConfirmar.getText() == null ? "" : etConfirmar.getText().toString();
        tvMensaje.setVisibility(View.GONE);

        boolean ok = true;
        ok &= marcar(tilDni, Validaciones.validarDni(dni));
        ok &= marcar(tilNombre, Validaciones.validarTexto(nombre));
        ok &= marcar(tilApellido, Validaciones.validarTexto(apellido));
        ok &= marcar(tilClave, Validaciones.validarClave(clave));
        ok &= marcar(tilConfirmar, Validaciones.validarConfirmacion(clave, confirmar));
        if (!ok) return;

        cargando(true);
        llamada = ApiClient.getService().registrar(new RegistroRequest(dni, clave, nombre, apellido));
        llamada.enqueue(new Callback<RegistroResponse>() {
            @Override
            public void onResponse(Call<RegistroResponse> call, Response<RegistroResponse> r) {
                cargando(false);
                if (r.isSuccessful() && r.body() != null && r.body().ok) {
                    mostrar(getString(R.string.registro_ok), false);
                } else if (r.code() == 409) {
                    mostrar(mensajeOr(r, R.string.registro_dni_existente), true);
                } else if (r.code() == 500) {
                    RegistroResponse e = r.isSuccessful() ? r.body() : ApiErrorParser.registroDeError(r);
                    String detalle = e != null && e.estado != null ? e.estado : null;
                    mostrar(detalle != null
                            ? getString(R.string.registro_error_servidor) + ": " + detalle
                            : getString(R.string.registro_error_servidor), true);
                } else {
                    mostrar(mensajeOr(r, R.string.error_desconocido), true);
                }
            }

            @Override
            public void onFailure(Call<RegistroResponse> call, Throwable t) {
                if (call.isCanceled()) return;
                cargando(false);
                mostrar(ApiErrorParser.mensajeDeFalla(RegistroActivity.this, t), true);
            }
        });
    }

    /** Muestra el error de un campo según el resultado de la validación; devuelve si es válido. */
    private boolean marcar(TextInputLayout til, Resultado r) {
        switch (r) {
            case OK: til.setError(null); return true;
            case VACIO: til.setError(getString(R.string.error_campo_vacio)); break;
            case DNI_NO_NUMERICO: til.setError(getString(R.string.error_dni_numerico)); break;
            case DNI_LARGO: til.setError(getString(R.string.error_dni_largo)); break;
            case CLAVE_CORTA: til.setError(getString(R.string.error_clave_corta)); break;
            case CLAVES_DISTINTAS: til.setError(getString(R.string.error_claves_distintas)); break;
        }
        return false;
    }

    private String mensajeOr(Response<?> r, int porDefecto) {
        String m = ApiErrorParser.mensajeDeError(r);
        return m != null ? m : getString(porDefecto);
    }

    private void mostrar(String mensaje, boolean error) {
        tvMensaje.setText(mensaje);
        tvMensaje.setTextColor(error
                ? com.google.android.material.color.MaterialColors.getColor(tvMensaje,
                        androidx.appcompat.R.attr.colorError)
                : getColor(R.color.ocupacion_baja));
        tvMensaje.setVisibility(View.VISIBLE);
    }

    private void cargando(boolean si) {
        progreso.setVisibility(si ? View.VISIBLE : View.GONE);
        btnRegistrar.setEnabled(!si);
    }

    private static String texto(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llamada != null) llamada.cancel();
    }
}
