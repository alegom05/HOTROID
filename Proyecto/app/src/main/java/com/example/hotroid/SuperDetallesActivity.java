package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesActivity extends AppCompatActivity {

    private TextView tvHotelName;
    private TextView tvHotelRating;
    private TextView tvHotelPrice;
    private TextView tvHotelDetailedAddress;
    private TextView tvHotelDescription;
    private ImageView imgHotelMain;
    private Button btnVerReservas;
    private String selectedHotelId;
    private String selectedHotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_hotel);

        // Inicializar vistas
        tvHotelName = findViewById(R.id.tvNombreHotel);
        tvHotelRating = findViewById(R.id.tvHotelRating);
        tvHotelPrice = findViewById(R.id.tvHotelPrice);
        tvHotelDetailedAddress = findViewById(R.id.tvHotelDetailedAddress);
        tvHotelDescription = findViewById(R.id.tvDescripcion);
        imgHotelMain = findViewById(R.id.imgHotel);
        btnVerReservas = findViewById(R.id.btnVerReservas);

        // Configurar navegación inferior
        setupBottomNavigation();

        // Obtener datos del Intent
        Intent intent = getIntent();
        if (intent != null) {
            selectedHotelId = intent.getStringExtra("hotel_id");
            selectedHotelName = intent.getStringExtra("hotel_name");
            String hotelLocation = intent.getStringExtra("hotel_location");
            float hotelRating = intent.getFloatExtra("hotel_rating", 0.0f);
            double hotelPrice = intent.getDoubleExtra("hotel_price", 0.0);
            String hotelDetailedAddress = intent.getStringExtra("hotel_detailed_address");
            String hotelDescription = intent.getStringExtra("hotel_description");
            String imageName = intent.getStringExtra("hotel_image_name");
            int imageResourceId = intent.getIntExtra("hotel_image_resource_id", 0);

            // Mostrar datos del hotel
            tvHotelName.setText(selectedHotelName);
            tvHotelRating.setText(String.format("%.1f", hotelRating));
            tvHotelPrice.setText(String.format("S/. %.2f", hotelPrice));
            tvHotelDetailedAddress.setText(hotelDetailedAddress);
            tvHotelDescription.setText(hotelDescription);

            if (imageResourceId != 0) {
                Glide.with(this)
                        .load(imageResourceId)
                        .placeholder(R.drawable.placeholder_hotel)
                        .into(imgHotelMain);
            } else {
                imgHotelMain.setImageResource(R.drawable.placeholder_hotel);
            }
        }

        // Configurar botón Ver Reservas
        btnVerReservas.setOnClickListener(v -> {
            if (selectedHotelId != null && !selectedHotelId.isEmpty()) {
                Intent reservasIntent = new Intent(SuperDetallesActivity.this, SuperReservasActivity.class);
                reservasIntent.putExtra("hotel_id", selectedHotelId);
                reservasIntent.putExtra("hotel_name", selectedHotelName);
                startActivity(reservasIntent);
            } else {
                Toast.makeText(this, "Error: No se pudo identificar el hotel", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                finish(); // Regresar a SuperActivity
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(this, SuperEventosActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}