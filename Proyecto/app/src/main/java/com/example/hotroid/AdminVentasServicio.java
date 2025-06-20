package com.example.hotroid;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore; // Importar MediaStore
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
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
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
import java.io.OutputStream; // Importar OutputStream
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
    private static final int STORAGE_PERMISSION_CODE = 1; // Código para solicitar permiso de almacenamiento

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

        // --- INICIO: SOLICITUD DE PERMISO DE ALMACENAMIENTO ---
        // Se requiere solo para Android 9 (API 28) y versiones anteriores para WRITE_EXTERNAL_STORAGE
        // Para Android 10 (API 29) y superiores, se usa MediaStore y no requiere este permiso explícito.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) { // P es API 28
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
        // --- FIN: SOLICITUD DE PERMISO DE ALMACENAMIENTO ---

        // *** TEMPORARY: Call this method to add sample data ***
        // Descomenta la siguiente línea solo para añadir los datos de prueba
        // Luego, vuelve a comentarla o bórrala después de la primera ejecución.
        //addSampleServicesToFirestore(); // Descomenta si necesitas añadir servicios primero
        //addSampleVentasServicioData(); // Descomenta si necesitas añadir ventas de prueba

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
                // Ya estamos en AdminVentasServicio, no navegar
                return true;
            }
            return false;
        });
    }

    // Maneja el resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de almacenamiento concedido.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado. No se podrá generar PDF.", Toast.LENGTH_LONG).show();
            }
        }
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
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, 1); // Fija el día 1 para asegurar el mes

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
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Date startDate = startOfMonth.getTime();

        Calendar endOfMonth = (Calendar) selectedCalendar.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);
        endOfMonth.set(Calendar.MILLISECOND, 999);
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

                                        // Ordenar por monto total (de menor a mayor)
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

        OutputStream outputStream = null;
        File pdfFile = null;
        Uri pdfUri = null;

        try {
            // Nombre del archivo PDF incluirá el mes y año
            SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMM_MMMM", new Locale("es", "ES"));
            String fileName = "Reporte_Ventas_Servicios_" + sdfFileName.format(selectedCalendar.getTime()) + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10 (API 29) y superior
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (pdfUri == null) {
                    throw new IOException("Failed to create new MediaStore entry.");
                }
                outputStream = resolver.openOutputStream(pdfUri);

            } else { // Android 9 (API 28) y anteriores
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de almacenamiento denegado. No se puede generar PDF.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
                    return;
                }

                File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!pdfDir.exists()) {
                    pdfDir.mkdirs();
                }
                pdfFile = new File(pdfDir, fileName);
                outputStream = new FileOutputStream(pdfFile);
                pdfUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        pdfFile
                );
            }

            if (outputStream == null) {
                throw new IOException("Output stream is null. Cannot write PDF.");
            }

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
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
            outputStream.close(); // Cierra el OutputStream

            if (pdfUri != null) {
                abrirPdf(pdfUri);
                mostrarNotificacion(pdfUri, fileName); // Pasa el URI y el nombre del archivo
                Toast.makeText(this, "PDF generado con éxito en la carpeta de Descargas.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No se pudo obtener la URI del PDF.", Toast.LENGTH_LONG).show();
            }


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

    // Modificado para aceptar Uri directamente
    private void abrirPdf(Uri uri) {
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

    // Modificado para aceptar Uri directamente
    private void mostrarNotificacion(Uri uri, String fileName) {
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
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de que este icono exista
                .setContentTitle("PDF de Reporte Generado")
                .setContentText("Haz clic para abrir el reporte de ventas de servicios: " + fileName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Si el permiso de notificaciones no está concedido (Android 13+), no se puede mostrar la notificación.
            // Para solicitar este permiso en tiempo de ejecución, necesitarías un manejo similar al de almacenamiento.
            Toast.makeText(this, "Permiso de notificaciones denegado. No se puede mostrar la notificación.", Toast.LENGTH_SHORT).show();
            return;
        }
        notificationManagerCompat.notify(100, builder.build());
    }

    // *** INICIO DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    // Puedes comentar o eliminar estos métodos después de usarlos.
    private void addSampleServicesToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Servicios> sampleServices = new ArrayList<>();
        sampleServices.add(new Servicios("Servicio de Limpieza General", "Limpieza completa de interiores.", 50.0, "9:00 AM - 5:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Jardinería", "Mantenimiento y cuidado de jardines.", 75.0, "8:00 AM - 4:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Electricidad", "Reparaciones e instalaciones eléctricas.", 120.0, "24 Horas", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Fontanería", "Reparación de tuberías y grifos.", 90.0, "24 Horas", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Pintura", "Pintura de paredes y techos.", 200.0, "9:00 AM - 6:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Sala de Karaoke", "Disfruta de una noche de diversión con amigos y familia.", 50.5, "11:00 PM - 2:00 AM", new ArrayList<>()));

        for (Servicios servicio : sampleServices) {
            db.collection("servicios").add(servicio)
                    .addOnSuccessListener(documentReference -> {
                        String generatedId = documentReference.getId();
                        Log.d(TAG, "Servicio añadido con ID: " + generatedId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir servicio: " + servicio.getNombre(), e);
                    });
        }
        Toast.makeText(this, "Añadiendo servicios de ejemplo...", Toast.LENGTH_SHORT).show();
    }

    // Ejemplo para añadir datos de ventas (requiere IDs de servicio existentes)
    /*private void addSampleVentasServicioData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // ESTOS IDS SON DE EJEMPLO. DEBES OBTENER LOS ID REALES DE TUS SERVICIOS DE FIRESTORE
        // Para obtener los IDs reales, ejecuta la aplicación, mira el Logcat cuando se añaden los servicios
        // con addSampleServicesToFirestore(), o consulta tu base de datos de Firebase.
        String idServicioLimpieza = "reemplazar_con_ID_real_de_limpieza"; // Reemplazar con ID real
        String idServicioJardineria = "reemplazar_con_ID_real_de_jardineria"; // Reemplazar con ID real
        String idServicioElectricidad = "reemplazar_con_ID_real_de_electricidad"; // Reemplazar con ID real

        List<VentaServicio> sampleVentas = new ArrayList<>();

        // Ventas para el mes actual
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 10); // Día 10 del mes actual (Junio 2025)
        sampleVentas.add(new VentaServicio(idServicioLimpieza, 2, 100.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 15); // Día 15 del mes actual (Junio 2025)
        sampleVentas.add(new VentaServicio(idServicioJardineria, 1, 75.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 20); // Día 20 del mes actual (Junio 2025)
        sampleVentas.add(new VentaServicio(idServicioElectricidad, 1, 120.0, cal.getTime()));

        // Ejemplo de ventas para otro mes (si quieres probar el selector de mes)
        Calendar calPreviousMonth = Calendar.getInstance();
        calPreviousMonth.add(Calendar.MONTH, -1); // Un mes antes
        calPreviousMonth.set(Calendar.DAY_OF_MONTH, 5);
        sampleVentas.add(new VentaServicio(idServicioLimpieza, 1, 50.0, calPreviousMonth.getTime()));

        for (VentaServicio venta : sampleVentas) {
            db.collection("ventas_servicios").add(venta)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Venta de servicio añadida con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir venta de servicio: ", e);
                    });
        }
        Toast.makeText(this, "Añadiendo ventas de ejemplo...", Toast.LENGTH_SHORT).show();
    }
    // *** FIN DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***/
}