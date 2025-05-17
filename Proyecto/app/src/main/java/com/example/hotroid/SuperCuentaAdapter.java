package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuperCuentaAdapter extends RecyclerView.Adapter<SuperCuentaAdapter.ViewHolder> {

    private final List<String[]> datosCuenta;

    public SuperCuentaAdapter(List<String[]> datosCuenta) {
        this.datosCuenta = datosCuenta;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.super_cuenta_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] par = datosCuenta.get(position);
        holder.tvClave.setText(par[0]);
        holder.tvValor.setText(par[1]);
    }

    @Override
    public int getItemCount() {
        return datosCuenta.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvClave;
        TextView tvValor;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClave = itemView.findViewById(R.id.tvClave);
            tvValor = itemView.findViewById(R.id.tvValor);
        }
    }
}