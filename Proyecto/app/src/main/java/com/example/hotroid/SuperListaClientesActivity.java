package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context; // Added for context in TextWatcher
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable; // Added for TextWatcher
import android.text.TextWatcher; // Added for search functionality
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button; // Added for the clear button
import android.widget.EditText; // Added for the search input
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SuperListaClientesActivity extends AppCompatActivity {

    private LinearLayout linearLayoutClientesContainer;
    private EditText etBuscador; // Declared EditText for search
    private Button btnLimpiar; // Declared Button for clear

    private static final String CHANNEL_ID = "client_notifications_channel";
    private static final int NOTIFICATION_ID = 2;

    private static final int REQUEST_CODE_DETALLES_CLIENTE = 200;

    // Lista estática para simular la base de datos de clientes
    // Formato de cada String[]: {Nombre Completo(usuario), Nombres, Apellidos, TipoDoc, NumDoc, FechaNac, Correo, Telefono, Domicilio, Estado (true/false)}
    public static List<String[]> clientDataList = new ArrayList<>();
    // Lista filtrada que se muestra en la UI
    private List<String[]> filteredClientList = new ArrayList<>();


    static {
        // Inicializa la lista con datos de ejemplo si está vacía
        if (clientDataList.isEmpty()) {
            clientDataList.add(new String[]{"Rodrigo Carrasco", "Rodrigo", "Carrasco", "DNI", "12345678", "01/01/1990", "rodrigo@example.com", "987654321", "Av. Principal 123", "true"});
            clientDataList.add(new String[]{"Angelo Velarde", "Angelo", "Velarde", "DNI", "87654321", "05/03/1985", "angelo@example.com", "912345678", "Calle Falsa 456", "false"});
            clientDataList.add(new String[]{"Juan Huapaya", "Juan", "Huapaya", "DNI", "11223344", "10/11/1992", "juan@example.com", "998877665", "Jr. Los Olivos 789", "true"});
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_clientes);

        createNotificationChannel();

        linearLayoutClientesContainer = findViewById(R.id.linearLayoutClientesContainer);
        etBuscador = findViewById(R.id.etBuscador); // Initialize EditText
        btnLimpiar = findViewById(R.id.btnLimpiar); // Initialize Button

        // Initialize filtered list with all elements
        filteredClientList.addAll(clientDataList);
        renderClientList();

        // Setup search functionality
        setupSearchFunctionality();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentHoteles = new Intent(SuperListaClientesActivity.this, SuperActivity.class);
                startActivity(intentHoteles);
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperListaClientesActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentEventos = new Intent(SuperListaClientesActivity.this, SuperEventosActivity.class);
                startActivity(intentEventos);
                return true;
            }
            return false;
        });

        CardView cardSuper = findViewById(R.id.cardSuper);
        if (cardSuper != null) {
            cardSuper.setOnClickListener(v -> {
                Intent intentCuenta = new Intent(SuperListaClientesActivity.this, SuperCuentaActivity.class);
                startActivity(intentCuenta);
            });
        }
    }

    private void setupSearchFunctionality() {
        // TextWatcher for real-time search
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterClientList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        // Clear button
        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetClientList();
        });
    }

    private void filterClientList(String searchText) {
        filteredClientList.clear();

        if (searchText.isEmpty()) {
            // If no search text, show all clients
            filteredClientList.addAll(clientDataList);
        } else {
            // Filter by full name (case-insensitive)
            String searchLower = searchText.toLowerCase().trim();
            for (String[] client : clientDataList) {
                if (client[0].toLowerCase().contains(searchLower)) { // client[0] is "Nombre Completo"
                    filteredClientList.add(client);
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Method updated to render the filtered list
    private void renderClientList() {
        linearLayoutClientesContainer.removeAllViews();

        if (filteredClientList.isEmpty()) {
            // Show message when no results are found
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron clientes");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutClientesContainer.addView(noResultsText);
            return;
        }

        for (int i = 0; i < filteredClientList.size(); i++) {
            String[] clientData = filteredClientList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View clientItemView = inflater.inflate(R.layout.super_lista_clientes_item, linearLayoutClientesContainer, false);

            TextView tvUsuario = clientItemView.findViewById(R.id.tvUsuario);
            ImageView ivEstado = clientItemView.findViewById(R.id.ivEstado);

            tvUsuario.setText(clientData[0]); // Nombre del cliente (índice 0: "Nombre Completo")

            boolean isActivado = Boolean.parseBoolean(clientData[9]); // Estado está en el índice 9

            if (isActivado) {
                ivEstado.setImageResource(R.drawable.circle_green);
            } else {
                ivEstado.setImageResource(R.drawable.circle_red);
            }

            // Find the original position in clientDataList for the click
            final int originalPosition = findOriginalPosition(clientData);


            clientItemView.setOnClickListener(v -> {
                Intent intent;
                if (isActivado) {
                    intent = new Intent(SuperListaClientesActivity.this, SuperDetallesClienteActivadoActivity.class);
                } else {
                    intent = new Intent(SuperListaClientesActivity.this, SuperDetallesClienteDesactivadoActivity.class);
                }
                // Pasa la posición del cliente y todos sus datos completos
                intent.putExtra("client_position", originalPosition);
                intent.putExtra("client_data_full", clientDataList.get(originalPosition)); // Pass the full original data array
                startActivityForResult(intent, REQUEST_CODE_DETALLES_CLIENTE);
            });

            linearLayoutClientesContainer.addView(clientItemView);
        }
    }

    // Helper method to find the original position in clientDataList
    private int findOriginalPosition(String[] targetClient) {
        for (int i = 0; i < clientDataList.size(); i++) {
            String[] client = clientDataList.get(i);
            // Assuming client[0] (full name) is unique enough for identification
            if (client[0].equals(targetClient[0])) {
                return i;
            }
        }
        return -1; // Should not happen if targetClient is from clientDataList
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_DETALLES_CLIENTE && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            int position = data.getIntExtra("client_position", -1);

            String statusMessage = "";

            if (action != null && position != -1 && position < clientDataList.size()) {
                String clientName = clientDataList.get(position)[0]; // Obtener el nombre del cliente de la lista estática

                if (action.equals("activado")) {
                    clientDataList.get(position)[9] = "true";
                    statusMessage = "El cliente " + clientName + " ha sido activado correctamente.";
                } else if (action.equals("desactivado")) {
                    clientDataList.get(position)[9] = "false";
                    statusMessage = "El cliente " + clientName + " ha sido desactivado correctamente.";
                }
            }

            if (!statusMessage.isEmpty()) {
                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Cliente", statusMessage);
            }

            // Update both lists and apply the current filter
            String currentSearch = etBuscador.getText().toString();
            if (currentSearch.isEmpty()) {
                resetClientList();
            } else {
                filterClientList(currentSearch);
            }
        }
    }
}