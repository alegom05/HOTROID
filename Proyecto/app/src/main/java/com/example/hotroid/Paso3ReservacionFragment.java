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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.example.hotroid.bean.Reserva;
import com.example.hotroid.util.ReservacionTempData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Paso3ReservacionFragment extends Fragment {

    private static final String CHANNEL_ID = "reservas_hotel_channel";
    private static final int NOTIFICATION_ID = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private TextView tvResumenHotel, tvResumenFechas, tvResumenHuespedes;
    private TextView tvResumenServicios, tvResumenPrecio;
    private Button btnReservar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paso3_reservacion, container, false);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Crear canal de notificación
        createNotificationChannel();

        // Inicializar vistas
        inicializarVistas(view);

        // Mostrar resumen
        mostrarResumen();

        // Configurar botón
        btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            guardarReservaEnFirebase();
        });

        return view;
    }

    private void inicializarVistas(View view) {
        tvResumenHotel = view.findViewById(R.id.tvResumenHotel);
        tvResumenFechas = view.findViewById(R.id.tvResumenFechas);
        tvResumenHuespedes = view.findViewById(R.id.tvResumenHuespedes);
        tvResumenServicios = view.findViewById(R.id.tvResumenServicios);
        tvResumenPrecio = view.findViewById(R.id.tvResumenPrecio);
        btnReservar = view.findViewById(R.id.btnReservar);
    }

    private void mostrarResumen() {
        // Mostrar información del hotel
        tvResumenHotel.setText("Hotel: " + ReservacionTempData.nombreHotel);

        // Mostrar fechas
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
        String fechaInicio = dateFormat.format(ReservacionTempData.fechaInicio);
        String fechaFin = dateFormat.format(ReservacionTempData.fechaFin);
        tvResumenFechas.setText("Check-in: " + fechaInicio + "\nCheck-out: " + fechaFin);

        // Mostrar información de huéspedes
        tvResumenHuespedes.setText("Habitaciones: " + ReservacionTempData.habitaciones +
                "\nAdultos: " + ReservacionTempData.adultos +
                "\nNiños: " + ReservacionTempData.ninos);

        // Mostrar servicios seleccionados
        StringBuilder servicios = new StringBuilder("Servicios adicionales:\n");
        if (ReservacionTempData.gimnasio) servicios.append("- Gimnasio\n");
        if (ReservacionTempData.desayuno) servicios.append("- Desayuno\n");
        if (ReservacionTempData.piscina) servicios.append("- Piscina\n");
        if (ReservacionTempData.parqueo) servicios.append("- Parqueo\n");
        if (!ReservacionTempData.gimnasio && !ReservacionTempData.desayuno &&
                !ReservacionTempData.piscina && !ReservacionTempData.parqueo) {
            servicios.append("- Ninguno\n");
        }
        tvResumenServicios.setText(servicios.toString());

        // Mostrar precio
        tvResumenPrecio.setText(String.format("Precio base: PEN %.2f\n" +
                        "Adicionales: PEN %.2f\n" +
                        "TOTAL: PEN %.2f",
                ReservacionTempData.precioBase,
                ReservacionTempData.cobrosAdicionales,
                ReservacionTempData.precioTotal));
    }

    private void guardarReservaEnFirebase() {
        // Generar número aleatorio para la habitación (esto sería reemplazado por lógica real)
        Random random = new Random();
        int roomNumber = 100 + random.nextInt(900);
        List<Integer> roomNumbers = new ArrayList<>();
        roomNumbers.add(roomNumber);

        // Crear objeto Reserva con datos completos
        Reserva nuevaReserva = new Reserva(
                UUID.randomUUID().toString(), // ID de reserva único
                auth.getCurrentUser().getUid(), // ID del usuario actual
                ReservacionTempData.nombresCliente,
                ReservacionTempData.apellidosCliente,
                ReservacionTempData.idHotel,
                ReservacionTempData.nombreHotel,
                ReservacionTempData.habitaciones,
                ReservacionTempData.adultos,
                ReservacionTempData.ninos,
                ReservacionTempData.fechaInicio,
                ReservacionTempData.fechaFin,
                "activo", // Estado inicial
                ReservacionTempData.precioTotal,
                false, // Check-in no realizado
                false, // Check-out no realizado
                ReservacionTempData.cobrosAdicionales,
                false, // No está cancelada
                null, // Fecha de cancelación null
                null, // Sin valoración aún
                roomNumbers, // Número de habitación asignado
                false // Sin valoración aún
        );

        // Guardar en Firestore
        db.collection("reservas")
                .document(nuevaReserva.getIdReserva())
                .set(nuevaReserva)
                .addOnSuccessListener(aVoid -> {
                    // Éxito: mostrar notificación y redireccionar
                    Toast.makeText(requireContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
                    mostrarNotificacionReserva();
                    navegarAMisReservas();
                })
                .addOnFailureListener(e -> {
                    // Error: mostrar mensaje de error
                    Toast.makeText(requireContext(), "Error al guardar la reserva: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void navegarAMisReservas() {
        // Limpiar datos temporales
        ReservacionTempData.reset();

        // Navegar a MisReservasUser
        Intent intent = new Intent(requireContext(), MisReservasUser.class);
        startActivity(intent);
        requireActivity().finish(); // Cerrar actividad actual
    }

    // Los demás métodos para notificaciones permanecen igual
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

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void mostrarNotificacionReserva() {
        // Crear Intent para abrir MisReservasUser directamente
        Intent intent = new Intent(requireContext(), MisReservasUser.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Crear PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotel_notification)
                .setContentTitle("¡Reserva Confirmada!")
                .setContentText("Tu habitación ha sido reservada exitosamente")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Tu habitación ha sido reservada exitosamente en " +
                                ReservacionTempData.nombreHotel + ". Toca aquí para ver tus reservas."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        // Mostrar la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());

        // Verificar permisos antes de mostrar (Android 13+)
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}