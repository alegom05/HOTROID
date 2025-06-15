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

import com.bumptech.glide.Glide; // Assuming you use Glide for image loading
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
import java.util.Map; // Import Map for update operations if needed

public class SuperListaAdminActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaAdminActivity";

    private LinearLayout linearLayoutAdminsContainer;
    private EditText etBuscador;
    private Button btnLimpiar;

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

        loadAdminsFromFirestore();

        setupSearchFunctionality();

        // Assuming your super_lista_admins.xml also has these elements,
        // similar to super_lista_clientes.xml's top card structure.
        TextView tvTitulo = findViewById(R.id.tvTitulo); // Assuming it has this ID
        TextView tvNombre = findViewById(R.id.tvNombre); // Assuming it has this ID for admin's name
        TextView tvRol = findViewById(R.id.tvRol);       // Assuming it has this ID for admin's role
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil); // Assuming it has this ID for admin's image

        if (tvTitulo != null) {
            tvTitulo.setText("Lista de Administradores"); // Or whatever title you prefer
        }
        if (tvNombre != null) {
            tvNombre.setText("Pedro Bustamante"); // Example Admin Name
        }
        if (tvRol != null) {
            tvRol.setText("Administrador General"); // Example Admin Role
        }
        if (imagenPerfil != null) {
            imagenPerfil.setImageResource(R.drawable.foto_admin); // Make sure this drawable exists
        }


        CardView cardSuper = findViewById(R.id.cardSuper); // Using cardSuper for consistency
        if (cardSuper != null) {
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });
        }

        Button botonRegistrar = findViewById(R.id.button_regist); // Button to add new admins
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
                .orderBy("nombres", Query.Direction.ASCENDING) // Added ordering for consistent display
                .get()
                .addOnCompleteListener(task -> {
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

                        // This block of code is only for populating the database if it's empty.
                        // *** UNCOMMENT AND RUN ONCE IF YOUR FIREBASE DATABASE IS EMPTY.
                        // *** THEN COMMENT IT OUT AGAIN.
                        /*
                        if (adminDataList.isEmpty()) {
                            addInitialAdminsToFirestore();
                        }
                        */

                    } else {
                        Log.w(TAG, "Error al obtener documentos: ", task.getException());
                        Toast.makeText(SuperListaAdminActivity.this, "Error al cargar administradores.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // This method is for populating initial data - use with caution!
    private void addInitialAdminsToFirestore() {
        List<Admin> initialAdmins = Arrays.asList(
                new Admin("Ana", "García", "true", "12345678A", "1990-05-15", "ana.garcia@example.com", "600111222", "Calle Falsa 123", "Hotel Sol"),
                new Admin("Luis", "Martínez", "true", "87654321B", "1988-11-20", "luis.martinez@example.com", "600333444", "Av. Siempre Viva 45", "Hotel Luna"),
                new Admin("Marta", "Pérez", "false", "11223344C", "1995-03-10", "marta.perez@example.com", "600555666", "Plaza Mayor 7", "Hotel Estrella")
        );

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
                        admin.getHotelAsignado().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        admin.getDni().toLowerCase(Locale.getDefault()).contains(searchLower) || // Added DNI search
                        admin.getCorreo().toLowerCase(Locale.getDefault()).contains(searchLower)) { // Added Email search
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
            View adminItemView = inflater.inflate(R.layout.item_admin_card, linearLayoutAdminsContainer, false); // Make sure you have item_admin_card.xml

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
                intent.putExtra("admin_dni", admin.getDni());
                intent.putExtra("admin_nacimiento", admin.getNacimiento());
                intent.putExtra("admin_correo", admin.getCorreo());
                intent.putExtra("admin_telefono", admin.getTelefono());
                intent.putExtra("admin_direccion", admin.getDireccion());
                intent.putExtra("admin_hotelAsignado", admin.getHotelAsignado());
                intent.putExtra("admin_fotoPerfilUrl", admin.getFotoPerfilUrl()); // Pass the URL

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

            // Using final local variable for statusMessage to be accessed in lambda
            final String finalStatusMessage = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido registrado correctamente.";
            uploadImageAndSaveAdmin(nuevoAdmin, adminFotoPerfilBytes, finalStatusMessage); // Pass message to be used in notifications
        }
    }

    private void handleDetailsResult(Intent data) {
        String action = data.getStringExtra("action");
        String adminFirestoreId = data.getStringExtra("admin_firestore_id");
        String adminNombres = data.getStringExtra("admin_nombres");
        String adminApellidos = data.getStringExtra("admin_apellidos");
        String nuevoHotelAsignado = data.getStringExtra("nuevo_hotel_asignado");

        // Use an array to make it effectively final and mutable within the scope.
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
                    updates.put("hotelAsignado", "-"); // Clear hotel assignment on deactivation
                    updates.put("estado", "false");
                    statusMessage[0] = "El administrador de hotel " + adminNombres + " " + adminApellidos + " ha sido desactivado y su hotel desasignado.";
                    break;
                case "actualizado": // Handle general updates from SuperDetallesAdminActivity
                    Log.d(TAG, "Admin updated from details screen. Reloading data.");
                    statusMessage[0] = "El administrador " + adminNombres + " " + adminApellidos + " ha sido actualizado.";
                    // For "actualizado", the loadAdminsFromFirestore() call below will refresh everything.
                    // Specific updates from the form would be handled within handleFormResult or by passing more data.
                    break;
            }

            // Only attempt Firestore update if there are actual updates to push
            if (!updates.isEmpty()) { // For 'activado'/'desactivado'
                db.collection("admins").document(adminFirestoreId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Estado del administrador actualizado en Firestore: " + adminFirestoreId);
                            Toast.makeText(SuperListaAdminActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Administrador", statusMessage[0]); // Notification for status change
                            loadAdminsFromFirestore(); // Reload to reflect changes
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al actualizar estado del administrador en Firestore", e);
                            Toast.makeText(SuperListaAdminActivity.this, "Error al actualizar estado del administrador.", Toast.LENGTH_SHORT).show();
                            showNotification("Error de Actualización", "Falló la actualización del administrador " + adminNombres + " " + adminApellidos + "."); // Notification on failure
                        });
            } else if ("actualizado".equals(action)) { // If action is "actualizado" but no specific updates map was built
                // This block is for general updates where the entire admin object might have changed in the details form.
                // We just rely on a reload and show a general notification/toast.
                Toast.makeText(SuperListaAdminActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Administrador", statusMessage[0]);
                loadAdminsFromFirestore(); // Always reload to ensure latest data is shown
            }
        } else {
            Log.w(TAG, "No se encontró el administrador con ID: " + adminFirestoreId + " para actualizar.");
            statusMessage[0] = "Error al actualizar administrador: Administrador no encontrado.";
            Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show();
            showNotification("Error de Actualización", statusMessage[0]); // Notify if admin not found
        }
    }


    // Modified to accept a message for the notification/toast after successful save.
    private void uploadImageAndSaveAdmin(Admin admin, @Nullable byte[] imageData, final String notificationMessage) {
        if (imageData != null && imageData.length > 0) {
            String fileName = "admin_photos/" + admin.getDni() + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    admin.setFotoPerfilUrl(imageUrl);
                    saveAdminToFirestore(admin, notificationMessage); // Pass message
                    Log.d(TAG, "Imagen subida a Storage. URL: " + imageUrl);
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Error al obtener la URL de descarga de la imagen", e);
                    Toast.makeText(this, "Error al obtener URL de imagen. Guardando admin sin foto.", Toast.LENGTH_SHORT).show();
                    showNotification("Error de Imagen", "No se pudo obtener la URL de la imagen de perfil."); // Notification on image URL error
                    admin.setFotoPerfilUrl("");
                    saveAdminToFirestore(admin, notificationMessage); // Still try to save admin
                });
            }).addOnFailureListener(e -> {
                Log.w(TAG, "Error al subir la imagen a Storage", e);
                Toast.makeText(this, "Error al subir la imagen. Guardando admin sin foto.", Toast.LENGTH_SHORT).show();
                showNotification("Error de Imagen", "No se pudo subir la imagen de perfil."); // Notification on image upload error
                admin.setFotoPerfilUrl("");
                saveAdminToFirestore(admin, notificationMessage); // Still try to save admin
            });
        } else {
            Log.d(TAG, "No hay imagen para subir o la imagen es nula/vacía. Guardando admin sin foto.");
            admin.setFotoPerfilUrl("");
            saveAdminToFirestore(admin, notificationMessage); // Save admin without photo
        }
    }

    // Modified to accept a message for the notification/toast after successful save.
    private void saveAdminToFirestore(Admin admin, final String notificationMessage) {
        db.collection("admins")
                .add(admin)
                .addOnSuccessListener(documentReference -> {
                    admin.setFirestoreId(documentReference.getId()); // Set the ID after it's generated by Firestore
                    Log.d(TAG, "Admin guardado en Firestore con ID: " + documentReference.getId());
                    Toast.makeText(this, notificationMessage, Toast.LENGTH_SHORT).show(); // Show success toast
                    showNotification("Administrador Registrado", notificationMessage); // Show success notification
                    loadAdminsFromFirestore(); // Reload list
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar admin en Firestore", e);
                    Toast.makeText(this, "Error al guardar admin en la base de datos.", Toast.LENGTH_SHORT).show();
                    showNotification("Error de Registro", "Falló el registro del administrador."); // Notification on save failure
                });
    }

    // This method is primarily for internal updates where the whole object is set.
    // In handleDetailsResult, we use .update() which is more efficient for partial changes.
    // You can keep this if you have other scenarios that need to fully replace the document.
    private void updateAdminInFirestore(Admin admin) {
        if (admin.getFirestoreId() != null && !admin.getFirestoreId().isEmpty()) {
            db.collection("admins").document(admin.getFirestoreId())
                    .set(admin) // Use set() to fully replace the document
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Admin actualizado en Firestore: " + admin.getNombres() + " " + admin.getApellidos());
                        // Toast and Notification handled in handleDetailsResult.
                        loadAdminsFromFirestore();
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Error al actualizar admin en Firestore", e);
                        Toast.makeText(SuperListaAdminActivity.this, "Error al actualizar admin.", Toast.LENGTH_SHORT).show();
                        // Notification for failure might be good here too, if not handled upstream.
                    });
        } else {
            Log.w(TAG, "No se puede actualizar Admin sin un ID de Firestore (firestoreId nulo o vacío).");
            Toast.makeText(this, "Error: Admin no tiene ID para actualizar.", Toast.LENGTH_SHORT).show();
        }
    }
}