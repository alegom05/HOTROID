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

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminEditarHabitacionActivity extends AppCompatActivity {
    private EditText etNumeroHabitacion, etTipoHabitacion, etAdultos, etNinos, etArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_habitacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos
        etNumeroHabitacion = findViewById(R.id.etNumeroHabitacion);
        etTipoHabitacion = findViewById(R.id.etTipoHabitacion);
        etAdultos = findViewById(R.id.etAdultos);
        etNinos = findViewById(R.id.etNinos);
        etArea = findViewById(R.id.etArea);

        // Obtener los datos de la habitación pasados a través del Intent
        String roomNumber = getIntent().getStringExtra("ROOM_NUMBER");
        String roomType = getIntent().getStringExtra("ROOM_TYPE");
        String capacity = getIntent().getStringExtra("CAPACITY");
        String areaValue = getIntent().getStringExtra("AREA");

        // Establecer los datos en los campos EditText
        etNumeroHabitacion.setText(roomNumber);
        etTipoHabitacion.setText(roomType);
        etAdultos.setText(capacity.split(",")[0].trim().split(" ")[0]);  // Extraer solo el número de adultos
        etNinos.setText(capacity.split(",")[1].trim().split(" ")[0]);    // Extraer solo el número de niños
        etArea.setText(areaValue);

        // Acción para guardar los cambios
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para guardar los datos actualizados
            String numeroHabitacion = etNumeroHabitacion.getText().toString();
            String tipoHabitacion = etTipoHabitacion.getText().toString();
            String adultos = etAdultos.getText().toString();
            String ninos = etNinos.getText().toString();
            String area = etArea.getText().toString();

            // Validación simple
            if (numeroHabitacion.isEmpty() || adultos.isEmpty() || ninos.isEmpty() || area.isEmpty()) {
                Toast.makeText(AdminEditarHabitacionActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí puedes guardar los datos actualizados en una base de datos o realizar la acción correspondiente
                Toast.makeText(AdminEditarHabitacionActivity.this, "Habitación actualizada con éxito.", Toast.LENGTH_SHORT).show();
                // Redirigir a la actividad AdminHabitacionesActivity después de guardar
                Intent intent = new Intent(AdminEditarHabitacionActivity.this, AdminHabitacionesActivity.class);
                startActivity(intent);
                finish();  // Finaliza la actividad actual para evitar que el usuario regrese a ella con el botón de atrás
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminEditarHabitacionActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminEditarHabitacionActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminEditarHabitacionActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminEditarHabitacionActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
}