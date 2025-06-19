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

import java.util.Locale; // Si aún quieres mostrar rating/precio

public class SuperDetallesActivity extends AppCompatActivity {

    private TextView tvHotelName; // Para el nombre del hotel
    private TextView tvHotelRating; // Para el rating
    private TextView tvHotelPrice; // Para el precio
    private TextView tvHotelDetailedAddress; // Para la dirección detallada
    private TextView tvHotelDescription; // Para la descripción
    private ImageView imgHotelMain; // Tu ImageView para la imagen principal
    private Button btnVerReservas; // Tu botón de ver reservas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_hotel); // ¡Tu XML correcto!

        // 1. Inicializar vistas según tus IDs de XML
        // tvTitulo es un TextView general que dice "Detalles del Hotel", no necesita ser modificado
        // tvNombre y tvRol son para la CardView del administrador, no para el hotel detallado.

        tvHotelName = findViewById(R.id.tvNombreHotel); // Usamos este para el nombre
        tvHotelRating = findViewById(R.id.tvHotelRating);
        tvHotelPrice = findViewById(R.id.tvHotelPrice);
        tvHotelDetailedAddress = findViewById(R.id.tvHotelDetailedAddress);
        tvHotelDescription = findViewById(R.id.tvDescripcion);
        imgHotelMain = findViewById(R.id.imgHotel);
        btnVerReservas = findViewById(R.id.btnVerReservas);

        // 2. Configurar el BottomNavigationView (lo tienes bien, solo asegúrate de los IDs)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    // Si esta actividad se abrió desde "Hoteles" en SuperActivity, al volver a presionar
                    // "Hoteles", simplemente finalizamos para regresar a SuperActivity.
                    // Si esta fuera la actividad principal de Hoteles, no usaríamos finish()
                    finish();
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    Intent intentUsuarios = new Intent(SuperDetallesActivity.this, SuperUsuariosActivity.class);
                    startActivity(intentUsuarios);
                    finish(); // Cierra esta actividad
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    Intent intentEventos = new Intent(SuperDetallesActivity.this, SuperEventosActivity.class);
                    startActivity(intentEventos);
                    finish(); // Cierra esta actividad
                    return true;
                }
                return false;
            });
        }

        // 3. Obtener datos del Intent y rellenar las vistas
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String hotelId = extras.getString("hotel_id");
            String hotelName = extras.getString("hotel_name");
            String hotelLocation = extras.getString("hotel_location"); // No usado directamente en el XML de detalles, pero disponible
            float hotelRating = extras.getFloat("hotel_rating", 0.0f);
            double hotelPrice = extras.getDouble("hotel_price", 0.0); // Ahora es double
            String hotelDetailedAddress = extras.getString("hotel_detailed_address");
            String hotelDescription = extras.getString("hotel_description");
            String imageName = extras.getString("hotel_image_name"); // El nombre del drawable

            // Rellenar TextViews con los datos
            tvHotelName.setText(hotelName); // Asigna solo el nombre al tvNombreHotel
            tvHotelRating.setText(String.format(Locale.getDefault(), "%.1f", hotelRating));
            tvHotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f", hotelPrice)); // Formatear precio
            tvHotelDetailedAddress.setText(hotelDetailedAddress); // Asigna la dirección detallada
            tvHotelDescription.setText(hotelDescription);

            // Cargar imagen usando el nombre del drawable
            if (imageName != null && !imageName.isEmpty()) {
                int imageResId = getResources().getIdentifier(imageName.toLowerCase(Locale.getDefault()), "drawable", getPackageName());
                if (imageResId != 0) {
                    imgHotelMain.setImageResource(imageResId);
                } else {
                    imgHotelMain.setImageResource(R.drawable.placeholder_hotel); // Placeholder si no se encuentra
                    Toast.makeText(this, "Imagen no encontrada: " + imageName, Toast.LENGTH_SHORT).show();
                }
            } else {
                imgHotelMain.setImageResource(R.drawable.placeholder_hotel); // Placeholder si no hay nombre de imagen
            }


            // 4. Configurar el click listener para el botón "Ver Reservas"
            btnVerReservas.setOnClickListener(v -> {
                // Navegar a SuperReservasActivity, pasando el ID del hotel
                Intent intentReservas = new Intent(SuperDetallesActivity.this, SuperReservasActivity.class);
                intentReservas.putExtra("hotel_id", hotelId); // Pasar el ID del documento del hotel
                Toast.makeText(SuperDetallesActivity.this, "Cargando reservas para: " + hotelName, Toast.LENGTH_SHORT).show();
                startActivity(intentReservas);
            });
        } else {
            Toast.makeText(this, "No se recibieron datos del hotel.", Toast.LENGTH_SHORT).show();
            // Opcional: Finalizar la actividad si no hay datos
            // finish();
        }
    }
}