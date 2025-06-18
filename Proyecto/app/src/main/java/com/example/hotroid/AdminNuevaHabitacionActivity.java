package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.bean.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminNuevaHabitacionActivity extends AppCompatActivity {
    private EditText etNumeroHabitacion, etAdultos, etNinos, etArea;
    private TextView tvTipoHabitacion;
    // ¡CORRECCIÓN AQUÍ! Tipos de habitación según lo especificado
    private String[] tipos = {"Doble Superior", "Suite Lujo", "Estándar"};
    private int tipoSeleccionado = -1; // Para mantener el índice de la opción seleccionada

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_nueva_habitacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        etNumeroHabitacion = findViewById(R.id.etNumeroHabitacion);
        etAdultos = findViewById(R.id.idAdultos); // Asegúrate de que este ID coincida con tu XML
        etNinos = findViewById(R.id.idNinos);     // Asegúrate de que este ID coincida con tu XML
        etArea = findViewById(R.id.idArea);       // Asegúrate de que este ID coincida con tu XML
        tvTipoHabitacion = findViewById(R.id.tvTipoHabitacion); // TextView para el tipo
        Button btnGuardarHabitacion = findViewById(R.id.btnGuardarHabitacion);

        // Configurar listener para el TextView que actúa como selector de tipo de habitación
        tvTipoHabitacion.setOnClickListener(v -> mostrarDialogoSeleccion());

        btnGuardarHabitacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroHabitacion = etNumeroHabitacion.getText().toString().trim();
                String tipoHabitacion = tvTipoHabitacion.getText().toString().trim(); // Obtener del TextView
                String adultosStr = etAdultos.getText().toString().trim();
                String ninosStr = etNinos.getText().toString().trim();
                String areaStr = etArea.getText().toString().trim();

                // Validaciones
                if (numeroHabitacion.isEmpty() || adultosStr.isEmpty() || ninosStr.isEmpty() || areaStr.isEmpty()) {
                    Toast.makeText(AdminNuevaHabitacionActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tipoSeleccionado == -1 || tipoHabitacion.equals("Selecciona el tipo de habitación")) {
                    Toast.makeText(AdminNuevaHabitacionActivity.this, "Por favor, seleccione el tipo de habitación.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int adultos = Integer.parseInt(adultosStr);
                    int ninos = Integer.parseInt(ninosStr);
                    double area = Double.parseDouble(areaStr);

                    // Creamos la nueva habitación con estado "Available" por defecto
                    // El primer argumento es null porque Firestore generará el ID automáticamente
                    Room newRoom = new Room(null, numeroHabitacion, tipoHabitacion, adultos, ninos, area);
                    newRoom.setStatus("Available"); // Estado inicial

                    db.collection("habitaciones")
                            .add(newRoom)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(AdminNuevaHabitacionActivity.this, "Habitación registrada correctamente", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(AdminNuevaHabitacionActivity.this, AdminHabitacionesActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AdminNuevaHabitacionActivity.this, "Error al registrar habitación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                Log.e("AdminNuevaHabitacion", "Error adding document", e);
                            });

                } catch (NumberFormatException e) {
                    Toast.makeText(AdminNuevaHabitacionActivity.this, "Por favor, ingrese números válidos para capacidad y área.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminNuevaHabitacion", "Error parsing number", e);
                }
            }
        });

        // Configuración de la barra de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentRegistros = new Intent(AdminNuevaHabitacionActivity.this, AdminHabitacionesActivity.class);
                    intentRegistros.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentRegistros);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminNuevaHabitacionActivity.this, AdminTaxistas.class);
                    intentUbicacion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    Intent intentCheckout = new Intent(AdminNuevaHabitacionActivity.this, AdminCheckout.class);
                    intentCheckout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCheckout);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentReportes = new Intent(AdminNuevaHabitacionActivity.this, AdminReportes.class);
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

        builder.setSingleChoiceItems(tipos, tipoSeleccionado, (dialog, which) -> {
            tipoSeleccionado = which;
        });

        builder.setPositiveButton("Aceptar", (dialog, id) -> {
            if (tipoSeleccionado == -1) {
                tvTipoHabitacion.setText("Selecciona el tipo de habitación");
            } else {
                tvTipoHabitacion.setText(tipos[tipoSeleccionado]);
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}