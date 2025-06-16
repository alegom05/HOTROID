package com.example.hotroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SuperDetallesClienteActivity extends AppCompatActivity {

    private static final String TAG = "SuperDetallesClienteAct";

    private TextView tvTituloDetalleCliente;
    private TextView tvNombresDetalle, tvApellidosDetalle, tvTipoDocumentoDetalle, tvNumeroDocumentoDetalle,
            tvFechaNacimientoDetalle, tvCorreoDetalle, tvTelefonoDetalle, tvDomicilioDetalle;

    private ImageView fotoPrincipal;
    private Button btnActivar, btnDesactivar;

    private FirebaseFirestore db;
    private String clientFirestoreId;
    private String currentClientStatus;
    private String clientNombres, clientApellidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_cliente);

        db = FirebaseFirestore.getInstance();

        // Inicializar vistas de detalles del cliente
        tvTituloDetalleCliente = findViewById(R.id.tvTituloDetalleCliente);
        tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        tvNumeroDocumentoDetalle = findViewById(R.id.tvNumeroDocumentoDetalle);
        tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);

        // Inicializar ImageView y Botones de acción
        fotoPrincipal = findViewById(R.id.fotoPrincipal);
        btnActivar = findViewById(R.id.btnActivar);
        btnDesactivar = findViewById(R.id.btnDesactivar);

        // Obtener datos del intent
        Intent intent = getIntent();
        if (intent != null) {
            clientFirestoreId = intent.getStringExtra("client_firestore_id");
            clientNombres = intent.getStringExtra("client_nombres");
            clientApellidos = intent.getStringExtra("client_apellidos");
            currentClientStatus = intent.getStringExtra("client_estado");
            String tipoDocumento = intent.getStringExtra("client_tipo_documento");
            String numeroDocumento = intent.getStringExtra("client_numero_documento");
            String nacimiento = intent.getStringExtra("client_nacimiento");
            String correo = intent.getStringExtra("client_correo");
            String telefono = intent.getStringExtra("client_telefono");
            String direccion = intent.getStringExtra("client_direccion");
            String fotoPerfilUrl = intent.getStringExtra("client_fotoPerfilUrl");

            // Rellenar los TextViews de detalles del cliente
            tvNombresDetalle.setText(clientNombres != null ? clientNombres : "-");
            tvApellidosDetalle.setText(clientApellidos != null ? clientApellidos : "-");
            tvTipoDocumentoDetalle.setText(tipoDocumento != null ? tipoDocumento : "-");
            tvNumeroDocumentoDetalle.setText(numeroDocumento != null ? numeroDocumento : "-");
            tvFechaNacimientoDetalle.setText(nacimiento != null ? nacimiento : "-");
            tvCorreoDetalle.setText(correo != null ? correo : "-");
            tvTelefonoDetalle.setText(telefono != null ? telefono : "-");
            tvDomicilioDetalle.setText(direccion != null ? direccion : "-");

            // Actualizar la UI de los botones basada en el estado
            updateClientStatusUI(currentClientStatus);

            // Cargar la foto de perfil usando Glide
            if (fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
                Glide.with(this)
                        .load(fotoPerfilUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_placeholder)
                        .into(fotoPrincipal);
            } else {
                fotoPrincipal.setImageResource(R.drawable.ic_user_placeholder);
            }
        }

        // Modificación: Agregar diálogo de confirmación para los botones
        btnActivar.setOnClickListener(v ->
                showConfirmationDialog("Activar Cliente",
                        "¿Estás seguro de que quieres activar a " + clientNombres + " " + clientApellidos + "?",
                        "true", "activado")
        );

        btnDesactivar.setOnClickListener(v ->
                showConfirmationDialog("Desactivar Cliente",
                        "¿Estás seguro de que quieres desactivar a " + clientNombres + " " + clientApellidos + "?",
                        "false", "desactivado")
        );
    }

    /**
     * Muestra un diálogo de confirmación antes de cambiar el estado del cliente.
     * @param title Título del diálogo.
     * @param message Mensaje a mostrar.
     * @param newStatus El nuevo estado ("true" para activo, "false" para inactivo).
     * @param actionType El tipo de acción ("activado" o "desactivado") para pasar al intent de resultado.
     */
    private void showConfirmationDialog(String title, String message, String newStatus, String actionType) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Si el usuario confirma, proceder con la actualización del estado
                    updateClientStatus(newStatus, actionType);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Si el usuario cancela, no hacer nada y quedarse en la vista actual
                    dialog.dismiss();
                })
                .show();
    }

    private void updateClientStatusUI(String status) {
        if (btnActivar == null || btnDesactivar == null) {
            Log.e(TAG, "Los botones Activar/Desactivar no se encontraron en el layout.");
            return;
        }

        boolean isActivado = Boolean.parseBoolean(status);
        if (isActivado) {
            btnActivar.setVisibility(View.GONE);
            btnDesactivar.setVisibility(View.VISIBLE);
        } else {
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
                    updateClientStatusUI(currentClientStatus); // Actualizar la visibilidad de los botones

                    // Preparar resultado y cerrar esta actividad para volver a la lista
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("action", actionType);
                    resultIntent.putExtra("client_firestore_id", clientFirestoreId);
                    resultIntent.putExtra("client_nombres", clientNombres);
                    resultIntent.putExtra("client_apellidos", clientApellidos);
                    setResult(RESULT_OK, resultIntent); // Establecer el resultado antes de finalizar
                    finish(); // Cierra esta actividad y regresa a la anterior (la lista de clientes)
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SuperDetallesClienteActivity.this, "Error al actualizar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al actualizar estado del cliente " + clientFirestoreId, e);
                });
    }

    @Override
    public void onBackPressed() {
        // Al presionar el botón de atrás, también se debería regresar a la lista
        // Puedes establecer un RESULT_CANCELED si no se realizó ninguna acción,
        // o simplemente llamar a super.onBackPressed() que ya cierra la actividad.
        setResult(RESULT_CANCELED); // Opcional: indicar que no hubo cambios
        super.onBackPressed();
    }
}