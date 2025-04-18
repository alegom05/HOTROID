package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesTaxiPendienteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_taxi_pendiente);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

        Button btnAprobar = findViewById(R.id.btnAprobar);
        btnAprobar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperListaTaxisActivity.class);
            startActivity(intent);
        });

        Button btnRechazar = findViewById(R.id.btnRechazar);
        btnRechazar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperListaTaxisActivity.class);
            startActivity(intent);
        });

        CardView cardSuper2 = findViewById(R.id.cardPerfil);
        cardSuper2.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesTaxiPendienteActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
    }

}
