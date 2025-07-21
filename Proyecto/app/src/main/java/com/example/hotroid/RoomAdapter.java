package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Room;
import com.example.hotroid.bean.RoomGroupOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

//    private List<RoomGroupOption> roomOptions;
//    private RoomGroupOption selectedOption;
//    private OnRoomSelectListener listener;
    private List<Room> roomList;
    private Context context;

    public RoomAdapter(List<Room> roomList, Context context) {
        this.roomList = roomList;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habitacion, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Room room = roomList.get(position);

        // Establecer el título de la habitación
        holder.roomTypeText.setText(room.getRoomType());

        // Establecer descripción de la cama según el tipo de habitación
        String bedDescription = getBedDescription(room.getRoomType());
        holder.bedTypeText.setText(bedDescription);

        // Establecer el área
        holder.areaText.setText(String.format(Locale.getDefault(), "%.2f m²", room.getArea()));

        // Establecer capacidad
        holder.capacityText.setText(room.getCapacityDescription());

        // Establecer precio
        holder.priceText.setText(String.format(Locale.getDefault(), "S/%.2f", room.getPrice()));

        // Establecer imagen
        setRoomImage(holder.roomImage, room.getRoomType());

        // Configurar botón "Ver más" con manejo de errores
        holder.viewMoreButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, DetalleHabitacionUser.class);
                intent.putExtra("ROOM_ID", room.getId());
                intent.putExtra("ROOM_TYPE", room.getRoomType());
                intent.putExtra("ROOM_NUMBER", room.getRoomNumber());
                intent.putExtra("ROOM_AREA", room.getArea());
                intent.putExtra("ROOM_CAPACITY_ADULTS", room.getCapacityAdults());
                intent.putExtra("ROOM_CAPACITY_CHILDREN", room.getCapacityChildren());
                intent.putExtra("ROOM_PRICE", room.getPrice());
                intent.putExtra("HOTEL_ID", room.getHotelId());
                intent.putStringArrayListExtra("ROOM_IMAGE_URLS", new ArrayList<>(room.getImageResourceName()));


                // Añadir log para debuguear
                Log.d("RoomAdapter", "Iniciando DetalleHabitacionUser con roomId: " + room.getId());

                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("RoomAdapter", "Error al iniciar DetalleHabitacionUser: " + e.getMessage(), e);
                Toast.makeText(context, "Error al abrir detalles: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    // Actualizar la lista de habitaciones
    public void updateRooms(List<Room> newRooms) {
        roomList = newRooms;
        notifyDataSetChanged();
    }

    // Método para determinar la descripción de la cama basada en el tipo de habitación
    private String getBedDescription(String roomType) {
        switch (roomType.toLowerCase()) {
            case "individual":
            case "estándar":
                return "Cama individual";
            case "doble":
            case "doble superior":
                return "Dos camas individuales";
            case "matrimonial":
                return "Cama matrimonial";
            case "suite":
            case "suite lujo":
            case "suite ejecutiva":
                return "Cama king size";
            default:
                return "Cama estándar";
        }
    }

    // Método para asignar imagen según el tipo de habitación
    private void setRoomImage(ImageView imageView, String roomType) {
        switch (roomType.toLowerCase()) {
            case "individual":
            case "estándar":
                imageView.setImageResource(R.drawable.hotel_room);
                break;
            case "doble":
            case "doble superior":
                imageView.setImageResource(R.drawable.hotel_room_doble);
                break;
            case "suite":
            case "suite lujo":
            case "suite ejecutiva":
                imageView.setImageResource(R.drawable.hotel_room_deluxe);
                break;
            default:
                imageView.setImageResource(R.drawable.hotel_room);
                break;
        }
    }

    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        ImageView roomImage;
        TextView roomTypeText;
        TextView bedTypeText;
        TextView areaText;
        TextView capacityText;
        TextView priceText;
        Button viewMoreButton;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomImage = itemView.findViewById(R.id.roomImage);
            roomTypeText = itemView.findViewById(R.id.roomTypeText);
            bedTypeText = itemView.findViewById(R.id.roomCountText);
            areaText = itemView.findViewById(R.id.roomTypeText);
            capacityText = itemView.findViewById(R.id.capacityText);
            priceText = itemView.findViewById(R.id.priceText);
            viewMoreButton = itemView.findViewById(R.id.selectButton);
        }
    }
}