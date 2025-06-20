package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans; // Use TaxiAlertasBeans

import java.util.List;

public class TaxiFinAdapter extends RecyclerView.Adapter<TaxiFinAdapter.ViewHolder> {

    private final List<TaxiAlertasBeans> listaViajes;

    public TaxiFinAdapter(List<TaxiAlertasBeans> listaViajes) {
        this.listaViajes = listaViajes;
    }

    @NonNull
    @Override
    public TaxiFinAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taxi_fin_item, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiFinAdapter.ViewHolder holder, int position) {
        TaxiAlertasBeans alerta = listaViajes.get(position); // Renamed for clarity

        // Concatenate first and last names for display
        holder.tvNombreCompleto.setText(alerta.getNombresCliente() + " " + alerta.getApellidosCliente());
        holder.tvOrigen.setText("Origen: " + alerta.getOrigen());
        holder.tvDestino.setText("Destino: " + alerta.getDestino());
        holder.tvTiempoTranscurrido.setText("Tiempo: " + alerta.getTiempoTranscurrido());
        holder.tvEstadoViaje.setText("Estado: " + alerta.getEstadoViaje());
    }

    @Override
    public int getItemCount() {
        return listaViajes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombreCompleto, tvOrigen, tvDestino, tvTiempoTranscurrido, tvEstadoViaje;

        public ViewHolder(@NonNull View itemView) {
            super(itemView); // Corrected this line!
            tvNombreCompleto = itemView.findViewById(R.id.tvNombreCompleto);
            tvOrigen = itemView.findViewById(R.id.tvOrigen);
            tvDestino = itemView.findViewById(R.id.tvDestino);
            tvTiempoTranscurrido = itemView.findViewById(R.id.tvTiempoTranscurrido);
            tvEstadoViaje = itemView.findViewById(R.id.tvEstadoViaje);
        }
    }
}