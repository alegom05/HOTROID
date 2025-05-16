package com.example.hotroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

        // Configurar botón de iniciar viaje
        btnIniciarViaje.setOnClickListener(v -> {
            tvEstadoViaje.setText("Viaje iniciado");
            tvEstadoViaje.setTextColor(getResources().getColor(R.color.verdejade));
            Toast.makeText(this, "Viaje iniciado con " + nombrePasajero, Toast.LENGTH_SHORT).show();
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
        }
    }


}