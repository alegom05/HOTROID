package com.example.hotroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HabitacionesAdapter extends RecyclerView.Adapter<HabitacionesAdapter.HabitacionesViewHolder> {

    private ArrayList<Room> roomList;
    private OnItemClickListener listener;

    // Constructor to initialize the list of rooms
    public HabitacionesAdapter(ArrayList<Room> roomList) {
        this.roomList = roomList;
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
        holder.tvRoomNumber.setText(currentRoom.getRoomNumber());
        holder.tvRoomType.setText(currentRoom.getRoomType());
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
    public class HabitacionesViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRoomNumber, tvRoomType;
        public ImageView arrowIcon;

        public HabitacionesViewHolder(View itemView) {
            super(itemView);
            // Initialize the views
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }
    }
}

