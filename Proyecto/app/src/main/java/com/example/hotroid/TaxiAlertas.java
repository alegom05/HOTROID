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

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_alertas);

        CardView cardUsuario = findViewById(R.id.cardUsuario);
        cardUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirige a TaxiAlertas al hacer clic
                Intent intent = new Intent(TaxiAlertas.this, TaxiCuenta.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TaxiAlertasBeans> lista = new ArrayList<>();
        lista.add(new TaxiAlertasBeans("Mauricio Guerra", "Hotel Marriot","Hotel Miraflores", "ahora",""));
        lista.add(new TaxiAlertasBeans("Lisa Cáceres", "Hotel Marriot", "Grand Hotel Madrid","hace 1 minuto",""));
        lista.add(new TaxiAlertasBeans("Sol Díaz", "Hotel Marriot", "Valencia Beach Resort","hace 20 minutos",""));

        // Usar constructor con contexto para facilitar la navegación
        TaxiAlertasAdapter adapter = new TaxiAlertasAdapter(this, lista);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notify);

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
                Intent intentAlertas = new Intent(TaxiAlertas.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });
    }
}