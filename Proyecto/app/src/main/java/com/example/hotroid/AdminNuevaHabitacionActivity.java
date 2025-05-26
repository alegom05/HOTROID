package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminNuevaHabitacionActivity extends AppCompatActivity {
    private EditText etNumeroHabitacion, etAdultos, etNinos, etArea;
    private TextView tvTipoHabitacion;
    private String[] tipos = {"Standard", "Económica", "Lux"};
    private int tipoSeleccionado = -1;
    private boolean modoEdicion = true;

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
        // Inicializar los campos
        etNumeroHabitacion = findViewById(R.id.etNumeroHabitacion);
        etAdultos = findViewById(R.id.etAdultos);
        etNinos = findViewById(R.id.etNinos);
        etArea = findViewById(R.id.etArea);
        tvTipoHabitacion = findViewById(R.id.tvTipoHabitacion);
        tvTipoHabitacion.setOnClickListener(v -> {
            if (modoEdicion) {
                mostrarDialogoSeleccion();
            }
        });


        // Acción del botón Guardar
        findViewById(R.id.btnGuardarHabitacion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String numeroHabitacion = etNumeroHabitacion.getText().toString();
                String tipoHabitacion = tvTipoHabitacion.getText().toString();
                String adultos = etAdultos.getText().toString();
                String ninos = etNinos.getText().toString();
                String area = etArea.getText().toString();

                if (numeroHabitacion.isEmpty() || adultos.isEmpty() || ninos.isEmpty() || area.isEmpty()) {
                    Toast.makeText(AdminNuevaHabitacionActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("roomNumber", numeroHabitacion);
                    resultIntent.putExtra("roomType", tipoHabitacion);
                    resultIntent.putExtra("capacityAdults", adultos);
                    resultIntent.putExtra("capacityChildren", ninos);
                    resultIntent.putExtra("area", area);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminNuevaHabitacionActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminNuevaHabitacionActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminNuevaHabitacionActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminNuevaHabitacionActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige el tipo de habitación");

        // Mostrar el diálogo como una opción única (solo puede seleccionar una opción)
        builder.setSingleChoiceItems(tipos, tipoSeleccionado, (dialog, which) -> {
            tipoSeleccionado = which; // Guardar el índice de la opción seleccionada
        });

        builder.setPositiveButton("Aceptar", (dialog, id) -> {
            if (tipoSeleccionado == -1) {
                tvTipoHabitacion.setText("Selecciona el tipo de habitación");
            } else {
                tvTipoHabitacion.setText(tipos[tipoSeleccionado]); // Mostrar la opción seleccionada
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}