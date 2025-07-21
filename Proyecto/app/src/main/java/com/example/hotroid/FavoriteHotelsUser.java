package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Hotel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FavoriteHotelsUser extends AppCompatActivity {
    private static final String TAG = "FavoriteHotelUser";

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private HotelFavoriteAdapter favoriteAdapter;
    private List<Hotel> favoriteHotels = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.user_favorite_hotels);

            db = FirebaseFirestore.getInstance();

            findViewById(R.id.back_button).setOnClickListener(v -> {
                // Regresar a la actividad anterior sin ir a HotelesFragment
                finish();
            });

            initViews();
            setupRecyclerView();
            loadFavoriteHotels();

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate", e);
            Toast.makeText(this, "Error al cargar la pantalla", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Solo cerrar esta actividad, no navegar
        super.onBackPressed();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewFavorites);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        favoriteAdapter = new HotelFavoriteAdapter(favoriteHotels, this);
        recyclerView.setAdapter(favoriteAdapter);
    }

    private void loadFavoriteHotels() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        String userId = currentUser.getUid();
        Log.d(TAG, "Cargando favoritos para usuario: " + userId);

        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> hotelIds = (List<String>) documentSnapshot.get("hotelesFav");
                        Log.d(TAG, "Hoteles favoritos encontrados: " + (hotelIds != null ? hotelIds.size() : 0));

                        if (hotelIds != null && !hotelIds.isEmpty()) {
                            loadHotelsData(hotelIds);
                        } else {
                            showEmptyState();
                        }
                    } else {
                        Log.w(TAG, "Documento de usuario no existe");
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error cargando favoritos", e);
                    showEmptyState();
                });
    }

    private void loadHotelsData(List<String> hotelIds) {
        favoriteHotels.clear();
        final int totalHotels = hotelIds.size();
        final int[] loadedCount = {0};

        for (String hotelId : hotelIds) {
            Log.d(TAG, "Cargando hotel: " + hotelId);

            db.collection("hoteles").document(hotelId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Hotel hotel = doc.toObject(Hotel.class);
                            if (hotel != null) {
                                hotel.setIdHotel(doc.getId());
                                favoriteHotels.add(hotel);
                                Log.d(TAG, "Hotel cargado: " + hotel.getName());
                            }
                        }
                        loadedCount[0]++;

                        if (loadedCount[0] == totalHotels) {
                            updateUI();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error cargando hotel: " + hotelId, e);
                        loadedCount[0]++;

                        if (loadedCount[0] == totalHotels) {
                            updateUI();
                        }
                    });
        }
    }

    private void updateUI() {
        Log.d(TAG, "Actualizando UI con " + favoriteHotels.size() + " hoteles");

        if (favoriteHotels.isEmpty()) {
            showEmptyState();
        } else {
            showHotels();
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
        Log.d(TAG, "Mostrando estado vacío");
    }

    private void showHotels() {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        favoriteAdapter.updateList(favoriteHotels);
        Log.d(TAG, "Mostrando hoteles");
    }
}