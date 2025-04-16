package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminCuentaActivity.class);
            startActivity(intent);
        });
        // Ejemplo para un tipo
        CardView cardUbicacion = findViewById(R.id.cardUbicacion); // Asegúrate que este ID coincida con tu XML
        cardUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, UbicacionActivity.class);
                startActivity(intent);
            }
        });
        CardView cardFotos = findViewById(R.id.cardFotos);
        cardFotos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminFotosActivity.class);
            startActivity(intent);
        });

    }
}
