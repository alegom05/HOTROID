package com.example.hotroid;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView; // This import is not used in the code you provided, you might want to remove it
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class TaxiLocation extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "TaxiLocationDebug";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db; // Agregado para la verificación de viaje

    private ListenerRegistration currentTripCheckListener; // Listener para verificar viaje en curso y redirigir

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_location);

        db = FirebaseFirestore.getInstance(); // Inicializar Firestore

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        // Inicializa el cliente de ubicación.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtiene el SupportMapFragment y notifica cuando el mapa está listo.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            // Manejo de error si el fragmento del mapa no se encuentra
            Log.e(TAG, "Error: SupportMapFragment no encontrado.");
            Toast.makeText(this, "Error al cargar el mapa.", Toast.LENGTH_SHORT).show();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.location);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.location) {
                return true;
            } else if (itemId == R.id.wifi) {
                // Navegar a TaxiActivity, que a su vez verificará y redirigirá si hay un viaje asignado
                Intent intentAlertas = new Intent(TaxiLocation.this, TaxiActivity.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.notify) {
                // Navegar a TaxiDashboardActivity, que a su vez verificará y redirigirá si hay un viaje asignado
                Intent intentDashboard = new Intent(TaxiLocation.this, TaxiDashboardActivity.class);
                startActivity(intentDashboard);
                return true;
            }
            return false;
        });

        // --- INICIO: Lógica para redirigir a TaxiViaje si ya hay un viaje "Asignado" ---
        // Este listener se dispara cuando TaxiLocation es creada/retomada.
        currentTripCheckListener = db.collection("alertas_taxi")
                .whereEqualTo("estadoViaje", "Asignado")
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error al verificar viaje actual para redirección: ", e);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        // Hay un viaje "Asignado", redirigir a TaxiViaje
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        TaxiAlertasBeans currentTrip = doc.toObject(TaxiAlertasBeans.class);
                        if (currentTrip != null) {
                            currentTrip.setDocumentId(doc.getId());

                            Log.d(TAG, "Viaje 'Asignado' detectado al iniciar TaxiLocation. Redirigiendo a TaxiViaje.");
                            Intent intent = new Intent(TaxiLocation.this, TaxiViaje.class);
                            intent.putExtra("documentId", currentTrip.getDocumentId());
                            intent.putExtra("nombresCliente", currentTrip.getNombresCliente());
                            intent.putExtra("apellidosCliente", currentTrip.getApellidosCliente());
                            intent.putExtra("origen", currentTrip.getOrigen());
                            intent.putExtra("destino", currentTrip.getDestino());
                            intent.putExtra("timestamp", currentTrip.getTimestamp() != null ? currentTrip.getTimestamp().getTime() : 0);
                            intent.putExtra("estadoViaje", currentTrip.getEstadoViaje());
                            intent.putExtra("region", currentTrip.getRegion());
                            // Limpia todas las actividades anteriores y crea una nueva tarea para TaxiViaje
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Finaliza esta actividad para no volver atrás
                        }
                    }
                });
        // --- FIN: Lógica para redirigir ---
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa listo.");
        // Una vez que el mapa está listo, habilita la ubicación.
        enableMyLocation();
    }

    private void enableMyLocation() {
        // Verifica si los permisos ya han sido concedidos.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Habilita la capa de "Mi Ubicación" en el mapa.
            mMap.setMyLocationEnabled(true);
            Log.d(TAG, "Mi ubicación habilitada en el mapa.");

            // Obtiene la última ubicación conocida del dispositivo.
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Si la ubicación no es nula, crea un objeto LatLng y lo muestra en el mapa.
                            LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                            // Agrega un marcador en la ubicación actual.
                            mMap.addMarker(new MarkerOptions().position(miPosicion).title("Mi posición actual"));
                            // Mueve la cámara del mapa a la ubicación actual con un zoom específico.
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));
                            Log.d(TAG, "Ubicación actual obtenida y marcada: " + miPosicion.latitude + ", " + miPosicion.longitude);
                        } else {
                            Log.w(TAG, "Ubicación actual es null. No se pudo obtener la última ubicación conocida.");
                            Toast.makeText(this, "No se pudo obtener la ubicación actual. Inténtelo de nuevo.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener la última ubicación: " + e.getMessage());
                        Toast.makeText(this, "Error al obtener la ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Si los permisos no han sido concedidos, los solicita al usuario.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d(TAG, "Solicitando permiso de ubicación.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso es concedido, vuelve a intentar habilitar la ubicación.
                enableMyLocation();
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de ubicación concedido.");
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para mostrar tu posición", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de ubicación denegado.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desregistra el listener de Firestore para evitar fugas de memoria.
        if (currentTripCheckListener != null) {
            currentTripCheckListener.remove();
            Log.d(TAG, "Listener de verificación de viaje actual (TaxiLocation) desregistrado.");
        }
    }
}