package com.example.hotroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class UserServTaxi extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "UserServTaxi";

    // UI Elements
    private TextView txtDriverInfo;
    private Button btnDriverDetails;
    private Button btnCancel;
    private Button btnCallDriver;
    private ProgressBar mapLoading;
    private ImageView mapView;
    private ImageView btnBack;

    // Trip details elements
    private TextView txtOrigen;
    private TextView txtDestino;
    private TextView txtTiempoTranscurrido;
    private TextView txtEstadoViaje;
    private TextView txtTaxista;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ListenerRegistration tripListener;
    private ListenerRegistration driverLocationListener;
    private ListenerRegistration alertaListener;

    // Trip variables
    private String clienteId;
    private String viajeId;
    private String alertaId;
    private String taxistaId;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_servicio_taxi);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Check if user is authenticated
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get authenticated user UID
        clienteId = currentUser.getUid();
        Log.d(TAG, "Cliente ID obtenido: " + clienteId);

        // Initialize components
        initViews();

        // Setup map
        setupMap();

        // Search for active taxi alert
        buscarAlertaTaxiActiva();

        // Setup listeners
        setupClickListeners();
    }

    private void initViews() {
        txtDriverInfo = findViewById(R.id.txt_driver_info);
        btnDriverDetails = findViewById(R.id.btn_driver_details);
        btnCancel = findViewById(R.id.btn_cancel);
        btnCallDriver = findViewById(R.id.btn_call_driver);
        mapLoading = findViewById(R.id.map_loading);
        mapView = findViewById(R.id.map_view);
        btnBack = findViewById(R.id.btn_back);

        // Initialize new elements
        txtOrigen = findViewById(R.id.txt_origen);
        txtDestino = findViewById(R.id.txt_destino);
        txtTiempoTranscurrido = findViewById(R.id.txt_tiempo_transcurrido);
        txtEstadoViaje = findViewById(R.id.txt_estado_viaje);
        txtTaxista = findViewById(R.id.txt_taxista);

        // Initial state
        txtDriverInfo.setText("Buscando detalles del viaje...");
        btnDriverDetails.setVisibility(View.GONE);
        btnCallDriver.setVisibility(View.GONE);

        // Default taxista (initially hidden)
        txtTaxista.setText("Asignando taxista...");
    }

    private void setupMap() {
        // Hide ImageView and show map fragment
        mapView.setVisibility(View.GONE);

        // Setup map fragment
        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Cancel button
        btnCancel.setOnClickListener(v -> cancelarViaje());

        // Driver details button
        btnDriverDetails.setOnClickListener(v -> {
            if (taxistaId != null) {
                Intent intent = new Intent(UserServTaxi.this, UserDetalleTaxista.class);
                intent.putExtra("taxistaId", taxistaId);
                intent.putExtra("viajeId", viajeId);
                startActivity(intent);
            }
        });

        // Call driver button
        btnCallDriver.setOnClickListener(v -> {
            // Here you would implement the call functionality
            Toast.makeText(this, "Llamando a Alejandro Gomez...", Toast.LENGTH_SHORT).show();
            // Intent callIntent = new Intent(Intent.ACTION_CALL);
            // callIntent.setData(Uri.parse("tel:" + phoneNumber));
            // startActivity(callIntent);
        });
    }

    private void buscarAlertaTaxiActiva() {
        Log.d(TAG, "Buscando alerta de taxi activa para cliente: " + clienteId);

        db.collection("alertas_taxi")
                .whereEqualTo("idCliente", clienteId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean alertaEncontrada = false;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String estadoViaje = document.getString("estadoViaje");

                        // Check that status is not "Completado"
                        if (estadoViaje != null && !estadoViaje.equals("Completado")) {
                            alertaId = document.getId();
                            alertaEncontrada = true;

                            Log.d(TAG, "Alerta encontrada: " + alertaId + ", Estado: " + estadoViaje);

                            // Show trip details
                            mostrarDetallesViaje(document);

                            // Setup listener for changes
                            escucharCambiosAlerta();
                            break;
                        }
                    }

                    if (!alertaEncontrada) {
                        txtDriverInfo.setText("No hay viajes activos");
                        txtEstadoViaje.setText("Sin viajes activos");
                        Toast.makeText(this, "No se encontraron viajes activos", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "No hay alertas activas para este cliente");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar alerta de taxi", e);
                    txtDriverInfo.setText("Error al cargar información del viaje");
                    txtEstadoViaje.setText("Error");
                    Toast.makeText(this, "Error al buscar viaje activo", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarDetallesViaje(DocumentSnapshot alertaDoc) {
        try {
            // Get document data
            String origen = alertaDoc.getString("origen");
            String destino = alertaDoc.getString("destino");
            String tiempoTranscurrido = alertaDoc.getString("tiempoTranscurrido");
            String estadoViaje = alertaDoc.getString("estadoViaje");

            // Show data in views
            txtOrigen.setText(origen != null ? origen : "No especificado");
            txtDestino.setText(destino != null ? destino : "No especificado");
            txtTiempoTranscurrido.setText("Tiempo: " + (tiempoTranscurrido != null ? tiempoTranscurrido : "0 min"));
            txtEstadoViaje.setText(estadoViaje != null ? estadoViaje : "Desconocido");

            // Update driver info based on status
            actualizarInfoConductor(estadoViaje);

            // Hide map loading
            mapLoading.setVisibility(View.GONE);

            Log.d(TAG, "Detalles del viaje mostrados - Origen: " + origen +
                    ", Destino: " + destino + ", Estado: " + estadoViaje);

        } catch (Exception e) {
            Log.e(TAG, "Error al mostrar detalles del viaje", e);
            txtDriverInfo.setText("Error al cargar detalles");
            txtEstadoViaje.setText("Error");
        }
    }

    private void actualizarInfoConductor(String estadoViaje) {
        if (estadoViaje != null) {
            String estado = estadoViaje.toLowerCase().trim();

            switch (estado) {
                case "No asignado":
                    txtDriverInfo.setText("Buscando taxista disponible...");
                    txtTaxista.setText("Asignando taxista...");
                    btnDriverDetails.setVisibility(View.GONE);
                    btnCallDriver.setVisibility(View.GONE);
                    break;

                case "En camino":
                    txtDriverInfo.setText("Taxista asignado y preparándose");
                    txtTaxista.setText("Alejandro Gomez");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    btnCallDriver.setVisibility(View.VISIBLE);
                    break;


                case "Asignado":
                    txtDriverInfo.setText("Tu taxista está afuera del hotel");
                    txtTaxista.setText("Alejandro Gomez");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    btnCallDriver.setVisibility(View.VISIBLE);

                    // Show toast notification when driver is on the way
                    Toast.makeText(this, "¡Alejandro Gomez está esperandote!", Toast.LENGTH_LONG).show();
                    break;


                case "Llego a destino":
                    txtDriverInfo.setText("Viaje casi finalizado");
                    txtTaxista.setText("Alejandro Gomez");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    btnCallDriver.setVisibility(View.VISIBLE);
                    break;

                case "Completado":
                    txtDriverInfo.setText("Viaje completado");
                    txtTaxista.setText("Alejandro Gomez");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    btnCallDriver.setVisibility(View.GONE);
                    btnCancel.setText("Finalizar");

                    Toast.makeText(this, "¡Viaje completado exitosamente!", Toast.LENGTH_SHORT).show();
                    break;

                case "cancelado":
                    txtDriverInfo.setText("Viaje cancelado");
                    txtTaxista.setText("Viaje cancelado");
                    btnDriverDetails.setVisibility(View.GONE);
                    btnCallDriver.setVisibility(View.GONE);
                    btnCancel.setText("Volver");

                    Toast.makeText(this, "El viaje ha sido cancelado", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    txtDriverInfo.setText("Estado: " + estadoViaje);
                    txtTaxista.setText("Alejandro Gomez");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    btnCallDriver.setVisibility(View.VISIBLE);
                    break;
            }

            Log.d(TAG, "Estado actualizado: " + estadoViaje + " -> Info: " + txtDriverInfo.getText());
        }
    }

    private void escucharCambiosAlerta() {
        if (alertaId == null) return;

        DocumentReference alertaRef = db.collection("alertas_taxi").document(alertaId);

        alertaListener = alertaRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Error al escuchar cambios de la alerta", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Alerta actualizada en tiempo real");

                String estadoPrevio = txtEstadoViaje.getText().toString();
                mostrarDetallesViaje(snapshot);

                String estadoNuevo = snapshot.getString("estadoViaje");

                // Special notification when status changes to "En camino"
                if (estadoNuevo != null &&
                        (estadoNuevo.equalsIgnoreCase("en camino") || estadoNuevo.equalsIgnoreCase("en_camino")) &&
                        !estadoPrevio.equalsIgnoreCase(estadoNuevo)) {

                    // Additional notification for status change
                    Toast.makeText(this, "🚖 ¡Tu taxista Alejandro Gomez está en camino!", Toast.LENGTH_LONG).show();
                }

                // Auto-close when completed
                if ("Completado".equals(estadoNuevo)) {
                    // Delay before closing to show completion message
                    new android.os.Handler().postDelayed(() -> finish(), 3000);
                }
            }
        });
    }

    private void cancelarViaje() {
        if (alertaId != null) {
            String estadoActual = txtEstadoViaje.getText().toString();

            // If already completed or cancelled, just finish
            if (estadoActual.equalsIgnoreCase("Completado") ||
                    estadoActual.equalsIgnoreCase("Cancelado")) {
                finish();
                return;
            }

            // Update alert status to "Cancelado"
            db.collection("alertas_taxi").document(alertaId)
                    .update("estadoViaje", "Cancelado")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Viaje cancelado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al cancelar viaje", e);
                        Toast.makeText(this, "Error al cancelar el viaje", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            finish();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configure map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        try {
            // Enable location layer if permission is granted
            mMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }

        // Initial location (Lima, Peru)
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 12));

        // Add example marker
        mMap.addMarker(new MarkerOptions()
                .position(lima)
                .title("Alejandro Gomez")
                .snippet("Tu taxista - En camino"));

        Log.d(TAG, "Mapa configurado correctamente");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove listeners to avoid memory leaks
        if (tripListener != null) {
            tripListener.remove();
        }
        if (driverLocationListener != null) {
            driverLocationListener.remove();
        }
        if (alertaListener != null) {
            alertaListener.remove();
        }

        Log.d(TAG, "Actividad destruida y listeners removidos");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh trip data when returning to activity
        if (alertaId != null) {
            Log.d(TAG, "Actividad resumida - actualizando datos del viaje");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Actividad pausada");
    }
}