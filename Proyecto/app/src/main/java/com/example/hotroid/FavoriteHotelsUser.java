package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Hotel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoriteHotelsUser extends AppCompatActivity {
    private static final String TAG = "FavoriteHotelUser";

    private RecyclerView recyclerView;
    private HotelAdapter adapter;
    private List<Hotel> favoriteHotels = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_favorite_hotels);

        db = FirebaseFirestore.getInstance();

        // Configurar botón de retroceso
        findViewById(R.id.back_button).setOnClickListener(v -> onBackPressed());

        // Configurar RecyclerView
        setupRecyclerView();

        // Cargar hoteles favoritos
        loadFavoriteHotels();
    }

    private void setupRecyclerView() {
        // Necesitamos reemplazar el GridLayout existente con un RecyclerView
        // Por ahora, vamos a usar el ScrollView como contenedor

        // Inicializar el adapter con la lista vacía
        adapter = new HotelAdapter(favoriteHotels, this);

        // Configurar listener de clicks
        adapter.setOnHotelClickListener(new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel, double precio) {
                // Ir a los detalles del hotel
                Intent intent = new Intent(FavoriteHotelsUser.this, HotelDetalladoUser.class);
                intent.putExtra("hotelId", hotel.getIdHotel());
                startActivity(intent);
            }
        });

        // Como no hay RecyclerView en el layout actual, vamos a mostrar los datos de otra forma
        // por ahora, hasta que se actualice el layout
    }

    private void loadFavoriteHotels() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> hotelIds = (List<String>) documentSnapshot.get("hotelesFav");
                        if (hotelIds != null && !hotelIds.isEmpty()) {
                            loadHotelsData(hotelIds);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando favoritos", e);
                    Toast.makeText(this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHotelsData(List<String> hotelIds) {
        favoriteHotels.clear();

        for (String hotelId : hotelIds) {
            db.collection("hoteles").document(hotelId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Hotel hotel = doc.toObject(Hotel.class);
                            if (hotel != null) {
                                hotel.setIdHotel(doc.getId());
                                favoriteHotels.add(hotel);
                                updateUI();
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error cargando hotel: " + hotelId, e));
        }
    }

    private void updateUI() {
        // Por ahora solo mostrar un toast con la cantidad de favoritos
        // hasta que se actualice el layout
        Toast.makeText(this, "Hoteles favoritos cargados: " + favoriteHotels.size(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Hoteles favoritos cargados: " + favoriteHotels.size());

        // Aquí podrías agregar lógica para poblar el GridLayout actual
        // o mostrar la información de otra manera
    }

    private void showEmptyState() {
        Toast.makeText(this, "No tienes hoteles favoritos aún", Toast.LENGTH_SHORT).show();
    }
}