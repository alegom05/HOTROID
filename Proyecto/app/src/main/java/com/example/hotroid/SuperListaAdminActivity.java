package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
    private static final String CHANNEL_ID = "admin_notifications_channel";
    private static final int NOTIFICATION_ID = 1;

    private static final int REQUEST_CODE_FORMULARIO = 100;
    private static final int REQUEST_CODE_DETALLES_ADMIN = 101; // Un solo request code para ambos detalles (activado/desactivado)

    // Lista estática para simular la base de datos de administradores
    // Formato de cada String[]: {Nombre Completo, Hotel Asignado, Estado (true/false)}
    // NOTA: En una aplicación real, considera un enfoque más robusto para la gestión de datos.
    public static List<String[]> adminDataList = new ArrayList<>();

    static {
        // Inicializa la lista si está vacía
        if (adminDataList.isEmpty()) {
            adminDataList.add(new String[]{"Victor Díaz", "Oro Verde", "true"});
            adminDataList.add(new String[]{"Moises Castro", "-", "false"}); // Desactivado sin hotel asignado
            adminDataList.add(new String[]{"Manuel Yarleque", "Sauce Resort", "true"});
            // Puedes añadir más admins desactivados que ya tenían un hotel para probar el otro caso
            // adminDataList.add(new String[]{"Ana Torres", "Hotel Fantasia", "false"});
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_lista_admins); // Asegúrate de que este layout exista

        createNotificationChannel();

        linearLayoutAdminsContainer = findViewById(R.id.linearLayoutAdminsContainer);

        renderAdminList(); // Carga y muestra la lista inicial

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperListaAdminActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                // Ya estamos en usuarios, no hacemos nada o refrescamos si es necesario
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperListaAdminActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            }
            return false;
        });

        CardView cardSuper = findViewById(R.id.cardSuper); // Asegúrate de que este ID exista en tu layout
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        Button botonRegistrar = findViewById(R.id.button_regist); // Asegúrate de que este ID exista en tu layout
        botonRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminFormularioActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FORMULARIO);
        });
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
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este icono en res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // La notificación se cierra al tocarla

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Método para renderizar/actualizar la lista de administradores
    private void renderAdminList() {
        linearLayoutAdminsContainer.removeAllViews(); // Limpia la vista antes de renderizar

        for (int i = 0; i < adminDataList.size(); i++) {
            String[] adminData = adminDataList.get(i);
            LayoutInflater inflater = LayoutInflater.from(this);
            View adminItemView = inflater.inflate(R.layout.super_lista_admins_item, linearLayoutAdminsContainer, false); // Asegúrate de que este layout exista

            TextView tvUsuario = adminItemView.findViewById(R.id.tvUsuario);
            TextView tvHotel = adminItemView.findViewById(R.id.tvHotel);
            ImageView ivEstado = adminItemView.findViewById(R.id.ivEstado);

            tvUsuario.setText(adminData[0]); // Nombre del administrador

            // Lógica para mostrar el hotel o "-"
            boolean isActivado = Boolean.parseBoolean(adminData[2]);
            if (isActivado) {
                tvHotel.setText(adminData[1]); // Muestra el hotel si está activado
            } else {
                tvHotel.setText("-"); // Siempre muestra "-" si está desactivado
            }

            if (isActivado) {
                ivEstado.setImageResource(R.drawable.circle_green); // Icono verde para activado
            } else {
                ivEstado.setImageResource(R.drawable.circle_red);   // Icono rojo para desactivado
            }

            final int clickedPosition = i; // Guardar la posición para el clic

            adminItemView.setOnClickListener(v -> {
                Intent intent;
                if (isActivado) {
                    intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminActivadoActivity.class);
                } else {
                    intent = new Intent(SuperListaAdminActivity.this, SuperDetallesAdminDesactivadoActivity.class);
                }
                // Pasar la posición y el nombre de usuario a la actividad de detalle
                intent.putExtra("admin_position", clickedPosition);
                intent.putExtra("admin_usuario", adminData[0]); // Pasar el nombre de usuario

                // Opcional: Pasar todos los detalles si tu adminDataList es más compleja
                // Esto es crucial si adminDataList no almacena todos los campos o si no quieres hacer la lista estática.
                // Ejemplo (descomenta y ajusta si tu adminDataList tiene más campos):
                /*
                if (adminData.length > 3) { // Asumiendo que los campos adicionales están después del estado
                    intent.putExtra("admin_nombres", adminData[3]);
                    intent.putExtra("admin_apellidos", adminData[4]);
                    intent.putExtra("admin_tipo_doc", adminData[5]);
                    // ... y así sucesivamente para todos los campos
                }
                */
                startActivityForResult(intent, REQUEST_CODE_DETALLES_ADMIN); // Usar un único REQUEST_CODE
            });

            linearLayoutAdminsContainer.addView(adminItemView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            String adminUsuario = data.getStringExtra("admin_usuario"); // Nombre del admin para el mensaje
            int position = data.getIntExtra("admin_position", -1); // Posición si viene de detalles

            String statusMessage = ""; // Mensaje para Toast y Notificación

            if (action != null) {
                switch (action) {
                    case "registrado":
                        String[] nuevoAdminCompleteData = data.getStringArrayExtra("nuevo_admin_data");
                        if (nuevoAdminCompleteData != null && nuevoAdminCompleteData.length >= 10) {
                            // Asume que nuevoAdminCompleteData[0] es el nombre completo
                            // y nuevoAdminCompleteData[9] es el hotel asignado
                            adminDataList.add(new String[]{
                                    nuevoAdminCompleteData[0],  // Nombre Completo
                                    nuevoAdminCompleteData[9],  // Hotel Asignado (índice 9 del array completo)
                                    "true"                      // Estado inicial: activado
                            });
                            statusMessage = "El administrador " + nuevoAdminCompleteData[0] + " ha sido registrado correctamente.";
                        } else {
                            statusMessage = "Error al registrar administrador: datos incompletos.";
                        }
                        break;
                    case "activado":
                        if (position != -1 && position < adminDataList.size()) {
                            String nuevoHotelAsignado = data.getStringExtra("nuevo_hotel_asignado");
                            adminDataList.get(position)[1] = nuevoHotelAsignado; // Actualiza el hotel
                            adminDataList.get(position)[2] = "true";             // Activa al administrador
                            statusMessage = "El administrador " + adminUsuario + " ha sido activado y asignado a " + nuevoHotelAsignado + ".";
                        }
                        break;
                    case "desactivado":
                        if (position != -1 && position < adminDataList.size()) {
                            adminDataList.get(position)[1] = "-";   // Asigna "-" al hotel cuando se desactiva
                            adminDataList.get(position)[2] = "false"; // Desactiva al administrador
                            statusMessage = "El administrador " + adminUsuario + " ha sido desactivado y su hotel desasignado.";
                        }
                        break;
                }
            }

            // Mostrar Toast y Notificación solo si hay un mensaje de estado
            if (!statusMessage.isEmpty()) {
                Toast.makeText(this, statusMessage, Toast.LENGTH_SHORT).show();
                showNotification("Actualización de Administrador", statusMessage);
            }

            // Volver a renderizar la lista para reflejar los cambios en la UI
            renderAdminList();
        }
    }
}