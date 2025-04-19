package com.example.hotroid;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminNuevoServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_nuevo_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);


        // Acción del botón Guardar
        findViewById(R.id.btnGuardarHabitacion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar la lógica para guardar los datos
                String nombreServicio = etNombreServicio.getText().toString();
                String descripcion = etDescripcion.getText().toString();
                String precio = etPrecio.getText().toString();

                // Validación simple
                if (nombreServicio.isEmpty() || descripcion.isEmpty() || precio.isEmpty()) {
                    Toast.makeText(AdminNuevoServicioActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                } else {
                    // Aquí puedes guardar los datos en una base de datos o realizar la acción correspondiente
                    Toast.makeText(AdminNuevoServicioActivity.this, "Servicio registrado con éxito.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}