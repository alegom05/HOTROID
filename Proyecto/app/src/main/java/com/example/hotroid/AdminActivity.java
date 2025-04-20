package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminCuentaActivity.class);
            startActivity(intent);
        });
        // Ejemplo para un tipo
        CardView cardUbicacion = findViewById(R.id.cardUbicacion); // Asegúrate que este ID coincida con tu XML
        cardUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, UbicacionActivity.class);
                startActivity(intent);
            }
        });
        CardView cardFotos = findViewById(R.id.cardFotos);
        cardFotos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminFotosActivity.class);
            startActivity(intent);
        });

        CardView cardHabitaciones = findViewById(R.id.cardHabitaciones);
        cardHabitaciones.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminHabitacionesActivity.class);
            startActivity(intent);
        });
        CardView cardServicios = findViewById(R.id.cardServicios);
        cardServicios.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminServiciosActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
}
