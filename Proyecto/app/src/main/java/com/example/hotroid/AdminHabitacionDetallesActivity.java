package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminHabitacionDetallesActivity extends AppCompatActivity {
    private TextView tvRoomNumber, tvRoomType, tvCapacity, tvArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_habitacion_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos de texto
        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvArea = findViewById(R.id.tvArea);

        // Obtener los datos de la habitación pasados a través del Intent
        String roomNumber = getIntent().getStringExtra("ROOM_NUMBER");
        String roomType = getIntent().getStringExtra("ROOM_TYPE");
        String capacity = getIntent().getStringExtra("CAPACITY");
        String area = getIntent().getStringExtra("AREA");

        // Establecer los datos en los TextViews
        tvRoomNumber.setText("Número: " + roomNumber);
        tvRoomType.setText("Tipo: " + roomType);
        tvCapacity.setText("Capacidad: " + capacity);
        tvArea.setText("Área(m²): " + area);

        // Acción para editar la habitación
        findViewById(R.id.btnEditarHabitacion).setOnClickListener(v -> {
            Intent intent = new Intent(AdminHabitacionDetallesActivity.this, AdminEditarHabitacionActivity.class);
            intent.putExtra("ROOM_NUMBER", roomNumber);
            intent.putExtra("ROOM_TYPE", roomType);
            intent.putExtra("CAPACITY", capacity);
            intent.putExtra("AREA", area);
            startActivity(intent);
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminHabitacionDetallesActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminHabitacionDetallesActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminHabitacionDetallesActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminHabitacionDetallesActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
}