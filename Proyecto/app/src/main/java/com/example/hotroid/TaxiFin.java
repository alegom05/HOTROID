package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class TaxiFin extends AppCompatActivity {

    private static final String TAG = "TaxiFinDebug";
    private Button btnScanQr;
    private TextView tvScanResult;
    private FirebaseFirestore db;
    private String currentTripDocumentId;

    private ImageButton btnAtras;
    private BottomNavigationView bottomNavigationView; // Declarar aquí

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_fin);

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        btnScanQr = findViewById(R.id.btnScanQr);
        tvScanResult = findViewById(R.id.tvScanResult);
        btnAtras = findViewById(R.id.btnAtras);
        bottomNavigationView = findViewById(R.id.bottom_navigation); // Inicializar aquí

        ImageView qrPlaceholder = findViewById(R.id.qrPlaceholderImage);
        if (qrPlaceholder != null) {
            qrPlaceholder.setVisibility(View.VISIBLE);
        }

        currentTripDocumentId = getIntent().getStringExtra("documentId");
        if (currentTripDocumentId != null && !currentTripDocumentId.isEmpty()) {
            db.collection("alertas_taxi").document(currentTripDocumentId).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String estado = snapshot.getString("estadoViaje");
                            if ("Completado".equals(estado) || "Cancelado".equals(estado)) {
                                Toast.makeText(this, "Este viaje ya ha finalizado.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(this, TaxiDashboardActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }


        if (currentTripDocumentId == null || currentTripDocumentId.isEmpty()) {
            Log.e(TAG, "TaxiFin iniciado sin documentId válido. Redirigiendo a Dashboard.");
            Toast.makeText(this, "Error: No se encontró información del viaje. Redirigiendo.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TaxiFin.this, TaxiDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d(TAG, "TaxiFin iniciado para el documento: " + currentTripDocumentId);
        }

        btnScanQr.setOnClickListener(v -> initiateScan());

        btnAtras.setOnClickListener(v -> {
            Log.d(TAG, "Botón de atrás presionado en TaxiFin. Volviendo al Dashboard.");
            Intent intent = new Intent(TaxiFin.this, TaxiDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // *** INICIO DE LA LÓGICA DE NAVEGACIÓN PARA BOTTOM NAVIGATION ***
        bottomNavigationView.setSelectedItemId(R.id.wifi); // Puedes elegir qué ítem seleccionar por defecto

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent targetIntent;

            if (itemId == R.id.wifi) {
                Log.d(TAG, "Navegando a TaxiActivity (Alertas) desde TaxiFin.");
                targetIntent = new Intent(TaxiFin.this, TaxiActivity.class);
            } else if (itemId == R.id.location) {
                Log.d(TAG, "Navegando a TaxiLocation (Ubicación) desde TaxiFin.");
                targetIntent = new Intent(TaxiFin.this, TaxiLocation.class);
            } else if (itemId == R.id.notify) {
                Log.d(TAG, "Navegando a TaxiDashboardActivity (Dashboard) desde TaxiFin.");
                targetIntent = new Intent(TaxiFin.this, TaxiDashboardActivity.class);
            } else {
                return false; // Si no es ninguno de los IDs del menú, no hace nada
            }

            // Estas flags aseguran que la actividad destino sea la raíz de una nueva tarea
            // y que las actividades anteriores se cierren, manteniendo una pila limpia.
            targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(targetIntent);
            finish(); // Cierra TaxiFin después de la navegación
            return true; // Indica que el ítem fue seleccionado y manejado
        });
        // *** FIN DE LA LÓGICA DE NAVEGACIÓN PARA BOTTOM NAVIGATION ***

        // Eliminamos el bucle que deshabilitaba los ítems, ya que la intención es que sean navegables
        // for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
        //     bottomNavigationView.getMenu().getItem(i).setEnabled(false);
        // }
    }

    private void initiateScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Escanea el código QR del cliente");
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
        Log.d(TAG, "Iniciando escaneo de QR.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                tvScanResult.setText("Escaneo cancelado");
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Escaneo de QR cancelado.");
                // Si se cancela el escaneo, regresamos al Dashboard
                Intent intent = new Intent(TaxiFin.this, TaxiDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                String scannedCode = result.getContents();
                tvScanResult.setText("Resultado: " + scannedCode);
                Log.d(TAG, "Código QR escaneado: " + scannedCode);

                // *** MODIFICADO: Ahora cualquier QR escaneado con éxito completa el viaje ***
                Toast.makeText(this, "¡QR escaneado! Viaje completado.", Toast.LENGTH_LONG).show();
                updateTripStatusInFirestore("Completado");
                Log.d(TAG, "Código QR escaneado. Viaje marcado como 'Completado'. Contenido: " + scannedCode);

                // Redirigir a ConfirmationActivity como solicitaste
                Intent intent = new Intent(TaxiFin.this, ConfirmationActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
    }

    private void updateTripStatusInFirestore(String newStatus) {
        if (currentTripDocumentId != null) {
            DocumentReference tripRef = db.collection("alertas_taxi").document(currentTripDocumentId);
            Map<String, Object> updates = new HashMap<>();
            updates.put("estadoViaje", newStatus);
            tripRef.update(updates)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Estado del viaje en Firestore actualizado a: " + newStatus))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar el estado del viaje en Firestore: " + e.getMessage()));
        }
    }
}