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

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.Cliente;
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
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperListaClientesActivity extends AppCompatActivity {

    private static final String TAG = "SuperListaClientesActivity";

    private LinearLayout linearLayoutClientesContainer;
    private EditText etBuscador;
    private Button btnLimpiar;
    private Button btnFilterActive; // New filter button
    private Button btnFilterInactive; // New filter button

    // Track the currently selected filter status
    private String currentFilterStatus = "all"; // "all", "true", or "false"

    private static final String CHANNEL_ID = "client_notifications_channel";
    private static final int NOTIFICATION_ID = 2;

    public List<Cliente> clientDataList = new ArrayList<>();
    public List<Cliente> filteredClientList = new ArrayList<>();

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> detailsResultLauncher;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_clientes);

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
        btnFilterActive = findViewById(R.id.btnFilterActive); // Initialize new button
        btnFilterInactive = findViewById(R.id.btnFilterInactive); // Initialize new button


        loadClientsFromFirestore();

        setupSearchFunctionality();
        setupFilterFunctionality(); // Setup filter for clients

        // Top bar elements from your super_lista_clientes.xml
        TextView tvTitulo = findViewById(R.id.tvTitulo);
        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvRol = findViewById(R.id.tvRol);
        ImageView imagenPerfil = findViewById(R.id.imagenPerfil);

        if (tvTitulo != null) {
            tvTitulo.setText("Gestión de Clientes");
        }
        if (tvNombre != null) {
            tvNombre.setText("Pedro Bustamante"); // Hardcoded admin name
        }
        if (tvRol != null) {
            tvRol.setText("Administrador General");
        }
        if (imagenPerfil != null) {
            imagenPerfil.setImageResource(R.drawable.foto_super); // Assuming 'foto_admin' drawable exists
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
                    // This activity shows client list, which falls under 'usuarios'
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    Intent intentAlertas = new Intent(SuperListaClientesActivity.this, SuperEventosActivity.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        }
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
                            // After loading, apply the current filter status and search text
                            filterClientList(etBuscador.getText().toString(), currentFilterStatus);
                            Log.d(TAG, "Clientes cargados desde Firestore: " + clientDataList.size());

                            // --- DESCOMENTAR ESTE BLOQUE LA PRIMERA VEZ PARA AÑADIR CLIENTES DE EJEMPLO ---
                            // --- LUEGO DE LA PRIMERA EJECUCIÓN EXITOSA, VUELVE A COMENTARLO PARA EVITAR DUPLICADOS ---
                            /*if (clientDataList.isEmpty()) { // Only add if the collection is empty
                                addInitialClientsToFirestore(); // ¡Asegúrate de que esta línea esté descomentada para la primera vez!
                            }*/
                            //----------------------------------------------------------------------------------

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

        List<Cliente> initialClients = Arrays.asList(

                // Clientes con dos nombres y dos apellidos
                new Cliente("Jorge Aaron", "Coronado Villacorta", "true", "DNI", "12345678", "1990-01-01", "jorge.coronado@example.com", "911223344", "Av. Los Rosales 101", "", Arrays.asList("WiFi", "TV", "Desayuno incluido")),
                new Cliente("Ana Sofía", "Gonzáles Flores", "true", "Pasaporte", "PA987654321", "1985-03-15", "ana.gonzales@example.com", "922334455", "Jr. Las Palmas 202", "", Arrays.asList()),
                new Cliente("Luis Miguel", "Ramírez Castro", "false", "CE", "CE11223344", "1992-07-20", "luis.ramirez@example.com", "933445566", "Calle Los Olivos 303", "", Arrays.asList()),
                new Cliente("María José", "Sánchez Vásquez", "true", "DNI", "98765432", "1998-11-25", "maria.sanchez@example.com", "944556677", "Urb. El Sol 404", "", Arrays.asList()),
                new Cliente("Pedro Alejandro", "Gómez Hidalgo", "false", "Pasaporte", "PA456789012", "1980-04-10", "pedro.gomez@example.com", "955667788", "Psje. Las Flores 505", "", Arrays.asList()),
                new Cliente("Laura Camila", "Morales Ruiz", "true", "DNI", "23456789", "1993-09-05", "laura.morales@example.com", "966778899", "Av. La Paz 606", "", Arrays.asList()),
                new Cliente("Carlos Alberto", "Díaz Pinedo", "true", "CE", "CE78901234", "1987-02-28", "carlos.diaz@example.com", "977889900", "Jr. San Martín 707", "", Arrays.asList()),
                new Cliente("Daniel Felipe", "Vargas Rojas", "true", "DNI", "87654321", "1995-06-01", "daniel.vargas@example.com", "987654321", "Calle Los Andes 808", "", Arrays.asList()),
                new Cliente("Sofía Alejandra", "Espinoza Luna", "false", "Pasaporte", "PA098765432", "1989-12-01", "sofia.espinoza@example.com", "998877665", "Av. Primavera 909", "", Arrays.asList()),
                new Cliente("Gabriel Andrés", "Herrera Salazar", "true", "DNI", "11223344", "1991-03-22", "gabriel.herrera@example.com", "912345678", "Urb. El Mirador 110", "", Arrays.asList()),
                new Cliente("Valentina Paz", "Guerrero Soto", "true", "CE", "CE56789012", "1983-09-10", "valentina.guerrero@example.com", "923456789", "Jr. Amazonas 120", "", Arrays.asList()),
                new Cliente("Diego Armando", "Ortiz Paredes", "false", "DNI", "55667788", "1997-01-05", "diego.ortiz@example.com", "934567890", "Av. Central 130", "", Arrays.asList()),
                new Cliente("Camila Fernanda", "Reyes Bustamante", "true", "Pasaporte", "PA345678901", "1988-07-18", "camila.reyes@example.com", "945678901", "Calle Los Sauces 140", "", Arrays.asList()),
                new Cliente("José Antonio", "Soto Valdivia", "true", "DNI", "99001122", "1994-04-30", "jose.soto@example.com", "956789012", "Psje. La Luna 150", "", Arrays.asList()),
                new Cliente("Martín Alonso", "Huamán Cueva", "false", "CE", "CE12345678", "1996-08-08", "martin.huaman@example.com", "967890123", "Av. Del Ejército 160", "", Arrays.asList()),
                new Cliente("Flavia Nicole", "Navarro Paredes", "true", "DNI", "33445566", "1999-02-14", "flavia.navarro@example.com", "978901234", "Jr. Grau 170", "", Arrays.asList()),
                new Cliente("Renzo Fabián", "Quispe Tello", "true", "Pasaporte", "PA234567890", "1982-10-21", "renzo.quispe@example.com", "989012345", "Calle Bolognesi 180", "", Arrays.asList()),
                new Cliente("Andrea Luciana", "Vásquez Rojas", "false", "DNI", "44556677", "1990-05-03", "andrea.vasquez@example.com", "990123456", "Urb. La Ensenada 190", "", Arrays.asList()),
                new Cliente("Juan Pablo", "Salazar Mendoza", "true", "CE", "CE98765432", "1986-11-11", "juanpablo.salazar@example.com", "901234567", "Av. El Sol 200", "", Arrays.asList()),
                new Cliente("Gabriela Fernanda", "Castañeda Ríos", "true", "DNI", "66778899", "1997-07-07", "gabriela.castaneda@example.com", "912345670", "Psje. Las Dalias 210", "", Arrays.asList())
        );

        // Use a Firestore batch to write all documents efficiently
        WriteBatch batch = db.batch();

        for (Cliente cliente : initialClients) {
            DocumentReference docRef = db.collection("clientes").document();
            batch.set(docRef, cliente);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Clientes iniciales añadidos a Firestore con éxito.");
                    Toast.makeText(SuperListaClientesActivity.this, "Clientes de ejemplo añadidos.", Toast.LENGTH_SHORT).show();
                    // After adding, reload to display them
                    loadClientsFromFirestore();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al añadir clientes iniciales: ", e);
                    Toast.makeText(SuperListaClientesActivity.this, "Error al añadir clientes de ejemplo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


    private void setupSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not necessary
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // When typing in search, apply current status filter (or all if none selected)
                filterClientList(s.toString(), currentFilterStatus);
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

    private void setupFilterFunctionality() {
        btnFilterActive.setOnClickListener(v -> {
            etBuscador.setText(""); // Clear search when filtering by status
            if (currentFilterStatus.equals("true")) {
                currentFilterStatus = "all"; // If already active, toggle to all
            } else {
                currentFilterStatus = "true";
            }
            updateFilterButtonsUI(); // Update button colors
            filterClientList("", currentFilterStatus);
        });

        btnFilterInactive.setOnClickListener(v -> {
            etBuscador.setText(""); // Clear search when filtering by status
            if (currentFilterStatus.equals("false")) {
                currentFilterStatus = "all"; // If already inactive, toggle to all
            } else {
                currentFilterStatus = "false";
            }
            updateFilterButtonsUI(); // Update button colors
            filterClientList("", currentFilterStatus);
        });

        // Initialize button colors when the activity starts
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

    private void filterClientList(String searchText, String statusFilter) {
        filteredClientList.clear();

        String searchLower = searchText.toLowerCase(Locale.getDefault()).trim();

        for (Cliente cliente : clientDataList) {
            boolean matchesSearch = true;
            boolean matchesStatus = true;

            // Check search text
            if (!searchText.isEmpty()) {
                // Search by name, surname, numeroDocumento, or email
                String nombreCompleto = (cliente.getNombres() + " " + cliente.getApellidos()).toLowerCase(Locale.getDefault());
                matchesSearch = nombreCompleto.contains(searchLower) ||
                        cliente.getNumeroDocumento().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        cliente.getCorreo().toLowerCase(Locale.getDefault()).contains(searchLower);
            }

            // Check status filter
            if (!statusFilter.equals("all")) {
                matchesStatus = cliente.getEstado().equals(statusFilter);
            }

            if (matchesSearch && matchesStatus) {
                filteredClientList.add(cliente);
            }
        }
        renderClientList();
    }

    private void resetClientList() {
        currentFilterStatus = "all"; // Reset status filter to show all
        updateFilterButtonsUI(); // Update button colors to reflect "all" state
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
            TextView tvEstado = clientItemView.findViewById(R.id.tvEstado);

            // Muestra nombres y apellidos juntos en la tarjeta
            tvNombreCliente.setText(cliente.getNombres() + " " + cliente.getApellidos());


            boolean isActivado = Boolean.parseBoolean(cliente.getEstado());
            if (isActivado) {
                tvEstado.setText("Estado: Activo");
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.green_status));
            } else {
                tvEstado.setText("Estado: Inactivo");
                tvEstado.setTextColor(ContextCompat.getColor(this, R.color.red_status));
            }

            clientItemView.setOnClickListener(v -> {
                Intent intent = new Intent(SuperListaClientesActivity.this, SuperDetallesClienteActivity.class);
                intent.putExtra("client_firestore_id", cliente.getFirestoreId());
                intent.putExtra("client_nombres", cliente.getNombres());
                intent.putExtra("client_apellidos", cliente.getApellidos());
                intent.putExtra("client_estado", cliente.getEstado());
                intent.putExtra("client_tipo_documento", cliente.getTipoDocumento());
                intent.putExtra("client_numero_documento", cliente.getNumeroDocumento());
                intent.putExtra("client_nacimiento", cliente.getNacimiento());
                intent.putExtra("client_correo", cliente.getCorreo());
                intent.putExtra("client_telefono", cliente.getTelefono());
                intent.putExtra("client_direccion", cliente.getDireccion());
                intent.putExtra("client_fotoPerfilUrl", cliente.getFotoPerfilUrl());

                detailsResultLauncher.launch(intent);
            });

            linearLayoutClientesContainer.addView(clientItemView);
        }
    }

    private void handleDetailsResult(Intent data) {
        String action = data.getStringExtra("action");
        String clientFirestoreId = data.getStringExtra("client_firestore_id");
        String clientNombres = data.getStringExtra("client_nombres");
        String clientApellidos = data.getStringExtra("client_apellidos");

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
                case "actualizado": // Si viene de una actualización general en SuperDetallesClienteActivity
                    Log.d(TAG, "Client updated from details screen. Reloading data.");
                    statusMessage[0] = "El cliente " + clientNombres + " " + clientApellidos + " ha sido actualizado.";
                    // No direct 'updates.put' here, loadClientsFromFirestore() will refresh all.
                    break;
            }

            if (!updates.isEmpty()) {
                db.collection("clientes").document(clientFirestoreId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Estado del cliente actualizado en Firestore: " + clientFirestoreId);
                            Toast.makeText(SuperListaClientesActivity.this, statusMessage[0], Toast.LENGTH_SHORT).show();
                            showNotification("Actualización de Cliente", statusMessage[0]);
                            loadClientsFromFirestore();
                        })
                        .addOnFailureListener(e -> {
                            Log.w(TAG, "Error al actualizar estado del cliente en Firestore", e);
                            Toast.makeText(SuperListaClientesActivity.this, "Error al actualizar estado del cliente.", Toast.LENGTH_SHORT).show();
                            showNotification("Error de Actualización", "Falló la actualización del cliente " + clientNombres + " " + clientApellidos + ".");
                        });
            } else if ("actualizado".equals(action)) {
                Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Cliente", statusMessage[0]);
                loadClientsFromFirestore(); // Ensure list refreshes even if no explicit Firestore update
            }
        } else {
            Log.w(TAG, "No se encontró el cliente con ID: " + clientFirestoreId + " para actualizar.");
            statusMessage[0] = "Error al actualizar cliente: Cliente no encontrado.";
            Toast.makeText(this, statusMessage[0], Toast.LENGTH_SHORT).show();
            showNotification("Error de Actualización", statusMessage[0]);
        }
    }
}