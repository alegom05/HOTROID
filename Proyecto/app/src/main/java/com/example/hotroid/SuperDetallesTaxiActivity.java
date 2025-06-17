package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Taxista;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperDetallesTaxiActivity extends AppCompatActivity {

    private static final String TAG = "SuperDetallesTaxi";

    private TextView tvNombresDetalle, tvApellidosDetalle, tvTipoDocumentoDetalle, tvNumDocumentoDetalle, tvFechaNacimientoDetalle,
            tvCorreoDetalle, tvTelefonoDetalle, tvDomicilioDetalle, tvPlacaVehiculoDetalle;

    private ImageView fotoPrincipal, fotoVehiculo;

    private Button btnAprobar, btnRechazar, btnDesactivar, btnActivar;

    private Taxista currentTaxista;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_taxi);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        tvNumDocumentoDetalle = findViewById(R.id.tvNumDocumentoDetalle);
        tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        tvPlacaVehiculoDetalle = findViewById(R.id.tvPlacaVehiculoDetalle);

        fotoPrincipal = findViewById(R.id.fotoPrincipal);
        fotoVehiculo = findViewById(R.id.fotoVehiculo);

        btnAprobar = findViewById(R.id.btnAprobar);
        btnRechazar = findViewById(R.id.btnRechazar);
        btnDesactivar = findViewById(R.id.btnDesactivar);
        btnActivar = findViewById(R.id.btnActivar);

        Intent intent = getIntent();
        if (intent != null) {
            String firestoreId = intent.getStringExtra("taxista_firestore_id");
            String nombres = intent.getStringExtra("taxista_nombres");
            String apellidos = intent.getStringExtra("taxista_apellidos");
            String tipoDocumento = intent.getStringExtra("taxista_tipo_documento");
            String numeroDocumento = intent.getStringExtra("taxista_numero_documento");
            String nacimiento = intent.getStringExtra("taxista_nacimiento");
            String correo = intent.getStringExtra("taxista_correo");
            String telefono = intent.getStringExtra("taxista_telefono");
            String direccion = intent.getStringExtra("taxista_direccion");
            String fotoPerfilUrl = intent.getStringExtra("taxista_foto_perfil_url");
            String placa = intent.getStringExtra("taxista_placa");
            String fotoVehiculoUrl = intent.getStringExtra("taxista_foto_vehiculo_url");
            String estado = intent.getStringExtra("taxista_estado");
            String estadoDeViaje = intent.getStringExtra("taxista_estado_viaje");

            Log.d(TAG, "Recibido firestoreId: " + firestoreId);

            if (firestoreId != null && !firestoreId.isEmpty()) {
                currentTaxista = new Taxista(
                        nombres, apellidos, tipoDocumento, numeroDocumento, nacimiento,
                        correo, telefono, direccion, fotoPerfilUrl, placa, fotoVehiculoUrl,
                        estado, estadoDeViaje
                );
                currentTaxista.setFirestoreId(firestoreId);
                displayTaxistaDetails();
                setupButtons();
            } else {
                Toast.makeText(this, "Error: Datos de taxista no encontrados (ID nulo).", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Datos de taxista esenciales (firestoreId) son nulos o vacíos en el Intent.");
                finish();
                return;
            }
        } else {
            Toast.makeText(this, "Error: No se recibieron datos del taxista.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Intent nulo al iniciar SuperDetallesTaxiActivity.");
            finish();
        }
    }

    /**
     * Muestra los detalles del taxista en las vistas de la actividad.
     */
    private void displayTaxistaDetails() {
        if (currentTaxista == null) {
            Log.e(TAG, "currentTaxista es null al intentar mostrar detalles.");
            return;
        }

        tvNombresDetalle.setText(currentTaxista.getNombres());
        tvApellidosDetalle.setText(currentTaxista.getApellidos());
        tvTipoDocumentoDetalle.setText(currentTaxista.getTipoDocumento());
        tvNumDocumentoDetalle.setText(currentTaxista.getNumeroDocumento());
        tvFechaNacimientoDetalle.setText(currentTaxista.getNacimiento());
        tvCorreoDetalle.setText(currentTaxista.getCorreo());
        tvTelefonoDetalle.setText(currentTaxista.getTelefono());
        tvDomicilioDetalle.setText(currentTaxista.getDireccion());
        tvPlacaVehiculoDetalle.setText(currentTaxista.getPlaca());

        // Cargar imágenes con Glide
        if (currentTaxista.getFotoPerfilUrl() != null && !currentTaxista.getFotoPerfilUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentTaxista.getFotoPerfilUrl())
                    .placeholder(R.drawable.ic_user_placeholder)
                    .error(R.drawable.ic_user_placeholder)
                    .into(fotoPrincipal);
        } else {
            fotoPrincipal.setImageResource(R.drawable.ic_user_placeholder);
        }

        if (currentTaxista.getFotoVehiculoUrl() != null && !currentTaxista.getFotoVehiculoUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentTaxista.getFotoVehiculoUrl())
                    .placeholder(R.drawable.carrito)
                    .error(R.drawable.carrito)
                    .into(fotoVehiculo);
        } else {
            fotoVehiculo.setImageResource(R.drawable.carrito);
        }
    }

    private void setupButtons() {
        if (currentTaxista == null) return;

        // Ocultar todos los botones inicialmente
        btnAprobar.setVisibility(View.GONE);
        btnRechazar.setVisibility(View.GONE);
        btnDesactivar.setVisibility(View.GONE);
        btnActivar.setVisibility(View.GONE);

        // Mostrar botones según el estado actual del taxista
        String estado = currentTaxista.getEstado();
        if (estado != null) {
            switch (estado.toLowerCase(Locale.getDefault())) {
                case "activado":
                    btnDesactivar.setVisibility(View.VISIBLE);
                    btnDesactivar.setOnClickListener(v -> showConfirmationDialog("Desactivar Taxista", "¿Estás seguro de que deseas desactivar a este taxista?", "desactivado"));
                    break;
                case "desactivado":
                    btnActivar.setVisibility(View.VISIBLE);
                    btnActivar.setOnClickListener(v -> showConfirmationDialog("Activar Taxista", "¿Estás seguro de que deseas activar a este taxista?", "activado"));
                    break;
                case "pendiente":
                    btnAprobar.setVisibility(View.VISIBLE);
                    btnAprobar.setOnClickListener(v -> showConfirmationDialog("Aprobar Taxista", "¿Estás seguro de que deseas aprobar a este taxista?", "aprobado"));
                    btnRechazar.setVisibility(View.VISIBLE);
                    btnRechazar.setOnClickListener(v -> showConfirmationDialog("Rechazar Taxista", "¿Estás seguro de que deseas rechazar a este taxista? Se eliminará de la base de datos.", "rechazar"));
                    break;
            }
        }
    }

    /**
     * Muestra un diálogo de confirmación antes de realizar una acción.
     * @param title Título del diálogo.
     * @param message Mensaje a mostrar.
     * @param actionType El tipo de acción a realizar ("aprobar", "rechazar", "activar", "desactivar").
     */
    private void showConfirmationDialog(String title, String message, String actionType) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sí", (dialog, which) -> performAction(actionType))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Realiza la acción solicitada sobre el taxista (actualizar estado o eliminar).
     * @param actionType El tipo de acción a realizar.
     */
    private void performAction(String actionType) {
        if (currentTaxista == null || currentTaxista.getFirestoreId() == null) {
            Toast.makeText(this, "Error: No se pudo realizar la acción. Taxista no válido.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Cannot perform action: currentTaxista or firestoreId is null.");
            return;
        }

        String firestoreId = currentTaxista.getFirestoreId();
        Map<String, Object> updates = new HashMap<>();
        String toastMessage = "";
        String returnAction = "";

        switch (actionType) {
            case "aprobado":
                updates.put("estado", "activado");
                updates.put("estadoDeViaje", "No Asignado");
                toastMessage = "Taxista aprobado y activado.";
                returnAction = "aprobado";
                updateTaxistaInFirestore(firestoreId, updates, toastMessage, returnAction);
                break;
            case "rechazar":
                toastMessage = "Taxista rechazado y eliminado.";
                returnAction = "rechazado";
                deleteTaxistaFromFirestore(firestoreId, toastMessage, returnAction);
                break;
            case "activado":
                updates.put("estado", "activado");
                updates.put("estadoDeViaje", "No Asignado");
                toastMessage = "Taxista activado.";
                returnAction = "activado";
                updateTaxistaInFirestore(firestoreId, updates, toastMessage, returnAction);
                break;
            case "desactivado":
                updates.put("estado", "desactivado");
                updates.put("estadoDeViaje", "No Asignado");
                toastMessage = "Taxista desactivado.";
                returnAction = "desactivado";
                updateTaxistaInFirestore(firestoreId, updates, toastMessage, returnAction);
                break;
            default:
                Toast.makeText(this, "Acción desconocida.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * Actualiza el estado o datos del taxista en Firestore.
     */
    private void updateTaxistaInFirestore(String firestoreId, Map<String, Object> updates, String toastMessage, String returnAction) {
        db.collection("taxistas").document(firestoreId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                    // Actualiza el objeto currentTaxista localmente para reflejar los cambios
                    if (updates.containsKey("estado")) {
                        currentTaxista.setEstado((String) updates.get("estado"));
                    }
                    if (updates.containsKey("estadoDeViaje")) {
                        currentTaxista.setEstadoDeViaje((String) updates.get("estadoDeViaje"));
                    }
                    displayTaxistaDetails(); // Refresca la UI con los nuevos datos en esta actividad
                    setResultAndFinish(returnAction, firestoreId, currentTaxista.getNombres(), currentTaxista.getApellidos());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando taxista en Firestore: " + e.getMessage());
                    Toast.makeText(this, "Error al actualizar taxista.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Elimina taxista de Firestore y Storage (para rechazo).
     */
    private void deleteTaxistaFromFirestore(String firestoreId, String toastMessage, String returnAction) {
        db.collection("taxistas").document(firestoreId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Taxista taxiToDelete = documentSnapshot.toObject(Taxista.class);
                        if (taxiToDelete != null) {
                            List<Task<Void>> deleteImageTasks = new ArrayList<>();

                            if (taxiToDelete.getFotoPerfilUrl() != null && !taxiToDelete.getFotoPerfilUrl().isEmpty()) {
                                try {
                                    StorageReference profileRef = storage.getReferenceFromUrl(taxiToDelete.getFotoPerfilUrl());
                                    deleteImageTasks.add(profileRef.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "Foto de perfil eliminada de Storage."))
                                            .addOnFailureListener(e -> Log.w(TAG, "Error al eliminar foto de perfil de Storage: " + e.getMessage())));
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, "URL de foto de perfil inválida: " + taxiToDelete.getFotoPerfilUrl() + ". Saltando eliminación.");
                                }
                            }
                            if (taxiToDelete.getFotoVehiculoUrl() != null && !taxiToDelete.getFotoVehiculoUrl().isEmpty()) {
                                try {
                                    StorageReference vehicleRef = storage.getReferenceFromUrl(taxiToDelete.getFotoVehiculoUrl());
                                    deleteImageTasks.add(vehicleRef.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "Foto de vehículo eliminada de Storage."))
                                            .addOnFailureListener(e -> Log.w(TAG, "Error al eliminar foto de vehículo de Storage: " + e.getMessage())));
                                } catch (IllegalArgumentException e) {
                                    Log.e(TAG, "URL de foto de vehículo inválida: " + taxiToDelete.getFotoVehiculoUrl() + ". Saltando eliminación.");
                                }
                            }

                            Tasks.whenAll(deleteImageTasks)
                                    .addOnCompleteListener(task -> {
                                        // Después de intentar eliminar imágenes, eliminar el documento de Firestore
                                        db.collection("taxistas").document(firestoreId)
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                                                    setResultAndFinish(returnAction, firestoreId, currentTaxista.getNombres(), currentTaxista.getApellidos());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error al eliminar taxista de Firestore: " + e.getMessage());
                                                    Toast.makeText(this, "Error al eliminar taxista.", Toast.LENGTH_SHORT).show();
                                                });
                                    });
                        } else {
                            // Si el objeto Taxista no pudo ser mapeado, igual intenta eliminar el documento
                            deleteFirestoreDocumentOnly(firestoreId, toastMessage, returnAction);
                        }
                    } else {
                        // Si el documento no existe, simplemente notifica y termina
                        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                        setResultAndFinish(returnAction, firestoreId, currentTaxista.getNombres(), currentTaxista.getApellidos());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al obtener taxista para eliminación de imágenes. Intentando eliminar solo documento.", e);
                    // Fallback: Si no se puede obtener el documento, intenta eliminarlo directamente
                    deleteFirestoreDocumentOnly(firestoreId, toastMessage, returnAction);
                });
    }

    private void deleteFirestoreDocumentOnly(String firestoreId, String toastMessage, String returnAction) {
        db.collection("taxistas").document(firestoreId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
                    setResultAndFinish(returnAction, firestoreId, currentTaxista.getNombres(), currentTaxista.getApellidos());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al eliminar taxista de Firestore (solo documento): " + e.getMessage());
                    Toast.makeText(this, "Error al eliminar taxista.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Establece el resultado para la actividad llamante (SuperListaTaxisActivity) y finaliza.
     */
    private void setResultAndFinish(String action, String firestoreId, String nombres, String apellidos) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("action", action);
        resultIntent.putExtra("taxista_firestore_id", firestoreId);
        resultIntent.putExtra("taxista_nombres", nombres);
        resultIntent.putExtra("taxista_apellidos", apellidos);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // Al presionar el botón de atrás, enviar RESULT_CANCELED
    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}