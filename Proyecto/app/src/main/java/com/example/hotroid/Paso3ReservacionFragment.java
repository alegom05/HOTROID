package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

public class Paso3ReservacionFragment extends Fragment {

    private static final String CHANNEL_ID = "reservas_hotel_channel";
    private static final int NOTIFICATION_ID = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paso3_reservacion, container, false);

        // Crear canal de notificación al inicializar el fragmento
        createNotificationChannel();

        Button btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            // Procesar la reserva
            procesarReserva();
        });

        return view;
    }

    /**
     * Crear canal de notificación para Android 8.0 (API 26) y superiores
     */
    private void createNotificationChannel() {
        // Solo crear canal si la versión es Android 8.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de Reservas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Canal para notificaciones de reservas de hotel");

            NotificationManager notificationManager =
                    requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // Solicitar permisos si es necesario (Android 13+)
            askNotificationPermission();
        }
    }

    /**
     * Solicitar permisos de notificación para Android 13 (API 33) y superiores
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Procesar la reserva, mostrar notificación y navegar a ReservasFragment
     */
    private void procesarReserva() {
        // Mostrar notificación de confirmación
        mostrarNotificacionReserva();

        // Navegar a ReservasFragment (tu lógica original)
        Intent intent = new Intent(requireContext(), MisReservasUser.class);
        startActivity(intent);


        // Mostrar mensaje de confirmación
        Toast.makeText(requireContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
    }

    /**
     * Mostrar notificación de confirmación de reserva
     * Al presionar la notificación, llevará a HotelesActivity con HotelesFragment
     */
    private void mostrarNotificacionReserva() {
        // Crear Intent para abrir HotelesActivity directamente
        Intent intent = new Intent(requireContext(), MisReservasUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Indicar que viene desde notificación
        intent.putExtra("from_notification", true);

        // Crear PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotel_notification) // Asegúrate de tener este icono
                .setContentTitle("¡Reserva Confirmada!")
                .setContentText("Tu habitación ha sido reservada exitosamente")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Tu habitación ha sido reservada exitosamente. " +
                                "Toca aquí para ver más hoteles disponibles."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) // La notificación se elimina al tocarla
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Sonido y vibración por defecto

        // Mostrar la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        // Verificar permisos antes de mostrar (Android 13+)
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Manejar el resultado de la solicitud de permisos
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(),
                        "Permisos de notificación concedidos",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(),
                        "Las notificaciones han sido deshabilitadas",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}