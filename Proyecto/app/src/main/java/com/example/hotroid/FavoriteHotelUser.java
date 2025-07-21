package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

public class FavoriteHotelUser extends AppCompatActivity {
    private static final String TAG = "FavoriteHotelUser";

    private RecyclerView recyclerView;
    private HotelAdapter adapter; // Usando el HotelAdapter existente
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
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Inicializar el adapter con la lista vacía
        adapter = new HotelAdapter(favoriteHotels, this);

        // Configurar listener de clicks
        adapter.setOnHotelClickListener(new HotelAdapter.OnHotelClickListener() {
            @Override
            public void onHotelClick(Hotel hotel, double precio) {
                // Ir a los detalles del hotel
                Intent intent = new Intent(FavoriteHotelUser.this, HotelDetalladoUser.class);
                intent.putExtra("hotelId", hotel.getIdHotel());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);
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
        if (adapter != null) {
            // Usar el método del HotelAdapter para actualizar la lista
            adapter.actualizarLista(favoriteHotels);
            Log.d(TAG, "Hoteles favoritos cargados: " + favoriteHotels.size());
        }
    }

    private void showEmptyState() {
        Toast.makeText(this, "No tienes hoteles favoritos aún", Toast.LENGTH_SHORT).show();
        // Opcional: mostrar un layout de estado vacío
        findViewById(R.id.recyclerViewFavorites).setVisibility(View.GONE);
        // findViewById(R.id.empty_state_layout).setVisibility(View.VISIBLE); // Si tienes este layout
    }
}