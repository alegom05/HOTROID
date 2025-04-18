package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperListaTaxisActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        {
            setContentView(R.layout.super_lista_taxis); // Corregido el nombre del layout

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

            // BottomNavigationView o Barra inferior de menú
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_hoteles) {
                    Intent intentInicio = new Intent(SuperListaTaxisActivity.this, SuperActivity.class);
                    startActivity(intentInicio);
                    return true;
                } else if (item.getItemId() == R.id.nav_usuarios) {
                    Intent intentUbicacion = new Intent(SuperListaTaxisActivity.this, SuperUsuariosActivity.class);
                    startActivity(intentUbicacion);
                    return true;
                } else if (item.getItemId() == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperListaTaxisActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    return true;
                } else {
                    return false;
                }
            });

            CardView cardSuper2 = findViewById(R.id.cardSuper);
            cardSuper2.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaTaxisActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });

            LinearLayout evento1 = findViewById(R.id.caso1);
            evento1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiActivadoActivity.class);
                    startActivity(intent);
                }
            });

            LinearLayout evento2 = findViewById(R.id.caso2);
            evento2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiDesactivadoActivity.class);
                    startActivity(intent);
                }
            });

            LinearLayout evento3 = findViewById(R.id.caso3);
            evento3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiPendienteActivity.class);
                    startActivity(intent);
                }
            });

            CardView cardSuper = findViewById(R.id.cardSuper);
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaTaxisActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });


        }

    }
}
