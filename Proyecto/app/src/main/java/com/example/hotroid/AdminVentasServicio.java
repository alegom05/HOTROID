package com.example.hotroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.VentaServicioAdapter;
import com.example.hotroid.bean.Servicios;
import com.example.hotroid.bean.VentaServicio;
import com.example.hotroid.bean.VentaServicioConsolidado;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminVentasServicio extends AppCompatActivity {

    private static final String TAG = "AdminVentasServicio";

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private VentaServicioAdapter adapter;
    private List<VentaServicioConsolidado> ventasConsolidadas;
    private TextView tvMesSeleccionado;
    private ImageView ivMonthPicker;

    private Calendar selectedCalendar; // Para almacenar el mes y año seleccionados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_ventas_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerVentasServicio);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasConsolidadas = new ArrayList<>();
        adapter = new VentaServicioAdapter(ventasConsolidadas);
        recyclerView.setAdapter(adapter);

        tvMesSeleccionado = findViewById(R.id.tvMesSeleccionado);
        ivMonthPicker = findViewById(R.id.ivMonthPicker);

        // Inicializar con el mes y año actuales
        selectedCalendar = Calendar.getInstance();
        updateMonthDisplay(); // Actualizar el TextView con el mes actual

        // Listener para abrir el DatePicker (para seleccionar mes/año)
        ivMonthPicker.setOnClickListener(v -> showMonthYearPicker());

        // *** TEMPORARY: Call this method to add sample data ***
        // Descomenta la siguiente línea solo para añadir los datos de prueba
        // Luego, vuelve a comentarla o bórrala después de la primera ejecución.
        // addSampleServicesToFirestore(); // Descomenta si necesitas añadir servicios primero
        // addSampleVentasServicioData();


        // Cargar los datos inicialmente para el mes actual
        loadVentasServicioData();

        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVentasServicio.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        Button btnGenerarPdf = findViewById(R.id.btnGenerarPdf);
        btnGenerarPdf.setOnClickListener(v -> generatePdfReport());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_reportes);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                startActivity(new Intent(AdminVentasServicio.this, AdminActivity.class));
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                startActivity(new Intent(AdminVentasServicio.this, AdminTaxistas.class));
                return true;
            } else if (itemId == R.id.nav_checkout) {
                startActivity(new Intent(AdminVentasServicio.this, AdminCheckout.class));
                return true;
            } else if (itemId == R.id.nav_reportes) {
                // Ya estamos en AdminVentasServicio (o AdminReportes si es el caso), no navegar
                return true;
            }
            return false;
        });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        tvMesSeleccionado.setText(sdf.format(selectedCalendar.getTime()));
    }

    private void showMonthYearPicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);

        DatePickerDialog picker = new DatePickerDialog(AdminVentasServicio.this,
                AlertDialog.THEME_HOLO_LIGHT,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, 1);

                    updateMonthDisplay();
                    loadVentasServicioData();
                }, year, month, 1);

        // Intenta ocultar el día del DatePickerDialog
        picker.getDatePicker().findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        picker.show();
    }


    private void loadVentasServicioData() {
        // Configuración de fechas
        Calendar startOfMonth = (Calendar) selectedCalendar.clone();
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        Date startDate = startOfMonth.getTime();

        Calendar endOfMonth = (Calendar) selectedCalendar.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        Date endDate = endOfMonth.getTime();

        // 1. Primero cargamos los servicios para tener los nombres
        db.collection("servicios")
                .get()
                .addOnCompleteListener(serviciosTask -> {
                    if (serviciosTask.isSuccessful()) {
                        Map<String, String> servicioNombres = new HashMap<>();

                        for (QueryDocumentSnapshot servicioDoc : serviciosTask.getResult()) {
                            Servicios servicio = servicioDoc.toObject(Servicios.class);
                            servicio.setDocumentId(servicioDoc.getId()); // Asignamos el ID correctamente
                            servicioNombres.put(servicioDoc.getId(), servicio.getNombre());

                            Log.d(TAG, "Servicio cargado - ID: " + servicioDoc.getId() +
                                    ", Nombre: " + servicio.getNombre());
                        }

                        // 2. Ahora cargamos las ventas filtradas por fecha
                        db.collection("ventas_servicios")
                                .whereGreaterThanOrEqualTo("fechaVenta", startDate)
                                .whereLessThanOrEqualTo("fechaVenta", endDate)
                                .get()
                                .addOnCompleteListener(ventasTask -> {
                                    if (ventasTask.isSuccessful()) {
                                        Map<String, VentaServicioConsolidado> consolidadoMap = new HashMap<>();

                                        for (QueryDocumentSnapshot ventaDoc : ventasTask.getResult()) {
                                            // Debug: Mostrar datos crudos de la venta
                                            Log.d(TAG, "Venta encontrada - ID Servicio: " +
                                                    ventaDoc.getString("idServicio") +
                                                    ", Fecha: " + ventaDoc.getDate("fechaVenta") +
                                                    ", Cantidad: " + ventaDoc.getLong("cantidad"));

                                            String idServicio = ventaDoc.getString("idServicio");
                                            String nombreServicio = servicioNombres.getOrDefault(idServicio, "Servicio Desconocido");
                                            long cantidad = ventaDoc.getLong("cantidad");
                                            double totalVenta = ventaDoc.getDouble("totalVenta");

                                            if (consolidadoMap.containsKey(idServicio)) {
                                                VentaServicioConsolidado existente = consolidadoMap.get(idServicio);
                                                existente.setCantidadTotal(existente.getCantidadTotal() + cantidad);
                                                existente.setMontoTotal(existente.getMontoTotal() + totalVenta);
                                            } else {
                                                consolidadoMap.put(idServicio, new VentaServicioConsolidado(
                                                        nombreServicio,
                                                        cantidad,
                                                        totalVenta
                                                ));
                                            }
                                        }

                                        // Actualizar RecyclerView
                                        ventasConsolidadas.clear();
                                        ventasConsolidadas.addAll(consolidadoMap.values());

                                        // Ordenar por monto total
                                        Collections.sort(ventasConsolidadas, (v1, v2) ->
                                                Double.compare(v1.getMontoTotal(), v2.getMontoTotal()));

                                        adapter.notifyDataSetChanged();

                                        Log.d(TAG, "Datos consolidados. Total items: " + ventasConsolidadas.size());
                                    } else {
                                        Log.e(TAG, "Error al cargar ventas", ventasTask.getException());
                                        Toast.makeText(this, "Error al cargar ventas", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e(TAG, "Error al cargar servicios", serviciosTask.getException());
                        Toast.makeText(this, "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generatePdfReport() {
        if (ventasConsolidadas.isEmpty()) {
            Toast.makeText(this, "No hay datos de ventas para el mes seleccionado para generar el PDF.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Document document = new Document(PageSize.A4);
            File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }

            // Nombre del archivo PDF incluirá el mes y año
            SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMM_MMMM", new Locale("es", "ES"));
            String fileName = "Reporte_Ventas_Servicios_" + sdfFileName.format(selectedCalendar.getTime()) + ".pdf";
            File file = new File(pdfDir, fileName);

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

            // Título principal
            Paragraph title = new Paragraph("Reporte de Venta de Servicios", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Subtítulo con el mes del reporte
            SimpleDateFormat sdfReportMonth = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
            Paragraph subtitle = new Paragraph("Mes: " + sdfReportMonth.format(selectedCalendar.getTime()), subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(20f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 2f, 3f});
            table.setSpacingBefore(10f);

            table.addCell(createCenteredCell("Servicio", headerFont));
            table.addCell(createCenteredCell("Cantidad", headerFont));
            table.addCell(createCenteredCell("Monto Total (S/.)", headerFont));

            DecimalFormat df = new DecimalFormat("0.00");

            for (VentaServicioConsolidado venta : ventasConsolidadas) {
                table.addCell(createCenteredCell(venta.getNombreServicio(), cellFont));
                table.addCell(createCenteredCell(String.valueOf(venta.getCantidadTotal()), cellFont));
                table.addCell(createCenteredCell(df.format(venta.getMontoTotal()), cellFont));
            }

            document.add(table);
            document.close();

            abrirPdf(file);
            mostrarNotificacion(file);

        } catch (DocumentException | IOException e) {
            Log.e(TAG, "Error generando PDF", e);
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private PdfPCell createCenteredCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private void abrirPdf(File file) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se encontró una aplicación para abrir el PDF.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error al abrir PDF", e);
        }
    }

    private void mostrarNotificacion(File file) {
        String CHANNEL_ID = "pdf_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificación PDF";
            String description = "Canal para notificaciones de PDFs generados";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Uri uri = FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("PDF de Reporte Generado")
                .setContentText("Haz clic para abrir el reporte de ventas de servicios.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(100, builder.build());
    }

    // *** INICIO DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    // Puedes comentar o eliminar estos métodos después de usarlos.

    // Método para añadir servicios de ejemplo si tu colección 'servicios' está vacía
    private void addSampleServicesToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Servicios> sampleServices = new ArrayList<>();
        // Asegúrate de que los constructores de Servicios coincidan con esta inicialización
        // Servicios(String nombre, String descripcion, double precio, String horario, ArrayList<String> imagenes)
        sampleServices.add(new Servicios("Servicio de Limpieza General", "Limpieza completa de interiores.", 50.0, "9:00 AM - 5:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Jardinería", "Mantenimiento y cuidado de jardines.", 75.0, "8:00 AM - 4:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Electricidad", "Reparaciones e instalaciones eléctricas.", 120.0, "24 Horas", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Fontanería", "Reparación de tuberías y grifos.", 90.0, "24 Horas", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Pintura", "Pintura de paredes y techos.", 200.0, "9:00 AM - 6:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Sala de Karaoke", "Disfruta de una noche de diversión con amigos y familia.", 50.5, "11:00 PM - 2:00 AM", new ArrayList<>())); // Añadido basado en tu captura

        for (Servicios servicio : sampleServices) {
            db.collection("servicios").add(servicio)
                    .addOnSuccessListener(documentReference -> {
                        String generatedId = documentReference.getId();
                        // Opcional: Si quieres que el documentId también esté dentro del documento de Firestore
                        // documentReference.update("documentId", generatedId)
                        //    .addOnSuccessListener(aVoid -> Log.d(TAG, "Servicio añadido y documentId actualizado: " + generatedId))
                        //    .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar documentId: " + generatedId, e));

                        // Para esta solución (Opción 2), no es estrictamente necesario actualizar el campo 'documentId' en Firestore,
                        // ya que estamos usando document.getId() directamente.
                        Log.d(TAG, "Servicio añadido con ID: " + generatedId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir servicio: " + servicio.getNombre(), e);
                    });
        }
        Toast.makeText(this, "Añadiendo servicios de ejemplo...", Toast.LENGTH_SHORT).show();
    }
    // *** FIN DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
}