package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxiAlertas extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_alertas);






        // BottomNavigationView o Barra inferior de menú
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiAlertas.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiAlertas.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiAlertas.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });




    }
}
