package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxiUsuario extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main);






        // BottomNavigationView o Barra inferior de menú
        BottomNavigationView bottomNavigationView = null;
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiUsuario.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiUsuario.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiUsuario.this, TaxiCuenta.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });




    }
}
