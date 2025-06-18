package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Para cargar imágenes desde URL
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Si aún quieres mostrar rating/precio

public class SuperDetallesActivity extends AppCompatActivity {

    private TextView tvHotelNameAndLocation; // Combina nombre y ubicación
    private TextView tvHotelDescription; // Para la descripción detallada
    private ImageView imgHotelMain; // Tu ImageView para la imagen principal
    private Button btnVerReservas; // Tu botón de ver reservas

    // Si quieres mostrar Rating, Precio, Dirección Detallada, necesitarás añadir estos TextViews a tu XML
    // y luego descomentarlos y referenciarlos aquí. Por ahora, los dejo comentados.
    // private TextView tvHotelRating;
    // private TextView tvHotelPrice;
    // private TextView tvHotelDetailedAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_hotel); // ¡Tu XML correcto!

        // Inicializar vistas según tus IDs de XML
        tvHotelNameAndLocation = findViewById(R.id.tvNombreHotel); // Combina nombre y ubicación en uno
        tvHotelDescription = findViewById(R.id.tvDescripcion);
        imgHotelMain = findViewById(R.id.imgHotel); // Tu ImageView
        btnVerReservas = findViewById(R.id.btnVerReservas);

        // Opcional: Referenciar otros TextViews si los agregas a tu XML
        // tvHotelRating = findViewById(R.id.tvHotelRating);
        // tvHotelPrice = findViewById(R.id.tvHotelPrice);
        // tvHotelDetailedAddress = findViewById(R.id.tvHotelDetailedAddress);

        // Configurar el click listener para la CardView del Super Admin (si es clickeable)
        // Aunque en tu XML es clickable, si no lo manejas, no hará nada.
        // Si cardSuper en activity_super_detalles.xml es la misma CardView que en super_main.xml
        // y quieres que vaya a SuperCuentaActivity, descomenta esto:
        /*
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
        */

        // Configurar el BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            // Asegúrate de que el ID seleccionado sea el correcto para esta vista,
            // o simplemente desactiva la selección si no quieres que un item esté "activo" aquí.
            // bottomNavigationView.setSelectedItemId(R.id.nav_hoteles); // O el que corresponda
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    // Si esta actividad es a la que se llega desde "Hoteles",
                    // al volver a presionar Hoteles, simplemente finaliza para volver a SuperActivity
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

        // Obtener datos del Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String hotelId = extras.getString("hotel_id");
            String hotelName = extras.getString("hotel_name");
            String hotelLocation = extras.getString("hotel_location");
            float hotelRating = extras.getFloat("hotel_rating", 0.0f); // Valor por defecto
            String hotelPrice = extras.getString("hotel_price");
            String hotelDetailedAddress = extras.getString("hotel_detailed_address");
            String hotelDescription = extras.getString("hotel_description");
            ArrayList<String> imageUrls = extras.getStringArrayList("hotel_image_urls");

            // Rellenar vistas con los datos
            // Combina nombre y ubicación en el TextView existente tvNombreHotel
            tvHotelNameAndLocation.setText(hotelName + ", " + hotelLocation);
            tvHotelDescription.setText(hotelDescription);

            // Si agregas estos TextViews a tu XML, descomenta estas líneas
            // tvHotelRating.setText(String.format(Locale.getDefault(), "%.1f", hotelRating));
            // tvHotelPrice.setText(hotelPrice);
            // tvHotelDetailedAddress.setText(hotelDetailedAddress);

            // Cargar la primera imagen de la lista en tu ImageView imgHotel
            if (imageUrls != null && !imageUrls.isEmpty()) {
                Glide.with(this)
                        .load(imageUrls.get(0)) // Carga la primera URL de la lista
                        .placeholder(R.drawable.placeholder_hotel) // Asegúrate de tener este drawable
                        .error(R.drawable.ic_user_error) // Asegúrate de tener este drawable
                        .into(imgHotelMain);

                // TODO: Aquí es donde integrarías la galería deslizable (ViewPager2 o similar)
                // Usarías la lista 'imageUrls' para un adaptador que muestre todas las imágenes.
                // Esta sección la desarrollarás cuando implementes la galería.
                // Por ejemplo, si tienes un ViewPager2 con ID 'viewPagerGallery':
                // ViewPager2 viewPagerGallery = findViewById(R.id.viewPagerGallery);
                // ImageGalleryAdapter adapter = new ImageGalleryAdapter(imageUrls);
                // viewPagerGallery.setAdapter(adapter);

            } else {
                imgHotelMain.setImageResource(R.drawable.placeholder_hotel); // Si no hay URLs, muestra un placeholder
            }

            // Click listener para el botón "Ver Reservas"
            btnVerReservas.setOnClickListener(v -> {
                // TODO: Aquí la lógica para ir a la actividad de reservas del hotel
                // Puedes pasar el hotelId para cargar las reservas específicas de este hotel.
                // Intent intentReservas = new Intent(SuperDetallesActivity.this, ReservasActivity.class);
                // intentReservas.putExtra("hotel_id", hotelId);
                // startActivity(intentReservas);
                Toast.makeText(SuperDetallesActivity.this, "Ver Reservas del Hotel: " + hotelName, Toast.LENGTH_SHORT).show();
            });
        }
    }
}