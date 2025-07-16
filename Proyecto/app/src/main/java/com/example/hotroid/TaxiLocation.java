package com.example.hotroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Importar CardView
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class TaxiLocation extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "TaxiLocationDebug";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private ListenerRegistration currentTripCheckListener;

    // Variables para guardar las direcciones de origen y destino del Intent
    private String origenDireccion;
    private String destinoDireccion;
    private String region; // Para tener más precisión en la geocodificación

    // Declarar el CardView
    private CardView cardTaxista; // <-- AÑADIDO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_location); // Asegúrate de que este sea el layout correcto que contiene el CardView

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- 1. Obtener las direcciones del Intent. Hacemos esto primero ---
        Intent intent = getIntent();
        origenDireccion = intent.getStringExtra("origen");
        destinoDireccion = intent.getStringExtra("destino");
        region = intent.getStringExtra("region"); // Asumimos que también pasas la región

        Log.d(TAG, "Dirección de Origen recibida: " + origenDireccion);
        Log.d(TAG, "Dirección de Destino recibida: " + destinoDireccion);
        Log.d(TAG, "Región recibida: " + region);

        // 2. Obtiene el SupportMapFragment y notifica cuando el mapa está listo.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
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
                Intent intentAlertas = new Intent(TaxiLocation.this, TaxiActivity.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.notify) {
                Intent intentDashboard = new Intent(TaxiLocation.this, TaxiDashboardActivity.class);
                startActivity(intentDashboard);
                return true;
            }
            return false;
        });

        // --- Lógica para el CardView ---
        cardTaxista = findViewById(R.id.cardTaxista); // Enlazar el CardView por su ID
        cardTaxista.setOnClickListener(v -> {
            Log.d(TAG, "CardView 'cardTaxista' clickeado. Redirigiendo a TaxiCuenta.");
            Intent goToTaxiCuenta = new Intent(TaxiLocation.this, TaxiCuenta.class);
            startActivity(goToTaxiCuenta);
        });
        // --- Fin de la lógica para el CardView ---


        // Lógica para redirigir a TaxiViaje si hay un viaje "Asignado"
        currentTripCheckListener = db.collection("alertas_taxi")
                .whereEqualTo("estadoViaje", "Asignado")
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error al verificar viaje actual para redirección: ", e);
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        TaxiAlertasBeans currentTrip = doc.toObject(TaxiAlertasBeans.class);
                        if (currentTrip != null) {
                            currentTrip.setDocumentId(doc.getId());
                            Log.d(TAG, "Viaje 'Asignado' detectado. Redirigiendo a TaxiViaje.");
                            Intent redirectIntent = new Intent(TaxiLocation.this, TaxiViaje.class);
                            redirectIntent.putExtra("documentId", currentTrip.getDocumentId());
                            redirectIntent.putExtra("nombresCliente", currentTrip.getNombresCliente());
                            redirectIntent.putExtra("apellidosCliente", currentTrip.getApellidosCliente());
                            redirectIntent.putExtra("origen", currentTrip.getOrigen());
                            redirectIntent.putExtra("destino", currentTrip.getDestino());
                            redirectIntent.putExtra("timestamp", currentTrip.getTimestamp() != null ? currentTrip.getTimestamp().getTime() : 0);
                            redirectIntent.putExtra("estadoViaje", currentTrip.getEstadoViaje());
                            redirectIntent.putExtra("region", currentTrip.getRegion());
                            redirectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(redirectIntent);
                            finish();
                        }
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa listo.");

        // Habilita la capa de mi ubicación.
        enableMyLocation();

        // 3. Añadir marcadores para la ruta si las direcciones están disponibles.
        if (origenDireccion != null && destinoDireccion != null) {
            String origenCompleto = getFullAddress(origenDireccion, region);
            String destinoCompleto = getFullAddress(destinoDireccion, region);
            addMarkersForRoute(origenCompleto, destinoCompleto);
        } else {
            Log.d(TAG, "No hay direcciones de origen y destino en el Intent para mostrar la ruta.");
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Log.d(TAG, "Mi ubicación habilitada en el mapa.");

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                            // Mueve la cámara a la ubicación actual solo si no hay una ruta definida para evitar conflictos con el zoom de la ruta
                            if (origenDireccion == null || destinoDireccion == null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));
                            }
                            Log.d(TAG, "Ubicación actual obtenida: " + miPosicion.latitude + ", " + miPosicion.longitude);
                        } else {
                            Log.w(TAG, "Ubicación actual es null.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener la última ubicación: " + e.getMessage());
                    });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d(TAG, "Solicitando permiso de ubicación.");
        }
    }

    /**
     * Mapea nombres cortos de lugares a direcciones completas y precisas.
     * @param shortName El nombre del lugar (ej. "Libertador").
     * @param region La región/ciudad para dar contexto (ej. "Cusco").
     * @return La dirección completa para geocodificar.
     */
    private String getFullAddress(String shortName, String region) {
        if (shortName == null) return null;

        // Mapea los nombres de tus lugares a direcciones reales para el Geocoder
        switch (shortName) {
            case "Libertador":
                // Esta es una dirección real y precisa para el Hotel Libertador en Cusco.
                return "Calle San Agustín 400, Cusco 08001, Perú";
            case "Aeropuerto de Chinchero (CUZ)":
                // Dirección más precisa del nuevo aeropuerto.
                return "Aeropuerto Internacional de Chinchero, Cusco, Perú";
            case "Aeropuerto Internacional Alejandro Velasco Astete (CUZ)":
                // Dirección del aeropuerto antiguo.
                return "Av. Velasco Astete s/n, Wanchaq 08002, Perú";
            // Agrega más casos si tienes otros lugares fijos
            // case "Mi Otro Lugar":
            //     return "Dirección completa de mi otro lugar, Ciudad, País";
            default:
                // Si el nombre no está en la lista fija, concatena con la región para tener una pista.
                return shortName + ", " + (region != null ? region : "") + ", Perú";
        }
    }

    /**
     * Geocodifica direcciones y añade marcadores de origen y destino al mapa.
     * Dibuja una polilínea entre los dos puntos y ajusta la cámara.
     * @param origenStr Dirección de origen completa.
     * @param destinoStr Dirección de destino completa.
     */
    private void addMarkersForRoute(String origenStr, String destinoStr) {
        if (origenStr == null || destinoStr == null || mMap == null) {
            Log.e(TAG, "Direcciones o mapa no disponibles para añadir marcadores.");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLng origenLatLng = null;
        LatLng destinoLatLng = null;

        try {
            // Geocodificar Origen
            List<android.location.Address> origenAddresses = geocoder.getFromLocationName(origenStr, 1);
            if (origenAddresses != null && !origenAddresses.isEmpty()) {
                android.location.Address origenAddress = origenAddresses.get(0);
                origenLatLng = new LatLng(origenAddress.getLatitude(), origenAddress.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(origenLatLng)
                        .title("Origen: " + origenStr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                Log.d(TAG, "Marcador de Origen añadido: " + origenLatLng);
            } else {
                Log.w(TAG, "No se encontraron coordenadas para la dirección de origen: " + origenStr);
                Toast.makeText(this, "No se pudo encontrar la dirección de origen: " + origenStr, Toast.LENGTH_LONG).show();
            }

            // Geocodificar Destino
            List<android.location.Address> destinoAddresses = geocoder.getFromLocationName(destinoStr, 1);
            if (destinoAddresses != null && !destinoAddresses.isEmpty()) {
                android.location.Address destinoAddress = destinoAddresses.get(0);
                destinoLatLng = new LatLng(destinoAddress.getLatitude(), destinoAddress.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(destinoLatLng)
                        .title("Destino: " + destinoStr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                Log.d(TAG, "Marcador de Destino añadido: " + destinoLatLng);
            } else {
                Log.w(TAG, "No se encontraron coordenadas para la dirección de destino: " + destinoStr);
                Toast.makeText(this, "No se pudo encontrar la dirección de destino: " + destinoStr, Toast.LENGTH_LONG).show();
            }

            // Dibuja la ruta y ajusta la cámara si ambos puntos fueron encontrados
            if (origenLatLng != null && destinoLatLng != null) {
                drawRoute(origenLatLng, destinoLatLng);
                zoomToFitMarkers(origenLatLng, destinoLatLng);
            }

        } catch (IOException e) {
            Log.e(TAG, "Error de servicio de geocodificación: " + e.getMessage());
            Toast.makeText(this, "Error de servicio de geocodificación. Asegúrate de tener conexión a internet.", Toast.LENGTH_LONG).show();
        }
    }

    private void drawRoute(LatLng origen, LatLng destino) {
        if (mMap == null) return;

        PolylineOptions polylineOptions = new PolylineOptions()
                .add(origen)
                .add(destino)
                .width(10)
                .color(ContextCompat.getColor(this, R.color.verdejade))
                .geodesic(true);

        mMap.addPolyline(polylineOptions);
        Log.d(TAG, "Ruta simple dibujada entre origen y destino.");
    }

    private void zoomToFitMarkers(LatLng origen, LatLng destino) {
        if (mMap == null) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origen);
        builder.include(destino);
        LatLngBounds bounds = builder.build();

        int padding = 200; // padding en píxeles
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        try {
            mMap.animateCamera(cu);
            Log.d(TAG, "Cámara ajustada para mostrar origen y destino.");
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error al animar la cámara: " + e.getMessage() + ". Intentando mover sin animación.");
            mMap.moveCamera(cu);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        if (currentTripCheckListener != null) {
            currentTripCheckListener.remove();
            Log.d(TAG, "Listener de verificación de viaje actual (TaxiLocation) desregistrado.");
        }
    }
}