package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.example.hotroid.bean.Admin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SuperListaAdminActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaAdminActivity";

    private LinearLayout linearLayoutAdminsContainer;
    private EditText etBuscador;
    private Button btnLimpiar;

    private static final String CHANNEL_ID = "admin_notifications_channel";
    private static final int NOTIFICATION_ID = 1;

    // Ya no se usan como constantes numéricas directas para ActivityResultLauncher
    // private static final int REQUEST_CODE_FORMULARIO = 100;
    // private static final int REQUEST_CODE_DETALLES_ADMIN = 101;

    public List<Admin> adminDataList = new ArrayList<>();
    public List<Admin> filteredAdminList = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> formResultLauncher;
    private ActivityResultLauncher<Intent> detailsResultLauncher;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_admins);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        createNotificationChannel();

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Permiso de notificación concedido.");
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
                    }
                });

        detailsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleDetailsResult(result.getData());
                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        linearLayoutAdminsContainer = findViewById(R.id.linearLayoutAdminsContainer);
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        loadAdminsFromFirestore();

        setupSearchFunctionality();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperListaAdminActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperListaAdminActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                finish();
                return true;
            }
            return false;
        });

        // Asegúrate de que cardSuper y tvNombreAdminActual estén en tu XML super_lista_admins.xml si los quieres aquí.
        // Si no, este bloque causará un error "Cannot resolve symbol".
        CardView cardSuper = findViewById(R.id.cardSuper);
        if (cardSuper != null ) { // Agregué null checks

            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });
        }


        Button botonRegistrar = findViewById(R.id.button_regist);
        botonRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminFormularioActivity.class);
            formResultLauncher.launch(intent);
        });
    }

    private void loadAdminsFromFirestore() {
        adminDataList.clear();
        filteredAdminList.clear();
        linearLayoutAdminsContainer.removeAllViews();

        db.collection("admins")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Admin admin = document.toObject(Admin.class);
                                admin.setFirestoreId(document.getId());
                                adminDataList.add(admin);
                                Log.d(TAG, "Admin cargado: " + document.getId() + " => " + document.getData());
                            }
                            filteredAdminList.addAll(adminDataList);
                            renderAdminList();
                            Log.d(TAG, "Admins cargados desde Firestore: " + adminDataList.size());

                            // Este bloque de código es solo para poblar la base de datos si está vacía.
                            // *** DESCOMENTAR Y EJECUTAR UNA SOLA VEZ SI TU BASE DE DATOS DE FIREBASE ESTÁ VACÍA.
                            // *** LUEGO VUELVE A COMENTARLO.
                        /*
                        if (adminDataList.isEmpty()) {
                            addInitialAdminsToFirestore();
                        }
                        */

                        } else {
                            Log.w(TAG, "Error al obtener documentos: ", task.getException());
                            Toast.makeText(SuperListaAdminActivity.this, "Error al cargar administradores.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addInitialAdminsToFirestore() {
        List<Admin> initialAdmins = Arrays.asList(
                new Admin("Victor", "Díaz Pérez", "true", "12345678", "01/01/1990", "victor@example.com", "987654321", "Calle Los Girasoles 123", "Hotel Oro Verde"),
                new Admin("Moises", "Castro Rivas", "false", "87654321", "05/03/1985", "moises@example.com", "912345678", "Av. San Martín 456", "-"),
                new Admin("Manuel", "Yarleque Soto", "true", "11223344", "10/11/1992", "manuel@example.com", "998877665", "Jr. Los Olivos 789", "Sauce Resort"),
                new Admin("Ana", "García López", "false", "22334455", "15/07/1991", "ana.g@example.com", "900112233", "Pasaje La Alameda 101", "-"),
                new Admin("Carlos", "Ruiz Vargas", "true", "33445566", "20/04/1988", "carlos.r@example.com", "987123456", "Urbanización El Sol 202", "Hotel Decameron"),
                new Admin("Elena", "Soto Montes", "false", "44556677", "03/09/1995", "elena.s@example.com", "976543210", "Prolongación La Marina 303", "-"),
                new Admin("Jorge", "Vargas Ríos", "true", "55667788", "12/12/1980", "jorge.v@example.com", "911223344", "Avenida Los Álamos 404", "Hotel Libertador"),
                new Admin("Laura", "Perez Luna", "false", "66778899", "25/02/1993", "laura.p@example.com", "922334455", "Jirón Puno 505", "-"),
                new Admin("Miguel Ángel", "Ramírez Torres", "true", "77889900", "08/06/1987", "miguel.r@example.com", "933445566", "Calle Las Begonias 606", "Hotel Costa del Sol"),
                new Admin("Sofía", "Bustamante Paz", "false", "00112233", "29/10/1994", "sofia.b@example.com", "944556677", "Av. Arequipa 707", "-")
        );

        for (Admin admin : initialAdmins) {
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_placeholder);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            defaultBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] defaultImageData = baos.toByteArray();

            uploadImageAndSaveAdmin(admin, defaultImageData);
        }
        Toast.makeText(this, "Datos de ejemplo añadiéndose a Firestore.", Toast.LENGTH_SHORT).show();
    }


    private void setupSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAdminList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesario
            }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetAdminList();
        });
    }

    private void filterAdminList(String searchText) {
        filteredAdminList.clear();

        if (searchText.isEmpty()) {
            filteredAdminList.addAll(adminDataList);
        } else {
            String searchLower = searchText.toLowerCase(Locale.getDefault()).trim();
            for (Admin admin : adminDataList) {
                if ((admin.getNombres() + " " + admin.getApellidos()).toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getHotelAsignado().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    filteredAdminList.add(admin);
                }
            }
        }
        renderAdminList();
    }

    private void resetAdminList() {
        filteredAdminList.clear();
        filteredAdminList.addAll(adminDataList);
        renderAdminList();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Administradores";
            String description = "Canal para notificaciones de registro y estado de administradores";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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

    private void renderAdminList() {
        linearLayoutAdminsContainer.removeAllViews();

        if (filteredAdminList.isEmpty()) {
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron administradores de hotel.");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutAdminsContainer.addView(noResultsText);
            return;
        }

        for (int i = 0; i < filteredAdminList.size(); i++) {
            Admin admin = filteredAdminList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View adminItemView = inflater.inflate(R.layout.item_admin_card, linearLayoutAdminsContainer, false);

            TextView tvAdminName = adminItemView.findViewById(R.id.tvAdminName);
            TextView tvAdminHotel = adminItemView.findViewById(R.id.tvAdminHotel);
            TextView tvAdminStatus = adminItemView.findViewById(R.id.tvAdminStatus);

            tvAdminName.setText(admin.getNombres() + " " + admin.getApellidos());

            boolean isActivado = Boolean.parseBoolean(admin.getEstado());
            if (isActivado) {
                tvAdminHotel.setText("Hotel: " + admin.getHotelAsignado());
                tvAdminStatus.setText("Estado: Activado");
                tvAdminStatus.setTextColor(ContextCompat.getColor(this, R.color.green_status));
            } else {
                tvAdminHotel.setText("Hotel: -");
                tvAdminStatus.setText("Estado: Desactivado");
                tvAdminStatus.setTextColor(ContextCompat.getColor(this, R.color.red_status));
            }

            final String adminFirestoreId = admin.getFirestoreId();

            adminItemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminActivity.class);

                intent.putExtra("admin_firestore_id", admin.getFirestoreId());
                intent.putExtra("admin_nombres", admin.getNombres());
                intent.putExtra("admin_apellidos", admin.getApellidos());
                intent.putExtra("admin_estado", admin.getEstado());
                intent.putExtra("admin_dni", admin.getDni());
                intent.putExtra("admin_nacimiento", admin.getNacimiento());
                intent.putExtra("admin_correo", admin.getCorreo());
                intent.putExtra("admin_telefono", admin.getTelefono());
                intent.putExtra("admin_direccion", admin.getDireccion());
                intent.putExtra("admin_hotelAsignado", admin.getHotelAsignado());
                intent.putExtra("admin_fotoPerfilUrl", admin.getFotoPerfilUrl()); // Pasar la URL

                detailsResultLauncher.launch(intent);
            });

            linearLayoutAdminsContainer.addView(adminItemView);
        }
    }

    private void handleFormResult(Intent data) {
        String action = data.getStringExtra("action");
        if ("registrado".equals(action)) {
            String adminNombres = data.getStringExtra("admin_nombres");
            String adminApellidos = data.getStringExtra("admin_apellidos");
            String dni = data.getStringExtra("admin_dni");
            String estado = data.getStringExtra("admin_estado");
            String nacimiento = data.getStringExtra("admin_nacimiento");
            String correo = data.getStringExtra("admin_correo");
            String telefono = data.getStringExtra("admin_telefono");
            String direccion = data.getStringExtra("admin_direccion");
            String hotelAsignado = data.getStringExtra("admin_hotelAsignado");
            byte[] adminFotoPerfilBytes = data.getByteArrayExtra("admin_fotoPerfilBytes");

            Admin nuevoAdmin = new Admin(
                    adminNombres,
                    adminApellidos,
                    estado,
                    dni,
                    nacimiento,
                    correo,
                    telefono,
                    direccion,
                    hotelAsignado
            );

            uploadImageAndSaveAdmin(nuevoAdmin, adminFotoPerfilBytes);
            String statusMessage = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido registrado correctamente.";
            Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
            showNotification("Actualización de Administrador de Hotel", statusMessage);
        }
    }

    private void handleDetailsResult(Intent data) {
        String action = data.getStringExtra("action");
        String adminFirestoreId = data.getStringExtra("admin_firestore_id");
        String adminNombres = data.getStringExtra("admin_nombres");
        String adminApellidos = data.getStringExtra("admin_apellidos");
        String nuevoHotelAsignado = data.getStringExtra("nuevo_hotel_asignado");

        String statusMessage = "";

        Admin adminToUpdate = null;
        for (Admin admin : adminDataList) {
            if (admin.getFirestoreId() != null && admin.getFirestoreId().equals(adminFirestoreId)) {
                adminToUpdate = admin;
                break;
            }
        }

        if (adminToUpdate != null) {
            switch (action) {
                case "activado":
                    adminToUpdate.setEstado("true");
                    if (nuevoHotelAsignado != null) {
                        adminToUpdate.setHotelAsignado(nuevoHotelAsignado);
                        statusMessage = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido activado y asignado a " + nuevoHotelAsignado + ".";
                    } else {
                        statusMessage = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido activado.";
                    }
                    updateAdminInFirestore(adminToUpdate);
                    break;
                case "desactivado":
                    adminToUpdate.setHotelAsignado("-");
                    adminToUpdate.setEstado("false");
                    statusMessage = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido desactivado y su hotel desasignado.";
                    updateAdminInFirestore(adminToUpdate);
                    break;
            }
        } else {
            Log.w(TAG, "No se encontró el administrador con ID: " + adminFirestoreId + " para actualizar.");
            statusMessage = "Error al actualizar administrador.";
        }

        if (!statusMessage.isEmpty()) {
            Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
            showNotification("Actualización de Administrador de Hotel", statusMessage);
        }

        // loadAdminsFromFirestore(); // Esto ya se llama dentro de saveAdminToFirestore y updateAdminInFirestore
    }


    private void uploadImageAndSaveAdmin(Admin admin, @Nullable byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            String fileName = "admin_photos/" + admin.getDni() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    admin.setFotoPerfilUrl(imageUrl);
                    saveAdminToFirestore(admin);
                    Log.d(TAG, "Imagen subida a Storage. URL: " + imageUrl);
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Error al obtener la URL de descarga de la imagen", e);
                    Toast.makeText(this, "Error al obtener URL de imagen. Guardando admin sin foto.", Toast.LENGTH_SHORT).show();
                    admin.setFotoPerfilUrl("");
                    saveAdminToFirestore(admin);
                });
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Error al subir la imagen a Storage", e);
                Toast.makeText(this, "Error al subir la imagen. Guardando admin sin foto.", Toast.LENGTH_SHORT).show();
                admin.setFotoPerfilUrl("");
                saveAdminToFirestore(admin);
            });
        } else {
            Log.d(TAG, "No hay imagen para subir o la imagen es nula/vacía. Guardando admin sin foto.");
            admin.setFotoPerfilUrl("");
            saveAdminToFirestore(admin);
        }
    }

    private void saveAdminToFirestore(Admin admin) {
        db.collection("admins")
                .add(admin)
                .addOnSuccessListener(documentReference -> {
                    admin.setFirestoreId(documentReference.getId());
                    Log.d(TAG, "Admin guardado en Firestore con ID: " + documentReference.getId());
                    loadAdminsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar admin en Firestore", e);
                    Toast.makeText(this, "Error al guardar admin en la base de datos.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAdminInFirestore(Admin admin) {
        if (admin.getFirestoreId() != null && !admin.getFirestoreId().isEmpty()) {
            db.collection("admins").document(admin.getFirestoreId())
                    .set(admin)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Admin actualizado en Firestore: " + admin.getNombres() + " " + admin.getApellidos());
                        loadAdminsFromFirestore();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al actualizar admin en Firestore", e);
                        Toast.makeText(SuperListaAdminActivity.this, "Error al actualizar admin.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w(TAG, "No se puede actualizar Admin sin un ID de Firestore (firestoreId nulo o vacío).");
            Toast.makeText(this, "Error: Admin no tiene ID para actualizar.", Toast.LENGTH_SHORT).show();
        }
    }
}