package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TaxiCuenta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_cuenta);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        Button btnVehiculo = findViewById(R.id.btnVehiculo);

        btnVehiculo.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiCuenta.this, TaxiVehiculo.class); // Redirige a TaxiCuenta
            startActivity(intent); // Inicia la nueva actividad
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiCuenta.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiCuenta.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiCuenta.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });

        btnVehiculo.setOnClickListener(v -> {
            Log.d("TaxiCuenta", "Botón de Vehículo presionado");
            Intent intent = new Intent(TaxiCuenta.this, TaxiVehiculo.class);
            startActivity(intent);
        });


    }
}
