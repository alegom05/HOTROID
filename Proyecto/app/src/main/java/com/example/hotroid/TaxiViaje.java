package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Date;
import java.util.Locale;

public class TaxiViaje extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final String CHANNEL_ID = "taxi_channel";
    private static final String TAG = "TaxiViajeDebug";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvNombrePasajero, tvOrigenViaje, tvDestinoViaje, tvTiempo, tvEstadoViaje;
    private Button btnIniciarViaje;
    private BottomNavigationView bottomNavigationView;

    private String nombrePasajero, origen, destino, estadoViajeActual;
    private long timestampViaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_viaje);

        // Inicializar vistas
        tvNombrePasajero = findViewById(R.id.tvCliente);
        tvOrigenViaje = findViewById(R.id.tvOrigen);
        tvDestinoViaje = findViewById(R.id.tvDestino);
        tvTiempo = findViewById(R.id.tvTiempo);
        tvEstadoViaje = findViewById(R.id.tvEstadoViaje);
        btnIniciarViaje = findViewById(R.id.btnIniciarViaje);

        // --- Configuración de BottomNavigationView ---
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.notify) {
                // Navegar al Dashboard principal de alertas/viajes completados
                Intent intent = new Intent(TaxiViaje.this, TaxiDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finaliza esta actividad
                return true;
            } else if (itemId == R.id.wifi) {
                Intent intentAlertas = new Intent(TaxiViaje.this, TaxiActivity.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiViaje.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            return false;
        });
        // --- FIN: Configuración de BottomNavigationView ---


        // Obtener datos del intent
        if (getIntent().getExtras() != null) {
            nombrePasajero = getIntent().getStringExtra("nombreCliente");
            origen = getIntent().getStringExtra("origen");
            destino = getIntent().getStringExtra("destino");
            timestampViaje = getIntent().getLongExtra("timestamp", 0);
            estadoViajeActual = getIntent().getStringExtra("estadoViaje");

            Log.d(TAG, "Datos recibidos en TaxiViaje:");
            Log.d(TAG, "  Nombre Cliente: " + nombrePasajero);
            Log.d(TAG, "  Origen: " + origen);
            Log.d(TAG, "  Destino: " + destino);
            Log.d(TAG, "  Timestamp: " + timestampViaje);
            Log.d(TAG, "  Estado Inicial de Viaje: " + estadoViajeActual);

            // Mostrar datos en las vistas
            tvNombrePasajero.setText(nombrePasajero);
            tvOrigenViaje.setText(origen);
            tvDestinoViaje.setText(destino);
            tvTiempo.setText(getTiempoTranscurrido(timestampViaje));

            // --- LÓGICA DE ESTADO INICIAL DEL BOTÓN Y TEXTO ---
            if (estadoViajeActual != null) {
                tvEstadoViaje.setText(estadoViajeActual);

                if ("Asignado".equals(estadoViajeActual)) {
                    tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
                    btnIniciarViaje.setEnabled(true);
                    btnIniciarViaje.setText("Iniciar Viaje");
                    Log.d(TAG, "Estado: Asignado. Botón: Iniciar Viaje");
                } else if ("En camino".equals(estadoViajeActual)) {
                    tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                    btnIniciarViaje.setEnabled(true);
                    btnIniciarViaje.setText("Llegué a Origen");
                    Log.d(TAG, "Estado: En camino. Botón: Llegué a Origen");
                } else if ("Llegó a destino".equals(estadoViajeActual)) {
                    tvEstadoViaje.setTextColor(ContextCompat.getColor(this, R.color.verdejade));
                    btnIniciarViaje.setEnabled(true);
                    btnIniciarViaje.setText("Viaje Completado");
                    Log.d(TAG, "Estado: Llegó a destino. Botón: Viaje Completado");
                } else {
                    tvEstadoViaje.setText("Estado: Desconocido");
                    tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                    btnIniciarViaje.setText("Estado no válido");
                    btnIniciarViaje.setEnabled(false);
                    Log.e(TAG, "Estado inicial de viaje no reconocido: " + estadoViajeActual);
                    Toast.makeText(this, "Error: Estado de viaje no reconocido.", Toast.LENGTH_LONG).show();
                }
            } else {
                tvEstadoViaje.setText("Estado: No disponible");
                tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                btnIniciarViaje.setText("No disponible");
                btnIniciarViaje.setEnabled(false);
                Log.e(TAG, "Estado inicial de viaje es NULL.");
                Toast.makeText(this, "Error: Datos de viaje incompletos.", Toast.LENGTH_LONG).show();
            }
            // --- FIN LÓGICA DE ESTADO INICIAL DEL BOTÓN Y TEXTO ---

        } else {
            tvEstadoViaje.setText("Datos de viaje no disponibles.");
            tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnIniciarViaje.setText("Error de carga");
            btnIniciarViaje.setEnabled(false);
            Log.e(TAG, "No se recibieron extras en el intent.");
            Toast.makeText(this, "Error: No se pudieron cargar los datos del viaje.", Toast.LENGTH_LONG).show();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        createNotificationChannel();

        btnIniciarViaje.setOnClickListener(v -> {
            Log.d(TAG, "Botón pulsado. Estado actual antes del cambio: " + estadoViajeActual);
            if ("Asignado".equals(estadoViajeActual)) {
                estadoViajeActual = "En camino";
                tvEstadoViaje.setText(estadoViajeActual);
                tvEstadoViaje.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
                btnIniciarViaje.setText("Llegué a Origen");
                Toast.makeText(this, "¡Viaje iniciado! En camino a origen.", Toast.LENGTH_SHORT).show();
                showTripStartedNotification("¡Viaje Iniciado!", "En camino a recoger a " + nombrePasajero + " en " + origen);
                Log.d(TAG, "Transición: Asignado -> En camino. Botón: Llegué a Origen");

            } else if ("En camino".equals(estadoViajeActual)) {
                estadoViajeActual = "Llegó a destino";
                tvEstadoViaje.setText(estadoViajeActual);
                tvEstadoViaje.setTextColor(ContextCompat.getColor(this, R.color.verdejade));
                btnIniciarViaje.setText("Viaje Completado");
                Toast.makeText(this, "¡Has llegado a destino!", Toast.LENGTH_SHORT).show();
                showTripStartedNotification("¡Viaje Finalizado!", "Has llegado a destino con " + nombrePasajero + ".");
                Log.d(TAG, "Transición: En camino -> Llegó a destino. Botón: Viaje Completado");

            } else if ("Llegó a destino".equals(estadoViajeActual)) {
                Toast.makeText(this, "Viaje completado. Volviendo al Dashboard.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Transición: Llegó a destino -> Navegando a TaxiDashboardActivity.");
                Intent intent = new Intent(TaxiViaje.this, TaxiDashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "Botón pulsado en estado inesperado: " + estadoViajeActual);
                Toast.makeText(this, "Acción no válida para el estado actual.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Mapa listo.");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            Log.d(TAG, "Solicitando permiso de ubicación.");
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permiso de ubicación no concedido. No se puede habilitar mi ubicación.");
            return;
        }

        mMap.setMyLocationEnabled(true);
        Log.d(TAG, "Mi ubicación habilitada en el mapa.");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(miPosicion).title("Mi posición actual"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));
                        Log.d(TAG, "Ubicación actual obtenida y marcada: " + miPosicion.latitude + ", " + miPosicion.longitude);
                    } else {
                        Log.w(TAG, "Ubicación actual es null.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener la última ubicación: " + e.getMessage());
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
                Log.d(TAG, "Permiso de ubicación concedido.");
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para mostrar tu posición", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de ubicación denegado.");
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permiso de notificación concedido.");
            } else {
                Toast.makeText(this, "No se pueden mostrar notificaciones sin el permiso.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permiso de notificación denegado.");
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
                Log.d(TAG, "Canal de notificación creado.");
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
                Log.d(TAG, "Solicitando permiso POST_NOTIFICATIONS.");
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
            Log.d(TAG, "Notificación mostrada: Título='" + title + "', Texto='" + text + "'");
        }
    }

    private String getTiempoTranscurrido(long timestampMillis) {
        if (timestampMillis == 0) {
            return "Tiempo desconocido";
        }

        long diffMillis = System.currentTimeMillis() - timestampMillis;
        long minutes = diffMillis / (60 * 1000);

        if (minutes < 1) {
            return "Hace un instante";
        } else if (minutes < 60) {
            return "Hace " + minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours < 2) {
                if (remainingMinutes > 0) {
                    return "Hace " + hours + " hr y " + remainingMinutes + " min";
                } else {
                    return "Hace " + hours + " hr";
                }
            } else {
                return "Más de 2 hrs";
            }
        }
    }
}