package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Hotel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperHotelAdapter extends RecyclerView.Adapter<SuperHotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private List<Hotel> hotelListFull;
    private Context context;

    public SuperHotelAdapter(Context context, List<Hotel> hotelList) {
        this.context = context;
        this.hotelList = hotelList;
        this.hotelListFull = new ArrayList<>(hotelList);
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);

        holder.tvHotelName.setText(hotel.getName());
        holder.tvHotelLocation.setText(hotel.getDireccion());
        // Formatear el rating con un decimal
        holder.tvHotelRating.setText(String.format(Locale.getDefault(), "%.1f", hotel.getRating()));
        // Formatear el precio con "S/." y dos decimales
        holder.tvHotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", hotel.getPrice()));

        // Configurar RatingBar
        holder.ratingBar.setRating(hotel.getRating());

        // Cargar imagen usando Glide (volviendo a la implementación que tenías y que funciona)
        if (hotel.getImageResourceId() != 0) {
            Glide.with(context)
                    .load(hotel.getImageResourceId())
                    .placeholder(R.drawable.placeholder_hotel) // Placeholder por si la imagen no carga
                    .error(R.drawable.ic_user_error) // Imagen de error
                    .into(holder.ivHotelImage);
        } else {
            // Si imageResourceId es 0 (no se encontró el drawable), muestra solo el placeholder
            holder.ivHotelImage.setImageResource(R.drawable.placeholder_hotel);
        }

        // --- Manejar el clic en el elemento de la lista (CardView) ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperDetallesActivity.class);

            // Pasamos TODOS los datos necesarios para SuperDetallesActivity
            intent.putExtra("hotel_id", hotel.getIdHotel()); // ID del documento de Firestore
            intent.putExtra("hotel_name", hotel.getName());
            intent.putExtra("hotel_location", hotel.getDireccion()); // Dirección principal (ciudad/país)
            intent.putExtra("hotel_detailed_address", hotel.getDireccionDetallada()); // Dirección calle/número
            intent.putExtra("hotel_rating", hotel.getRating());
            intent.putExtra("hotel_price", hotel.getPrice()); // Ya es un double
            intent.putExtra("hotel_description", hotel.getDescription());
            intent.putExtra("hotel_image_name", hotel.getImageName()); // Nombre del drawable (ej. "hotel_boca_raton")
            intent.putExtra("hotel_image_resource_id", hotel.getImageResourceId()); // El ID del recurso ya resuelto

            // Si tienes URLs de imágenes para una galería, las pasarías aquí
            // intent.putStringArrayListExtra("hotel_image_urls", (ArrayList<String>) hotel.getImageUrls());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotelList.size();
    }

    public static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView ivHotelImage;
        TextView tvHotelName, tvHotelLocation, tvHotelRating, tvHotelPrice;
        RatingBar ratingBar; // Asegúrate de que este ID existe en item_hotel.xml si lo usas

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            // ¡¡¡AQUÍ ESTÁ LA CORRECCIÓN CLAVE!!!
            // Usar los IDs que terminan en '2' según tu item_hotel.xml original que funcionaba.
            ivHotelImage = itemView.findViewById(R.id.ivHotelImage2);
            tvHotelName = itemView.findViewById(R.id.tvHotelName2);
            tvHotelLocation = itemView.findViewById(R.id.tvHotelLocation2);
            tvHotelRating = itemView.findViewById(R.id.tvHotelRating2);
            tvHotelPrice = itemView.findViewById(R.id.tvHotelPrice2);
            ratingBar = itemView.findViewById(R.id.ratingBar2); // Asegúrate de que este ID existe en tu XML
        }
    }

    public void setHotels(List<Hotel> newHotels) { // Cambié el parámetro a newHotels para mayor claridad
        this.hotelList.clear();
        this.hotelList.addAll(newHotels);
        this.hotelListFull = new ArrayList<>(newHotels); // Actualiza también la lista completa para el filtro
        notifyDataSetChanged();
    }

    public void filter(String text) {
        hotelList.clear();
        if (text.isEmpty()) {
            hotelList.addAll(hotelListFull);
        } else {
            text = text.toLowerCase(Locale.getDefault()); // Usa Locale.getDefault() para consistencia
            for (Hotel hotel : hotelListFull) {
                if (hotel.getName().toLowerCase(Locale.getDefault()).contains(text) ||
                        hotel.getDireccion().toLowerCase(Locale.getDefault()).contains(text)) {
                    hotelList.add(hotel);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void clearFilter() {
        hotelList.clear();
        hotelList.addAll(hotelListFull);
        notifyDataSetChanged();
    }
}