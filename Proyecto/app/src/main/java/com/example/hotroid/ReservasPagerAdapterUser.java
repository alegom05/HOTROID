package com.example.hotroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.ReservaItemAdapter;
import com.example.hotroid.bean.ReservaConHotel;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class ReservasPagerAdapterUser extends RecyclerView.Adapter<ReservasPagerAdapterUser.ReservasPagerViewHolder> {

    private final Context context;
    private final List<List<ReservaConHotel>> listaDeReservas;

    public ReservasPagerAdapterUser(Context context, List<List<ReservaConHotel>> listaDeReservas) {
        this.context = context;
        this.listaDeReservas = listaDeReservas;
    }

    @NonNull
    @Override
    public ReservasPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_content_reservas, parent, false);
        return new ReservasPagerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservasPagerViewHolder holder, int position) {
        List<ReservaConHotel> reservasActuales = listaDeReservas.get(position);

        if (reservasActuales.isEmpty()) {
            // Mostrar mensaje de que no hay reservas
            holder.recyclerView.setVisibility(View.GONE);
            holder.mensajeVacio.setVisibility(View.VISIBLE);

            switch (position) {
                case 0:
                    holder.mensajeVacio.setText("No tienes reservas activas.");
                    break;
                case 1:
                    holder.mensajeVacio.setText("No tienes reservas pasadas.");
                    break;
                case 2:
                    holder.mensajeVacio.setText("No tienes reservas canceladas.");
                    break;
            }
        } else {
            // Mostrar lista de reservas
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.mensajeVacio.setVisibility(View.GONE);

            ReservaItemAdapter adapter = new ReservaItemAdapter(context, reservasActuales);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
    }

    @Override
    public int getItemCount() {
        return listaDeReservas.size();
    }

    static class ReservasPagerViewHolder extends RecyclerView.ViewHolder {
        final RecyclerView recyclerView;
        final MaterialTextView mensajeVacio;

        public ReservasPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rvReservas);
            mensajeVacio = itemView.findViewById(R.id.tvMensajeVacio);
        }
    }
}