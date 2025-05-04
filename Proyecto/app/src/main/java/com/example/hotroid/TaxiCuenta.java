package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerCuenta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> datos = new ArrayList<>();
        datos.add("Nombres: Alejandro");
        datos.add("Apellidos: Gómez Mostacero");
        datos.add("DNI: 12341234");
        datos.add("Fecha de nacimiento: 21 de mayo de 1998");
        datos.add("Correo electrónico: agomez@gmail.com");
        datos.add("Teléfono: 934567213");
        datos.add("Domicilio: Av. Benavides 123, Miraflores");

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

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiCuenta.this, MainActivity.class);
            startActivity(intent);
        });


    }
}
