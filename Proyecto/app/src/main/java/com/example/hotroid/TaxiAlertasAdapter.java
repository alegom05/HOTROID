package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
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
    private Context context;

    public TaxiAlertasAdapter(Context context, List<TaxiAlertasBeans> lista) {
        this.context = context;
        this.lista = lista;
    }

    public TaxiAlertasAdapter(List<TaxiAlertasBeans> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.taxi_alertas_item, parent, false);
        context = parent.getContext(); // Aseguramos tener contexto incluso si se usó el constructor sin contexto
        return new NotificacionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        TaxiAlertasBeans noti = lista.get(position);
        holder.tvNombre.setText(noti.getNombre());
        holder.tvLugar.setText(noti.getLugar());
        holder.tvTiempo.setText(noti.getTiempo());
        holder.tvLugar2.setText("Destino: " + noti.getDestino());

        // Configurar el clic en toda la vista del item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaxiViaje.class);
            // Pasamos los datos necesarios para TaxiViaje
            intent.putExtra("NOMBRE_USUARIO", noti.getNombre());
            intent.putExtra("ORIGEN", noti.getLugar());
            intent.putExtra("DESTINO", noti.getDestino());
            context.startActivity(intent);
        });
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