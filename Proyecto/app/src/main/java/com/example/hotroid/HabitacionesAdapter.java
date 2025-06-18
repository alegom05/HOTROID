package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Importar ImageView
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Room; // Asegúrate de que esta importación sea correcta

import java.util.ArrayList;
import java.util.List; // Usaremos List en lugar de ArrayList para mayor flexibilidad

public class HabitacionesAdapter extends RecyclerView.Adapter<HabitacionesAdapter.HabitacionesViewHolder> {

    private List<Room> roomList; // Cambiado a List
    private OnItemClickListener listener;

    // Constructor to initialize the list of rooms
    public HabitacionesAdapter(List<Room> roomList) { // Cambiado a List
        this.roomList = roomList;
    }

    // Método para actualizar los datos del adaptador (útil para el buscador)
    public void updateList(List<Room> newList) {
        roomList.clear();
        roomList.addAll(newList);
        notifyDataSetChanged();
    }

    @Override
    public HabitacionesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each room item
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_room, parent, false);
        return new HabitacionesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(HabitacionesViewHolder holder, int position) {
        // Get the current room
        Room currentRoom = roomList.get(position);

        // Set the data for the current room
        holder.tvRoomNumber.setText("Habitación " + currentRoom.getRoomNumber()); // Añadir prefijo "Habitación "
        holder.tvRoomType.setText("Tipo: " + currentRoom.getRoomType()); // Añadir prefijo "Tipo: "

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
        return roomList.size();
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    // ViewHolder class to hold the views for each room item
    public static class HabitacionesViewHolder extends RecyclerView.ViewHolder { // Hacer static para evitar memory leaks
        public TextView tvRoomNumber, tvRoomType;
        public ImageView arrowIcon;

        public HabitacionesViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            arrowIcon = itemView.findViewById(R.id.arrowIcon); // Asegurarse de que el ID es correcto
        }
    }
}