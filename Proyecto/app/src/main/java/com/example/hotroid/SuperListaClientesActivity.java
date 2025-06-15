package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.example.hotroid.bean.Cliente; // Make sure this bean class exists and is correct
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
import com.google.firebase.firestore.WriteBatch; // Import for batch writes

import java.util.ArrayList;
import java.util.Arrays; // Added for Arrays.asList
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperListaClientesActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaClientesActivity";

    private LinearLayout linearLayoutClientesContainer;
    private EditText etBuscador;
    private Button btnLimpiar;

    private static final String CHANNEL_ID = "client_notifications_channel";
    private static final int NOTIFICATION_ID = 2; // Different ID for clients

    public List<Cliente> clientDataList = new ArrayList<>();
    public List<Cliente> filteredClientList = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> detailsResultLauncher; // Only for details, no form launcher here

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_clientes); // Your main layout for client list

        db = FirebaseFirestore.getInstance();

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

        detailsResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleDetailsResult(result.getData());
                    }
                });

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        linearLayoutClientesContainer = findViewById(R.id.linearLayoutClientesContainer);
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        loadClientsFromFirestore();

        setupSearchFunctionality();

        // Top bar elements from your super_lista_clientes.xml
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvNombre = findViewById(R.id.tvNombre); // Corresponds to tvNombreAdminActual in SuperListaAdminActivity
        TextView tvRol = findViewById(R.id.tvRol);
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil);

        if (tvTitulo != null) {
            tvTitulo.setText("Gestión de Clientes"); // Matching your XML title
        }
        if (tvNombre != null) {
            tvNombre.setText("Pedro Bustamante"); // Hardcoded admin name
        }
        if (tvRol != null) {
            tvRol.setText("Administrador General");
        }
        if (imagenPerfil != null) {
            imagenPerfil.setImageResource(R.drawable.foto_admin); // Assuming 'foto_admin' drawable exists
        }

        // Admin profile card click listener (using cardSuper ID from your XML)
        CardView cardSuper = findViewById(R.id.cardSuper);
        if (cardSuper != null) {
            cardSuper.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaClientesActivity.this, SuperCuentaActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    Intent intentInicio = new Intent(SuperListaClientesActivity.this, SuperActivity.class);
                    startActivity(intentInicio);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    return true; // Already on the users (clients) list
                } else if (itemId == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperListaClientesActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        }

        // No "Registrar" button for clients as per previous request to exclude registration functionality
        // Button botonRegistrar = findViewById(R.id.button_regist); // Removed
        // if (botonRegistrar != null) { ... } // Removed
    }

    private void loadClientsFromFirestore() {
        clientDataList.clear();
        filteredClientList.clear();
        linearLayoutClientesContainer.removeAllViews(); // Clear existing views

        db.collection("clientes")
                .orderBy("nombres", Query.Direction.ASCENDING) // Order clients by name for consistent display
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Cliente cliente = document.toObject(Cliente.class);
                                cliente.setFirestoreId(document.getId()); // Set the Firestore ID
                                clientDataList.add(cliente);
                                Log.d(TAG, "Cliente cargado: " + document.getId() + " => " + document.getData());
                            }
                            filteredClientList.addAll(clientDataList); // Populate filtered list initially
                            renderClientList(); // Display the list
                            Log.d(TAG, "Clientes cargados desde Firestore: " + clientDataList.size());

                            // *** ESTE ES EL BLOQUE QUE DEBES EJECUTAR UNA SOLA VEZ ***
                            // *** DESPUÉS DE LA PRIMERA EJECUCIÓN, COMENTA LAS SIGUIENTES 3 LÍNEAS. ***
                            if (clientDataList.isEmpty()) {
                                addInitialClientsToFirestore();
                            }
                            // *******************************************************************

                        } else {
                            Log.w(TAG, "Error al obtener documentos de clientes: ", task.getException());
                            Toast.makeText(SuperListaClientesActivity.this, "Error al cargar clientes.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Adds initial client data to Firestore.
     * CALL THIS METHOD ONLY ONCE TO POPULATE THE DATABASE.
     * After the first successful run, comment out the call to this method
     * in loadClientsFromFirestore() to prevent duplicate entries.
     */
    private void addInitialClientsToFirestore() {
        /*List<Cliente> initialClients = Arrays.asList(
                new Cliente("Juan", "Pérez", "true", "12345678X", "1990-01-01", "juan.perez@example.com", "911223344", "Calle Falsa 123", ""),
                new Cliente("María", "González", "true", "87654321Y", "1985-03-15", "maria.gonzalez@example.com", "922334455", "Av. Siempre Viva 45", ""),
                new Cliente("Carlos", "Rodríguez", "false", "11223344Z", "1992-07-20", "carlos.rodriguez@example.com", "933445566", "Plaza Central 7", ""),
                new Cliente("Laura", "Fernández", "true", "98765432A", "1998-11-25", "laura.fernandez@example.com", "944556677", "Paseo de las Flores 8", ""),
                new Cliente("Pedro", "Sánchez", "false", "45678901B", "1980-04-10", "pedro.sanchez@example.com", "955667788", "Avenida del Sol 21", ""),
                new Cliente("Ana", "López", "true", "23456789C", "1993-09-05", "ana.lopez@example.com", "966778899", "Calle de la Luna 10", ""),
                new Cliente("David", "Gómez", "true", "78901234D", "1987-02-28", "david.gomez@example.com", "977889900", "Bulevar del Río 5", "")
        );

        // Use a Firestore batch to write all documents efficiently
        WriteBatch batch = db.batch();

        for (Cliente cliente : initialClients) {
            // Firestore will auto-generate an ID for each new document
            DocumentReference docRef = db.collection("clientes").document();
            batch.set(docRef, cliente); // Use set to create the document
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "7 clientes iniciales añadidos a Firestore con éxito.");
                    Toast.makeText(SuperListaClientesActivity.this, "Clientes de ejemplo añadidos.", Toast.LENGTH_SHORT).show();
                    // After adding, reload to display them
                    loadClientsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al añadir clientes iniciales: ", e);
                    Toast.makeText(SuperListaClientesActivity.this, "Error al añadir clientes de ejemplo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }); */
    }


    private void setupSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not necessary
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClientList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not necessary
            }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetClientList();
        });
    }

    private void filterClientList(String searchText) {
        filteredClientList.clear();

        if (searchText.isEmpty()) {
            filteredClientList.addAll(clientDataList);
        } else {
            String searchLower = searchText.toLowerCase(Locale.getDefault()).trim();
            for (Cliente cliente : clientDataList) {
                // Search by name, surname, DNI, or email, similar to Admin search
                if ((cliente.getNombres() + " " + cliente.getApellidos()).toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        cliente.getDni().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        cliente.getCorreo().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    filteredClientList.add(cliente);
                }
            }
        }
        renderClientList();
    }

    private void resetClientList() {
        filteredClientList.clear();
        filteredClientList.addAll(clientDataList);
        renderClientList();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Clientes";
            String description = "Canal para notificaciones de estado de clientes";
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
            return; // Don't show notification if permission is not granted on Android 13+
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Ensure this icon exists in your drawables
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void renderClientList() {
        linearLayoutClientesContainer.removeAllViews();

        if (filteredClientList.isEmpty()) {
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron clientes.");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutClientesContainer.addView(noResultsText);
            return;
        }

        for (int i = 0; i < filteredClientList.size(); i++) {
            Cliente cliente = filteredClientList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            // Inflate with item_client_card.xml
            View clientItemView = inflater.inflate(R.layout.item_client_card, linearLayoutClientesContainer, false);

            TextView tvNombreCliente = clientItemView.findViewById(R.id.tvNombreCliente);
            TextView tvEstado = clientItemView.findViewById(R.id.tvEstado); // Matches your item_client_card.xml
            tvNombreCliente.setText(cliente.getNombres() + " " + cliente.getApellidos());

            boolean isActivado = Boolean.parseBoolean(cliente.getEstado());
            if (isActivado) {
                tvEstado.setText("Estado: Activo");
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.green_status));
                // ivEstadoColor.setImageResource(R.drawable.circle_green); // Only if you kept ivEstadoColor in item_client_card.xml
            } else {
                tvEstado.setText("Estado: Inactivo");
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.red_status));
                // ivEstadoColor.setImageResource(R.drawable.circle_red); // Only if you kept ivEstadoColor in item_client_card.xml
            }


            clientItemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaClientesActivity.this, SuperDetallesClienteActivity.class);
                intent.putExtra("client_firestore_id", cliente.getFirestoreId());
                // Pass all relevant client data, similar to how Admin data is passed
                intent.putExtra("client_nombres", cliente.getNombres());
                intent.putExtra("client_apellidos", cliente.getApellidos());
                intent.putExtra("client_estado", cliente.getEstado());
                intent.putExtra("client_dni", cliente.getDni());
                intent.putExtra("client_nacimiento", cliente.getNacimiento());
                intent.putExtra("client_correo", cliente.getCorreo());
                intent.putExtra("client_telefono", cliente.getTelefono());
                intent.putExtra("client_direccion", cliente.getDireccion());
                intent.putExtra("client_fotoPerfilUrl", cliente.getFotoPerfilUrl()); // Include photo URL

                detailsResultLauncher.launch(intent);
            });

            linearLayoutClientesContainer.addView(clientItemView);
        }
    }

    private void handleDetailsResult(Intent data) {
        String action = data.getStringExtra("action");
        String clientFirestoreId = data.getStringExtra("client_firestore_id");
        // Also retrieve names if you want to use them in Toast/Log for clarity, like in Admin.
        String clientNombres = data.getStringExtra("client_nombres");
        String clientApellidos = data.getStringExtra("client_apellidos");

        // Use an array to make statusMessage effectively final for the lambda expressions
        final String[] statusMessage = {""};

        Cliente clientToUpdate = null;
        for (Cliente client : clientDataList) {
            if (client.getFirestoreId() != null && client.getFirestoreId().equals(clientFirestoreId)) {
                clientToUpdate = client;
                break;
            }
        }

        if (clientToUpdate != null) {
            Map<String, Object> updates = new HashMap<>();
            switch (action) {
                case "activado":
                    updates.put("estado", "true");
                    statusMessage[0] = "El cliente " + clientNombres + " " + clientApellidos + " ha sido activado.";
                    break;
                case "desactivado":
                    updates.put("estado", "false");
                    statusMessage[0] = "El cliente " + clientNombres + " " + clientApellidos + " ha sido desactivado.";
                    break;
                case "actualizado": // Similar to Admin's "actualizado" case for general updates
                    Log.d(TAG, "Client updated from details screen. Reloading data.");
                    statusMessage[0] = "El cliente " + clientNombres + " " + clientApellidos + " ha sido actualizado.";
                    // No direct 'updates.put' for "actualizado" action here, as loadClientsFromFirestore() will refresh all.
                    break;
            }

            // Only attempt Firestore update if an actual status change or general update action occurred
            if (!updates.isEmpty()) { // Only update if there's something to update
                db.collection("clientes").document(clientFirestoreId)
                        .update(updates) // Use update instead of set to only modify specified fields
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Estado del cliente actualizado en Firestore: " + clientFirestoreId);
                            Toast.makeText(SuperListaClientesActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Cliente", statusMessage[0]); // Re-added notification
                            loadClientsFromFirestore(); // Reload to reflect changes
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al actualizar estado del cliente en Firestore", e);
                            Toast.makeText(SuperListaClientesActivity.this, "Error al actualizar estado del cliente.", Toast.LENGTH_SHORT).show();
                            showNotification("Error de Actualización", "Falló la actualización del cliente " + clientNombres + " " + clientApellidos + "."); // Notification on failure
                        });
            } else if ("actualizado".equals(action)) { // Handle "actualizado" if no direct updates were made
                Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Cliente", statusMessage[0]);
                loadClientsFromFirestore(); // Ensure list refreshes even if no explicit Firestore update for general "actualizado"
            }
        } else {
            Log.w(TAG, "No se encontró el cliente con ID: " + clientFirestoreId + " para actualizar.");
            statusMessage[0] = "Error al actualizar cliente: Cliente no encontrado.";
            Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show(); // Show toast even if client not found locally
            showNotification("Error de Actualización", statusMessage[0]); // Notify if client not found
        }
    }
}