package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesClienteActivadoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_cliente_activado);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperDetallesClienteActivadoActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesClienteActivadoActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperDetallesClienteActivadoActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

        Button btnCerrarSesion = findViewById(R.id.btnDesactivar);

        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesClienteActivadoActivity.this, SuperListaClientesActivity.class);
            startActivity(intent);
        });

        CardView cardSuper2 = findViewById(R.id.cardPerfil);
        cardSuper2.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesClienteActivadoActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
    }
}
