package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminServiciosDetallesActivity extends AppCompatActivity {
    private TextView tvName, tvDescripcion, tvPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_servicios_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvName = findViewById(R.id.tvName);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvPrecio = findViewById(R.id.tvPrecio);


        // Obtener los datos de la habitación pasados a través del Intent
        String servicioNombre = getIntent().getStringExtra("Service_name");
        String servicioDescripcion = getIntent().getStringExtra("Service_description");
        String precio = getIntent().getStringExtra("price");

        // Establecer los datos en los TextViews
        tvName.setText("Nombre: " + servicioNombre);
        tvDescripcion.setText("Descripcion: " + servicioDescripcion);
        tvPrecio.setText("Precio (S/.): " + precio);

        // Acción para editar la habitación
        findViewById(R.id.btnEditarHabitacion).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosDetallesActivity.this, AdminEditarServicioActivity.class);
            intent.putExtra("Service_name", servicioNombre);
            intent.putExtra("Service_description", servicioDescripcion);
            intent.putExtra("price", precio);
            startActivity(intent);
        });
    }
}