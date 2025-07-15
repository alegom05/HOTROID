package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Servicios; // Make sure this import is correct

import java.util.ArrayList;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServiciosViewHolder>{

    private ArrayList<Servicios> serviciosList;
    private OnItemClickListener listener;

    public ServiciosAdapter(ArrayList<Servicios> serviciosList) {
        this.serviciosList = serviciosList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiciosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servicio, parent, false);
        return new ServiciosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiciosViewHolder holder, int position) {
        Servicios currentServicio = serviciosList.get(position);

        holder.tvNombreServicio.setText(currentServicio.getNombre());

        // Now directly use getHoraInicio() and getHoraFin()
        String horaInicio = currentServicio.getHoraInicio();
        String horaFin = currentServicio.getHoraFin();

        holder.tvHoraInicio.setText("Hora Inicio: " + (horaInicio != null && !horaInicio.isEmpty() ? horaInicio : "--:-- --"));
        holder.tvHoraFin.setText("Hora Fin: " + (horaFin != null && !horaFin.isEmpty() ? horaFin : "--:-- --"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return serviciosList.size();
    }

    public class ServiciosViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombreServicio;
        public TextView tvHoraInicio;
        public TextView tvHoraFin;
        public ImageView arrowIcon;

        public ServiciosViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreServicio = itemView.findViewById(R.id.tvNombreServicio);
            tvHoraInicio = itemView.findViewById(R.id.tvHoraInicio);
            tvHoraFin = itemView.findViewById(R.id.tvHoraFin);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}