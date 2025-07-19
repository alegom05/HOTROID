package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView; // Import TextView

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.text.HtmlCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesEventosActivity extends AppCompatActivity {

    private TextView tvNombreHotel; // This will display the event title (e.g., "Corte de energía en el hotel Oro Verde")
    private TextView tvDescripcion; // This will display the detailed description

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_eventos);

        // Initialize TextViews
        tvNombreHotel = findViewById(R.id.tvNombreHotel);
        tvDescripcion = findViewById(R.id.tvDescripcion);

        // Get data from the Intent that started this activity
        Intent intent = getIntent();
        if (intent != null) {
            String eventFecha = intent.getStringExtra("event_fecha"); // Not used on this screen, but good to have
            String eventTitulo = intent.getStringExtra("event_titulo"); // e.g., "Corte de energía en la torre A"
            String eventHotel = intent.getStringExtra("event_hotel");   // e.g., "Oro Verde"
            String eventDescripcion = intent.getStringExtra("event_descripcion"); // The detailed description

            // Set the event title (e.g., "Corte de energía en la torre A en el hotel Oro Verde")
            if (eventTitulo != null && eventHotel != null) {
                tvNombreHotel.setText(eventTitulo + " en el hotel " + eventHotel);
            } else if (eventTitulo != null) {
                tvNombreHotel.setText(eventTitulo); // Fallback if hotel name is missing
            } else {
                tvNombreHotel.setText("Título del Evento Desconocido");
            }

            // Set the detailed description
            if (eventDescripcion != null && !eventDescripcion.isEmpty()) {
                tvDescripcion.setText(HtmlCompat.fromHtml(eventDescripcion, HtmlCompat.FROM_HTML_MODE_LEGACY));
            } else {
                tvDescripcion.setText("No hay una descripción detallada disponible para este evento.");
            }
        }


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperDetallesEventosActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesEventosActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentEventos = new Intent(SuperDetallesEventosActivity.this, SuperEventosActivity.class);
                startActivity(intentEventos);
                return true;
            } else {
                return false;
            }
        });

        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intentAccount = new Intent(SuperDetallesEventosActivity.this, SuperCuentaActivity.class);
            startActivity(intentAccount);
        });
    }
}