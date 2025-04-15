package com.example.hotroid;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class SuperEventosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        {
            setContentView(R.layout.super_eventos); // Corregido el nombre del layout

            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

            // BottomNavigationView o Barra inferior de menú
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_hoteles) {
                    Intent intentInicio = new Intent(SuperEventosActivity.this, SuperActivity.class);
                    startActivity(intentInicio);
                    return true;
                } else if (item.getItemId() == R.id.nav_usuarios) {
                    Intent intentUbicacion = new Intent(SuperEventosActivity.this, SuperUsuariosActivity.class);
                    startActivity(intentUbicacion);
                    return true;
                } else if (item.getItemId() == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperEventosActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    return true;
                } else {
                    return false;
                }
            });

            LinearLayout evento1 = findViewById(R.id.evento1);
            evento1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuperEventosActivity.this, SuperDetallesEventosActivity.class);
                    startActivity(intent);
                }
            });

            CardView cardSuper = findViewById(R.id.cardSuper);
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperEventosActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });


        }

    }}
