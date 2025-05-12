package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxiFin extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_fin);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        ImageButton btnAtras = findViewById(R.id.btnAtras);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiFin.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiFin.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiFin.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViajes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ViajeFinBeans> lista = new ArrayList<>();
        lista.add(new ViajeFinBeans("Juan Pérez", "9:00 am - 9:30 am", "Taxista", R.drawable.usuariopic1));

        TaxiFinAdapter adapter = new TaxiFinAdapter(lista);
        recyclerView.setAdapter(adapter);


        btnAtras.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(TaxiFin.this, TaxiActivity.class); // Redirige a TaxiCuenta
            startActivity(intent); // Inicia la nueva actividad
        });

    }
}
