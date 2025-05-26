package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxiAlertas extends AppCompatActivity {

    private GoogleMap mMap; // Esta variable no se usa en esta Activity, podrías quitarla si no la necesitas.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_alertas);

        CardView cardUsuario = findViewById(R.id.cardUsuario);
        cardUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirige a TaxiCuenta al hacer clic en la tarjeta de usuario
                Intent intent = new Intent(TaxiAlertas.this, TaxiCuenta.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TaxiAlertasBeans> lista = new ArrayList<>();
        // Creamos las notificaciones con Origen y Destino claros
        lista.add(new TaxiAlertasBeans("Mauricio Guerra", "Hotel Marriot","Hotel Miraflores", "ahora"));
        lista.add(new TaxiAlertasBeans("Lisa Cáceres", "Hotel Marriot", "Grand Hotel Madrid","hace 1 minuto"));
        lista.add(new TaxiAlertasBeans("Sol Díaz", "Hotel Marriot", "Valencia Beach Resort","hace 20 minutos"));

        TaxiAlertasAdapter adapter = new TaxiAlertasAdapter(this, lista);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notify); // Selecciona el ícono de notificaciones por defecto

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                Intent intentInicio = new Intent(TaxiAlertas.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiAlertas.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                // Ya estás en esta Activity, no necesitas iniciarla de nuevo
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });
    }
}