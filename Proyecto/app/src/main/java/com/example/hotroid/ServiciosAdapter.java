package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull; // Added for clarity
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Servicios;

import java.util.ArrayList;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServiciosViewHolder>{

    private ArrayList<Servicios> serviciosList;
    private OnItemClickListener listener; // Changed to the inner interface type directly

    // Constructor to initialize the list of services
    public ServiciosAdapter(ArrayList<Servicios> serviciosList) {
        this.serviciosList = serviciosList;
    }

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull // Added for clarity
    @Override
    public ServiciosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each service item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servicio, parent, false); // Make sure this points to your updated XML
        return new ServiciosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiciosViewHolder holder, int position) {
        // Get the current service
        Servicios currentServicio = serviciosList.get(position);

        // Set the data for the current service
        holder.tvNombreServicio.setText(currentServicio.getNombre());
        // Set the schedule. Provide a default text if the schedule is null or empty.
        holder.tvHorarioServicio.setText("Horario: " +
                (currentServicio.getHorario() != null && !currentServicio.getHorario().isEmpty() ?
                        currentServicio.getHorario() : "No especificado"));

        // Handle click on each service item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position); // Pass the clicked position
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the service list
        return serviciosList.size();
    }

    // ViewHolder class to hold the views for each service item
    public class ServiciosViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombreServicio; // Changed ID to match item_servicio.xml
        public TextView tvHorarioServicio; // New TextView for schedule
        public ImageView arrowIcon;

        public ServiciosViewHolder(@NonNull View itemView) { // Added for clarity
            super(itemView);
            // Initialize the views, ensuring they match item_servicio.xml
            tvNombreServicio = itemView.findViewById(R.id.tvNombreServicio); // Corrected ID
            tvHorarioServicio = itemView.findViewById(R.id.tvHorarioServicio); // New
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}