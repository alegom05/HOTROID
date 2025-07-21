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

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Admin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
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

public class SuperListaAdminActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaAdminActivity";

    private LinearLayout linearLayoutAdminsContainer;
    private EditText etBuscador;
    private Button btnLimpiar;
    private Button btnFilterActive;
    private Button btnFilterInactive;

    // Track the currently selected filter status
    private String currentFilterStatus = "all"; // "all", "true", or "false"

    private static final String CHANNEL_ID = "admin_notifications_channel";
    private static final int NOTIFICATION_ID = 1;

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
        btnFilterActive = findViewById(R.id.btnFilterActive);
        btnFilterInactive = findViewById(R.id.btnFilterInactive);

        loadAdminsFromFirestore();

        setupSearchFunctionality();
        setupFilterFunctionality(); // Method for filter buttons

        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvRol = findViewById(R.id.tvRol);
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil);

        if (tvTitulo != null) {
            tvTitulo.setText("Lista de Administradores");
        }
        if (tvNombre != null) {
            tvNombre.setText("Pedro Bustamante");
        }
        if (tvRol != null) {
            tvRol.setText("Administrador General");
        }
        if (imagenPerfil != null) {
            imagenPerfil.setImageResource(R.drawable.foto_super); // Make sure this drawable exists
        }

        CardView cardSuper = findViewById(R.id.cardSuper);
        if (cardSuper != null) {
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });
        }

        Button botonRegistrar = findViewById(R.id.button_regist);
        if (botonRegistrar != null) {
            botonRegistrar.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminFormularioActivity.class);
                formResultLauncher.launch(intent);
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    Intent intentInicio = new Intent(SuperListaAdminActivity.this, SuperActivity.class);
                    startActivity(intentInicio);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    return true; // Already on this activity
                } else if (itemId == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperListaAdminActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void loadAdminsFromFirestore() {
        adminDataList.clear();
        filteredAdminList.clear();
        linearLayoutAdminsContainer.removeAllViews();

        db.collection("admins")
                .orderBy("nombres", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Admin admin = document.toObject(Admin.class);
                            admin.setFirestoreId(document.getId());
                            adminDataList.add(admin);
                            Log.d(TAG, "Admin cargado: " + document.getId() + " => " + document.getData());
                        }
                        // After loading, apply the current filter status
                        filterAdminList(etBuscador.getText().toString(), currentFilterStatus);
                        Log.d(TAG, "Admins cargados desde Firestore: " + adminDataList.size());

                        // --- ADICIÓN DE ADMINISTRADORES DE PRUEBA ---
                        // Comentar esta sección después de la primera ejecución exitosa
                        // para evitar duplicados en cada inicio de la actividad.
                        /*
                        if (adminDataList.isEmpty()) {
                            addInitialAdminsToFirestore();
                        }
                        */
                        // ------------------------------------------

                    } else {
                        Log.w(TAG, "Error al obtener documentos: ", task.getException());
                        Toast.makeText(SuperListaAdminActivity.this, "Error al cargar administradores.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addInitialAdminsToFirestore() {
        List<Admin> initialAdmins = Arrays.asList(
                new Admin("Ana", "García", "true", "DNI", "12345678A", "1990-05-15", "ana.garcia@example.com", "600111222", "Calle Falsa 123", "Hotel Sol"),
                new Admin("Luis", "Martínez", "true", "Pasaporte", "PAS987654", "1988-11-20", "luis.martinez@example.com", "600333444", "Av. Siempre Viva 45", "Hotel Luna"),
                new Admin("Marta", "Pérez", "false", "DNI", "11223344C", "1995-03-10", "marta.perez@example.com", "600555666", "Plaza Mayor 7", "Hotel Estrella"),
                new Admin("Carlos", "Ruiz", "true", "Carnet de Extranjería", "CE0012345", "1985-07-22", "carlos.ruiz@example.com", "600777888", "Jr. Unión 100", "Hotel Mar"),
                new Admin("Sofía", "Herrera", "false", "DNI", "99887766D", "1992-01-30", "sofia.herrera@example.com", "600999000", "Av. La Paz 250", "Hotel Cielo")
        );

        // Subir cada administrador con la imagen por defecto
        for (Admin admin : initialAdmins) {
            Bitmap defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foto_admin); // Usa tu drawable existente
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            defaultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); // Puedes usar PNG si es necesario
            byte[] imageData = baos.toByteArray();

            // Mensaje para la notificación
            final String notificationMessage = "Administrador de prueba " + admin.getNombres() + " " + admin.getApellidos() + " registrado.";

            // Save admin to Firestore (without image upload for now)
            saveAdminToFirestore(admin, notificationMessage);
        }
    }


    private void setupSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When typing in search, apply current status filter (or all if none selected)
                filterAdminList(s.toString(), currentFilterStatus);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesario
            }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetAdminList(); // Resets both search and status filter
        });
    }

    private void setupFilterFunctionality() {
        btnFilterActive.setOnClickListener(v -> {
            etBuscador.setText(""); // Clear search when filtering by status
            if (currentFilterStatus.equals("true")) {
                currentFilterStatus = "all"; // If already active, toggle to all
            } else {
                currentFilterStatus = "true";
            }
            updateFilterButtonsUI(); // Update button colors
            filterAdminList("", currentFilterStatus);
        });

        btnFilterInactive.setOnClickListener(v -> {
            etBuscador.setText(""); // Clear search when filtering by status
            if (currentFilterStatus.equals("false")) {
                currentFilterStatus = "all"; // If already inactive, toggle to all
            } else {
                currentFilterStatus = "false";
            }
            updateFilterButtonsUI(); // Update button colors
            filterAdminList("", currentFilterStatus);
        });

        // Initialize button colors
        updateFilterButtonsUI();
    }

    private void updateFilterButtonsUI() {
        if (currentFilterStatus.equals("true")) {
            btnFilterActive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_status));
            btnFilterInactive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red_status));
        } else if (currentFilterStatus.equals("false")) {
            btnFilterActive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_status));
            btnFilterInactive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red_status));
        } else { // "all"
            btnFilterActive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green_status));
            btnFilterInactive.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.red_status));
        }
    }

    private void filterAdminList(String searchText, String statusFilter) {
        filteredAdminList.clear();

        String searchLower = searchText.toLowerCase(Locale.getDefault()).trim();

        for (Admin admin : adminDataList) {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // Check search text
            if (!searchText.isEmpty()) {
                matchesSearch = (admin.getNombres() + " " + admin.getApellidos()).toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getHotelAsignado().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getTipoDocumento().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getNumeroDocumento().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getCorreo().toLowerCase(Locale.getDefault()).contains(searchLower);
            }

            // Check status filter
            if (!statusFilter.equals("all")) {
                matchesStatus = admin.getEstado().equals(statusFilter);
            }

            if (matchesSearch && matchesStatus) {
                filteredAdminList.add(admin);
            }
        }
        renderAdminList();
    }


    private void resetAdminList() {
        currentFilterStatus = "all"; // Reset status filter
        updateFilterButtonsUI(); // Update button colors
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
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de que este ícono exista
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

            adminItemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminActivity.class);

                intent.putExtra("admin_firestore_id", admin.getFirestoreId());
                intent.putExtra("admin_nombres", admin.getNombres());
                intent.putExtra("admin_apellidos", admin.getApellidos());
                intent.putExtra("admin_estado", admin.getEstado());
                // CAMBIOS AQUÍ: Pasar tipoDocumento y numeroDocumento
                intent.putExtra("admin_tipo_documento", admin.getTipoDocumento());
                intent.putExtra("admin_numero_documento", admin.getNumeroDocumento());
                intent.putExtra("admin_nacimiento", admin.getNacimiento());
                intent.putExtra("admin_correo", admin.getCorreo());
                intent.putExtra("admin_telefono", admin.getTelefono());
                intent.putExtra("admin_direccion", admin.getDireccion());
                intent.putExtra("admin_hotelAsignado", admin.getHotelAsignado());
                intent.putExtra("admin_fotoPerfilUrl", admin.getFotoPerfilUrl());

                detailsResultLauncher.launch(intent);
            });

            linearLayoutAdminsContainer.addView(adminItemView);
        }
    }

    private void handleFormResult(Intent data) {
        String action = data.getStringExtra("action");
        if ("registrado".equals(action)) {
            String nombre = data.getStringExtra("admin_nombres");
            String apellido = data.getStringExtra("admin_apellidos");

            showNotification("Administrador Registrado", "Se registró a " + nombre + " " + apellido);
            loadAdminsFromFirestore();
        }
    }


    private void handleDetailsResult(Intent data) {
        String action = data.getStringExtra("action");
        String adminFirestoreId = data.getStringExtra("admin_firestore_id");
        String adminNombres = data.getStringExtra("admin_nombres");
        String adminApellidos = data.getStringExtra("admin_apellidos");
        String nuevoHotelAsignado = data.getStringExtra("nuevo_hotel_asignado");

        final String[] statusMessage = {""};

        Admin adminToUpdate = null;
        for (Admin admin : adminDataList) {
            if (admin.getFirestoreId() != null && admin.getFirestoreId().equals(adminFirestoreId)) {
                adminToUpdate = admin;
                break;
            }
        }

        if (adminToUpdate != null) {
            Map<String, Object> updates = new HashMap<>();
            switch (action) {
                case "activado":
                    updates.put("estado", "true");
                    if (nuevoHotelAsignado != null && !nuevoHotelAsignado.isEmpty()) {
                        updates.put("hotelAsignado", nuevoHotelAsignado);
                        statusMessage[0] = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido activado y asignado a " + nuevoHotelAsignado + ".";
                    } else {
                        statusMessage[0] = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido activado.";
                    }
                    break;
                case "desactivado":
                    updates.put("hotelAsignado", "-");
                    updates.put("estado", "false");
                    statusMessage[0] = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido desactivado y su hotel desasignado.";
                    break;
                case "actualizado":
                    Log.d(TAG, "Admin updated from details screen. Reloading data.");
                    statusMessage[0] = "El administrador " + adminNombres + " " + adminApellidos + " ha sido actualizado.";
                    break;
            }

            if (!updates.isEmpty()) {
                db.collection("admins").document(adminFirestoreId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Estado del administrador actualizado en Firestore: " + adminFirestoreId);
                            Toast.makeText(SuperListaAdminActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Administrador", statusMessage[0]);
                            loadAdminsFromFirestore();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al actualizar estado del administrador en Firestore", e);
                            Toast.makeText(SuperListaAdminActivity.this, "Error al actualizar estado del administrador.", Toast.LENGTH_SHORT).show();
                            showNotification("Error de Actualización", "Falló la actualización del administrador " + adminNombres + " " + adminApellidos + ".");
                        });
            } else if ("actualizado".equals(action)) {
                Toast.makeText(SuperListaAdminActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Administrador", statusMessage[0]);
                loadAdminsFromFirestore();
            }
        } else {
            Log.w(TAG, "No se encontró el administrador con ID: " + adminFirestoreId + " para actualizar.");
            statusMessage[0] = "Error al actualizar administrador: Administrador no encontrado.";
            Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show();
            showNotification("Error de Actualización", statusMessage[0]);
        }
    }

    private void saveAdminToFirestore(Admin admin, final String notificationMessage) {
        db.collection("admins")
                .add(admin)
                .addOnSuccessListener(documentReference -> {
                    admin.setFirestoreId(documentReference.getId());
                    Log.d(TAG, "Admin guardado en Firestore con ID: " + documentReference.getId());
                    Toast.makeText(this, notificationMessage, Toast.LENGTH_SHORT).show();
                    showNotification("Administrador Registrado", notificationMessage);
                    loadAdminsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar admin en la base de datos.", Toast.LENGTH_SHORT).show();
                    showNotification("Error de Registro", "Falló el registro del administrador.");
                });
    }
}