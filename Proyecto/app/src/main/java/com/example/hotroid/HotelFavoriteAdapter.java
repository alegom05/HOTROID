package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Hotel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.Calendar;
import java.util.Date;

public class HotelFavoriteAdapter extends RecyclerView.Adapter<HotelFavoriteAdapter.FavoriteHotelViewHolder> {

    private List<Hotel> favoriteHotelList;
    private Context context;
    private FirebaseFirestore db;

    public HotelFavoriteAdapter(List<Hotel> favoriteHotelList, Context context) {
        this.favoriteHotelList = favoriteHotelList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public FavoriteHotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hotel_favorite_card, parent, false);
        return new FavoriteHotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteHotelViewHolder holder, int position) {
        Hotel hotel = favoriteHotelList.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return favoriteHotelList.size();
    }

    public void updateList(List<Hotel> newFavoriteHotels) {
        this.favoriteHotelList = newFavoriteHotels;
        notifyDataSetChanged();
    }

    public class FavoriteHotelViewHolder extends RecyclerView.ViewHolder {
        private CardView favoriteCardView;
        private ImageView hotelImage;
        private TextView hotelName;
        private TextView hotelRating;
        private TextView hotelAddress;
        private TextView hotelDescription;
        private TextView hotelPrice;

        public FavoriteHotelViewHolder(@NonNull View itemView) {
            super(itemView);
            favoriteCardView = itemView.findViewById(R.id.favorite_hotel_card);
            hotelImage = itemView.findViewById(R.id.favorite_hotel_image);
            hotelName = itemView.findViewById(R.id.favorite_hotel_name);
            hotelRating = itemView.findViewById(R.id.favorite_hotel_rating);
            hotelAddress = itemView.findViewById(R.id.favorite_hotel_address);
            hotelDescription = itemView.findViewById(R.id.favorite_hotel_description);
            hotelPrice = itemView.findViewById(R.id.favorite_hotel_price);

            // Click listener directo sin interface
            favoriteCardView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Hotel hotel = favoriteHotelList.get(position);
                    abrirDetallesHotel(hotel);
                }
            });
        }

        private void abrirDetallesHotel(Hotel hotel) {
            try {
                Intent intent = new Intent(context, HotelDetalladoUser.class);
                intent.putExtra("hotelId", hotel.getIdHotel());
                intent.putExtra("hotelName", hotel.getName());
                intent.putExtra("hotelRating", hotel.getRating());
                intent.putExtra("hotelDireccion", hotel.getDireccion());
                intent.putExtra("precioMinimo", 0.0);

                // Agregar fechas por defecto
                Calendar cal = Calendar.getInstance();
                Date fechaInicio = cal.getTime();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                Date fechaFin = cal.getTime();

                intent.putExtra("fechaInicio", fechaInicio.getTime());
                intent.putExtra("fechaFin", fechaFin.getTime());
                intent.putExtra("numHabitaciones", 1);
                intent.putExtra("numPersonas", 1);
                intent.putExtra("niniosSolicitados", 0);

                // Flag para indicar que viene de favoritos
                intent.putExtra("fromFavorites", true);

                context.startActivity(intent);
                Log.d("FAVORITE_CLICK", "Navegando a detalles del hotel: " + hotel.getName());

            } catch (Exception e) {
                Log.e("FAVORITE_CLICK", "Error al navegar a detalles", e);
            }
        }

        public void bind(Hotel hotel) {
            hotelName.setText(hotel.getName());
            hotelRating.setText(String.format("★ %.1f", hotel.getRating()));
            hotelAddress.setText(hotel.getDireccion());

            String description = hotel.getDescription();
            if (description != null && description.length() > 120) {
                description = description.substring(0, 120) + "...";
            }
            hotelDescription.setText(description);

            // Cargar precio
            obtenerPrecioMasEconomico(hotel);

            // Cargar imagen
            if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
                Glide.with(context)
                        .load(hotel.getImageUrls().get(0))
                        .into(hotelImage);
            } else {
                cargarImagenDesdeFirestore(hotel);
            }

            Log.d("FAVORITE_HOTEL", "Hotel cargado: " + hotel.getName());
        }

        private void cargarImagenDesdeFirestore(Hotel hotel) {
            db.collection("hoteles")
                    .document(hotel.getIdHotel())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                            if (imageUrls != null && !imageUrls.isEmpty()) {
                                Glide.with(context)
                                        .load(imageUrls.get(0))
                                        .into(hotelImage);
                            }
                        }
                    });
        }

        private void obtenerPrecioMasEconomico(Hotel hotel) {
            hotelPrice.setText("Consultando precio...");
            db.collection("habitaciones")
                    .whereEqualTo("hotelId", hotel.getIdHotel())
                    .whereEqualTo("status", "Available")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        double precioMinimo = Double.MAX_VALUE;
                        boolean encontradoPrecio = false;

                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Double precio = doc.getDouble("price");
                            if (precio != null && precio < precioMinimo) {
                                precioMinimo = precio;
                                encontradoPrecio = true;
                            }
                        }

                        if (encontradoPrecio) {
                            hotelPrice.setText(String.format("S/ %.0f/noche", precioMinimo));
                        } else {
                            hotelPrice.setText("Precio no disponible");
                        }
                    })
                    .addOnFailureListener(e -> {
                        hotelPrice.setText("Error precio");
                        Log.e("PRICE_ERROR", "Error al cargar precio", e);
                    });
        }
    }
}