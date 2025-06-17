package com.example.hotroid;

import com.google.android.gms.tasks.Tasks;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.hotroid.bean.Taxista; // Asegúrate que la importación a tu clase Taxista sea correcta
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperListaTaxisActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaTaxisActivity";

    private LinearLayout linearLayoutTaxisContainer;
    private EditText etBuscador;
    private Button btnLimpiar;

    private static final String CHANNEL_ID = "taxi_management_channel";
    private static final int NOTIFICATION_ID = 1001;

    public List<Taxista> taxiDataList = new ArrayList<>();
    public List<Taxista> filteredTaxiList = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> formResultLauncher;
    private ActivityResultLauncher<Intent> detailsResultLauncher;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_taxis);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        createNotificationChannel();

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Permiso de notificación concedido.");
                        Toast.makeText(this, "Permiso de notificaciones concedido.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Permiso de notificación denegado. Algunas notificaciones no se mostrarán.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Permiso de notificación denegado.");
                    }
                });

        formResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleFormResult(result.getData());
                        // loadTaxisFromFirestore() will be called by onResume after this activity resumes
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.d(TAG, "Formulario de taxista cancelado.");
                        Toast.makeText(this, "Registro de taxista cancelado.", Toast.LENGTH_SHORT).show();
                        // loadTaxisFromFirestore() will be called by onResume after this activity resumes
                    }
                });

        detailsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Regardless of RESULT_OK or RESULT_CANCELED, onResume will be called
                    // when SuperListaTaxisActivity becomes foreground.
                    // So, we only need to handle notifications here, and let onResume handle data refresh.
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String action = result.getData().getStringExtra("action");
                        String taxistaNombres = result.getData().getStringExtra("taxista_nombres");
                        String taxistaApellidos = result.getData().getStringExtra("taxista_apellidos");

                        String notificationMessage = "";
                        if (taxistaNombres != null && taxistaApellidos != null) {
                            switch (action) {
                                case "activado":
                                    notificationMessage = "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido activado.";
                                    break;
                                case "desactivado":
                                    notificationMessage = "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido desactivado.";
                                    break;
                                case "aprobado":
                                    notificationMessage = "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido aprobado y activado.";
                                    break;
                                case "rechazado":
                                    notificationMessage = "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido rechazado y eliminado.";
                                    break;
                                case "actualizado": // Assuming "actualizado" for general edits
                                    notificationMessage = "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido actualizado.";
                                    break;
                                default:
                                    notificationMessage = "Acción de taxista completada.";
                                    break;
                            }
                            Toast.makeText(this, notificationMessage, Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Taxista", notificationMessage);
                        } else {
                            Toast.makeText(this, "Acción de taxista completada. (Datos de taxista no disponibles para notificación)", Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Taxista", "Acción de taxista completada.");
                        }
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.d(TAG, "Detalles de taxista cerrados sin cambios.");
                    }
                    // loadTaxisFromFirestore() will be called by onResume when this activity resumes
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        linearLayoutTaxisContainer = findViewById(R.id.linearLayoutTaxisContainer);
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // Do NOT call loadTaxisFromFirestore() here. It will be called in onResume().
        // loadTaxisFromFirestore();

        setupSearchFunctionality();

        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvRol = findViewById(R.id.tvRol);
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil);

        if (tvTitulo != null) {
            tvTitulo.setText("Gestión de Taxistas");
        }
        if (tvNombre != null) {
            tvNombre.setText("Pedro Bustamante");
        }
        if (tvRol != null) {
            tvRol.setText("Super Administrador");
        }
        if (imagenPerfil != null) {
            imagenPerfil.setImageResource(R.drawable.foto_admin);
        }

        CardView cardSuper = findViewById(R.id.cardSuper);
        if (cardSuper != null) {
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaTaxisActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    Intent intentInicio = new Intent(SuperListaTaxisActivity.this, SuperActivity.class);
                    startActivity(intentInicio);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperListaTaxisActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        }

        // Optional: Call addInitialTaxisToFirestore() ONCE to populate your DB for testing
        // You should comment this out or remove it after the first run.
        // addInitialTaxisToFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // This is the primary place to load data, ensuring it's fresh whenever the activity
        // comes into the foreground (after onCreate, or returning from another activity).
        loadTaxisFromFirestore();
    }

    /**
     * Carga la lista de taxistas desde Firestore.
     */
    private void loadTaxisFromFirestore() {
        taxiDataList.clear();
        filteredTaxiList.clear();
        linearLayoutTaxisContainer.removeAllViews();

        db.collection("taxistas")
                .orderBy("nombres", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Taxista taxista = document.toObject(Taxista.class);
                            taxista.setFirestoreId(document.getId());
                            taxiDataList.add(taxista);
                            Log.d(TAG, "Taxista cargado: " + document.getId() + " => " + document.getData());
                        }

                        filterTaxiList(etBuscador.getText().toString());
                        Log.d(TAG, "Taxistas cargados desde Firestore: " + taxiDataList.size());
                    } else {
                        Log.w(TAG, "Error al obtener documentos de taxistas: ", task.getException());
                        Toast.makeText(SuperListaTaxisActivity.this, "Error al cargar taxistas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Añade un conjunto de taxistas iniciales a Firestore.
     * **Solo debe ejecutarse una vez para poblar la base de datos.**
     * Make sure you have `taxista1.png` and `car_taxi_driver.png` in your drawable folder.
     */
    private void addInitialTaxisToFirestore() {
        List<Taxista> initialTaxis = Arrays.asList(
                new Taxista("Carlos Joaquín", "Alvarez Retes", "DNI", "12345678", "1990-01-15",
                        "carlos@example.com", "987654321", "Av. Siempre Viva 123",
                        null, // Placeholder, will use default image bytes
                        "ABC-123", null, // Placeholder, will use default image bytes
                        "activado", "En Camino"
                ),
                new Taxista(
                        "Alex David", "Russo Muro", "DNI", "87654321", "1988-05-20",
                        "alex@example.com", "912345678", "Calle Falsa 456",
                        null,
                        "DEF-456", null,
                        "activado", "Asignado"
                ),
                new Taxista(
                        "Marcelo Juan", "Vilca Lora", "DNI", "11223344", "1992-11-01",
                        "marcelo@example.com", "934567890", "Jr. Luna 789",
                        null,
                        "GHI-789", null,
                        "activado", "No Asignado"
                ),
                new Taxista(
                        "Arturo Manuel", "Delgado Flores", "DNI", "55667788", "1995-07-25",
                        "arturo@example.com", "978901234", "Pje. Estrella 202",
                        null,
                        "MNO-202", null,
                        "desactivado", "No Asignado"
                ),
                new Taxista(
                        "Farid Antony", "Puente Aguilar", "DNI", "44332211", "1991-09-05",
                        "farith@example.com", "990123456", "Cl. Diamante 303",
                        null,
                        "PQR-303", null,
                        "pendiente", "No Asignado"
                )
        );

        for (Taxista taxi : initialTaxis) {
            Bitmap profileBitmap = null;
            Bitmap vehicleBitmap = null;

            switch (taxi.getNumeroDocumento()) {
                case "12345678": // Carlos Joaquín
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista1);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car_taxi_driver);
                    break;
                case "87654321": // Alex David
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista2);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carrito);
                    break;
                case "11223344": // Marcelo Juan
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista3);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car_taxi_driver);
                    break;
                case "55667788": // Arturo Manuel
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista5);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car_taxi_driver);
                    break;
                case "44332211": // Farid Antony
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista6);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.carrito);
                    break;
                default:
                    profileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.taxista1);
                    vehicleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.car_taxi_driver);
                    break;
            }

            ByteArrayOutputStream profileBaos = new ByteArrayOutputStream();
            if (profileBitmap != null) {
                profileBitmap.compress(Bitmap.CompressFormat.JPEG, 80, profileBaos);
            }
            byte[] profileImageData = profileBaos.toByteArray();

            ByteArrayOutputStream vehicleBaos = new ByteArrayOutputStream();
            if (vehicleBitmap != null) {
                vehicleBitmap.compress(Bitmap.CompressFormat.JPEG, 80, vehicleBaos);
            }
            byte[] vehicleImageData = vehicleBaos.toByteArray();

            final String notificationMessage = "Taxista inicial " + taxi.getNombres() + " " + taxi.getApellidos() + " registrado.";
            uploadImagesAndSaveTaxi(taxi, profileImageData, vehicleImageData, notificationMessage);
        }
        Log.d(TAG, "Inicio de carga de taxistas iniciales a Firestore y Storage.");
    }

    private void setupSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /* No op */ }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTaxiList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { /* No op */ }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetTaxiList();
        });
    }

    /**
     * Filtra la lista de taxistas basándose en el texto de búsqueda.
     */
    private void filterTaxiList(String searchText) {
        filteredTaxiList.clear();

        if (searchText.isEmpty()) {
            filteredTaxiList.addAll(taxiDataList);
        } else {
            String searchLower = searchText.toLowerCase(Locale.getDefault()).trim();
            for (Taxista taxi : taxiDataList) {
                if ((taxi.getNombres() + " " + taxi.getApellidos()).toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        taxi.getPlaca().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        taxi.getNumeroDocumento().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        taxi.getCorreo().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        (taxi.getEstado() != null && taxi.getEstado().toLowerCase(Locale.getDefault()).contains(searchLower)) ||
                        (taxi.getEstadoDeViaje() != null && taxi.getEstadoDeViaje().toLowerCase(Locale.getDefault()).contains(searchLower))) {
                    filteredTaxiList.add(taxi);
                }
            }
        }
        renderTaxiList();
    }

    /**
     * Resetea la lista filtrada para mostrar todos los taxistas.
     */
    private void resetTaxiList() {
        filteredTaxiList.clear();
        filteredTaxiList.addAll(taxiDataList);
        renderTaxiList();
    }

    /**
     * Crea el canal de notificación para Android 8.0 (Oreo) y superior.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Taxistas";
            String description = "Canal para notificaciones de registro y estado de taxistas";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Muestra una notificación. Requiere permiso POST_NOTIFICATIONS en Android 13+.
     */
    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. No se pudo mostrar la notificación.");
            Toast.makeText(this, "Permiso de notificaciones denegado, no se pudo mostrar la alerta.", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Renderiza la lista de taxistas en el LinearLayout.
     */
    private void renderTaxiList() {
        linearLayoutTaxisContainer.removeAllViews();

        if (filteredTaxiList.isEmpty()) {
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron taxistas.");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutTaxisContainer.addView(noResultsText);
            return;
        }

        for (int i = 0; i < filteredTaxiList.size(); i++) {
            Taxista taxi = filteredTaxiList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View taxiItemView = inflater.inflate(R.layout.item_taxi_card, linearLayoutTaxisContainer, false);

            TextView tvTaxiName = taxiItemView.findViewById(R.id.tvAdminName);
            tvTaxiName.setText(taxi.getNombres() + " " + taxi.getApellidos());

            TextView tvTaxiStatusTrip = taxiItemView.findViewById(R.id.tvAdminStatusTrip);
            tvTaxiStatusTrip.setText("Viaje: " + taxi.getEstadoDeViaje());

            TextView tvTaxiStatus = taxiItemView.findViewById(R.id.tvAdminStatus);
            String estadoActivacion = taxi.getEstado();

            if (estadoActivacion != null) {
                switch (estadoActivacion.toLowerCase(Locale.getDefault())) {
                    case "activado":
                        tvTaxiStatus.setText("Estado: Activado");
                        tvTaxiStatus.setTextColor(ContextCompat.getColor(this, R.color.green_status));
                        break;
                    case "desactivado":
                        tvTaxiStatus.setText("Estado: Desactivado");
                        tvTaxiStatus.setTextColor(ContextCompat.getColor(this, R.color.red_status));
                        break;
                    case "pendiente":
                        tvTaxiStatus.setText("Estado: Pendiente");
                        tvTaxiStatus.setTextColor(ContextCompat.getColor(this, R.color.naranja)); // Usando naranja
                        break;
                    default:
                        tvTaxiStatus.setText("Estado: Desconocido");
                        tvTaxiStatus.setTextColor(ContextCompat.getColor(this, R.color.gray_tone)); // Usando gray_tone
                        break;
                }
            } else {
                tvTaxiStatus.setText("Estado: N/A");
                tvTaxiStatus.setTextColor(ContextCompat.getColor(this, R.color.gray_tone));
            }

            taxiItemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiActivity.class);

                intent.putExtra("taxista_firestore_id", taxi.getFirestoreId());
                intent.putExtra("taxista_nombres", taxi.getNombres());
                intent.putExtra("taxista_apellidos", taxi.getApellidos());
                intent.putExtra("taxista_tipo_documento", taxi.getTipoDocumento());
                intent.putExtra("taxista_numero_documento", taxi.getNumeroDocumento());
                intent.putExtra("taxista_nacimiento", taxi.getNacimiento());
                intent.putExtra("taxista_correo", taxi.getCorreo());
                intent.putExtra("taxista_telefono", taxi.getTelefono());
                intent.putExtra("taxista_direccion", taxi.getDireccion());
                intent.putExtra("taxista_foto_perfil_url", taxi.getFotoPerfilUrl());
                intent.putExtra("taxista_placa", taxi.getPlaca());
                intent.putExtra("taxista_foto_vehiculo_url", taxi.getFotoVehiculoUrl());
                intent.putExtra("taxista_estado", taxi.getEstado());
                intent.putExtra("taxista_estado_viaje", taxi.getEstadoDeViaje());

                Log.d(TAG, "Enviando taxista con firestoreId: " + taxi.getFirestoreId());

                detailsResultLauncher.launch(intent);
            });

            linearLayoutTaxisContainer.addView(taxiItemView);
        }
    }

    private void handleFormResult(Intent data) {
        String action = data.getStringExtra("action");
        if ("registrado".equals(action)) {
            String nombres = data.getStringExtra("taxista_nombres");
            String apellidos = data.getStringExtra("taxista_apellidos");
            String tipoDocumento = data.getStringExtra("taxista_tipo_documento");
            String numeroDocumento = data.getStringExtra("taxista_numero_documento");
            String nacimiento = data.getStringExtra("taxista_nacimiento");
            String correo = data.getStringExtra("taxista_correo");
            String telefono = data.getStringExtra("taxista_telefono");
            String direccion = data.getStringExtra("taxista_direccion");
            String placa = data.getStringExtra("taxista_placa");
            String estado = data.getStringExtra("taxista_estado");
            String estadoDeViaje = data.getStringExtra("taxista_estado_viaje");
            byte[] fotoPerfilBytes = data.getByteArrayExtra("taxista_foto_perfil_bytes");
            byte[] fotoVehiculoBytes = data.getByteArrayExtra("taxista_foto_vehiculo_bytes");

            Taxista nuevoTaxista = new Taxista(
                    nombres, apellidos, tipoDocumento, numeroDocumento, nacimiento,
                    correo, telefono, direccion, "", placa, "", estado, estadoDeViaje
            );

            final String finalStatusMessage = "El taxista " + nombres + " " + apellidos + " ha sido registrado correctamente.";
            uploadImagesAndSaveTaxi(nuevoTaxista, fotoPerfilBytes, fotoVehiculoBytes, finalStatusMessage);
        }
    }

    /**
     * Sube las fotos de perfil y vehículo a Firebase Storage y luego guarda los datos del taxista en Firestore.
     *
     * @param taxista           El objeto Taxista con los datos.
     * @param profileImageData  Bytes de la imagen de perfil. Puede ser null/vacío.
     * @param vehicleImageData  Bytes de la imagen del vehículo. Puede ser null/vacío.
     * @param notificationMessage Mensaje para la notificación después de guardar.
     */
    private void uploadImagesAndSaveTaxi(Taxista taxista, @Nullable byte[] profileImageData, @Nullable byte[] vehicleImageData, final String notificationMessage) {
        Task<Uri> profileUploadTask = null;
        Task<Uri> vehicleUploadTask = null;

        if (profileImageData != null && profileImageData.length > 0) {
            String profileFileName = "taxi_profile_photos/" + taxista.getNumeroDocumento() + "_" + System.currentTimeMillis() + "_profile.jpg";
            StorageReference profileImageRef = storage.getReference().child(profileFileName);
            profileUploadTask = profileImageRef.putBytes(profileImageData)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return profileImageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        taxista.setFotoPerfilUrl(uri.toString());
                        Log.d(TAG, "Foto de perfil de taxista subida. URL: " + uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al subir foto de perfil de taxista", e);
                        Toast.makeText(this, "Error al subir foto de perfil.", Toast.LENGTH_SHORT).show();
                    });
        }

        if (vehicleImageData != null && vehicleImageData.length > 0) {
            String vehicleFileName = "taxi_vehicle_photos/" + taxista.getPlaca() + "_" + System.currentTimeMillis() + "_vehicle.jpg";
            StorageReference vehicleImageRef = storage.getReference().child(vehicleFileName);
            vehicleUploadTask = vehicleImageRef.putBytes(vehicleImageData)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return vehicleImageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        taxista.setFotoVehiculoUrl(uri.toString());
                        Log.d(TAG, "Foto de vehículo de taxista subida. URL: " + uri.toString());
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al subir foto de vehículo de taxista", e);
                        Toast.makeText(this, "Error al subir foto de vehículo.", Toast.LENGTH_SHORT).show();
                    });
        }

        Task<Void> combinedUploadTask;
        if (profileUploadTask != null && vehicleUploadTask != null) {
            combinedUploadTask = Tasks.whenAll(profileUploadTask, vehicleUploadTask);
        } else if (profileUploadTask != null) {
            combinedUploadTask = profileUploadTask.continueWith(task -> null);
        } else if (vehicleUploadTask != null) {
            combinedUploadTask = vehicleUploadTask.continueWith(task -> null);
        } else {
            saveTaxiToFirestore(taxista, notificationMessage);
            return;
        }

        combinedUploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveTaxiToFirestore(taxista, notificationMessage);
            } else {
                Log.w(TAG, "Fallo en la subida combinada de imágenes. Guardando taxista sin URLs de imagen.", task.getException());
                Toast.makeText(this, "Algunas imágenes no se pudieron subir. Guardando taxista sin ellas.", Toast.LENGTH_LONG).show();
                saveTaxiToFirestore(taxista, notificationMessage);
            }
        });
    }

    /**
     * Guarda el objeto Taxista en Firestore.
     * @param taxista El objeto Taxista a guardar.
     * @param notificationMessage Mensaje a mostrar en la notificación.
     */
    private void saveTaxiToFirestore(Taxista taxista, String notificationMessage) {
        if (taxista.getFirestoreId() != null && !taxista.getFirestoreId().isEmpty()) {
            db.collection("taxistas").document(taxista.getFirestoreId())
                    .set(taxista)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Taxista actualizado en Firestore con ID: " + taxista.getFirestoreId());
                        Toast.makeText(this, "Taxista actualizado correctamente.", Toast.LENGTH_SHORT).show();
                        showNotification("Taxista Actualizado", notificationMessage);
                        // No need to call loadTaxisFromFirestore() here directly, onResume will handle it.
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al actualizar taxista en Firestore", e);
                        Toast.makeText(this, "Error al actualizar taxista.", Toast.LENGTH_SHORT).show();
                        showNotification("Error", "Falló la actualización del taxista.");
                    });
        } else {
            db.collection("taxistas")
                    .add(taxista)
                    .addOnSuccessListener(documentReference -> {
                        taxista.setFirestoreId(documentReference.getId());
                        Log.d(TAG, "Taxista añadido a Firestore con ID: " + documentReference.getId());
                        Toast.makeText(this, "Taxista registrado correctamente.", Toast.LENGTH_SHORT).show();
                        showNotification("Nuevo Taxista Registrado", notificationMessage);
                        // No need to call loadTaxisFromFirestore() here directly, onResume will handle it.
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al añadir taxista a Firestore", e);
                        Toast.makeText(this, "Error al registrar taxista.", Toast.LENGTH_SHORT).show();
                        showNotification("Error", "Falló el registro del taxista.");
                    });
        }
    }

    // This method is now effectively not called directly from this activity if delete logic is in details.
    // Keeping it here for completeness if you have other delete paths.
    // If it's only deleted via SuperDetallesTaxiActivity, this method can be removed or kept as a utility.
    /**
     * Elimina un taxista de Firestore y sus imágenes asociadas en Storage.
     * @param firestoreId ID del documento del taxista a eliminar.
     * @param taxistaNombres Nombre del taxista para la notificación.
     * @param taxistaApellidos Apellido del taxista para la notificación.
     */
    private void deleteTaxiFromFirestore(String firestoreId, String taxistaNombres, String taxistaApellidos) {
        db.collection("taxistas").document(firestoreId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Taxista taxistaToDelete = documentSnapshot.toObject(Taxista.class);
                        List<Task<Void>> deleteImageTasks = new ArrayList<>();

                        if (taxistaToDelete != null && taxistaToDelete.getFotoPerfilUrl() != null && !taxistaToDelete.getFotoPerfilUrl().isEmpty()) {
                            try {
                                StorageReference profileRef = storage.getReferenceFromUrl(taxistaToDelete.getFotoPerfilUrl());
                                deleteImageTasks.add(profileRef.delete()
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Foto de perfil eliminada: " + taxistaToDelete.getFotoPerfilUrl()))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar foto de perfil: " + taxistaToDelete.getFotoPerfilUrl(), e)));
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "URL de foto de perfil inválida, no se pudo eliminar: " + taxistaToDelete.getFotoPerfilUrl(), e);
                            }
                        }

                        if (taxistaToDelete != null && taxistaToDelete.getFotoVehiculoUrl() != null && !taxistaToDelete.getFotoVehiculoUrl().isEmpty()) {
                            try {
                                StorageReference vehicleRef = storage.getReferenceFromUrl(taxistaToDelete.getFotoVehiculoUrl());
                                deleteImageTasks.add(vehicleRef.delete()
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Foto de vehículo eliminada: " + taxistaToDelete.getFotoVehiculoUrl()))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar foto de vehículo: " + taxistaToDelete.getFotoVehiculoUrl(), e)));
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "URL de foto de vehículo inválida, no se pudo eliminar: " + taxistaToDelete.getFotoVehiculoUrl(), e);
                            }
                        }

                        Tasks.whenAllComplete(deleteImageTasks)
                                .addOnCompleteListener(imageDeleteTask -> {
                                    db.collection("taxistas").document(firestoreId).delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Taxista eliminado de Firestore: " + firestoreId);
                                                Toast.makeText(this, "Taxista " + taxistaNombres + " " + taxistaApellidos + " eliminado.", Toast.LENGTH_SHORT).show();
                                                showNotification("Taxista Eliminado", "El taxista " + taxistaNombres + " " + taxistaApellidos + " ha sido eliminado.");
                                                // No need to call loadTaxisFromFirestore() here directly, onResume will handle it.
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error al eliminar taxista de Firestore", e);
                                                Toast.makeText(this, "Error al eliminar taxista.", Toast.LENGTH_SHORT).show();
                                                showNotification("Error", "Falló la eliminación del taxista.");
                                            });
                                });
                    } else {
                        Log.d(TAG, "Documento de taxista no encontrado para eliminar: " + firestoreId);
                        Toast.makeText(this, "Taxista no encontrado para eliminar.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al obtener documento del taxista para eliminación", e);
                    Toast.makeText(this, "Error al obtener datos del taxista para eliminación.", Toast.LENGTH_SHORT).show();
                });
    }

}