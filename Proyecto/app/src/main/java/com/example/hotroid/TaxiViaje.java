package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Arrays;

public class TaxiViaje extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final String CHANNEL_ID = "taxi_channel";
    private static final String TAG = "TaxiViajeDebug";

    private FirebaseFirestore db;
    private DocumentReference currentTripDocRef;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvClienteInfo, tvOrigenInfo, tvDestinoInfo, tvTiempoEstimado, tvEstadoViaje;
    private Button btnIniciarViaje, btnLlegueOrigen, btnViajeCompletado;
    private BottomNavigationView bottomNavigationView;

    private String currentTripDocumentId;
    private ListenerRegistration tripStatusListener;
    private Timer timer;
    private long tripStartTimeMillis;

    // Variables para las direcciones del viaje
    private String origenDireccion;
    private String destinoDireccion;
    private String region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_viaje);

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        tvClienteInfo = findViewById(R.id.tvClienteInfo);
        tvOrigenInfo = findViewById(R.id.tvOrigenInfo);
        tvDestinoInfo = findViewById(R.id.tvDestinoInfo);
        tvTiempoEstimado = findViewById(R.id.tvTiempoEstimado);
        tvEstadoViaje = findViewById(R.id.tvEstadoViaje);
        btnIniciarViaje = findViewById(R.id.btnIniciarViaje);
        btnLlegueOrigen = findViewById(R.id.btnLlegueOrigen);
        btnViajeCompletado = findViewById(R.id.btnViajeCompletado);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        Intent intent = getIntent();
        currentTripDocumentId = intent.getStringExtra("documentId");

        // --- 1. Recuperar las direcciones y la región del Intent ---
        origenDireccion = intent.getStringExtra("origen");
        destinoDireccion = intent.getStringExtra("destino");
        region = intent.getStringExtra("region");
        Log.d(TAG, "Datos recibidos en el Intent:" + origenDireccion + "," + destinoDireccion + ", Region=" + region);

        if (currentTripDocumentId == null || currentTripDocumentId.isEmpty()) {
            Log.e(TAG, "TaxiViaje iniciado sin documentId válido. Redirigiendo a Dashboard.");
            Toast.makeText(this, "Error: No se encontró información del viaje. Redirigiendo.", Toast.LENGTH_LONG).show();
            Intent mainIntent = new Intent(this, TaxiDashboardActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
            finish();
            return;
        }

        currentTripDocRef = db.collection("alertas_taxi").document(currentTripDocumentId);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tripStatusListener = currentTripDocRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed on current trip document.", e);
                Toast.makeText(this, "Error al actualizar el viaje.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                TaxiAlertasBeans trip = snapshot.toObject(TaxiAlertasBeans.class);
                if (trip != null) {
                    if (trip.getDocumentId() == null) {
                        trip.setDocumentId(snapshot.getId());
                    }

                    tvClienteInfo.setText("" + trip.getNombresCliente() + " " + trip.getApellidosCliente());
                    tvOrigenInfo.setText("" + trip.getOrigen());
                    tvDestinoInfo.setText("" + trip.getDestino());
                    tvEstadoViaje.setText("Estado: " + trip.getEstadoViaje());

                    if (trip.getTimestamp() != null &&
                            ("Asignado".equals(trip.getEstadoViaje()) || "En viaje".equals(trip.getEstadoViaje()))) {
                        tripStartTimeMillis = trip.getTimestamp().getTime();
                        startTimer();
                    } else {
                        stopTimer();
                        tvTiempoEstimado.setText("00:00:00");
                    }

                    updateButtonsAndNavigation(trip.getEstadoViaje());
                    Log.d(TAG, "Estado del viaje actualizado en UI: " + trip.getEstadoViaje());

                    if ("Llegó a destino".equals(trip.getEstadoViaje())) {
                        Log.d(TAG, "Viaje llegó a destino, redirigiendo a TaxiFin para QR. DocumentId: " + trip.getDocumentId());
                        Intent finIntent = new Intent(TaxiViaje.this, TaxiFin.class);
                        finIntent.putExtra("documentId", trip.getDocumentId());
                        finIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(finIntent);
                        finish();
                        return;
                    }
                    else if ("Completado".equals(trip.getEstadoViaje()) || "Cancelado".equals(trip.getEstadoViaje())) {
                        Log.d(TAG, "Viaje marcado como finalizado (" + trip.getEstadoViaje() + "). Volviendo al Dashboard.");
                        Toast.makeText(TaxiViaje.this, "El viaje ha finalizado.", Toast.LENGTH_LONG).show();
                        Intent mainIntent = new Intent(this, TaxiDashboardActivity.class);
                        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                        return;
                    }
                }
            } else {
                Log.d(TAG, "Documento de viaje ya no existe en Firestore. Volviendo al Dashboard.");
                Toast.makeText(TaxiViaje.this, "El viaje ha sido cancelado o completado externamente.", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(this, TaxiDashboardActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainIntent);
                finish();
                return;
            }
        });

        // Los botones cambian el estado en Firestore.
        btnIniciarViaje.setOnClickListener(v -> updateTripStatus("Asignado"));
        btnLlegueOrigen.setOnClickListener(v -> updateTripStatus("En viaje"));
        btnViajeCompletado.setOnClickListener(v -> updateTripStatus("Llegó a destino"));

        // 2. Obtiene el fragmento del mapa y lo inicializa.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "Solicitando el mapa de forma asíncrona en TaxiViaje.");
        } else {
            Log.e(TAG, "Error: SupportMapFragment con ID R.id.mapFragment no encontrado en taxi_viaje.xml.");
        }

        // --- INICIALIZAR LISTENER DEL BOTTOM NAVIGATION SOLO UNA VEZ ---
        setupBottomNavigationListener();

        createNotificationChannel();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa de Google listo en TaxiViaje.");

        // Habilita la capa de mi ubicación.
        enableMyLocation();

        // --- 3. Dibuja la ruta y los marcadores si las direcciones están disponibles ---
        if (origenDireccion != null && destinoDireccion != null && region != null) {
            addRouteMarkersAndPath(origenDireccion, destinoDireccion, region);
        } else {
            Log.d(TAG, "No se recibieron direcciones completas para dibujar la ruta.");
            Toast.makeText(this, "No se pudo cargar la ruta completa.", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "Solicitando permiso de ubicación en TaxiViaje.");
        }
    }

    /**
     * Mapea nombres cortos de lugares a direcciones completas para el Geocoder.
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
            default:
                // Si el nombre no está en la lista fija, concatena con la región para tener una pista.
                return shortName + ", " + (region != null ? region : "") + ", Perú";
        }
    }

    /**
     * Geocodifica direcciones y añade marcadores de origen y destino al mapa.
     * Dibuja una polilínea entre los dos puntos y ajusta la cámara.
     * @param origenStr Dirección de origen (nombre corto).
     * @param destinoStr Dirección de destino (nombre corto).
     * @param region La región para la geocodificación.
     */
    private void addRouteMarkersAndPath(String origenStr, String destinoStr, String region) {
        if (mMap == null) return;

        // Convierte los nombres cortos a direcciones completas
        String origenCompleto = getFullAddress(origenStr, region);
        String destinoCompleto = getFullAddress(destinoStr, region);

        if (origenCompleto == null || destinoCompleto == null) {
            Log.e(TAG, "Una o ambas direcciones completas son nulas después de mapear.");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        LatLng origenLatLng = null;
        LatLng destinoLatLng = null;

        try {
            // Geocodificar Origen
            List<android.location.Address> origenAddresses = geocoder.getFromLocationName(origenCompleto, 1);
            if (origenAddresses != null && !origenAddresses.isEmpty()) {
                android.location.Address origenAddress = origenAddresses.get(0);
                origenLatLng = new LatLng(origenAddress.getLatitude(), origenAddress.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(origenLatLng)
                        .title("Origen: " + origenStr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                Log.d(TAG, "Marcador de Origen añadido: " + origenLatLng);
            } else {
                Log.w(TAG, "No se encontraron coordenadas para la dirección de origen: " + origenCompleto);
                Toast.makeText(this, "No se pudo encontrar la dirección de origen.", Toast.LENGTH_LONG).show();
            }

            // Geocodificar Destino
            List<android.location.Address> destinoAddresses = geocoder.getFromLocationName(destinoCompleto, 1);
            if (destinoAddresses != null && !destinoAddresses.isEmpty()) {
                android.location.Address destinoAddress = destinoAddresses.get(0);
                destinoLatLng = new LatLng(destinoAddress.getLatitude(), destinoAddress.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(destinoLatLng)
                        .title("Destino: " + destinoStr)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                Log.d(TAG, "Marcador de Destino añadido: " + destinoLatLng);
            } else {
                Log.w(TAG, "No se encontraron coordenadas para la dirección de destino: " + destinoCompleto);
                Toast.makeText(this, "No se pudo encontrar la dirección de destino.", Toast.LENGTH_LONG).show();
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

    // --- NUEVO MÉTODO: Lógica para manejar la navegación inferior ---
    private void setupBottomNavigationListener() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (!item.isEnabled()) {
                Log.d(TAG, "Intento de navegación con botón deshabilitado.");
                return false;
            }

            int itemId = item.getItemId();
            Intent targetIntent;

            if (itemId == R.id.wifi) {
                Log.d(TAG, "Navegando a TaxiActivity (Alertas).");
                targetIntent = new Intent(TaxiViaje.this, TaxiActivity.class);
            } else if (itemId == R.id.location) {
                Log.d(TAG, "Navegando a TaxiLocation (Ubicación).");
                targetIntent = new Intent(TaxiViaje.this, TaxiLocation.class);
            } else if (itemId == R.id.notify) {
                Log.d(TAG, "Navegando a TaxiDashboardActivity (Dashboard).");
                targetIntent = new Intent(TaxiViaje.this, TaxiDashboardActivity.class);
            } else {
                return false;
            }

            targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(targetIntent);
            finish();
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                Toast.makeText(this, "Permiso de ubicación concedido", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de ubicación concedido en TaxiViaje.");
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para mostrar tu posición", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de ubicación denegado en TaxiViaje.");
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de notificación concedido en TaxiViaje.");
            } else {
                Toast.makeText(this, "No se pueden mostrar notificaciones sin el permiso.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de notificación denegado en TaxiViaje.");
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Viaje";
            String description = "Notificaciones sobre el estado de los viajes de taxi";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.GREEN);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Canal de notificación creado en TaxiViaje.");
            }
        }
    }

    private void showTripStartedNotification(String title, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                Log.d(TAG, "Solicitando permiso POST_NOTIFICATIONS en TaxiViaje.");
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_hotroid2_round)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
            Log.d(TAG, "Notificación mostrada en TaxiViaje: Título='" + title + "', Texto='" + text + "'");
        }
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (tripStartTimeMillis > 0) {
                        long elapsedTime = System.currentTimeMillis() - tripStartTimeMillis;
                        long seconds = (elapsedTime / 1000) % 60;
                        long minutes = (elapsedTime / (1000 * 60)) % 60;
                        long hours = (elapsedTime / (1000 * 60 * 60));
                        tvTiempoEstimado.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateTripStatus(String newStatus) {
        if (currentTripDocRef == null) {
            Log.e(TAG, "currentTripDocRef es nulo. No se puede actualizar el estado.");
            Toast.makeText(this, "Error interno: No se puede actualizar el viaje.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("estadoViaje", newStatus);

        if ("Asignado".equals(newStatus)) {
            updates.put("timestamp", new Date());
        }

        currentTripDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Estado del viaje actualizado a: " + newStatus);
                    String notificationTitle = "";
                    String notificationText = "";

                    switch (newStatus) {
                        case "Asignado":
                            notificationTitle = "¡Viaje Iniciado!";
                            notificationText = "En camino a recoger al pasajero.";
                            break;
                        case "En viaje":
                            notificationTitle = "¡Recogida Exitosa!";
                            notificationText = "En ruta hacia el destino.";
                            break;
                        case "Llegó a destino":
                            notificationTitle = "¡Llegada a Destino!";
                            notificationText = "El pasajero ha llegado a su destino. Esperando QR.";
                            break;
                    }
                    if (!notificationTitle.isEmpty()) {
                        showTripStartedNotification(notificationTitle, notificationText);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error actualizando estado del viaje en Firestore: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al guardar estado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateButtonsAndNavigation(String currentStatus) {
        switch (currentStatus) {
            case "En camino":
                btnIniciarViaje.setVisibility(View.VISIBLE);
                btnLlegueOrigen.setVisibility(View.GONE);
                btnViajeCompletado.setVisibility(View.GONE);
                break;
            case "Asignado":
                btnIniciarViaje.setVisibility(View.GONE);
                btnLlegueOrigen.setVisibility(View.VISIBLE);
                btnViajeCompletado.setVisibility(View.GONE);
                break;
            case "En viaje":
                btnIniciarViaje.setVisibility(View.GONE);
                btnLlegueOrigen.setVisibility(View.GONE);
                btnViajeCompletado.setVisibility(View.VISIBLE);
                break;
            case "Llegó a destino":
                btnIniciarViaje.setVisibility(View.GONE);
                btnLlegueOrigen.setVisibility(View.GONE);
                btnViajeCompletado.setVisibility(View.GONE);
                break;
            case "Completado":
            case "Cancelado":
                btnIniciarViaje.setVisibility(View.GONE);
                btnLlegueOrigen.setVisibility(View.GONE);
                btnViajeCompletado.setVisibility(View.GONE);
                break;
        }

        boolean enableBottomNav = !("Llegó a destino".equals(currentStatus) || "Completado".equals(currentStatus) || "Cancelado".equals(currentStatus));

        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setEnabled(enableBottomNav);
        }
    }
}

