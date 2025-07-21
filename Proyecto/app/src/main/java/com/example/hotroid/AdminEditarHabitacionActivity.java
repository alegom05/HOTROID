package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView; // Importar TextView

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // Importar AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class AdminEditarHabitacionActivity extends AppCompatActivity {
    private EditText etNumeroHabitacion, etAdultos, etNinos, etArea;
    private TextView tvTipoHabitacion; // CAMBIADO A TextView
    private Button btnGuardarCambios;
    private String roomId;
    private FirebaseFirestore db;

    // Tipos de habitación para el selector
    // ¡CORRECCIÓN AQUÍ! Tipos de habitación según lo especificado
    private String[] tipos = {"Doble Superior", "Suite Lujo", "Estándar"};
    private int tipoSeleccionado = -1; // Para mantener el índice de la opción seleccionada
    private String currentStatus; // Para almacenar el estado actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_habitacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        etNumeroHabitacion = findViewById(R.id.etNumeroHabitacion);
        tvTipoHabitacion = findViewById(R.id.tvTipoHabitacion); // Inicializar tvTipoHabitacion
        etAdultos = findViewById(R.id.etAdultos);
        etNinos = findViewById(R.id.etNinos);
        etArea = findViewById(R.id.etArea);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        // Obtener los datos de la habitación pasados a través del Intent
        roomId = getIntent().getStringExtra("ROOM_ID");
        int roomNumber = getIntent().getIntExtra("ROOM_NUMBER", 0);

        String roomType = getIntent().getStringExtra("ROOM_TYPE");
        int capacityAdults = getIntent().getIntExtra("CAPACITY_ADULTS", 0);
        int capacityChildren = getIntent().getIntExtra("CAPACITY_CHILDREN", 0);
        double areaValue = getIntent().getDoubleExtra("AREA", 0.0);
        currentStatus = getIntent().getStringExtra("STATUS"); // Recibir el estado

        // Establecer los datos en los campos de vista
        etNumeroHabitacion.setText(String.valueOf(roomNumber));
        tvTipoHabitacion.setText(roomType); // Establecer el tipo de habitación actual en el TextView
        // Encontrar el índice del tipo de habitación actual para preseleccionar en el diálogo
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i].equals(roomType)) {
                tipoSeleccionado = i;
                break;
            }
        }

        etAdultos.setText(String.valueOf(capacityAdults));
        etNinos.setText(String.valueOf(capacityChildren));
        etArea.setText(String.format(Locale.getDefault(), "%.2f", areaValue));

        // Listener para el TextView de tipo de habitación: muestra el diálogo de selección
        tvTipoHabitacion.setOnClickListener(v -> mostrarDialogoSeleccion());

        btnGuardarCambios.setOnClickListener(v -> {
            String numeroHabitacion = etNumeroHabitacion.getText().toString().trim();
            String tipoHabitacion = tvTipoHabitacion.getText().toString().trim(); // Obtener del TextView
            String adultosStr = etAdultos.getText().toString().trim();
            String ninosStr = etNinos.getText().toString().trim();
            String areaStr = etArea.getText().toString().trim();

            // Validación de campo tipo de habitación
            if (tipoSeleccionado == -1 || tipoHabitacion.equals("Selecciona el tipo de habitación")) {
                Toast.makeText(AdminEditarHabitacionActivity.this, "Por favor, seleccione el tipo de habitación.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (numeroHabitacion.isEmpty() || adultosStr.isEmpty() || ninosStr.isEmpty() || areaStr.isEmpty()) {
                Toast.makeText(AdminEditarHabitacionActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    int adultos = Integer.parseInt(adultosStr);
                    int ninos = Integer.parseInt(ninosStr);
                    double area = Double.parseDouble(areaStr);

                    if (roomId != null && !roomId.isEmpty()) {
                        db.collection("habitaciones").document(roomId)
                                .update(
                                        "roomNumber", Integer.parseInt(numeroHabitacion),
                                        "roomType", tipoHabitacion, // Guardar el valor del TextView
                                        "capacityAdults", adultos,
                                        "capacityChildren", ninos,
                                        "area", area,
                                        "status", currentStatus // Mantenemos el status que traemos
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AdminEditarHabitacionActivity.this, "Habitación actualizada con éxito.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AdminEditarHabitacionActivity.this, AdminHabitacionesActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AdminEditarHabitacionActivity.this, "Error al actualizar habitación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    Log.e("AdminEditarHabitacion", "Error updating document", e);
                                });
                    } else {
                        Toast.makeText(AdminEditarHabitacionActivity.this, "Error: ID de habitación no encontrado para actualizar.", Toast.LENGTH_SHORT).show();
                        Log.e("AdminEditarHabitacion", "ROOM_ID is null or empty, cannot update.");
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(AdminEditarHabitacionActivity.this, "Por favor, ingrese números válidos para capacidad y área.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminEditarHabitacion", "Error parsing number", e);
                }
            }
        });

        // Configuración de la barra de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentRegistros = new Intent(AdminEditarHabitacionActivity.this, AdminActivity.class);
                    intentRegistros.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentRegistros);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminEditarHabitacionActivity.this, AdminTaxistas.class);
                    intentUbicacion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    Intent intentCheckout = new Intent(AdminEditarHabitacionActivity.this, AdminCheckout.class);
                    intentCheckout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCheckout);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentReportes = new Intent(AdminEditarHabitacionActivity.this, AdminReportes.class);
                    intentReportes.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentReportes);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    // Método para mostrar el diálogo de selección de tipo de habitación
    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige el tipo de habitación");

        // Mostrar el diálogo como una opción única (solo puede seleccionar una opción)
        builder.setSingleChoiceItems(tipos, tipoSeleccionado, (dialog, which) -> {
            tipoSeleccionado = which; // Guardar el índice de la opción seleccionada
        });

        builder.setPositiveButton("Aceptar", (dialog, id) -> {
            if (tipoSeleccionado == -1) {
                // Esto puede ocurrir si no había una selección previa o si se desmarcó accidentalmente
                tvTipoHabitacion.setText("Selecciona el tipo de habitación");
            } else {
                tvTipoHabitacion.setText(tipos[tipoSeleccionado]); // Mostrar la opción seleccionada
            }
        });

        builder.setNegativeButton("Cancelar", null); // No hace nada al cancelar
        builder.show();
    }
}