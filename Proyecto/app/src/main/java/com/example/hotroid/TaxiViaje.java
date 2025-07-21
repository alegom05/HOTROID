package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.android.PolyUtil; // Todavía se usa si decides decodificar polilíneas en el futuro, pero no para esta solución básica.

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TaxiViaje extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final String CHANNEL_ID = "taxi_channel";
    private static final String TAG = "TaxiViajeDebug";

    // Your Google Maps API Key will be loaded from strings.xml
    private String Maps_API_KEY; // Aunque no se usará directamente para las rutas ahora, se mantiene por si se vuelve a usar

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

    // Variables para almacenar LatLng geocodificados
    private LatLng origenLatLng;
    private LatLng destinoLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_viaje);

        // Load your API key from strings.xml
        Maps_API_KEY = getString(R.string.Maps_api_key);
        if (Maps_API_KEY.isEmpty() || Maps_API_KEY.equals("YOUR_Maps_API_KEY")) {
            Log.e(TAG, "ERROR: Maps_API_KEY no está configurada correctamente en strings.xml. Las funciones del mapa pueden verse afectadas.");
            Toast.makeText(this, "Advertencia: Clave de API de Mapas ausente o incorrecta.", Toast.LENGTH_LONG).show();
        }

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
        Log.d(TAG, "Datos recibidos en el Intent: Origen=" + origenDireccion + ", Destino=" + destinoDireccion + ", Region=" + region);

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

                    // Iniciar/Detener temporizador basado en el estado
                    if ("En camino".equals(trip.getEstadoViaje()) || "Asignado".equals(trip.getEstadoViaje())) {
                        tripStartTimeMillis = trip.getTimestamp() != null ? trip.getTimestamp().getTime() : System.currentTimeMillis();
                        startTimer();
                    } else {
                        stopTimer();
                        tvTiempoEstimado.setText("00:00:00");
                    }

                    updateButtonsAndNavigation(trip.getEstadoViaje());
                    Log.d(TAG, "Estado del viaje actualizado en UI: " + trip.getEstadoViaje());

                    // Redirección a TaxiFin solo si el estado es "Llegó a destino"
                    if ("Llegó a destino".equals(trip.getEstadoViaje())) {
                        Log.d(TAG, "Viaje llegó a destino, redirigiendo a TaxiFin para QR. DocumentId: " + trip.getDocumentId());
                        Intent finIntent = new Intent(TaxiViaje.this, TaxiFin.class);
                        finIntent.putExtra("documentId", trip.getDocumentId());
                        finIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(finIntent);
                        finish();
                        return;
                    }
                    // Redirección al Dashboard si el viaje se completa o cancela (desde TaxiFin o externamente)
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

        // Los botones ahora reflejan la nueva secuencia de estados.
        btnIniciarViaje.setOnClickListener(v -> updateTripStatus("En camino")); // No asignado -> En camino
        btnLlegueOrigen.setOnClickListener(v -> updateTripStatus("Asignado"));  // En camino -> Asignado
        btnViajeCompletado.setOnClickListener(v -> updateTripStatus("Llegó a destino")); // Asignado -> Llegó a destino

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

        // --- 3. Geocodifica y dibuja la línea básica ---
        new GeocodeAndDrawRouteTask().execute(origenDireccion, destinoDireccion, region);
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
                            // Este ajuste de cámara se hará después de dibujar la ruta en GeocodeAndDrawRouteTask.
                            if (origenLatLng == null || destinoLatLng == null) {
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
     * @param region La región/ciudad para dar contexto (ej. "Lima").
     * @return La dirección completa para geocodificar.
     */
    private String getFullAddress(String shortName, String region) {
        if (shortName == null) return null;

        // Mapea los nombres de tus lugares a direcciones reales para el Geocoder
        // Estas direcciones deben ser lo más precisas posible para un buen geocodificado.
        // Considerando la ubicación actual: Villa María del Triunfo, Lima Province, Peru
        switch (shortName.trim().toLowerCase(Locale.getDefault())) {
            case "libertador":
                // Asumiendo que "Libertador" podría referirse a una calle o plaza conocida en Lima o en VMT.
                // Es crucial que esta dirección sea lo más específica posible.
                // Ejemplo: Una dirección real en Lima. Si se refiere a PUCP como antes, es una dirección conocida.
                return "Av. La Marina 200, San Miguel 15088, Lima, Perú"; // Ejemplo de dirección real para Libertador (si se refiere a PUCP)
            case "aeropuerto internacional jorge chávez":
                return "Av. Elmer Faucett s/n, Callao 07031, Perú";
            case "aeropuerto de santa maría del mar":
                return "Santa María del Mar, Lima, Perú"; // Ubicación general, puede necesitar una dirección más específica
            case "base aérea las palmas":
                return "Av. Jorge Chávez s/n, Santiago de Surco, Lima, Perú";
            // Agrega más casos si tienes otros nombres cortos en tu base de datos y necesitan una dirección específica
            default:
                // Si el nombre no está en la lista fija, concatena con la región para una pista.
                // Es importante que la base de datos almacene direcciones lo más completas posible.
                return shortName + ", " + (region != null ? region : "Lima") + ", Perú";
        }
    }


    /**
     * AsyncTask para geocodificar direcciones en segundo plano y luego dibujar la línea recta.
     */
    private class GeocodeAndDrawRouteTask extends AsyncTask<String, Void, Map<String, LatLng>> {

        @Override
        protected Map<String, LatLng> doInBackground(String... params) {
            String origenStr = params[0];
            String destinoStr = params[1];
            String region = params[2];

            Map<String, LatLng> latLngMap = new HashMap<>();
            // Es importante que el contexto para Geocoder sea el de la aplicación o actividad
            // y que haya conectividad a internet para que funcione correctamente.
            Geocoder geocoder = new Geocoder(TaxiViaje.this, Locale.getDefault());

            String origenCompleto = getFullAddress(origenStr, region);
            String destinoCompleto = getFullAddress(destinoStr, region);

            if (origenCompleto == null || destinoCompleto == null) {
                Log.e(TAG, "Una o ambas direcciones completas son nulas después de mapear en GeocodeAndDrawRouteTask.");
                return null;
            }

            try {
                // Geocodificar Origen
                List<Address> origenAddresses = geocoder.getFromLocationName(origenCompleto, 1);
                if (origenAddresses != null && !origenAddresses.isEmpty()) {
                    Address origenAddress = origenAddresses.get(0);
                    latLngMap.put("origen", new LatLng(origenAddress.getLatitude(), origenAddress.getLongitude()));
                    Log.d(TAG, "Origen geocodificado: " + latLngMap.get("origen") + " para: " + origenCompleto);
                } else {
                    Log.w(TAG, "No se encontraron coordenadas para la dirección de origen: " + origenCompleto);
                }

                // Geocodificar Destino
                List<Address> destinoAddresses = geocoder.getFromLocationName(destinoCompleto, 1);
                if (destinoAddresses != null && !destinoAddresses.isEmpty()) {
                    Address destinoAddress = destinoAddresses.get(0);
                    latLngMap.put("destino", new LatLng(destinoAddress.getLatitude(), destinoAddress.getLongitude()));
                    Log.d(TAG, "Destino geocodificado: " + latLngMap.get("destino") + " para: " + destinoCompleto);
                } else {
                    Log.w(TAG, "No se encontraron coordenadas para la dirección de destino: " + destinoCompleto);
                }

            } catch (IOException e) {
                Log.e(TAG, "Error de servicio de geocodificación en segundo plano (IOException): " + e.getMessage());
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error de argumento en geocodificación (IllegalArgumentException): " + e.getMessage());
            }
            return latLngMap;
        }

        @Override
        protected void onPostExecute(Map<String, LatLng> latLngMap) {
            super.onPostExecute(latLngMap);

            if (latLngMap != null && mMap != null) {
                origenLatLng = latLngMap.get("origen");
                destinoLatLng = latLngMap.get("destino");

                mMap.clear(); // Limpia marcadores y polilíneas anteriores si las hubiera

                if (origenLatLng != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(origenLatLng)
                            .title("Origen: " + origenDireccion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                } else {
                    Toast.makeText(TaxiViaje.this, "No se pudo marcar el origen en el mapa.", Toast.LENGTH_SHORT).show();
                }

                if (destinoLatLng != null) {
                    mMap.addMarker(new MarkerOptions()
                            .position(destinoLatLng)
                            .title("Destino: " + destinoDireccion)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                } else {
                    Toast.makeText(TaxiViaje.this, "No se pudo marcar el destino en el mapa.", Toast.LENGTH_SHORT).show();
                }

                // Dibuja una línea recta simple si ambos puntos fueron encontrados
                if (origenLatLng != null && destinoLatLng != null) {
                    mMap.addPolyline(new PolylineOptions()
                            .add(origenLatLng, destinoLatLng)
                            .width(10)
                            .color(ContextCompat.getColor(TaxiViaje.this, R.color.verdejade))); // Usar tu color verdejade
                    Log.d(TAG, "Línea recta dibujada entre origen y destino.");
                    zoomToFitMarkers(origenLatLng, destinoLatLng); // Ajusta la cámara para ver ambos puntos
                } else {
                    Toast.makeText(TaxiViaje.this, "No se pudieron obtener coordenadas válidas para trazar la línea.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "No se pudieron obtener LatLngs válidos para trazar la línea después de geocodificación.");
                }
            } else {
                Toast.makeText(TaxiViaje.this, "Error al procesar las direcciones para el mapa.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * COMENTADO: Esta clase ya no se usa para dibujar una línea recta simple.
     * Si en el futuro se decide usar la API de Directions/Routes, esta clase
     * deberá ser actualizada y descomentada.
     */
    /*
    private class FetchDirectionsTask extends AsyncTask<Void, Void, String> {
        private LatLng origin;
        private LatLng destination;

        public FetchDirectionsTask(LatLng origin, LatLng destination) {
            this.origin = origin;
            this.destination = destination;
        }

        @Override
        protected String doInBackground(Void... voids) {
            // Este método estaba haciendo llamadas a la API de Directions/Routes.
            // Por ahora, lo dejamos vacío o retornando null para no ejecutar esas llamadas.
            Log.d(TAG, "FetchDirectionsTask ha sido omitida para una línea recta simple.");
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // No hay procesamiento de resultados aquí ya que la tarea está omitida.
        }
    }
    */

    /**
     * COMENTADO: Este método ya no es necesario si no se decodifican polilíneas complejas de una API.
     */
    /*
    private void parseDirectionsResult(String jsonResponse) {
        // Este método estaba procesando la respuesta JSON de la API de Directions/Routes.
        // Ahora la ruta se dibuja directamente en GeocodeAndDrawRouteTask.
        Log.d(TAG, "parseDirectionsResult ha sido omitida para una línea recta simple.");
    }
    */

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

    // --- Lógica para manejar la navegación inferior ---
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

            // Para evitar la creación de múltiples instancias de la misma actividad
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

        // Solo actualiza el timestamp cuando el viaje pasa a "En camino"
        if ("En camino".equals(newStatus)) {
            updates.put("timestamp", new Date());
        }

        currentTripDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Estado del viaje actualizado a: " + newStatus);
                    String notificationTitle = "";
                    String notificationText = "";

                    switch (newStatus) {
                        case "En camino": // Corresponds to "Iniciar Viaje" button click
                            notificationTitle = "¡Viaje Iniciado!";
                            notificationText = "En camino a recoger al pasajero.";
                            break;
                        case "Asignado": // Corresponds to "Llegue a origen" button click
                            notificationTitle = "¡Recogida Exitosa!";
                            notificationText = "En ruta hacia el destino.";
                            break;
                        case "Llegó a destino":
                            notificationTitle = "¡Llegaste a destino!";
                            notificationText = "Por favor, finaliza el viaje con el código QR.";
                            break;
                    }
                    if (!notificationTitle.isEmpty()) {
                        showTripStartedNotification(notificationTitle, notificationText);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al actualizar el estado del viaje a " + newStatus + ": " + e.getMessage());
                    Toast.makeText(this, "Error al actualizar el estado del viaje.", Toast.LENGTH_SHORT).show();
                });
    }

    // Método updateButtonsAndNavigation (añadido previamente)
    private void updateButtonsAndNavigation(String tripStatus) {
        // Reset all buttons and navigation items to default disabled state
        btnIniciarViaje.setVisibility(View.GONE);
        btnLlegueOrigen.setVisibility(View.GONE);
        btnViajeCompletado.setVisibility(View.GONE);

        // Disable all bottom navigation items by default, then enable based on status
        bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(false);
        bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(false);
        bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(false);


        int colorRes;
        switch (tripStatus) {
            case "No asignado": // Initial state
                btnIniciarViaje.setVisibility(View.VISIBLE);
                btnIniciarViaje.setText("Iniciar Viaje");
                // Allow navigation to alerts and dashboard
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true); // Alerts
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true); // Dashboard
                colorRes = android.R.color.darker_gray; // Grey for not assigned
                break;
            case "En camino": // On the way to pick up client
                btnLlegueOrigen.setVisibility(View.VISIBLE);
                btnLlegueOrigen.setText("Llegué a Origen");
                // Allow all navigation during trip
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true);
                colorRes = android.R.color.holo_blue_dark; // Blue for en camino
                break;
            case "Asignado": // Client picked up, on the way to destination
                btnViajeCompletado.setVisibility(View.VISIBLE);
                btnViajeCompletado.setText("Llegué a Destino"); // Now this button means arrived at *final* destination
                // Allow all navigation during trip
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true);
                colorRes = android.R.color.holo_blue_dark; // Blue for assigned
                break;
            case "Llegó a destino": // Arrived at final destination, awaiting payment/QR scan
                // No action button visible here, as this state leads to TaxiFin
                // Navigation should still be possible if user returns
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true);
                colorRes = R.color.naranja; // Orange for arrived at destination
                break;
            case "Completado": // Trip finished
            case "Cancelado": // Trip cancelled
                // No buttons visible, all navigation items enabled to go back to dashboard/alerts
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true);
                colorRes = android.R.color.black; // Black or default for final states
                break;
            default:
                // Default case if status is unexpected
                bottomNavigationView.getMenu().findItem(R.id.wifi).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.location).setEnabled(true);
                bottomNavigationView.getMenu().findItem(R.id.notify).setEnabled(true);
                colorRes = android.R.color.black;
                break;
        }
        tvEstadoViaje.setTextColor(ContextCompat.getColor(this, colorRes));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tripStatusListener != null) {
            tripStatusListener.remove();
            Log.d(TAG, "Listener de estado de viaje desregistrado.");
        }
        stopTimer();
    }
}