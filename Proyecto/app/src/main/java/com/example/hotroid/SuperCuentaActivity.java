package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SuperCuentaActivity extends AppCompatActivity {

    private RecyclerView recyclerCuenta;
    private SuperCuentaAdapter adapter;
    private List<String[]> listaDatosCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_cuenta);

        // Inicializar vistas
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerCuenta = findViewById(R.id.recyclerCuenta);
        recyclerCuenta.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar la lista de datos de la cuenta
        listaDatosCuenta = new ArrayList<>();
        listaDatosCuenta.add(new String[]{"Nombres", "Pedro Miguel"});
        listaDatosCuenta.add(new String[]{"Apellidos", "Bustamante Melo"});
        listaDatosCuenta.add(new String[]{"Modo de Identificación", "DNI"});
        listaDatosCuenta.add(new String[]{"Número de Identidad", "12341234"});
        listaDatosCuenta.add(new String[]{"Fecha de nacimiento", "15 de septiembre de 2002"});
        listaDatosCuenta.add(new String[]{"Correo electrónico", "pedro.bume@gmail.com"});
        listaDatosCuenta.add(new String[]{"Teléfono", "912345123"});
        listaDatosCuenta.add(new String[]{"Domicilio", "Av. Las Palmeras 123, Los Olivos"});

        // Inicializar y configurar el adaptador
        adapter = new SuperCuentaAdapter(listaDatosCuenta);
        recyclerCuenta.setAdapter(adapter);

        ImageButton btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(SuperCuentaActivity.this, SuperActivity.class); // Redirige a TaxiCuenta
            startActivity(intent); // Inicia la nueva actividad
        });

        // Configurar listeners
        btnCerrarSesion.setOnClickListener(v -> {
            Intent intent = new Intent(SuperCuentaActivity.this, MainActivity.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperCuentaActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperCuentaActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperCuentaActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
}