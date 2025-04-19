package com.example.hotroid;
import com.example.hotroid.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.example.hotroid.bean.Notificacion;
import java.util.List;

public class NotificacionesAdapterUser extends RecyclerView.Adapter<NotificacionesAdapterUser.ViewHolder> {

    private List<Notificacion> notificaciones;

    public NotificacionesAdapterUser(List<Notificacion> notificaciones){
        this.notificaciones = notificaciones;
    }

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacion_user, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacion noti = notificaciones.get(position);
        holder.tvMensajeResumen.setText(noti.getMensajeResumen());
        holder.imgAdjunto.setImageResource(noti.getRecursoImagen());
    }
    @Override
    public int getItemCount() {
        return notificaciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMensajeResumen;
        ImageView imgAdjunto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMensajeResumen = itemView.findViewById(R.id.tvMensajeResumen);
            imgAdjunto = itemView.findViewById(R.id.imgAdjunto);
        }
    }
}