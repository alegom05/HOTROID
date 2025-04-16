package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class UbicacionActivity extends AppCompatActivity {

    private TextView tvSeleccionLugares;
    private AutoCompleteTextView autoCompleteDireccion;
    private Button btnGuardarActualizar;
    private String[] lugares = {"Museo Larco", "Circuito de agua", "Parque del amor"};
    private boolean[] seleccionados = new boolean[lugares.length];
    private List<String> lugaresElegidos = new ArrayList<>();

    private boolean modoEdicion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ubicacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(UbicacionActivity.this, AdminCuentaActivity.class);
            startActivity(intent);
        });
        // Referencias
        autoCompleteDireccion = findViewById(R.id.autoCompleteDireccion);
        tvSeleccionLugares = findViewById(R.id.tvSeleccionLugares);
        btnGuardarActualizar = findViewById(R.id.btnGuardarUbicacion);

        // Selección de lugares turísticos
        tvSeleccionLugares.setOnClickListener(v -> {
            if (modoEdicion) {
                mostrarDialogoSeleccion();
            }
        });

        // Acción del botón "Guardar"
        btnGuardarActualizar.setOnClickListener(v -> {
            if (modoEdicion) {
                if (autoCompleteDireccion.getText().toString().isEmpty() || lugaresElegidos.isEmpty()) {
                    Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                } else {
                    // Mostrar en modo solo lectura
                    autoCompleteDireccion.setEnabled(false);
                    tvSeleccionLugares.setClickable(false);
                    tvSeleccionLugares.setFocusable(false);
                    btnGuardarActualizar.setText("Actualizar");
                    modoEdicion = false;
                }
            } else {
                // Volver a modo edición
                autoCompleteDireccion.setEnabled(true);
                tvSeleccionLugares.setClickable(true);
                tvSeleccionLugares.setFocusable(true);
                btnGuardarActualizar.setText("Guardar");
                modoEdicion = true;
            }
        });
    }
    private void mostrarDialogoSeleccion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Elige lugares turísticos");

        builder.setMultiChoiceItems(lugares, seleccionados, (dialog, indexSelected, isChecked) -> {
            if (isChecked) {
                lugaresElegidos.add(lugares[indexSelected]);
            } else {
                lugaresElegidos.remove(lugares[indexSelected]);
            }
        });

        builder.setPositiveButton("Aceptar", (dialog, id) -> {
            if (lugaresElegidos.isEmpty()) {
                tvSeleccionLugares.setText("Selecciona lugares turísticos");
            } else {
                tvSeleccionLugares.setText(String.join(", ", lugaresElegidos));
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

}