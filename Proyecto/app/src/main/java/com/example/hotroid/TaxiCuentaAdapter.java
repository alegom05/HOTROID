package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaxiCuentaAdapter extends RecyclerView.Adapter<TaxiCuentaAdapter.CuentaViewHolder> {

    private final List<String> datos;

    public TaxiCuentaAdapter(List<String> datos) {
        this.datos = datos;
    }

    @NonNull
    @Override
    public CuentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuenta, parent, false);
        return new CuentaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CuentaViewHolder holder, int position) {
        String dato = datos.get(position);
        holder.tvDato.setText(dato);
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public static class CuentaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDato;

        public CuentaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDato = itemView.findViewById(R.id.tvDato);
        }
    }
}
