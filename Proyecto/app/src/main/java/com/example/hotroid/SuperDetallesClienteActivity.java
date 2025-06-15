package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SuperDetallesClienteActivity extends AppCompatActivity {

    private static final String TAG = "SuperDetallesClienteAct";

    // Reemplazados los EditText por TextViews para el modo solo lectura
    private TextView tvTituloDetalleCliente; // Nuevo ID
    private TextView tvNombresDetalle, tvApellidosDetalle, tvDniDetalle, tvFechaNacimientoDetalle, tvCorreoDetalle, tvTelefonoDetalle, tvDomicilioDetalle, tvEstadoDetalle;
    private ImageView fotoPrincipal; // ImageView para la foto de perfil
    private Button btnActivar, btnDesactivar;

    private FirebaseFirestore db;
    private String clientFirestoreId;
    private String currentClientStatus;
    private String clientNombres, clientApellidos; // Para pasar de vuelta a la lista

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_cliente);

        db = FirebaseFirestore.getInstance();

        // Inicializar vistas con los IDs del nuevo layout
        tvTituloDetalleCliente = findViewById(R.id.tvTituloDetalleCliente); // El nuevo título
        tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        tvDniDetalle = findViewById(R.id.tvDniDetalle);
        tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        tvEstadoDetalle = findViewById(R.id.tvEstadoDetalle); // El estado del cliente

        fotoPrincipal = findViewById(R.id.fotoPrincipal); // La ImageView para la foto de perfil

        btnActivar = findViewById(R.id.btnActivar);
        btnDesactivar = findViewById(R.id.btnDesactivar);

        // Obtener datos del intent
        Intent intent = getIntent();
        if (intent != null) {
            clientFirestoreId = intent.getStringExtra("client_firestore_id");
            clientNombres = intent.getStringExtra("client_nombres");
            clientApellidos = intent.getStringExtra("client_apellidos");
            currentClientStatus = intent.getStringExtra("client_estado");
            String dni = intent.getStringExtra("client_dni");
            String nacimiento = intent.getStringExtra("client_nacimiento");
            String correo = intent.getStringExtra("client_correo");
            String telefono = intent.getStringExtra("client_telefono");
            String direccion = intent.getStringExtra("client_direccion");
            String fotoPerfilUrl = intent.getStringExtra("client_fotoPerfilUrl");

            // Rellenar los TextViews
            tvNombresDetalle.setText(clientNombres);
            tvApellidosDetalle.setText(clientApellidos);
            tvDniDetalle.setText(dni);
            tvFechaNacimientoDetalle.setText(nacimiento);
            tvCorreoDetalle.setText(correo);
            tvTelefonoDetalle.setText(telefono);
            tvDomicilioDetalle.setText(direccion);

            // Actualizar la UI del estado y visibilidad de botones
            updateClientStatusUI(currentClientStatus);

            // Cargar la foto de perfil usando Glide
            if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
                Glide.with(this)
                        .load(fotoPerfilUrl)
                        .placeholder(R.drawable.ic_user_placeholder) // Asegúrate de que exista
                        .error(R.drawable.ic_user_placeholder) // Asegúrate de que exista
                        .into(fotoPrincipal);
            } else {
                fotoPrincipal.setImageResource(R.drawable.ic_user_placeholder); // Default si no hay URL
            }
        }

        // Listeners para los botones de activación/desactivación
        btnActivar.setOnClickListener(v -> updateClientStatus("true", "activado"));
        btnDesactivar.setOnClickListener(v -> updateClientStatus("false", "desactivado"));

        // No hay un botón de "atrás" explícito en el diseño del admin que proporcionaste,
        // así que confiaremos en el botón de "atrás" del sistema.
    }

    private void updateClientStatusUI(String status) {
        boolean isActivado = Boolean.parseBoolean(status);
        if (isActivado) {
            tvEstadoDetalle.setText("Activo");
            tvEstadoDetalle.setTextColor(ContextCompat.getColor(this, R.color.green_status));
            btnActivar.setVisibility(View.GONE);
            btnDesactivar.setVisibility(View.VISIBLE);
        } else {
            tvEstadoDetalle.setText("Inactivo");
            tvEstadoDetalle.setTextColor(ContextCompat.getColor(this, R.color.red_status));
            btnActivar.setVisibility(View.VISIBLE);
            btnDesactivar.setVisibility(View.GONE);
        }
    }

    private void updateClientStatus(String newStatus, String actionType) {
        if (clientFirestoreId == null || clientFirestoreId.isEmpty()) {
            Toast.makeText(this, "Error: ID de cliente no disponible.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", newStatus);

        db.collection("clientes").document(clientFirestoreId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SuperDetallesClienteActivity.this, "Estado del cliente actualizado.", Toast.LENGTH_SHORT).show();
                    currentClientStatus = newStatus; // Actualizar el estado local
                    updateClientStatusUI(currentClientStatus); // Actualizar la UI

                    // Preparar resultado para SuperListaClientesActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("action", actionType);
                    resultIntent.putExtra("client_firestore_id", clientFirestoreId);
                    resultIntent.putExtra("client_nombres", clientNombres);
                    resultIntent.putExtra("client_apellidos", clientApellidos);
                    setResult(RESULT_OK, resultIntent);
                    finish(); // Regresar a la lista
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SuperDetallesClienteActivity.this, "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar estado del cliente " + clientFirestoreId, e);
                });
    }

    @Override
    public void onBackPressed() {
        // Al igual que en el admin, confiamos en el botón de atrás del sistema
        // y el setResult ya se manejó en updateClientStatus si hubo cambios.
        super.onBackPressed();
    }
}