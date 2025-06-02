package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SuperActivity extends AppCompatActivity {

    // Define a simple class to hold hotel data
    // This class could also be defined in its own file (e.g., Hotel.java)
    // if you plan to reuse it extensively. For now, it's nested for simplicity.
    public static class Hotel {
        private String name;
        private String location;
        private int imageResId; // Resource ID for the drawable image

        public Hotel(String name, String location, int imageResId) {
            this.name = name;
            this.location = location;
            this.imageResId = imageResId;
        }

        public String getName() { return name; }
        public String getLocation() { return location; }
        public int getImageResId() { return imageResId; }
    }

    // List to store hotel data
    private List<Hotel> hotelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_main); // Make sure this matches your XML file name for the main hotels list

        // Initialize hotel data
        hotelList = new ArrayList<>();
        hotelList.add(new Hotel("Aranwa", "Paracas, Ica", R.drawable.hotel_aranwa));
        hotelList.add(new Hotel("Decameron", "Punta Sal, Piura", R.drawable.hotel_decameron));
        hotelList.add(new Hotel("Oro Verde", "Iquitos, Loreto", R.drawable.hotel_oro_verde));
        hotelList.add(new Hotel("Boca Ratón", "Tarapoto, San Martín", R.drawable.hotel_boca_raton));
        hotelList.add(new Hotel("Libertador", "Machu Picchu, Cusco", R.drawable.hotel_libertador));
        hotelList.add(new Hotel("Costa del Sol", "Trujillo, La Libertad", R.drawable.hotel_costa_sol));
        hotelList.add(new Hotel("Sonesta", "Arequipa", R.drawable.hotel_sonesta));

        // Set up click listener for the Super Admin profile card
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        // Set up click listeners for each hotel card
        setupHotelCardListeners();

        // Set up BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles); // Highlight 'Hoteles' as the current screen
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                // Already on this activity, no need to restart
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUsuarios = new Intent(SuperActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUsuarios);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentEventos = new Intent(SuperActivity.this, SuperEventosActivity.class);
                startActivity(intentEventos);
                return true;
            }
            return false;
        });
    }

    // This method sets up click listeners for all hotel cards
    private void setupHotelCardListeners() {
        // Array of CardView IDs for each hotel in super_main.xml
        int[] cardIds = {
                R.id.cardHotel1, R.id.cardHotel2, R.id.cardHotel3,
                R.id.cardHotel4, R.id.cardHotel5, R.id.cardHotel6,
                R.id.cardHotel7
        };

        // Loop through each card ID and attach a click listener
        for (int i = 0; i < cardIds.length; i++) {
            CardView cardView = findViewById(cardIds[i]);
            final int hotelIndex = i; // This variable will be used in the lambda, so it must be final or effectively final

            cardView.setOnClickListener(v -> {
                // Get the Hotel object corresponding to the clicked card
                Hotel selectedHotel = hotelList.get(hotelIndex);
                Intent intent = new Intent(SuperActivity.this, SuperDetallesActivity.class);

                // Pass the hotel data to SuperDetallesActivity using Intent extras
                intent.putExtra("hotel_name", selectedHotel.getName());
                intent.putExtra("hotel_location", selectedHotel.getLocation());
                intent.putExtra("hotel_image_res_id", selectedHotel.getImageResId());

                startActivity(intent);
            });
        }
    }
}