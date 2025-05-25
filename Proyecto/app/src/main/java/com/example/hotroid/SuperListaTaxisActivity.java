package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat; // Para construir la notificación
import androidx.core.app.NotificationManagerCompat; // Para usar NotificationManagerCompat

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays; // No estrictamente necesario si solo manejas ArrayLists directamente

public class SuperListaTaxisActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_TAXI_DETAIL = 1;
    private static final String CHANNEL_ID = "taxi_management_channel";
    // Usaremos un ID único para cada notificación para que no se sobrescriban.
    // Aunque el ejemplo de Admin usa un ID fijo, para taxistas donde puede haber
    // múltiples actualizaciones, es mejor que cada notificación sea nueva.
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

        createNotificationChannel(); // Crear el canal de notificación
        setupBottomNavigationView();
        setupCardSuperClickListener();
        displayTaxis(); // Muestra los taxistas en la lista
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Asegúrate de refrescar la lista cada vez que la actividad vuelve a estar en primer plano
        displayTaxis();
    }

    private void createNotificationChannel() {
        // Solo para Android 8.0 (API nivel 26) y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Se usa el R.string para que sea consistente con la buena práctica.
            // Asegúrate de que estas cadenas existan en res/values/strings.xml
            CharSequence name = "Notificaciones de Taxistas";
            String description = "Canal para notificaciones de registro y estado de taxistas";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Registrar el canal con el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        // Crear un Intent que se abrirá al hacer clic en la notificación
        Intent intent = new Intent(this, SuperListaTaxisActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // FLAG_IMMUTABLE es requerido en API 31+ para PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                // Usando el icono de ejemplo que tenías en SuperListaAdminActivity
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de tener este icono en res/drawable
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad para heads-up notification (pop-up)
                .setContentIntent(pendingIntent) // Establece el Intent que se lanza al hacer clic
                .setAutoCancel(true); // La notificación se borra automáticamente al hacer clic

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId++ asegura que cada notificación tenga un ID único y no sobrescriba la anterior
        // El ejemplo de Admin usa un ID fijo (NOTIFICATION_ID = 1), pero para múltiples notificaciones
        // de estado, es mejor usar un ID incremental para que aparezcan todas.
        notificationManager.notify(notificationId++, builder.build());
    }

    private void displayTaxis() {
        linearLayoutTaxisContainer.removeAllViews(); // Limpia vistas anteriores

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < taxiDataList.size(); i++) {
            final int position = i;
            final String[] taxiData = taxiDataList.get(i);

            View taxiItemView = inflater.inflate(R.layout.super_lista_taxis_item, linearLayoutTaxisContainer, false);

            TextView tvNombreTaxistaItem = taxiItemView.findViewById(R.id.tvNombreTaxistaItem);
            ImageView ivEstadoTaxistaItem = taxiItemView.findViewById(R.id.ivEstadoTaxistaItem);

            // Nombre completo del taxista
            tvNombreTaxistaItem.setText(taxiData[1] + " " + taxiData[2]); // Nombres + Apellidos

            // Establecer el drawable del estado
            String estado = taxiData[10]; // El estado está en la posición 10
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
                    ivEstadoTaxistaItem.setImageResource(R.drawable.circle_red); // Default a rojo
                    break;
            }

            // Configurar el click listener para cada item
            taxiItemView.setOnClickListener(v -> {
                Intent intent;
                if ("activado".equals(estado)) {
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiActivadoActivity.class);
                } else if ("desactivado".equals(estado)) {
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiDesactivadoActivity.class);
                } else { // "pendiente"
                    intent = new Intent(SuperListaTaxisActivity.this, SuperDetallesTaxiPendienteActivity.class);
                }

                intent.putExtra("taxi_position", position);
                intent.putExtra("taxi_data_full", taxiData); // Pasa todo el array de datos
                startActivityForResult(intent, REQUEST_CODE_TAXI_DETAIL);
            });

            linearLayoutTaxisContainer.addView(taxiItemView);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_TAXI_DETAIL && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            int position = data.getIntExtra("taxi_position", -1);
            String taxiName = data.getStringExtra("taxi_name"); // El nombre del taxista para la notificación

            if (position != -1 && position < taxiDataList.size()) {
                String[] currentTaxiData = taxiDataList.get(position);
                String notificationMessage = "";
                String notificationTitle = "Gestión de Taxistas"; // Título genérico para la notificación

                switch (action) {
                    case "activado":
                        currentTaxiData[10] = "activado"; // Actualiza el estado a activado
                        notificationMessage = "El taxista " + taxiName + " se ha activado correctamente.";
                        break;
                    case "desactivado":
                        currentTaxiData[10] = "desactivado"; // Actualiza el estado a desactivado
                        notificationMessage = "El taxista " + taxiName + " se ha desactivado correctamente.";
                        break;
                    case "aprobado":
                        currentTaxiData[10] = "activado"; // Si se aprueba, cambia a activado
                        notificationMessage = "El taxista " + taxiName + " ha sido aprobado correctamente.";
                        break;
                    case "rechazado":
                        taxiDataList.remove(position); // Elimina al taxista de la lista
                        notificationMessage = "El taxista " + taxiName + " ha sido rechazado correctamente.";
                        break;
                }
                // Mostrar la notificación en la barra de estado SIN SONIDO (como en tu ejemplo de Admin)
                showNotification(notificationTitle, notificationMessage);

                // Opcional: Si aún quieres el Toast además de la notificación en barra de estado
                // Toast.makeText(this, notificationMessage, Toast.LENGTH_SHORT).show();

                displayTaxis(); // Refresca la lista para mostrar los cambios
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