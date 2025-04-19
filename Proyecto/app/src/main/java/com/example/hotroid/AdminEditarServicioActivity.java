package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminEditarServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);

        // Obtener los datos de la habitación pasados a través del Intent
        String ServicioNombre = getIntent().getStringExtra("Service_name");
        String Descripcion = getIntent().getStringExtra("Service_description");
        String precioValue = getIntent().getStringExtra("price");

        // Establecer los datos en los campos EditText
        etNombreServicio.setText(ServicioNombre);
        etDescripcion.setText(Descripcion);
        etPrecio.setText(precioValue);

        // Acción para guardar los cambios
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para guardar los datos actualizados
            String NombreServicio = etNombreServicio.getText().toString();
            String DescripcionServicio = etDescripcion.getText().toString();
            String precio = etPrecio.getText().toString();

            // Validación simple
            if (NombreServicio.isEmpty() || DescripcionServicio.isEmpty() || precio.isEmpty()) {
                Toast.makeText(AdminEditarServicioActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí puedes guardar los datos actualizados en una base de datos o realizar la acción correspondiente
                Toast.makeText(AdminEditarServicioActivity.this, "Servicio actualizado con éxito.", Toast.LENGTH_SHORT).show();
                // Redirigir a la actividad AdminHabitacionesActivity después de guardar
                Intent intent = new Intent(AdminEditarServicioActivity.this, AdminServiciosActivity.class);
                startActivity(intent);
                finish();  // Finaliza la actividad actual para evitar que el usuario regrese a ella con el botón de atrás
            }
        });
    }
}