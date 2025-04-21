package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminTaxistas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_taxistas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.cardTaxista1).setOnClickListener(v -> abrirDetalle("Carlos Alvarez", "En camino"));
        findViewById(R.id.cardTaxista2).setOnClickListener(v -> abrirDetalle("Arturo Delgado", "Asignado"));
        findViewById(R.id.cardTaxista3).setOnClickListener(v -> abrirDetalle("Carlos Alvarez", "No Asignado"));
        findViewById(R.id.cardTaxista4).setOnClickListener(v -> abrirDetalle("Arturo Delgado", "LLegó a destino"));
        findViewById(R.id.cardTaxista5).setOnClickListener(v -> abrirDetalle("Carlos Alvarez", "En camino"));
        findViewById(R.id.cardTaxista6).setOnClickListener(v -> abrirDetalle("Arturo Delgado", "Asignado"));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminTaxistas.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminTaxistas.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminTaxistas.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminTaxistas.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
    private void abrirDetalle(String nombre, String estado) {
        Intent intent = new Intent(AdminTaxistas.this, AdminTaxistaDetalles.class);
        intent.putExtra("nombre", nombre);
        intent.putExtra("estado", estado);
        startActivity(intent);
    }

}