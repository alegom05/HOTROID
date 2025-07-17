package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.hotroid.bean.Hotel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {

    private List<Hotel> hotelList;
    private List<Hotel> hotelListFiltrada;
    private FirebaseFirestore db;
    private Context context;
    private OnHotelClickListener listener;

    public interface OnHotelClickListener {
        void onHotelClick(Hotel hotel, double precio);
    }
    public HotelAdapter(List<Hotel> hotelList) {
        this.hotelList = hotelList;
    }
//    public HotelAdapter(List<Hotel> hotels, Context context) {
//        this.hotels = hotels;
//        this.hotelListFiltrada = new ArrayList<>(hotels); // Inicializar con todos los hoteles
//        this.context = context;
//    }

    public HotelAdapter(List<Hotel> hotelList, Context context) {
        this.hotelList = hotelList;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setOnHotelClickListener(OnHotelClickListener listener) {
        this.listener = listener;
    }
    // Método para actualizar la lista filtrada
    public void actualizarLista(List<Hotel> nuevaLista) {
        this.hotelList = nuevaLista;
        // Notificar al adapter que los datos han cambiado
        notifyDataSetChanged();
    }

    // Método alternativo más eficiente usando DiffUtil (recomendado para listas grandes)
    public void actualizarListaConDiffUtil(List<Hotel> nuevaLista) {
        List<Hotel> listaAnterior = new ArrayList<>(this.hotelListFiltrada);
        this.hotelListFiltrada.clear();

        if (nuevaLista != null) {
            this.hotelListFiltrada.addAll(nuevaLista);
        }

        // Usar DiffUtil para calcular las diferencias
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return listaAnterior.size();
            }

            @Override
            public int getNewListSize() {
                return hotelListFiltrada.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return listaAnterior.get(oldItemPosition).getIdHotel()
                        .equals(hotelListFiltrada.get(newItemPosition).getIdHotel());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Hotel oldHotel = listaAnterior.get(oldItemPosition);
                Hotel newHotel = hotelListFiltrada.get(newItemPosition);
                return oldHotel.equals(newHotel); // Necesitas implementar equals() en Hotel
            }
        });

        // Aplicar los cambios de manera eficiente
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotelList.get(position);
        holder.bind(hotel);
    }

    @Override
    public int getItemCount() {
        return hotelList.size(); // Usar la lista filtrada
    }

    public class HotelViewHolder extends RecyclerView.ViewHolder {
        private double precioActual = 0.0;
        private ImageView hotelImage;
        private TextView hotelName;
        private RatingBar hotelRating;
        private TextView ratingText;
        private TextView hotelPrice;
        private MaterialButton viewDetailButton;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotelImage);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelRating = itemView.findViewById(R.id.hotelRating);
            ratingText = itemView.findViewById(R.id.ratingText);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            viewDetailButton = itemView.findViewById(R.id.viewDetailButton);
        }

        public void bind(Hotel hotel) {
            // Configurar nombre del hotel
            hotelName.setText(hotel.getName());
            Log.d("HOTEL", "ID: " + hotel.getIdHotel());
            Log.d("HOTEL", "NAME: " + hotel.getName());

            // Configurar valoración
            float rating = hotel.getRating();
            hotelRating.setRating(rating);
            ratingText.setText(String.format(Locale.getDefault(), "%.1f", rating));

            // Cargar imagen principal (con id 0)
            cargarImagenPrincipal(hotel);

            // Obtener y mostrar precio más económico
            obtenerPrecioMasEconomico(hotel);

            // Configurar botón de detalle
            viewDetailButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHotelClick(hotel, precioActual);
                }
            });
        }

        private void cargarImagenPrincipal(Hotel hotel) {
            // Buscar la imagen con id 0
            db.collection("hoteles")
                    .document(hotel.getIdHotel())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");

                            if (imageUrls != null && !imageUrls.isEmpty()) {
                                // Usar la primera imagen como imagen principal
                                String imageUrl = imageUrls.get(0);

                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_hotel_placeholder) // Crear este drawable
                                        .error(R.drawable.ic_hotel_error) // Crear este drawable
                                        .transform(new CenterCrop(), new RoundedCorners(16))
                                        .into(hotelImage);
                            } else {
                                // Si no hay imágenes, usar placeholder
                                Glide.with(context)
                                        .load(R.drawable.ic_hotel_placeholder)
                                        .into(hotelImage);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // En caso de error, usar imagen de error
                        Glide.with(context)
                                .load(R.drawable.ic_hotel_error)
                                .into(hotelImage);
                    });
        }

        private void obtenerPrecioMasEconomico(Hotel hotel){
            hotelPrice.setText("Cargando...");
            db.collection("habitaciones")
                    .whereEqualTo("hotelId",hotel.getIdHotel())
                    .whereEqualTo("status","Available")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        double precioMinimo = Double.MAX_VALUE;
                        boolean encontradoPrecio = false;

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            Double precio = doc.getDouble("price");
                            Log.d("PRECIO", "Habitación: " + doc.getId() + " - Precio: " + precio);
                            if (precio != null && precio < precioMinimo) {
                                precioMinimo = precio;
                                encontradoPrecio = true;
                            }
                        }
                        if (encontradoPrecio) {
                            precioActual=precioMinimo;
                            // Formatear precio según la moneda
                            String precioFormateado = String.format(Locale.getDefault(),
                                    "S/ %.0f/noche", precioMinimo);
                            hotelPrice.setText(precioFormateado);
                        } else {
                            precioActual=0.0;
                            hotelPrice.setText("Precio no disponible");
                        }
                        if (queryDocumentSnapshots.isEmpty()) {
                            Log.w("PRECIO", "No se encontraron habitaciones disponibles para el hotel: " + hotel.getIdHotel());
                        }
                    })
                    .addOnFailureListener(e->{
                        precioActual=0.0;
                        hotelPrice.setText("Error al cargar precio");
                        Log.e("PRECIO", "Error al cargar habitaciones: ", e);
                    });

        }
    }
}

// Alternativa si quieres manejar diferentes tipos de moneda
//class PrecioUtils {
//    public static String formatearPrecio(double precio, String moneda) {
//        switch (moneda) {
//            case "USD":
//                return String.format(Locale.US, "$%.0f/noche", precio);
//            case "EUR":
//                return String.format(Locale.GERMANY, "€%.0f/noche", precio);
//            case "PEN":
//            default:
//                return String.format(Locale.getDefault(), "S/ %.0f/noche", precio);
//        }
//    }
//}