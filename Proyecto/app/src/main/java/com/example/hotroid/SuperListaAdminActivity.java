package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.List;

public class SuperListaAdminActivity extends AppCompatActivity {

    private LinearLayout linearLayoutAdminsContainer;
    private EditText etBuscador;
    private Button btnLimpiar;

    private static final String CHANNEL_ID = "admin_notifications_channel";
    private static final int NOTIFICATION_ID = 1;

    private static final int REQUEST_CODE_FORMULARIO = 100;
    private static final int REQUEST_CODE_DETALLES_ADMIN = 101;

    // Lista original completa de administradores
    public static List<String[]> adminDataList = new ArrayList<>();
    // Lista filtrada que se muestra en la UI
    private List<String[]> filteredAdminList = new ArrayList<>();

    static {
        if (adminDataList.isEmpty()) {
            adminDataList.add(new String[]{"Victor Díaz", "Oro Verde", "true"});
            adminDataList.add(new String[]{"Moises Castro", "-", "false"});
            adminDataList.add(new String[]{"Manuel Yarleque", "Sauce Resort", "true"});
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_admins);

        createNotificationChannel();

        // Inicializar vistas
        linearLayoutAdminsContainer = findViewById(R.id.linearLayoutAdminsContainer);
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // Inicializar lista filtrada con todos los elementos
        filteredAdminList.addAll(adminDataList);
        renderAdminList();

        // Configurar buscador
        setupSearchFunctionality();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperListaAdminActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperListaAdminActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperListaAdminActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            }
            return false;
        });

        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        Button botonRegistrar = findViewById(R.id.button_regist);
        botonRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminFormularioActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FORMULARIO);
        });
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
                filterAdminList(s.toString());
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
            resetAdminList();
        });
    }

    private void filterAdminList(String searchText) {
        filteredAdminList.clear();

        if (searchText.isEmpty()) {
            // Si no hay texto de búsqueda, mostrar todos
            filteredAdminList.addAll(adminDataList);
        } else {
            // Filtrar por nombre (sin distinguir mayúsculas/minúsculas)
            String searchLower = searchText.toLowerCase().trim();
            for (String[] admin : adminDataList) {
                if (admin[0].toLowerCase().contains(searchLower)) {
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Método actualizado para renderizar la lista filtrada
    private void renderAdminList() {
        linearLayoutAdminsContainer.removeAllViews();

        if (filteredAdminList.isEmpty()) {
            // Mostrar mensaje cuando no hay resultados
            TextView noResultsText = new TextView(this);
            noResultsText.setText("No se encontraron administradores");
            noResultsText.setTextSize(16);
            noResultsText.setPadding(16, 32, 16, 32);
            noResultsText.setGravity(android.view.Gravity.CENTER);
            linearLayoutAdminsContainer.addView(noResultsText);
            return;
        }

        for (int i = 0; i < filteredAdminList.size(); i++) {
            String[] adminData = filteredAdminList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View adminItemView = inflater.inflate(R.layout.super_lista_admins_item, linearLayoutAdminsContainer, false);

            TextView tvUsuario = adminItemView.findViewById(R.id.tvUsuario);
            TextView tvHotel = adminItemView.findViewById(R.id.tvHotel);
            ImageView ivEstado = adminItemView.findViewById(R.id.ivEstado);

            tvUsuario.setText(adminData[0]);

            boolean isActivado = Boolean.parseBoolean(adminData[2]);
            if (isActivado) {
                tvHotel.setText(adminData[1]);
                ivEstado.setImageResource(R.drawable.circle_green);
            } else {
                tvHotel.setText("-");
                ivEstado.setImageResource(R.drawable.circle_red);
            }

            // Encontrar la posición original en adminDataList para el clic
            final int originalPosition = findOriginalPosition(adminData);

            adminItemView.setOnClickListener(v -> {
                Intent intent;
                if (isActivado) {
                    intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminActivadoActivity.class);
                } else {
                    intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminDesactivadoActivity.class);
                }

                intent.putExtra("admin_position", originalPosition);
                intent.putExtra("admin_usuario", adminData[0]);
                startActivityForResult(intent, REQUEST_CODE_DETALLES_ADMIN);
            });

            linearLayoutAdminsContainer.addView(adminItemView);
        }
    }

    // Método auxiliar para encontrar la posición original en adminDataList
    private int findOriginalPosition(String[] targetAdmin) {
        for (int i = 0; i < adminDataList.size(); i++) {
            String[] admin = adminDataList.get(i);
            if (admin[0].equals(targetAdmin[0]) && admin[1].equals(targetAdmin[1]) && admin[2].equals(targetAdmin[2])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            String adminUsuario = data.getStringExtra("admin_usuario");
            int position = data.getIntExtra("admin_position", -1);

            String statusMessage = "";

            if (action != null) {
                switch (action) {
                    case "registrado":
                        String[] nuevoAdminCompleteData = data.getStringArrayExtra("nuevo_admin_data");
                        if (nuevoAdminCompleteData != null && nuevoAdminCompleteData.length >= 10) {
                            adminDataList.add(new String[]{
                                    nuevoAdminCompleteData[0],
                                    nuevoAdminCompleteData[9],
                                    "true"
                            });
                            statusMessage = "El administrador " + nuevoAdminCompleteData[0] + " ha sido registrado correctamente.";
                        } else {
                            statusMessage = "Error al registrar administrador: datos incompletos.";
                        }
                        break;
                    case "activado":
                        if (position != -1 && position < adminDataList.size()) {
                            String nuevoHotelAsignado = data.getStringExtra("nuevo_hotel_asignado");
                            adminDataList.get(position)[1] = nuevoHotelAsignado;
                            adminDataList.get(position)[2] = "true";
                            statusMessage = "El administrador " + adminUsuario + " ha sido activado y asignado a " + nuevoHotelAsignado + ".";
                        }
                        break;
                    case "desactivado":
                        if (position != -1 && position < adminDataList.size()) {
                            adminDataList.get(position)[1] = "-";
                            adminDataList.get(position)[2] = "false";
                            statusMessage = "El administrador " + adminUsuario + " ha sido desactivado y su hotel desasignado.";
                        }
                        break;
                }
            }

            if (!statusMessage.isEmpty()) {
                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Administrador", statusMessage);
            }

            // Actualizar ambas listas y aplicar el filtro actual
            String currentSearch = etBuscador.getText().toString();
            if (currentSearch.isEmpty()) {
                resetAdminList();
            } else {
                filterAdminList(currentSearch);
            }
        }
    }
}