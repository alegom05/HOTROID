package com.example.hotroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hotroid.bean.RoomGroupOption;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hotroid.util.ReservacionTempData;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Paso2ReservacionFragment extends Fragment {
    private static final String CHANNEL_ID = "reservas_hotel_channel";
    private static final int NOTIFICATION_ID = 100;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private TextView tvResumenCliente, tvResumenHabitaciones, tvResumenFechas, tvResumenServicios;
    private MaterialButton btnConfirmarReserva;
    // Datos recibidos
    private ArrayList<Integer> roomNumbersSeleccionados;
    private TextView tvHuesped, tvFechaInicioResumen, tvFechaFinResumen, tvDuracionResumen;
    private TextView tvCantidadPersonas, tvTipoHabitacion;
    private TextView tvPrecioHabitacion, tvPrecioGimnasio, tvPrecioDesayuno, tvPrecioPiscina, tvPrecioParqueo;
    private TextView tvSubtotal, tvImpuestos, tvTotal;

    private String nombreCompleto, idHotel, numeroTarjeta, fechaVencimiento, cvv;
    private String dni, nombre, apellido;
    private RoomGroupOption opcionSeleccionada;
    private Date fechaInicio, fechaFin;
    private int cantidadPersonas, cantidadNinios, numNoches,numHabitaciones;
    private boolean gimnasio, desayuno, piscina, parqueo;
    private double costoHabitacion;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));

    //private double precioBase = 200; // Precio base por habitación
    private final double IGV = 0.18;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paso2_reservacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Crear canal de notificación
        createNotificationChannel();

        // Inicializar componentes
        // Enlazar vistas
        tvHuesped = view.findViewById(R.id.tvHuesped);
        tvFechaInicioResumen = view.findViewById(R.id.tvFechaInicioResumen);
        tvFechaFinResumen = view.findViewById(R.id.tvFechaFinResumen);
        tvDuracionResumen = view.findViewById(R.id.tvDuracionResumen);
        tvCantidadPersonas = view.findViewById(R.id.tvHuespedesResumen);
        tvTipoHabitacion = view.findViewById(R.id.tvTipoHabitacion);
        tvPrecioHabitacion = view.findViewById(R.id.tvCostoHabitacion);
        tvPrecioGimnasio = view.findViewById(R.id.tvPrecioGimnasio);
        tvPrecioDesayuno = view.findViewById(R.id.tvCostoDesayuno);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvImpuestos = view.findViewById(R.id.tvImpuestos);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnConfirmarReserva = view.findViewById(R.id.btnConfirmarReserva);

        // Recuperar Bundle
        Bundle args = getArguments();
        if (args != null) {
            opcionSeleccionada = args.getParcelable("opcionSeleccionada");
            roomNumbersSeleccionados = args.getIntegerArrayList("roomNumbersSeleccionados");
            fechaInicio = new Date(args.getLong("fechaInicio"));
            fechaFin = new Date(args.getLong("fechaFin"));
            cantidadPersonas = args.getInt("cantidadPersonas");
            cantidadNinios = args.getInt("niniosSolicitados");
            numHabitaciones = args.getInt("numHabitaciones");
            idHotel = args.getString("idHotel");
            nombre = args.getString("nombre");
            apellido = args.getString("apellido");
            dni = args.getString("dni");
            numeroTarjeta = args.getString("numeroTarjeta");
            fechaVencimiento = args.getString("fechaVencimiento");
            cvv = args.getString("cvv");

            long diff = fechaFin.getTime() - fechaInicio.getTime();
            numNoches = (int) (diff / (1000 * 60 * 60 * 24));

            gimnasio = args.getBoolean("gimnasio");

            mostrarResumen();
        }


        // Listener del botón
        btnConfirmarReserva.setOnClickListener(v -> {
            guardarReservaEnFirebase();
        });

    }

    private void mostrarResumen( ) {
        tvHuesped.setText(nombre + " " + apellido);
        tvFechaInicioResumen.setText(formatoFecha.format(fechaInicio));
        tvFechaFinResumen.setText(formatoFecha.format(fechaFin));
        tvDuracionResumen.setText(numNoches + " noches");
        tvCantidadPersonas.setText(cantidadPersonas + " adultos, " + cantidadNinios + " niños");
        tvTipoHabitacion.setText(numHabitaciones + " x " + opcionSeleccionada.getRoomType());

        costoHabitacion = opcionSeleccionada.getPrecioPorHabitacion() * numHabitaciones * numNoches;
        tvPrecioHabitacion.setText("S/ " + costoHabitacion);

        double costoGimnasio = gimnasio ? 20 : 0;
        double costoDesayuno = desayuno ? 30 : 0;
        double costoPiscina = piscina ? 25 : 0;
        double costoParqueo = parqueo ? 15 : 0;
        tvPrecioGimnasio.setText("S/ " + costoGimnasio);
        tvPrecioDesayuno.setText("S/ " + costoDesayuno);
        tvPrecioPiscina.setText("S/ " + costoPiscina);
        tvPrecioParqueo.setText("S/ " + costoParqueo);

        double subtotal = costoHabitacion + costoGimnasio + costoDesayuno + costoPiscina + costoParqueo;
        double impuestos = subtotal * IGV;
        double total = subtotal + impuestos;

        tvSubtotal.setText("S/ " + subtotal);
        tvImpuestos.setText("S/ " + impuestos);
        tvTotal.setText("S/ " + total);
    }

    private void guardarReservaEnFirebase() {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> reserva = new HashMap<>();
        reserva.put("idPersona", userId); //16
        reserva.put("idHotel", idHotel); //15
        reserva.put("estado", "activo"); //8
        reserva.put("nombresCliente", nombre);
        reserva.put("apellidosCliente", apellido); //2.
        reserva.put("dniCliente", dni);
        reserva.put("fechaInicio", fechaInicio); //12
        reserva.put("fechaFin", fechaFin); //11
        reserva.put("numNoches", numNoches); //13
        reserva.put("roomNumber", roomNumbersSeleccionados); //21
        reserva.put("numHabitaciones", numHabitaciones); //14
        reserva.put("adultos", cantidadPersonas); //1. adultos
        reserva.put("ninos", cantidadNinios); //19
        reserva.put("tipoHabitacion", opcionSeleccionada.getRoomType());
        reserva.put("gimnasio", gimnasio);
        reserva.put("desayuno", desayuno);
        reserva.put("piscina", piscina);
        reserva.put("parqueo", parqueo);
        reserva.put("idValoracion", null); //18
        reserva.put("precioTotal", costoHabitacion); //20. costo toal de las habitaciones
        reserva.put("cobrosAdicionales", 0.0); //6
        reserva.put("checkInRealizado", false); //4
        reserva.put("checkOutRealizado", false); //5
        reserva.put("fechaCancelacion", null); //9
        reserva.put("fechaCreacion", FieldValue.serverTimestamp()); //10


        db.collection("reservas")
                .add(reserva)
                .addOnSuccessListener(documentReference -> {
                    mostrarNotificacionReserva();
                    Toast.makeText(getContext(), "Reserva confirmada correctamente",
                            Toast.LENGTH_LONG).show();
                    navegarAMisReservas();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar reserva: " + e.getMessage(),
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