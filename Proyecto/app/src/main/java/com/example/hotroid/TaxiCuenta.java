package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.authentication.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxiCuenta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_cuenta);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        Button btnVehiculo = findViewById(R.id.btnVehiculo);

        ImageButton btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(TaxiCuenta.this, TaxiActivity.class); // Redirige a TaxiCuenta
            startActivity(intent); // Inicia la nueva actividad
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerCuenta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String[]> datos = new ArrayList<>();
        datos.add(new String[]{"Nombre:", "Alejandro"});
        datos.add(new String[]{"Apellidos:", "Gómez Mostacero"});
        datos.add(new String[]{"Tipo de documento:", "DNI"});
        datos.add(new String[]{"Número de documento:", "45464546"});
        datos.add(new String[]{"Fecha de nacimiento:", "28 de mayo de 1998"});
        datos.add(new String[]{"Teléfono:", "913454319"});
        datos.add(new String[]{"Dirección:", "Av. Los Taxis 123, Lima"});

        TaxiCuentaAdapter adapter = new TaxiCuentaAdapter(datos);
        recyclerView.setAdapter(adapter);

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
                Intent intentAlertas = new Intent(TaxiCuenta.this, TaxiDashboardActivity.class);
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

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiCuenta.this, LoginActivity.class);
            startActivity(intent);
        });


    }
}
