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
import androidx.core.app.NotificationCompat; // Importa NotificationCompat
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TaxiViaje extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    // Agregamos un código para el permiso de notificaciones (para Android 13+)
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 2;
    private static final String CHANNEL_ID = "taxi_channel"; // ID del canal de notificación

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvNombrePasajero, tvOrigenViaje, tvDestinoViaje, tvEstadoViaje;
    private Button btnIniciarViaje;

    private String nombrePasajero, origen, destino;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_viaje);

        // Inicializar vistas
        tvNombrePasajero = findViewById(R.id.tvCliente);
        tvOrigenViaje = findViewById(R.id.tvOrigen);
        tvDestinoViaje = findViewById(R.id.tvDestino);
        tvEstadoViaje = findViewById(R.id.tvEstadoViaje);
        btnIniciarViaje = findViewById(R.id.btnIniciarViaje);

        // Obtener datos del intent
        if (getIntent().getExtras() != null) {
            nombrePasajero = getIntent().getStringExtra("NOMBRE_USUARIO");
            origen = getIntent().getStringExtra("ORIGEN");
            destino = getIntent().getStringExtra("DESTINO");

            // Mostrar datos en las vistas
            tvNombrePasajero.setText(nombrePasajero);
            tvOrigenViaje.setText(origen);
            tvDestinoViaje.setText(destino);
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
            tvEstadoViaje.setTextColor(getResources().getColor(R.color.verdejade));
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
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) { // Manejar el resultado del permiso de notificación
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de notificación concedido, intentar mostrar la notificación de nuevo
                showTripStartedNotification();
            } else {
                Toast.makeText(this, "No se pueden mostrar notificaciones sin el permiso.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para crear el canal de notificación
    private void createNotificationChannel() {
        // Los canales de notificación solo son necesarios en Android 8.0 (API nivel 26) y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Viaje"; // Nombre visible para el usuario
            String description = "Notificaciones sobre el estado de los viajes de taxi"; // Descripción para el usuario
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // Nivel de importancia

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableLights(true); // Habilitar luces de notificación
            channel.setLightColor(Color.GREEN); // Color de la luz
            channel.enableVibration(true); // Habilitar vibración

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Método para mostrar la notificación de inicio de viaje
    private void showTripStartedNotification() {
        // Para Android 13 (API 33) y superiores, necesitamos solicitar el permiso POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Si el permiso no está concedido, solicitarlo
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                return; // Salir para que la notificación se muestre después de conceder el permiso
            }
        }

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_hotroid2_round) // Icono pequeño de la notificación (asegúrate de tener uno)
                .setContentTitle("¡Viaje Iniciado!") // Título de la notificación
                .setContentText("Tu viaje con " + nombrePasajero + " desde " + origen + " ha comenzado.") // Contenido principal
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad de la notificación
                .setAutoCancel(true); // La notificación se cierra automáticamente al hacer clic

        // Obtener el NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Mostrar la notificación
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build()); // El "1" es un ID único para esta notificación
        }
    }
}