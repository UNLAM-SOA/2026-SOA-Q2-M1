package com.soa.gympro.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.soa.gympro.R;
import com.soa.gympro.data.db.Pago;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Adapter de la lista de pagos. */
public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.Holder> {

    private final List<Pago> pagos = new ArrayList<>();
    private final NumberFormat moneda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR"));
    private final DateFormat fecha = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public void setPagos(List<Pago> nuevos) {
        pagos.clear();
        pagos.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pago, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Pago p = pagos.get(position);
        h.concepto.setText(p.concepto);
        h.fecha.setText(fecha.format(new Date(p.fecha)));
        h.monto.setText(moneda.format(p.monto));
    }

    @Override
    public int getItemCount() {
        return pagos.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView concepto, fecha, monto;

        Holder(View v) {
            super(v);
            concepto = v.findViewById(R.id.tvConcepto);
            fecha = v.findViewById(R.id.tvFecha);
            monto = v.findViewById(R.id.tvMonto);
        }
    }
}
