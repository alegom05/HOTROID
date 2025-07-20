package com.example.hotroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng; // <<<<<<<<<<<<<<<< CORREGIDO AQUÍ
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaxiLocation extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "TaxiLocationDebug";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private ListenerRegistration currentTripCheckListener;

    private CardView cardTaxista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_location);

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtiene el SupportMapFragment y notifica cuando el mapa está listo.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "Solicitando el mapa de forma asíncrona en TaxiLocation.");
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

        cardTaxista = findViewById(R.id.cardTaxista);
        cardTaxista.setOnClickListener(v -> {
            Log.d(TAG, "CardView 'cardTaxista' clickeado. Redirigiendo a TaxiCuenta.");
            Intent goToTaxiCuenta = new Intent(TaxiLocation.this, TaxiCuenta.class);
            startActivity(goToTaxiCuenta);
        });

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
        Log.d(TAG, "Google Map está listo en TaxiLocation.");

        // Habilita la capa de mi ubicación.
        enableMyLocation();

        // Añadir todos los marcadores fijos específicos (hoteles y aeropuertos)
        addFixedMarkers();
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
                            Log.d(TAG, "Ubicación actual obtenida: " + miPosicion.latitude + ", " + miPosicion.longitude);
                            // La cámara se ajustará a los marcadores fijos, no a la ubicación actual del usuario aquí.
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
            Log.d(TAG, "Solicitando permiso de ubicación en TaxiLocation.");
        }
    }

    /**
     * Mapea nombres de lugares a direcciones completas y precisas.
     * Centraliza la información de dirección para los puntos de interés.
     * @param shortName El nombre del lugar (ej. "Libertador").
     * @return La dirección completa para geocodificar, o null si no se reconoce.
     */
    private String getFullAddress(String shortName) {
        if (shortName == null) return null;

        switch (shortName) {
            case "Hotel Libertador": return "Calle San Agustín 400, Cusco 08001, Perú";
            case "Palacio del Inka": return "Plazoleta Santo Domingo 259, Cusco 08002, Perú";
            case "JW Marriott El Convento Cusco": return "Esquina de la Calle Ruinas 432 y San Agustín, Cusco 08002, Perú";
            case "Belmond Hotel Monasterio": return "Calle Palacio 140, Cusco 08002, Perú";
            case "Novotel Cusco": return "Calle San Agustín 239, Cusco 08002, Perú";
            case "Hilton Garden Inn Cusco": return "Av. Pachacuteq 101, Cusco 08002, Perú";
            case "Casa Andina Premium Cusco": return "Av. El Sol 954, Cusco 08002, Perú";
            case "El Mercado Tunqui": return "Calle Garcilaso 210, Cusco 08002, Perú";
            case "Antigua Casona San Blas": return "Tandapata 116, Cusco 08007, Perú";
            case "Hotel Rumi Punku": return "Calle Choquechaka 339, Cusco 08002, Perú";
            case "Aeropuerto Internacional Alejandro Velasco Astete": return "Av. Velasco Astete s/n, Wanchaq 08002, Perú";
            case "Aeropuerto Internacional de Chinchero": return "Aeropuerto Internacional de Chinchero, Cusco, Perú";
            default: return null; // Si el nombre no está en la lista fija
        }
    }

    /**
     * Añade marcadores para el Hotel Libertador + 9 hoteles más (rojos)
     * y dos aeropuertos (verdes). Ajusta la cámara para mostrar todos los puntos.
     */
    private void addFixedMarkers() {
        if (mMap == null) {
            Log.e(TAG, "Mapa no disponible para añadir marcadores fijos.");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean markersAdded = false;

        // --- Hoteles (Marcadores Rojos) ---
        List<String> hotels = new ArrayList<>();
        hotels.add("Hotel Libertador");
        hotels.add("Palacio del Inka");
        hotels.add("JW Marriott El Convento Cusco");
        hotels.add("Belmond Hotel Monasterio");
        hotels.add("Novotel Cusco");
        hotels.add("Hilton Garden Inn Cusco");
        hotels.add("Casa Andina Premium Cusco");
        hotels.add("El Mercado Tunqui");
        hotels.add("Antigua Casona San Blas");
        hotels.add("Hotel Rumi Punku");

        for (String hotelName : hotels) {
            String address = getFullAddress(hotelName);
            if (address != null) {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(address, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(hotelName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        boundsBuilder.include(latLng);
                        markersAdded = true;
                        Log.d(TAG, "Marcador rojo añadido: " + hotelName + " en " + latLng);
                    } else {
                        Log.w(TAG, "No se encontraron coordenadas para el hotel: " + hotelName + " (" + address + ")");
                        Toast.makeText(this, "No se pudo encontrar el hotel: " + hotelName, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error de geocodificación para el hotel " + hotelName + ": " + e.getMessage());
                }
            } else {
                Log.w(TAG, "Dirección no definida para el hotel: " + hotelName);
            }
        }

        // --- Aeropuertos (Marcadores Verdes) ---
        List<String> airports = new ArrayList<>();
        airports.add("Aeropuerto Internacional Alejandro Velasco Astete");
        airports.add("Aeropuerto Internacional de Chinchero");

        for (String airportName : airports) {
            String address = getFullAddress(airportName);
            if (address != null) {
                try {
                    List<Address> addresses = geocoder.getFromLocationName(address, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address location = addresses.get(0);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(airportName)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        boundsBuilder.include(latLng);
                        markersAdded = true;
                        Log.d(TAG, "Marcador verde añadido: " + airportName + " en " + latLng);
                    } else {
                        Log.w(TAG, "No se encontraron coordenadas para el aeropuerto: " + airportName + " (" + address + ")");
                        Toast.makeText(this, "No se pudo encontrar el aeropuerto: " + airportName, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error de geocodificación para el aeropuerto " + airportName + ": " + e.getMessage());
                }
            } else {
                Log.w(TAG, "Dirección no definida para el aeropuerto: " + airportName);
            }
        }

        // Ajustar la cámara para mostrar todos los marcadores añadidos
        if (markersAdded) {
            int padding = 200; // padding en píxeles para que los marcadores no queden pegados al borde
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), padding);
            try {
                mMap.animateCamera(cu);
                Log.d(TAG, "Cámara ajustada para mostrar todos los marcadores fijos.");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error al animar la cámara: " + e.getMessage() + ". Intentando mover sin animación.");
                mMap.moveCamera(cu);
            }
        } else {
            Log.w(TAG, "No se añadió ningún marcador al mapa. No se pudo ajustar la cámara.");
            Toast.makeText(this, "No se pudieron cargar los puntos de interés.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
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