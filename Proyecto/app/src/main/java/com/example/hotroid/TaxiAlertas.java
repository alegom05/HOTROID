package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class TaxiAlertas extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_alertas);

 /*       RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));*/

        List<Notificacion> lista = new ArrayList<>();
        lista.add(new Notificacion("Mauricio Guerra", "Hotel Marriot", "hace 20 minutos"));
        lista.add(new Notificacion("Lisa Cáceres", "Hotel Marriot", "hace 1 minuto"));
        lista.add(new Notificacion("Sol Díaz", "Hotel Marriot", "ahora"));

        NotificacionAdapter adapter = new NotificacionAdapter(lista);
/*
        recyclerView.setAdapter(adapter);
*/

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
