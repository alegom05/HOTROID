package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxiLocation extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_location);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiLocation.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiLocation.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiLocation.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });
    }
}
