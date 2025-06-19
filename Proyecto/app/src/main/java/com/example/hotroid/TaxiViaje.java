package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Date;
import java.util.Locale;

public class TaxiViaje extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final String CHANNEL_ID = "taxi_channel";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvNombrePasajero, tvOrigenViaje, tvDestinoViaje, tvTiempo, tvEstadoViaje; // Añadido tvTiempo
    private Button btnIniciarViaje;

    private String nombrePasajero, origen, destino;
    private long timestampViaje; // Para almacenar el timestamp del viaje

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_viaje);

        // Inicializar vistas
        tvNombrePasajero = findViewById(R.id.tvCliente);
        tvOrigenViaje = findViewById(R.id.tvOrigen);
        tvDestinoViaje = findViewById(R.id.tvDestino);
        tvTiempo = findViewById(R.id.tvTiempo); // Inicializar tvTiempo
        tvEstadoViaje = findViewById(R.id.tvEstadoViaje);
        btnIniciarViaje = findViewById(R.id.btnIniciarViaje);

        // Obtener datos del intent
        if (getIntent().getExtras() != null) {
            nombrePasajero = getIntent().getStringExtra("nombreCliente"); // Clave corregida
            origen = getIntent().getStringExtra("origen"); // Clave corregida
            destino = getIntent().getStringExtra("destino"); // Clave corregida
            timestampViaje = getIntent().getLongExtra("timestamp", 0); // Obtener el timestamp como long

            // Mostrar datos en las vistas
            tvNombrePasajero.setText(nombrePasajero);
            tvOrigenViaje.setText(origen);
            tvDestinoViaje.setText(destino);
            // Mostrar el tiempo transcurrido
            tvTiempo.setText(getTiempoTranscurrido(timestampViaje));
        }

        // Configurar el cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Crear el canal de notificación (solo es necesario una vez)
        createNotificationChannel();

        // Configurar botón de iniciar viaje
        btnIniciarViaje.setOnClickListener(v -> {
            tvEstadoViaje.setText("Viaje iniciado");
            // Usar ContextCompat.getColor para obtener el color de forma segura
            tvEstadoViaje.setTextColor(ContextCompat.getColor(this, R.color.verdejade));
            Toast.makeText(this, "Viaje iniciado con " + nombrePasajero, Toast.LENGTH_SHORT).show();

            // Llamar al método para mostrar la notificación
            showTripStartedNotification();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Obtener ubicación actual
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Obtuvimos la ubicación exitosamente
                    if (location != null) {
                        LatLng miPosicion = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(miPosicion).title("Mi posición actual"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miPosicion, 15));
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Se requiere permiso de ubicación para mostrar tu posición", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showTripStartedNotification();
            } else {
                Toast.makeText(this, "No se pueden mostrar notificaciones sin el permiso.", Toast.LENGTH_SHORT).show();
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
            }
        }
    }

    private void showTripStartedNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_hotroid2_round)
                .setContentTitle("¡Viaje Iniciado!")
                .setContentText("Tu viaje con " + nombrePasajero + " desde " + origen + " ha comenzado.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    /**
     * Helper method to format elapsed time similar to TaxiAlertasBeans.
     * Replicated here to avoid dependency if TaxiViaje only needs this function.
     */
    private String getTiempoTranscurrido(long timestampMillis) {
        if (timestampMillis == 0) { // Check for default value if not passed
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