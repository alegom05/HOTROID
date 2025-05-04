package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {
    private List<Notificacion> lista;

    public NotificacionAdapter(List<Notificacion> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notificacion, parent, false);
        return new NotificacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificacion noti = lista.get(position);
        holder.tvNombre.setText(noti.getNombre());
        holder.tvLugar.setText(noti.getLugar());
        holder.tvTiempo.setText(noti.getTiempo());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvLugar, tvTiempo;

        public NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreNoti);
            tvLugar = itemView.findViewById(R.id.tvLugarNoti);
            tvTiempo = itemView.findViewById(R.id.tvTiempoNoti);
        }
    }
}
