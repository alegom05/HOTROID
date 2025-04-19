package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        CardView cardTaxista = findViewById(R.id.cardTaxista);
        CardView cardAlertas = findViewById(R.id.cardAlertas);

        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });

        cardAlertas.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiActivity.this, TaxiAlertas.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiActivity.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiActivity.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });
    }
}
