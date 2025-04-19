package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ServiciosAdapter extends RecyclerView.Adapter<ServiciosAdapter.ServiciosViewHolder>{
    private ArrayList<Servicios> serviciosList;
    private ServiciosAdapter.OnItemClickListener listener;

    // Constructor to initialize the list of rooms
    public ServiciosAdapter(ArrayList<Servicios> serviciosList) {
        this.serviciosList = serviciosList;
    }

    @Override
    public ServiciosAdapter.ServiciosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each room item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_servicio, parent, false);
        return new ServiciosAdapter.ServiciosViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ServiciosAdapter.ServiciosViewHolder holder, int position) {
        // Get the current room
        Servicios currentServicio = serviciosList.get(position);

        // Set the data for the current room
        holder.tvNombre.setText(currentServicio.getNombre());
        // Handle click on each room item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position); // Pass the clicked position
            }
        });
    }

    @Override
    public int getItemCount() {
        // Return the size of the room list
        return serviciosList.size();
    }
    public void setOnItemClickListener(ServiciosAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // ViewHolder class to hold the views for each room item
    public class ServiciosViewHolder extends RecyclerView.ViewHolder {
        public TextView tvNombre;
        public ImageView arrowIcon;

        public ServiciosViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            tvNombre = itemView.findViewById(R.id.tvNombre);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}
