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

    private TextView txtDriverInfo;
    private Button btnDriverDetails;
    private Button btnCancel;
    private ProgressBar mapLoading;
    private ImageView mapView;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private ListenerRegistration tripListener;
    private ListenerRegistration driverLocationListener;

    // Variables del viaje
    private String clienteId;
    private String viajeId;
    private String taxistaId;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_servicio_taxi);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        // Verificar si el usuario está autenticado
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            // Redirigir al login si es necesario
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            finish();
            return;
        }

        // Obtener el UID del usuario autenticado
        clienteId = currentUser.getUid();
        Log.d(TAG, "Cliente ID obtenido: " + clienteId);

        // Inicializar componentes
        initViews();

        // Configurar mapa
        setupMap();

        // Buscar viaje activo del cliente
        buscarViajeActivo();

        // Configurar listeners
        setupClickListeners();
    }

    private void initViews() {
        txtDriverInfo = findViewById(R.id.txt_driver_info);
        btnDriverDetails = findViewById(R.id.btn_driver_details);
        btnCancel = findViewById(R.id.btn_cancel);
        mapLoading = findViewById(R.id.map_loading);
        mapView = findViewById(R.id.map_view);

        // Estado inicial
        txtDriverInfo.setText(R.string.finding_driver);
        btnDriverDetails.setVisibility(View.GONE);
    }

    private void setupMap() {
        // Ocultar ImageView y mostrar fragment del mapa
        mapView.setVisibility(View.GONE);

        // Configurar el fragmento del mapa
        mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> cancelarViaje());

        btnDriverDetails.setOnClickListener(v -> {
            if (taxistaId != null) {
                Intent intent = new Intent(UserServTaxi.this, UserDetalleTaxista.class);
                intent.putExtra("taxistaId", taxistaId);
                intent.putExtra("viajeId", viajeId);
                startActivity(intent);
            }
        });
    }

    private void buscarViajeActivo() {
        db.collection("viajes")
                .whereEqualTo("idCliente", clienteId)
                .whereIn("estado", java.util.Arrays.asList("pendiente", "asignado", "en_progreso"))
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot viajeDoc = queryDocumentSnapshots.getDocuments().get(0);
                        viajeId = viajeDoc.getId();
                        String estado = viajeDoc.getString("estado");

                        Log.d(TAG, "Viaje encontrado: " + viajeId + ", Estado: " + estado);

                        if ("pendiente".equals(estado)) {
                            // El viaje está pendiente, esperar asignación
                            esperarAsignacion();
                        } else if ("asignado".equals(estado) || "en_progreso".equals(estado)) {
                            // Ya hay un taxista asignado
                            taxistaId = viajeDoc.getString("idTaxista");
                            mostrarTaxistaAsignado();
                        }
                    } else {
                        // No hay viaje activo, crear uno nuevo
                        crearNuevoViaje();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar viaje activo", e);
                    Toast.makeText(this, "Error al buscar viaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void crearNuevoViaje() {
        // Para el hotelId, primero intenta obtenerlo desde SharedPreferences
        // Si no existe, podrías obtenerlo de otra forma o pedírselo al usuario
        SharedPreferences sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE);
        String hotelId = sharedPref.getString("hotelId", null);

        // Si no hay hotelId en SharedPreferences, podrías:
        // 1. Obtenerlo del Intent que llamó a esta actividad
        // 2. Obtenerlo de la base de datos del usuario
        // 3. Usar un valor por defecto o mostrar un selector de hoteles

        if (hotelId == null) {
            // Intentar obtener desde Intent
            hotelId = getIntent().getStringExtra("hotelId");
        }

        if (hotelId == null) {
            Toast.makeText(this, "Error: No se pudo obtener el hotel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Crear nuevo documento de viaje
        com.google.firebase.firestore.CollectionReference viajesRef = db.collection("viajes");

        java.util.Map<String, Object> nuevoViaje = new java.util.HashMap<>();
        nuevoViaje.put("idCliente", clienteId);
        nuevoViaje.put("idHotel", hotelId);
        nuevoViaje.put("estado", "pendiente");
        nuevoViaje.put("fechaCreacion", com.google.firebase.Timestamp.now());

        viajesRef.add(nuevoViaje)
                .addOnSuccessListener(documentReference -> {
                    viajeId = documentReference.getId();
                    Log.d(TAG, "Nuevo viaje creado: " + viajeId);
                    esperarAsignacion();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear nuevo viaje", e);
                    Toast.makeText(this, "Error al crear el viaje", Toast.LENGTH_SHORT).show();
                });
    }

    private void esperarAsignacion() {
        txtDriverInfo.setText(R.string.finding_driver);

        // Escuchar cambios en el documento del viaje
        DocumentReference viajeRef = db.collection("viajes").document(viajeId);

        tripListener = viajeRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Error al escuchar cambios del viaje", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                String estado = snapshot.getString("estado");
                Log.d(TAG, "Estado del viaje actualizado: " + estado);

                if ("asignado".equals(estado) || "en_progreso".equals(estado)) {
                    taxistaId = snapshot.getString("idTaxista");
                    if (taxistaId != null) {
                        mostrarTaxistaAsignado();
                    }
                } else if ("cancelado".equals(estado)) {
                    Toast.makeText(this, "El viaje ha sido cancelado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void mostrarTaxistaAsignado() {
        // Remover el listener del viaje pendiente si existe
        if (tripListener != null) {
            tripListener.remove();
        }

        // Obtener información del taxista desde la colección usuarios
        db.collection("usuarios").document(taxistaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String apellido = documentSnapshot.getString("apellido");

                        // Construir nombre completo
                        String nombreCompleto = "";
                        if (nombre != null) {
                            nombreCompleto = nombre.trim();
                        }
                        if (apellido != null && !apellido.trim().isEmpty()) {
                            nombreCompleto += " " + apellido.trim();
                        }
                        if (nombreCompleto.isEmpty()) {
                            nombreCompleto = "Taxista";
                        }

                        txtDriverInfo.setText(getString(R.string.driver_assigned, nombreCompleto));
                        btnDriverDetails.setVisibility(View.VISIBLE);

                        // Ocultar loading del mapa
                        mapLoading.setVisibility(View.GONE);

                        // Verificar si el taxista tiene coordenadas, si no, agregarlas
                        verificarYAgregarCoordenadas(documentSnapshot);

                        // Comenzar a escuchar la ubicación del taxista
                        escucharUbicacionTaxista();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al obtener información del taxista", e);
                    txtDriverInfo.setText("Taxista asignado");
                    btnDriverDetails.setVisibility(View.VISIBLE);
                    mapLoading.setVisibility(View.GONE);
                    escucharUbicacionTaxista();
                });
    }

    private void escucharUbicacionTaxista() {
        if (taxistaId == null) return;

        // Escuchar cambios en el documento del usuario taxista para obtener sus coordenadas
        DocumentReference taxistaRef = db.collection("usuarios").document(taxistaId);

        driverLocationListener = taxistaRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Error al escuchar ubicación del taxista", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Obtener las coordenadas del taxista desde el documento de usuarios
                Double latitud = snapshot.getDouble("latitud");
                Double longitud = snapshot.getDouble("longitud");

                if (latitud != null && longitud != null && mMap != null) {
                    actualizarUbicacionEnMapa(latitud, longitud);
                } else {
                    // Si no tiene coordenadas, agregamos unas coordenadas de ejemplo (Lima, Peru)
                    Log.d(TAG, "Taxista sin coordenadas, usando ubicación por defecto");
                    // Puedes usar coordenadas fijas o generar aleatorias cerca de Lima
                    actualizarUbicacionEnMapa(-12.073151046604849, -77.08191024613195);
                }
            }
        });
    }

    private double[] generarCoordenadasAleatorias() {
        // Coordenadas base de Lima, Peru
        double latitudBase = -12.0464;
        double longitudBase = -77.0428;

        // Generar variación aleatoria de aproximadamente ±0.05 grados (unos 5km de radio)
        java.util.Random random = new java.util.Random();
        double variacionLat = (random.nextDouble() - 0.5) * 0.1; // ±0.05 grados
        double variacionLng = (random.nextDouble() - 0.5) * 0.1; // ±0.05 grados

        double latitudFinal = latitudBase + variacionLat;
        double longitudFinal = longitudBase + variacionLng;

        return new double[]{latitudFinal, longitudFinal};
    }

    private void verificarYAgregarCoordenadas(DocumentSnapshot taxistaDoc) {
        Double latitud = taxistaDoc.getDouble("latitud");
        Double longitud = taxistaDoc.getDouble("longitud");

        // Si el taxista no tiene coordenadas, agregar unas coordenadas aleatorias
        if (latitud == null || longitud == null) {
            Log.d(TAG, "Taxista sin coordenadas, agregando coordenadas aleatorias");

            // Generar coordenadas aleatorias cerca de Lima
            double[] coordenadas = generarCoordenadasAleatorias();

            java.util.Map<String, Object> actualizacion = new java.util.HashMap<>();
            actualizacion.put("latitud", coordenadas[0]);
            actualizacion.put("longitud", coordenadas[1]);
            actualizacion.put("ultimaActualizacion", com.google.firebase.Timestamp.now());

            // Actualizar el documento del taxista con las coordenadas
            db.collection("usuarios").document(taxistaId)
                    .update(actualizacion)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Coordenadas agregadas al taxista: " + taxistaId +
                                " - Lat: " + coordenadas[0] + ", Lng: " + coordenadas[1]);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al agregar coordenadas al taxista", e);
                    });
        }
    }

    private void actualizarUbicacionEnMapa(double latitud, double longitud) {
        if (mMap != null) {
            LatLng ubicacionTaxista = new LatLng(latitud, longitud);

            // Limpiar marcadores anteriores
            mMap.clear();

            // Agregar marcador del taxista
            mMap.addMarker(new MarkerOptions()
                    .position(ubicacionTaxista)
                    .title("Tu taxista"));

            // Mover la cámara a la ubicación del taxista
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionTaxista, 15));
        }
    }

    private void cancelarViaje() {
        if (viajeId != null) {
            // Actualizar el estado del viaje a cancelado
            db.collection("viajes").document(viajeId)
                    .update("estado", "cancelado")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Viaje cancelado", Toast.LENGTH_SHORT).show();
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

        // Configurar el mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Ubicación inicial (Lima, Peru)
        LatLng lima = new LatLng(-12.0464, -77.0428);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lima, 10));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remover listeners para evitar memory leaks
        if (tripListener != null) {
            tripListener.remove();
        }
        if (driverLocationListener != null) {
            driverLocationListener.remove();
        }
    }
}