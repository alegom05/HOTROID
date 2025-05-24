package com.example.hotroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SuperListaAdminActivity extends AppCompatActivity {

    private LinearLayout linearLayoutAdminsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_admins);

        linearLayoutAdminsContainer = findViewById(R.id.linearLayoutAdminsContainer);

        // Datos de los administradores como una lista de arreglos de String
        // Cada arreglo es: {usuario, hotel, estado_boolean_como_string_para_facilitar}
        // "true" para ACTIVADO, "false" para DESACTIVADO
        List<String[]> adminDataList = new ArrayList<>();
        adminDataList.add(new String[]{"Victor Díaz", "Oro Verde", "true"});
        adminDataList.add(new String[]{"Moises Castro", "-", "false"});
        adminDataList.add(new String[]{"Manuel Yarleque", "Sauce Resort", "true"});

        // Inflar y añadir cada ítem de administrador dinámicamente
        for (String[] adminData : adminDataList) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View adminItemView = inflater.inflate(R.layout.super_lista_admins_item, linearLayoutAdminsContainer, false);

            TextView tvUsuario = adminItemView.findViewById(R.id.tvUsuario);
            TextView tvHotel = adminItemView.findViewById(R.id.tvHotel);
            ImageView ivEstado = adminItemView.findViewById(R.id.ivEstado);

            tvUsuario.setText(adminData[0]); // Usuario
            tvHotel.setText(adminData[1]);   // Hotel

            // Convertir el string de estado a booleano
            boolean isActivado = Boolean.parseBoolean(adminData[2]);

            // Configurar el círculo de estado
            if (isActivado) {
                ivEstado.setImageResource(R.drawable.circle_green);
            } else {
                ivEstado.setImageResource(R.drawable.circle_red);
            }

            // Configurar el click listener para cada fila generada
            adminItemView.setOnClickListener(v -> {
                if (isActivado) {
                    Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminActivadoActivity.class);
                    // Puedes pasar datos adicionales si es necesario
                    // intent.putExtra("usuario", adminData[0]);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminDesactivadoActivity.class);
                    // intent.putExtra("usuario", adminData[0]);
                    startActivity(intent);
                }
            });

            linearLayoutAdminsContainer.addView(adminItemView);
        }

        // --- Resto de tu código (BottomNavigationView, Card click, Button click) ---

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperListaAdminActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                return true; // Ya estamos en SuperListaAdminActivity
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperListaAdminActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        Button botonRegistrar = findViewById(R.id.button_regist);
        botonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminFormularioActivity.class);
                startActivity(intent);
            }
        });
    }
}