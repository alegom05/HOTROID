package com.example.hotroid;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.hotroid.bean.ReservaConHotel;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.Timestamp;

public class DetalleReservaActivo extends AppCompatActivity {

    private TextView tvHotelName, tvHotelLocation, tvRoomDetails;
    private TextView tvStatus, tvArrivalDay, tvGuestsInfo, tvTimeRemaining;
    private TextView tvCheckInDate, tvCheckOutDate, tvReservationCode;
    private Button btnCheckIn, btnCancelReservation, btnCheckOut, btnSolicitarTaxi;
    private Bitmap qrCodeBitmap;
    private LinearLayout checkOutContainer, cancelContainer;
    private ImageView imgCheckInComplete;
    private ReservaConHotel reserva;
    // Datos recibidos del Intent
    private String hotelName, city, hotelLocation, roomDetails, checkInDate,
            checkOutDate, guestsInfo, reservationCode, estado;
    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private boolean checkInRealizado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detalle_reserva_activo);
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //obtener daa del intent
        inicializarVistas();

        btnSolicitarTaxi = findViewById(R.id.btnSolicitarTaxi);

        btnSolicitarTaxi.setOnClickListener(v -> {
            new AlertDialog.Builder(DetalleReservaActivo.this)
                    .setTitle("Solicitar Taxi")
                    .setMessage("¿Desea solicitar un taxi?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        solicitarTaxi();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Obtener datos del Intent
        obtenerDatosIntent();

        // Mostrar datos básicos
        mostrarDatos();
        // Cargar datos adicionales desde Firestore
        cargarDatosAdicionales();


//        try {
//            // Obtener datos del intent
//            Intent intent = getIntent();
//            String hotelName = intent.getStringExtra("hotel_name");
//            String roomDetails = intent.getStringExtra("room_details");
//            String hotelciudad = intent.getStringExtra("city");
//            String hotelLocation = intent.getStringExtra("hotel_location");
//            String status = intent.getStringExtra("status");
//            String checkInDate = intent.getStringExtra("checkInDate");
//            String checkOutDate = intent.getStringExtra("checkOutDate");
//            String reservationCode = intent.getStringExtra("reservationCode");
//            String guestsInfo = intent.getStringExtra("guestsInfo"); // Nuevo extra para información de huéspedes
//
//            // Configurar datos
//            tvHotelName.setText((hotelName != null ? hotelName : "Hotel Desconocido") +
//                    (hotelciudad != null ? hotelciudad : ""));
//            tvHotelLocation.setText(hotelLocation != null ? hotelLocation : "");
//            tvRoomDetails.setText(roomDetails != null ? roomDetails : "Detalles no disponibles");
//
//            // Usar el parámetro guestsInfo si está disponible, o usar una opción alternativa
//            if (guestsInfo != null) {
//                tvGuestsInfo.setText(guestsInfo);
//            } else {
//                // Intentar extraer información de huéspedes de roomDetails como fallback
//                try {
//                    if (roomDetails != null && roomDetails.contains(", ") && roomDetails.split(", ").length >= 3) {
//                        tvGuestsInfo.setText(roomDetails.split(", ")[1] + ", " + roomDetails.split(", ")[2]);
//                    } else {
//                        tvGuestsInfo.setText("Información no disponible");
//                    }
//                } catch (Exception e) {
//                    tvGuestsInfo.setText("Información no disponible");
//                }
//            }
//
//            tvStatus.setText("Estado: " + (status != null ? status : "Confirmado"));
//
//            // Fechas de check-in y check-out
//            if (checkInDate != null && !checkInDate.startsWith("Check-in:")) {
//                tvCheckInDate.setText("Check-in: " + checkInDate);
//            } else {
//                tvCheckInDate.setText(checkInDate != null ? checkInDate : "Check-in: -");
//            }
//
//            if (checkOutDate != null && !checkOutDate.startsWith("Check-out:")) {
//                tvCheckOutDate.setText("Check-out: " + checkOutDate);
//            } else {
//                tvCheckOutDate.setText(checkOutDate != null ? checkOutDate : "Check-out: -");
//            }
//
//            // Intentar calcular el día de la semana
//            String dayOfWeek = "Desconocido";
//            try {
//                // Extraer la fecha sin el prefijo "Check-in: " si existe
//                String dateStr = checkInDate;
//                if (dateStr != null && dateStr.startsWith("Check-in: ")) {
//                    dateStr = dateStr.substring("Check-in: ".length());
//                }
//                dayOfWeek = getDayOfWeek(dateStr);
//            } catch (Exception e) {
//                // En caso de error, usar valor por defecto
//            }
//
//            tvArrivalDay.setText("Día de llegada: " + dayOfWeek);
//            tvReservationCode.setText(reservationCode != null ? reservationCode : "Código de reserva: -");
//
//        } catch (Exception e) {
//            // Manejar cualquier excepción
//            Toast.makeText(this, "Error al cargar los datos de la reserva", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//        }

        // Configurar botones
        setupButtons();
    }

    private void inicializarVistas() {
        tvHotelName = findViewById(R.id.tvHotelNameDetail);
        tvHotelLocation = findViewById(R.id.tvHotelLocation);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvCheckInDate = findViewById(R.id.tvCheckInDate);  //<<--fecha de checkin
        tvArrivalDay = findViewById(R.id.tvArrivalDay);  //<.. dia de llegada (nombre de dia de la semana)
        tvCheckOutDate = findViewById(R.id.tvCheckOutDate); //<<--fecha de checkout
        tvReservationCode = findViewById(R.id.tvReservationCode);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining); //<--tiempo hasta que se active check-in
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnCancelReservation = findViewById(R.id.btnCancelReservation);
        imgCheckInComplete = findViewById(R.id.imgCheckInComplete);
        checkOutContainer = findViewById(R.id.checkOutContainer);
        cancelContainer = findViewById(R.id.cancelContainer);
        tvRoomDetails = findViewById(R.id.tvRoomDetails);
        tvGuestsInfo = findViewById(R.id.tvGuestsInfo);


    }

    private void obtenerDatosIntent() {
        Intent intent = getIntent();
        hotelName = intent.getStringExtra("hotel_name");
        city = intent.getStringExtra("city");
        hotelLocation = intent.getStringExtra("hotel_location");
        roomDetails = intent.getStringExtra("room_details");
        estado = intent.getStringExtra("estado");
        checkInDate = intent.getStringExtra("checkInDate");
        checkOutDate = intent.getStringExtra("checkOutDate");
        reservationCode = intent.getStringExtra("reservationCode");
        guestsInfo = intent.getStringExtra("guestsInfo");

        Log.d(TAG, "Datos recibidos - Hotel: " + hotelName + ", Código: " + reservationCode);
    }

    private void mostrarDatos() {
        // Mostrar datos básicos recibidos del Intent
        Log.d("DetalleReserva", "hotelName: " + hotelName);
        Log.d("DetalleReserva", "city: " + city);
        Log.d("DetalleReserva", "roomDetails: " + roomDetails);
        Log.d("DetalleReserva", "checkInDate: " + checkInDate);
        Log.d("DetalleReserva", "checkOutDate: " + checkOutDate);
        Log.d("DetalleReserva", "estado: " + estado);
        Log.d("DetalleReserva", "reservationCode: " + reservationCode);
        Log.d("DetalleReserva", "guestsInfo: " + guestsInfo);

        if (hotelName != null) {
            tvHotelName.setText(hotelName);
        }

        if (hotelLocation != null && !hotelLocation.isEmpty()) {
            tvHotelLocation.setText(hotelLocation);
        } else if (city != null && !city.isEmpty()) {
            tvHotelLocation.setText(city);
        } else {
            tvHotelLocation.setText("Ubicación no disponible");
        }

        if (roomDetails != null) {
            tvRoomDetails.setText(roomDetails);
        }

        if (checkInDate != null) {
            tvCheckInDate.setText("Check-in: "+ checkInDate);
        }

        if (checkOutDate != null) {
            tvCheckOutDate.setText("Check-out: "+ checkOutDate);
        }

        if (guestsInfo != null) {
            tvGuestsInfo.setText(guestsInfo);
        }

        if (reservationCode != null) {
            tvReservationCode.setText("Código: " + reservationCode);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d("IntentExtra", key + " => " + extras.get(key));
            }
        } else {
            Log.d("IntentExtra", "No extras found");
        }

    }

    private void cargarDatosAdicionales() {
        if (reservationCode == null) {
            Log.w(TAG, "No se puede cargar datos adicionales: código de reserva nulo");
            return;
        }

        // Buscar la reserva en Firestore para obtener datos adicionales
        db.collection("reservas")
                .document(reservationCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Reserva encontrada en Firestore");

                        // Obtener datos adicionales de la reserva
                        String hotelId = documentSnapshot.getString("idHotel");

                        // Cargar información del hotel
                        if (hotelId != null) {
                            cargarInformacionHotel(hotelId);
                        }

                        // Aquí puedes obtener más datos si son necesarios
                        // Por ejemplo: precio, servicios adicionales, etc.

                    } else {
                        Log.w(TAG, "Reserva no encontrada en Firestore");
                        Toast.makeText(this, "No se pudieron cargar algunos detalles", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos de la reserva: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar detalles completos", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarInformacionHotel(String hotelId) {
        db.collection("hoteles")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
//                        // Cargar imagen del hotel
//                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
//                        if (imageUrls != null && !imageUrls.isEmpty()) {
//                            Glide.with(this)
//                                    .load(imageUrls.get(0))
//                                    .placeholder(R.drawable.placeholder_hotel)
//                                    .error(R.drawable.ic_hotel_error)
//                                    .centerCrop()
//                                    .into(ivHotelImage);
//                        }

                        // Actualizar ubicación si no se tenía
                        String direccion = documentSnapshot.getString("direccion");
                        String pais = documentSnapshot.getString("País");
                        String direccionaven = documentSnapshot.getString("direccionDetallada");
                        if (direccion != null && (hotelLocation == null || hotelLocation.isEmpty())) {
                            tvHotelLocation.setText(direccion);
                            if(direccionaven!=null){
                                tvHotelLocation.setText(direccionaven + ", "+ direccion);
                            }
                        }

                        Log.d(TAG, "Información del hotel cargada exitosamente");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar información del hotel: " + e.getMessage());
                    // Cargar imagen por defecto
//                    ivHotelImage.setImageResource(R.drawable.placeholder_hotel);
                });
    }


    private void cancelarReserva() {
        if (reservationCode == null) {
            Toast.makeText(this, "Error: Código de reserva no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la reserva se pueda cancelar
        // (por ejemplo, verificar fechas, políticas de cancelación, etc.)
        verificarPoliticaCancelacion();
    }

    private void verificarPoliticaCancelacion() {
        try {
            // Verificar si estamos dentro del plazo de cancelación
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date fechaCheckIn = sdf.parse(checkInDate);
            Date fechaActual = new Date();

            if (fechaCheckIn != null) {
                long diferenciaMilisegundos = fechaCheckIn.getTime() - fechaActual.getTime();
                long diferenciaDias = diferenciaMilisegundos / (1000 * 60 * 60 * 24);

                if (diferenciaDias >= 1) { // Se puede cancelar si falta al menos 1 día
                    showCancelReservationDialog();
                } else {
                    Toast.makeText(this, "No se puede cancelar la reserva. Debe hacerlo al menos 24 horas antes.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al verificar política de cancelación: " + e.getMessage());
            showCancelReservationDialog(); // En caso de error, permitir cancelación
        }
    }

    private void solicitarTaxi() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidCliente = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uidCliente == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener datos del usuario desde Firestore
        db.collection("usuarios").document(uidCliente)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Extraer nombres y apellidos del documento del usuario
                        String nombresCliente = documentSnapshot.getString("nombres");
                        String apellidosCliente = documentSnapshot.getString("apellidos");

                        // Valores por defecto si no existen en el documento
                        if (nombresCliente == null) nombresCliente = "Isaac";
                        if (apellidosCliente == null) apellidosCliente = "Huamani";

                        // Crear el documento para alertas_taxi
                        Map<String, Object> alertaTaxi = new HashMap<>();
                        alertaTaxi.put("apellidosCliente", apellidosCliente);
                            alertaTaxi.put("destino", "Aeropuerto Internacional Jorge Chávez"); // Puedes hacer esto dinámico si es necesario
                        alertaTaxi.put("documentId", null);
                        alertaTaxi.put("estadoViaje", "No asignado");
                        alertaTaxi.put("nombresCliente", nombresCliente);
                        alertaTaxi.put("origen", "Libertador"); // Puedes hacer esto dinámico basado en el hotel
                        alertaTaxi.put("region", "Lima"); // Puedes hacer esto dinámico basado en la ubicación
                        alertaTaxi.put("tiempoTranscurrido", "Tiempo desconocido");
                        alertaTaxi.put("timestamp", Timestamp.now()); // Usar Timestamp de Firebase
                        alertaTaxi.put("idCliente", uidCliente); // Agregar el ID del cliente para referencia

                        // Crear el documento en la colección alertas_taxi
                        db.collection("alertas_taxi")
                                .add(alertaTaxi)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(DetalleReservaActivo.this, "Taxi solicitado correctamente", Toast.LENGTH_SHORT).show();
                                    // Ocultar el botón de solicitar taxi
                                    btnSolicitarTaxi.setVisibility(View.GONE);
                                    // Navegar a la actividad de servicio de taxi
                                    Intent intent = new Intent(DetalleReservaActivo.this, UserServTaxi.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetalleReservaActivo.this, "Error al solicitar taxi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(DetalleReservaActivo.this, "Error: No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DetalleReservaActivo.this, "Error al obtener datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void verificarViajeExistente() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uidCliente = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (uidCliente == null) return;

        // Verificar si ya existe una alerta de taxi pendiente para este usuario
        db.collection("alertas_taxi")
                .whereEqualTo("idCliente", uidCliente)
                .whereEqualTo("estadoViaje", "No asignado")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // El usuario ya tiene una alerta de taxi pendiente
                        btnSolicitarTaxi.setVisibility(View.GONE);
                    }
                });
    }

    private void setupButtons() {
        // Botón Check-In
        btnCheckIn.setOnClickListener(v -> {
            showCheckInDialog();
        });

        // Botón Cancelar Reserva
        btnCancelReservation.setOnClickListener(v -> {
//            showCancelReservationDialog();
            cancelarReserva();
        });

        btnCheckOut.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleReservaActivo.this, CheckOutUser.class);
            startActivity(intent);
        });


        verificarViajeExistente();
    }

    private void showCheckInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_check_in, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        // Obtener referencias a las vistas del diálogo
        ImageView imageViewQRCode = dialogView.findViewById(R.id.imageViewQRCode);
        TextView tvReservationCodeQR = dialogView.findViewById(R.id.tvReservationCodeQR);
        TextView tvGuestNameQR = dialogView.findViewById(R.id.tvGuestNameQR);
        Button btnCancelCheckIn = dialogView.findViewById(R.id.btnCancelCheckIn);
        Button btnDownloadQR = dialogView.findViewById(R.id.btnDownloadQR);

        // Establecer datos en las vistas
        tvReservationCodeQR.setText("RES123456");
        tvGuestNameQR.setText("Juan Pérez"); // Este sería el nombre del usuario logueado

        // Generar código QR
        String qrCodeData = generateQRCodeData("RES123456", "Juan Pérez");
        try {
            qrCodeBitmap = generateQRCode(qrCodeData, 500);
            imageViewQRCode.setImageBitmap(qrCodeBitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al generar el código QR", Toast.LENGTH_SHORT).show();
        }

        // Configurar botones
        btnCancelCheckIn.setOnClickListener(v -> dialog.dismiss());

        btnDownloadQR.setOnClickListener(v -> {
            if (qrCodeBitmap != null) {
                saveQRCodeToGallery(qrCodeBitmap);
            }
        });

        dialog.show();
    }

    // Método para generar los datos del código QR
    private String generateQRCodeData(String reservationCode, String guestName) {
        // Aquí puedes formatear los datos como desees, por ejemplo, en formato JSON
        return "{\"reservationCode\":\"" + reservationCode +
                "\",\"guestName\":\"" + guestName +
                "\",\"hotelName\":\"Hotel Libertador\",\"checkInDate\":\"21/04/2025\"}";
    }

    // Método para generar el código QR como un Bitmap
    private Bitmap generateQRCode(String data, int size) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    size, size, null
            );
        } catch (IllegalArgumentException e) {
            return null;
        }

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    // Método para guardar el código QR en la galería
    private void saveQRCodeToGallery(Bitmap bitmap) {
        String fileName = "QR_Reservation_" + System.currentTimeMillis() + ".jpg";

        try {
            // Para versiones más recientes de Android (>= Q)
            String savedImageURL = MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    bitmap,
                    fileName,
                    "Código QR de reserva Hotel Libertador"
            );

            if (savedImageURL != null) {
                Toast.makeText(this, "Código QR guardado en la galería", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al guardar el código QR", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar el código QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCancelReservationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancelar Reserva");
        builder.setMessage("¿Estás seguro de que deseas cancelar esta reserva?");

        builder.setPositiveButton("Sí, cancelar", (dialog, which) -> {
            // Lógica para cancelar la reserva
            ejecutarCancelacion();
            Toast.makeText(DetalleReservaActivo.this,
                    "Reserva cancelada con éxito", Toast.LENGTH_LONG).show();
            finish(); // Cerrar la actividad después de cancelar
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void ejecutarCancelacion() {
        if (reservationCode == null) return;

        // Mostrar indicador de carga
        btnCancelReservation.setEnabled(false);
        btnCancelReservation.setText("Cancelando...");

        // Actualizar el estado de la reserva en Firestore
        db.collection("reservas")
                .document(reservationCode)
                .update("estado", "cancelado")
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Reserva cancelada exitosamente");
                    Toast.makeText(this, "Reserva cancelada exitosamente", Toast.LENGTH_SHORT).show();

                    // Regresar a la actividad anterior
                    setResult(RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cancelar reserva: " + e.getMessage());
                    Toast.makeText(this, "Error al cancelar la reserva. Inténtalo más tarde.",
                            Toast.LENGTH_SHORT).show();

                    // Restaurar botón
                    btnCancelReservation.setEnabled(true);
                    btnCancelReservation.setText("Cancelar Reserva");
                });
    }


    // Método para obtener el día de la semana a partir de una fecha
    private String getDayOfWeek(String dateString) {
        if (dateString == null) return "Desconocido";

        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = format.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            String[] days = new String[] {"Domingo", "Lunes", "Martes", "Miércoles",
                    "Jueves", "Viernes", "Sábado"};
            return days[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        } catch (Exception e) {
            e.printStackTrace();
            return "Desconocido";
        }
    }

    // Manejar clic en la flecha de retroceso
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // vuelve a la actividad anterior
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}