package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;

import java.util.List;

public class TaxiAlertasAdapter extends RecyclerView.Adapter<TaxiAlertasAdapter.NotificacionViewHolder> {
    private List<TaxiAlertasBeans> lista;

    public TaxiAlertasAdapter(List<TaxiAlertasBeans> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_alertas_item, parent, false);
        return new NotificacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        TaxiAlertasBeans noti = lista.get(position);
        holder.tvNombre.setText(noti.getNombre());
        holder.tvLugar.setText(noti.getLugar());
        holder.tvTiempo.setText(noti.getTiempo());
        holder.tvLugar2.setText("Destino: " + noti.getDestino());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvLugar, tvLugar2, tvTiempo;

        public NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreNoti);
            tvLugar = itemView.findViewById(R.id.tvLugarNoti);
            tvLugar2 = itemView.findViewById(R.id.tvLugarNoti2);
            tvTiempo = itemView.findViewById(R.id.tvTiempoNoti);
        }
    }
}
