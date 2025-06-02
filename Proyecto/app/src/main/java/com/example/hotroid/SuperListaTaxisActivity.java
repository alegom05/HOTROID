package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable; // Import for TextWatcher
import android.text.TextWatcher; // Import for TextWatcher
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button; // Import for Button
import android.widget.EditText; // Import for EditText
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List; // Use List interface for filteredList

public class SuperListaTaxisActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAXI_DETAIL = 1;
    private static final String CHANNEL_ID = "taxi_management_channel";
    private static int notificationId = 0;

    // Lista estática para simular la base de datos y persistir cambios entre actividades
    // Cada String[] contiene:
    // 0: Nombre Completo (para mostrar en la lista)
    // 1: Nombres
    // 2: Apellidos
    // 3: Tipo de Documento
    // 4: Número de Documento
    // 5: Fecha de Nacimiento
    // 6: Correo Electrónico
    // 7: Teléfono
    // 8: Domicilio
    // 9: Placa de Vehículo
    // 10: Estado ("activado", "desactivado", "pendiente")
    public static ArrayList<String[]> taxiDataList = new ArrayList<>();
    private LinearLayout linearLayoutTaxisContainer;
    private EditText etBuscador; // Declared EditText for search
    private Button btnLimpiar; // Declared Button for clear

    // Lista filtrada que se muestra en la UI
    private List<String[]> filteredTaxiList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_taxis);

        // Inicializar datos de ejemplo si la lista está vacía
        if (taxiDataList.isEmpty()) {
            taxiDataList.add(new String[]{"Eduardo Campos", "Eduardo", "Campos", "DNI", "12345678", "15 de enero de 1990", "eduardo.c@example.com", "987654321", "Av. Siempre Viva 742", "XYZ-001", "activado"});
            taxiDataList.add(new String[]{"Walter Nuñez", "Walter", "Nuñez", "DNI", "87654321", "20 de febrero de 1985", "walter.n@example.com", "912345678", "Calle Falsa 123", "ABC-002", "desactivado"});
            taxiDataList.add(new String[]{"Gumercindo Bartra", "Gumercindo", "Bartra", "DNI", "11223344", "01 de marzo de 1992", "gumercindo.b@example.com", "998877665", "Jr. Luna 456", "DEF-003", "pendiente"});
            taxiDataList.add(new String[]{"Maria Perez", "Maria", "Perez", "DNI", "55667788", "10 de abril de 1988", "maria.p@example.com", "901234567", "Av. Sol 789", "GHI-004", "pendiente"});
        }

        linearLayoutTaxisContainer = findViewById(R.id.linearLayoutTaxisContainer);
        etBuscador = findViewById(R.id.etBuscador); // Initialize EditText
        btnLimpiar = findViewById(R.id.btnLimpiar); // Initialize Button

        // Initialize filtered list with all elements
        filteredTaxiList.addAll(taxiDataList);

        createNotificationChannel();
        setupBottomNavigationView();
        setupCardSuperClickListener();
        setupSearchFunctionality(); // Setup search functionality
        displayTaxis(); // Muestra los taxistas en la lista (ahora usará filteredTaxiList)
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegúrate de refrescar la lista cada vez que la actividad vuelve a estar en primer plano
        // Esto también aplicará el filtro actual si hay texto en el buscador.
        String currentSearch = etBuscador.getText().toString();
        if (currentSearch.isEmpty()) {
            resetTaxiList();
        } else {
            filterTaxiList(currentSearch);
        }
    }

    private void setupSearchFunctionality() {
        // TextWatcher para el buscador en tiempo real
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesario
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTaxiList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesario
            }
        });

        // Botón limpiar
        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            resetTaxiList();
        });
    }

    private void filterTaxiList(String searchText) {
        filteredTaxiList.clear();

        if (searchText.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos
            filteredTaxiList.addAll(taxiDataList);
        } else {
            // Filtrar por nombre completo (sin distinguir mayúsculas/minúsculas)
            String searchLower = searchText.toLowerCase().trim();
            for (String[] taxi : taxiDataList) {
                // taxi[0] es el "Nombre Completo"
                if (taxi[0].toLowerCase().contains(searchLower)) {
                    filteredTaxiList.add(taxi);
                }
            }
        }
        displayTaxis(); // Refrescar la UI con la lista filtrada
    }

    private void resetTaxiList() {
        filteredTaxiList.clear();
        filteredTaxiList.addAll(taxiDataList);
        displayTaxis(); // Refrescar la UI con la lista completa
    }

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

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, SuperListaTaxisActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId++, builder.build());
    }

    // Método displayTaxis ahora usa filteredTaxiList
    private void displayTaxis() {
        linearLayoutTaxisContainer.removeAllViews(); // Limpia vistas anteriores

        if (filteredTaxiList.isEmpty()) {
            // Mostrar mensaje cuando no hay resultados
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron taxistas");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutTaxisContainer.addView(noResultsText);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < filteredTaxiList.size(); i++) {
            final String[] taxiData = filteredTaxiList.get(i);
            // Encontrar la posición original en taxiDataList
            final int originalPosition = findOriginalPosition(taxiData);

            View taxiItemView = inflater.inflate(R.layout.super_lista_taxis_item, linearLayoutTaxisContainer, false);

            TextView tvNombreTaxistaItem = taxiItemView.findViewById(R.id.tvNombreTaxistaItem);
            ImageView ivEstadoTaxistaItem = taxiItemView.findViewById(R.id.ivEstadoTaxistaItem);

            tvNombreTaxistaItem.setText(taxiData[0]); // Usar Nombre Completo (índice 0)

            String estado = taxiData[10];
            switch (estado) {
                case "activado":
                    ivEstadoTaxistaItem.setImageResource(R.drawable.circle_green);
                    break;
                case "desactivado":
                    ivEstadoTaxistaItem.setImageResource(R.drawable.circle_red);
                    break;
                case "pendiente":
                    ivEstadoTaxistaItem.setImageResource(R.drawable.circle_amber);
                    break;
                default:
                    ivEstadoTaxistaItem.setImageResource(R.drawable.circle_red);
                    break;
            }

            taxiItemView.setOnClickListener(v -> {
                Intent intent;
                if ("activado".equals(estado)) {
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiActivadoActivity.class);
                } else if ("desactivado".equals(estado)) {
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiDesactivadoActivity.class);
                } else { // "pendiente"
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiPendienteActivity.class);
                }

                intent.putExtra("taxi_position", originalPosition); // Pasa la posición original
                intent.putExtra("taxi_data_full", taxiDataList.get(originalPosition)); // Pasa todo el array de datos original
                startActivityForResult(intent, REQUEST_CODE_TAXI_DETAIL);
            });

            linearLayoutTaxisContainer.addView(taxiItemView);
        }
    }

    // Método auxiliar para encontrar la posición original en taxiDataList
    private int findOriginalPosition(String[] targetTaxi) {
        for (int i = 0; i < taxiDataList.size(); i++) {
            String[] taxi = taxiDataList.get(i);
            // Compara por el nombre completo y la placa para una identificación más robusta
            if (taxi[0].equals(targetTaxi[0]) && taxi[9].equals(targetTaxi[9])) {
                return i;
            }
        }
        return -1; // Debería encontrarlo si el targetTaxi proviene de taxiDataList
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TAXI_DETAIL && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            int position = data.getIntExtra("taxi_position", -1);
            String taxiName = data.getStringExtra("taxi_name");

            if (position != -1 && position < taxiDataList.size()) {
                String[] currentTaxiData = taxiDataList.get(position);
                String notificationMessage = "";
                String notificationTitle = "Gestión de Taxistas";

                switch (action) {
                    case "activado":
                        currentTaxiData[10] = "activado";
                        notificationMessage = "El taxista " + taxiName + " se ha activado correctamente.";
                        break;
                    case "desactivado":
                        currentTaxiData[10] = "desactivado";
                        notificationMessage = "El taxista " + taxiName + " se ha desactivado correctamente.";
                        break;
                    case "aprobado":
                        currentTaxiData[10] = "activado";
                        notificationMessage = "El taxista " + taxiName + " ha sido aprobado correctamente.";
                        break;
                    case "rechazado":
                        taxiDataList.remove(position);
                        notificationMessage = "El taxista " + taxiName + " ha sido rechazado correctamente.";
                        break;
                }
                showNotification(notificationTitle, notificationMessage);
            }
            // Actualizar ambas listas y aplicar el filtro actual
            String currentSearch = etBuscador.getText().toString();
            if (currentSearch.isEmpty()) {
                resetTaxiList();
            } else {
                filterTaxiList(currentSearch);
            }
        }
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperListaTaxisActivity.this, SuperActivity.class));
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperListaTaxisActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperListaTaxisActivity.this, SuperEventosActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupCardSuperClickListener() {
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaTaxisActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
    }
}