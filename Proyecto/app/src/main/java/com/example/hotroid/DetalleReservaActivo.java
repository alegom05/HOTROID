package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

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
import java.util.Locale;
import java.util.Map;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.Timestamp;

public class DetalleReservaActivo extends AppCompatActivity {

    private TextView tvHotelName, tvHotelLocation, tvRoomDetails;
    private TextView tvStatus, tvArrivalDay, tvGuestsInfo;
    private TextView tvCheckIn, tvCheckOut, tvReservationCode;
    private Button btnCheckIn, btnCancelReservation, btnCheckOut, btnSolicitarTaxi;
    private Bitmap qrCodeBitmap;
    private boolean checkInRealizado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_detalle_reserva_activo);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        // Inicializar vistas
        tvHotelName = findViewById(R.id.tvHotelNameDetail);
        tvHotelLocation = findViewById(R.id.tvHotelLocation);
        tvRoomDetails = findViewById(R.id.tvRoomDetailsDetail);
        tvGuestsInfo = findViewById(R.id.tvGuestsInfo);
        tvStatus = findViewById(R.id.tvStatusDetail);
        tvCheckIn = findViewById(R.id.tvCheckInDate);
        tvCheckOut = findViewById(R.id.tvCheckOutDate);
        tvArrivalDay = findViewById(R.id.tvArrivalDay);
        tvReservationCode = findViewById(R.id.tvReservationCode);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCancelReservation = findViewById(R.id.btnCancelReservation);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        try {
            // Obtener datos del intent
            Intent intent = getIntent();
            String hotelName = intent.getStringExtra("hotel_name");
            String roomDetails = intent.getStringExtra("room_details");
            String hotelciudad = intent.getStringExtra("city");
            String hotelLocation = intent.getStringExtra("hotel_location");
            String status = intent.getStringExtra("status");
            String checkInDate = intent.getStringExtra("checkInDate");
            String checkOutDate = intent.getStringExtra("checkOutDate");
            String reservationCode = intent.getStringExtra("reservationCode");
            String guestsInfo = intent.getStringExtra("guestsInfo"); // Nuevo extra para información de huéspedes

            // Configurar datos
            tvHotelName.setText((hotelName != null ? hotelName : "Hotel Desconocido") +
                    (hotelciudad != null ? hotelciudad : ""));
            tvHotelLocation.setText(hotelLocation != null ? hotelLocation : "");
            tvRoomDetails.setText(roomDetails != null ? roomDetails : "Detalles no disponibles");

            // Usar el parámetro guestsInfo si está disponible, o usar una opción alternativa
            if (guestsInfo != null) {
                tvGuestsInfo.setText(guestsInfo);
            } else {
                // Intentar extraer información de huéspedes de roomDetails como fallback
                try {
                    if (roomDetails != null && roomDetails.contains(", ") && roomDetails.split(", ").length >= 3) {
                        tvGuestsInfo.setText(roomDetails.split(", ")[1] + ", " + roomDetails.split(", ")[2]);
                    } else {
                        tvGuestsInfo.setText("Información no disponible");
                    }
                } catch (Exception e) {
                    tvGuestsInfo.setText("Información no disponible");
                }
            }

            tvStatus.setText("Estado: " + (status != null ? status : "Confirmado"));

            // Fechas de check-in y check-out
            if (checkInDate != null && !checkInDate.startsWith("Check-in:")) {
                tvCheckIn.setText("Check-in: " + checkInDate);
            } else {
                tvCheckIn.setText(checkInDate != null ? checkInDate : "Check-in: -");
            }

            if (checkOutDate != null && !checkOutDate.startsWith("Check-out:")) {
                tvCheckOut.setText("Check-out: " + checkOutDate);
            } else {
                tvCheckOut.setText(checkOutDate != null ? checkOutDate : "Check-out: -");
            }

            // Intentar calcular el día de la semana
            String dayOfWeek = "Desconocido";
            try {
                // Extraer la fecha sin el prefijo "Check-in: " si existe
                String dateStr = checkInDate;
                if (dateStr != null && dateStr.startsWith("Check-in: ")) {
                    dateStr = dateStr.substring("Check-in: ".length());
                }
                dayOfWeek = getDayOfWeek(dateStr);
            } catch (Exception e) {
                // En caso de error, usar valor por defecto
            }

            tvArrivalDay.setText("Día de llegada: " + dayOfWeek);
            tvReservationCode.setText(reservationCode != null ? reservationCode : "Código de reserva: -");

        } catch (Exception e) {
            // Manejar cualquier excepción
            Toast.makeText(this, "Error al cargar los datos de la reserva", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Configurar botones
        setupButtons();
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
                        if (apellidosCliente == null) apellidosCliente = "HS";

                        // Crear el documento para alertas_taxi
                        Map<String, Object> alertaTaxi = new HashMap<>();
                        alertaTaxi.put("apellidosCliente", apellidosCliente);
                        alertaTaxi.put("destino", "Aeropuerto de Chinchero (CUZ)"); // Puedes hacer esto dinámico si es necesario
                        alertaTaxi.put("documentId", null);
                        alertaTaxi.put("estadoViaje", "No asignado");
                        alertaTaxi.put("nombresCliente", nombresCliente);
                        alertaTaxi.put("origen", "Libertador"); // Puedes hacer esto dinámico basado en el hotel
                        alertaTaxi.put("region", "Cusco"); // Puedes hacer esto dinámico basado en la ubicación
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
            showCancelReservationDialog();
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
                "\",\"hotelName\":\"Hotel Los Andes\",\"checkInDate\":\"22/04/2025\"}";
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
                    "Código QR de reserva Hotel Los Andes"
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
            Toast.makeText(DetalleReservaActivo.this,
                    "Reserva cancelada con éxito", Toast.LENGTH_LONG).show();
            finish(); // Cerrar la actividad después de cancelar
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        builder.show();
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