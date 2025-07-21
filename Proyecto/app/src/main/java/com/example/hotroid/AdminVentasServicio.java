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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminVentasServicio extends AppCompatActivity {

    private static final String TAG = "AdminVentasServicio";
    private static final int STORAGE_PERMISSION_CODE = 1;

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private VentaServicioAdapter adapter;
    private List<VentaServicioConsolidado> ventasConsolidadasOriginal;
    private TextView tvMesSeleccionado;
    private ImageView ivMonthPicker;
    private EditText etBuscadorServicio;
    private Button btnLimpiarBuscador;

    private Calendar selectedCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_ventas_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerVentasServicio);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasConsolidadasOriginal = new ArrayList<>();
        adapter = new VentaServicioAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        tvMesSeleccionado = findViewById(R.id.tvMesSeleccionado);
        ivMonthPicker = findViewById(R.id.ivMonthPicker);
        etBuscadorServicio = findViewById(R.id.etBuscadorServicio);
        btnLimpiarBuscador = findViewById(R.id.btnLimpiar);

        selectedCalendar = Calendar.getInstance();
        updateMonthDisplay();

        ivMonthPicker.setOnClickListener(v -> showMonthYearPicker());

        etBuscadorServicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                btnLimpiarBuscador.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnLimpiarBuscador.setOnClickListener(v -> {
            etBuscadorServicio.setText("");
        });

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }

        // addSampleServicesToFirestore(); // Descomenta si necesitas añadir servicios primero
        // addSampleVentasServicioData(); // Descomenta si necesitas añadir ventas de prueba

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
                return true;
            }
            return false;
        });
    }

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
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, 1); // Set to first day to ensure month selection

                    updateMonthDisplay();
                    loadVentasServicioData();
                }, year, month, 1);

        // Hide the day picker as we only care about month and year
        picker.getDatePicker().findViewById(getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        picker.show();
    }


    private void loadVentasServicioData() {
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

        db.collection("servicios")
                .get()
                .addOnCompleteListener(serviciosTask -> {
                    if (serviciosTask.isSuccessful()) {
                        Map<String, String> servicioNombres = new HashMap<>();

                        for (QueryDocumentSnapshot servicioDoc : serviciosTask.getResult()) {
                            Servicios servicio = servicioDoc.toObject(Servicios.class);
                            servicio.setDocumentId(servicioDoc.getId());
                            servicioNombres.put(servicioDoc.getId(), servicio.getNombre());

                            Log.d(TAG, "Servicio cargado - ID: " + servicioDoc.getId() +
                                    ", Nombre: " + servicio.getNombre());
                        }

                        db.collection("ventas_servicios")
                                .whereGreaterThanOrEqualTo("fechaVenta", startDate)
                                .whereLessThanOrEqualTo("fechaVenta", endDate)
                                .get()
                                .addOnCompleteListener(ventasTask -> {
                                    if (ventasTask.isSuccessful()) {
                                        Map<String, VentaServicioConsolidado> consolidadoMap = new HashMap<>();

                                        for (QueryDocumentSnapshot ventaDoc : ventasTask.getResult()) {
                                            Log.d(TAG, "Venta encontrada - ID Servicio: " +
                                                    ventaDoc.getString("idServicio") +
                                                    ", Fecha: " + ventaDoc.getDate("fechaVenta") +
                                                    ", Cantidad: " + ventaDoc.getLong("cantidad"));

                                            String idServicio = ventaDoc.getString("idServicio");
                                            String nombreServicio = servicioNombres.getOrDefault(idServicio, "Servicio Desconocido");
                                            long cantidad = ventaDoc.getLong("cantidad");
                                            // Asegurarse de que totalVenta sea un Double, si puede ser Long en Firestore, cástelo.
                                            Double totalVenta = ventaDoc.getDouble("totalVenta");
                                            if (totalVenta == null) {
                                                totalVenta = 0.0;
                                            }


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

                                        ventasConsolidadasOriginal.clear();
                                        ventasConsolidadasOriginal.addAll(consolidadoMap.values());

                                        Collections.sort(ventasConsolidadasOriginal, (v1, v2) ->
                                                Double.compare(v1.getMontoTotal(), v2.getMontoTotal()));

                                        adapter.setListaVentas(new ArrayList<>(ventasConsolidadasOriginal));
                                        adapter.getFilter().filter(etBuscadorServicio.getText().toString());

                                        Log.d(TAG, "Datos consolidados. Total items: " + ventasConsolidadasOriginal.size());
                                    } else {
                                        Log.e(TAG, "Error al cargar ventas", ventasTask.getException());
                                        Toast.makeText(AdminVentasServicio.this, "Error al cargar ventas", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Log.e(TAG, "Error al cargar servicios", serviciosTask.getException());
                        Toast.makeText(AdminVentasServicio.this, "Error al cargar servicios", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generatePdfReport() {
        if (ventasConsolidadasOriginal.isEmpty()) {
            Toast.makeText(this, "No hay datos de ventas para el mes seleccionado para generar el PDF.", Toast.LENGTH_SHORT).show();
            return;
        }

        OutputStream outputStream = null;
        File pdfFile = null;
        Uri pdfUri = null;

        try {
            SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMM_MMMM", new Locale("es", "ES"));
            String fileName = "Reporte_Ventas_Servicios_" + sdfFileName.format(selectedCalendar.getTime()) + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (pdfUri == null) throw new IOException("Failed to create MediaStore entry.");
                outputStream = resolver.openOutputStream(pdfUri);

            } else {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permiso de almacenamiento denegado.", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            STORAGE_PERMISSION_CODE);
                    return;
                }

                File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!pdfDir.exists()) pdfDir.mkdirs();
                pdfFile = new File(pdfDir, fileName);
                outputStream = new FileOutputStream(pdfFile);
                pdfUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".provider",
                        pdfFile
                );
            }

            if (outputStream == null) throw new IOException("Output stream is null");

            Document document = new Document();
            PdfWriter.getInstance(document, outputStream); // ✅ corregido
            document.open();

            // Fuentes
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font cellBoldFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 13);
            DecimalFormat df = new DecimalFormat("0.00");

            // Agregar logo alineado a la derecha
            try {
                Drawable d = ContextCompat.getDrawable(this, R.drawable.logo_app);
                Bitmap originalBitmap = ((BitmapDrawable) d).getBitmap();

                // Convertir el bitmap en redondo
                int size = Math.min(originalBitmap.getWidth(), originalBitmap.getHeight());
                Bitmap roundedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(roundedBitmap);
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                RectF rect = new RectF(0f, 0f, size, size);
                canvas.drawOval(rect, paint);

                // Usar DST_IN para recortar en forma de círculo
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(originalBitmap, new Rect(0, 0, size, size), rect, paint);

                // Convertir a Image de iText
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                roundedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image logo = Image.getInstance(stream.toByteArray());


                logo.scaleAbsolute(50f, 50f);
                logo.setAbsolutePosition(document.right() - 50, document.top() - 50);
                document.add(logo);
            } catch (Exception e) {
                Log.e("PDF", "No se pudo cargar el logo: " + e.getMessage());
            }

            // Hotel
            Paragraph hotelName = new Paragraph("Hotel Libertador", titleFont);
            hotelName.setAlignment(Element.ALIGN_LEFT);
            hotelName.setSpacingAfter(5f);
            document.add(hotelName);

            // Título
            Paragraph title = new Paragraph("Reporte de Venta de Servicios", subtitleFont);
            title.setAlignment(Element.ALIGN_LEFT);
            title.setSpacingAfter(2f);
            document.add(title);

            // Subtítulo
            SimpleDateFormat sdfMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
            String mesTexto = sdfMes.format(selectedCalendar.getTime());
            Paragraph month = new Paragraph("Mes: " + mesTexto, cellBoldFont);
            month.setAlignment(Element.ALIGN_LEFT);
            month.setSpacingAfter(10f);
            document.add(month);

            // Tabla
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3f, 1f, 2f});

            PdfPCell cell1 = new PdfPCell(new Phrase("Servicio", cellBoldFont));
            PdfPCell cell2 = new PdfPCell(new Phrase("Cantidad", cellBoldFont));
            PdfPCell cell3 = new PdfPCell(new Phrase("Monto Total (S/.)", cellBoldFont));
            for (PdfPCell cell : new PdfPCell[]{cell1, cell2, cell3}) {
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPaddingTop(6f);
                cell.setPaddingBottom(6f);
                table.addCell(cell);
            }

            // Datos
            int totalCantidad = 0;
            double totalMonto = 0.0;

            for (VentaServicioConsolidado venta : ventasConsolidadasOriginal) {
                String nombre = venta.getNombreServicio();
                int cantidad = (int) venta.getCantidadTotal();
                double monto = venta.getMontoTotal();

                totalCantidad += cantidad;
                totalMonto += monto;

                PdfPCell c1 = new PdfPCell(new Phrase(nombre, cellFont));
                PdfPCell c2 = new PdfPCell(new Phrase(String.valueOf(cantidad), cellFont));
                PdfPCell c3 = new PdfPCell(new Phrase(df.format(monto), cellFont));

                c1.setVerticalAlignment(Element.ALIGN_MIDDLE);
                c2.setHorizontalAlignment(Element.ALIGN_CENTER);
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);

                for (PdfPCell c : new PdfPCell[]{c1, c2, c3}) {
                    c.setPaddingTop(6f);
                    c.setPaddingBottom(6f);
                }

                table.addCell(c1);
                table.addCell(c2);
                table.addCell(c3);
            }


            // Totales
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTALES", cellBoldFont));
            totalLabel.setColspan(1);
            totalLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalLabel.setBackgroundColor(new BaseColor(220, 220, 250));
            totalLabel.setPaddingTop(6f);
            totalLabel.setPaddingBottom(6f);

            PdfPCell totalCant = new PdfPCell(new Phrase(String.valueOf(totalCantidad), cellBoldFont));
            totalCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalCant.setBackgroundColor(new BaseColor(220, 220, 250));
            totalCant.setPaddingTop(6f);
            totalCant.setPaddingBottom(6f);

            PdfPCell totalMontoCell = new PdfPCell(new Phrase(df.format(totalMonto), cellBoldFont));
            totalMontoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalMontoCell.setBackgroundColor(new BaseColor(220, 220, 250));
            totalMontoCell.setPaddingTop(6f);
            totalMontoCell.setPaddingBottom(6f);

            table.addCell(totalLabel);
            table.addCell(totalCant);
            table.addCell(totalMontoCell);

            document.add(table);
            document.close();
            outputStream.close();

            if (pdfUri != null) {
                abrirPdf(pdfUri);
                mostrarNotificacion(pdfUri, fileName);
                Toast.makeText(this, "PDF generado con éxito en la carpeta de Descargas.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "No se pudo obtener la URI del PDF.", Toast.LENGTH_LONG).show();
            }

        } catch (DocumentException | IOException e) {
            Log.e(TAG, "Error generando PDF", e);
            Toast.makeText(this, "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



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
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("PDF de Reporte Generado")
                .setContentText("Haz clic para abrir el reporte de ventas de servicios: " + fileName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso de notificaciones denegado. No se puede mostrar la notificación.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        notificationManagerCompat.notify(100, builder.build());
    }

    // *** INICIO DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    private void addSampleServicesToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Servicios> sampleServices = new ArrayList<>();
        // Updated constructors to use horaInicio and horaFin
        sampleServices.add(new Servicios("Servicio de Limpieza General", "Limpieza completa de interiores.", 50.0, "9:00 AM", "5:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Jardinería", "Mantenimiento y cuidado de jardines.", 75.0, "8:00 AM", "4:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Servicio de Electricidad", "Reparaciones e instalaciones eléctricas.", 120.0, "12:00 AM", "11:59 PM", new ArrayList<>())); // Assuming 24 Horas means whole day
        sampleServices.add(new Servicios("Servicio de Fontanería", "Reparación de tuberías y grifos.", 90.0, "12:00 AM", "11:59 PM", new ArrayList<>())); // Assuming 24 Horas means whole day
        sampleServices.add(new Servicios("Servicio de Pintura", "Pintura de paredes y techos.", 200.0, "9:00 AM", "6:00 PM", new ArrayList<>()));
        sampleServices.add(new Servicios("Sala de Karaoke", "Disfruta de una noche de diversión con amigos y familia.", 50.5, "11:00 PM", "2:00 AM", new ArrayList<>()));

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

    private void addSampleVentasServicioData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // OBTEN LOS ID REALES DE TUS SERVICIOS DE FIRESTORE
        // Y LOS ID REALES DE TUS CLIENTES DE FIRESTORE.
        // Basado en image_acbba9.png y image_3bbcca.png:
        // idServicio de venta_servicio: "TAiV2R8xqogP8WF0KUN8"
        // idCliente de venta_servicio: "VswY6QDHjyCuEITLIDv"
        // idCliente del documento "6wRPhUThELOmMML7L3p4" es "VswY6QDHjyCuEITLIDv"
        // Para "servicios" necesitarías ver los IDs generados cuando los añades o si ya los tienes.
        // Voy a usar los que parecen ser IDs válidos de tu Firestore para el ejemplo.

        // YOU MUST REPLACE THESE WITH ACTUAL SERVICE IDs FROM YOUR FIRESTORE DATABASE
        // AFTER RUNNING addSampleServicesToFirestore() and checking your console.
        String idServicioEjemplo1 = "ID_SERVICIO_LIMPIEZA"; // Replace with actual ID for "Servicio de Limpieza General"
        String idServicioEjemplo2 = "ID_SERVICIO_JARDINERIA"; // Replace with actual ID for "Servicio de Jardinería"
        String idServicioEjemplo3 = "ID_SERVICIO_ELECTRICIDAD"; // Replace with actual ID for "Servicio de Electricidad"
        String idServicioEjemplo4 = "ID_SERVICIO_FONTANERIA"; // Replace with actual ID for "Servicio de Fontanería"
        String idServicioEjemplo5 = "ID_SERVICIO_PINTURA"; // Replace with actual ID for "Servicio de Pintura"
        String idServicioEjemplo6 = "ID_SERVICIO_KARAOKE"; // Replace with actual ID for "Sala de Karaoke"

        String idClienteEjemplo = "VswY6QDHjyCuEITLIDv"; // This seems to be a real client ID from your example

        List<VentaServicio> sampleVentas = new ArrayList<>();

        Calendar cal = Calendar.getInstance();

        // Ventas para el mes actual (Ajusta el mes y año si es necesario)
        // Por ejemplo, para crear ventas en Mayo de 2025:
        // cal.set(Calendar.MONTH, Calendar.MAY); // Mes de mayo (0-11)
        // cal.set(Calendar.YEAR, 2025); // Año 2025

        cal.set(Calendar.DAY_OF_MONTH, 10);
        // Constructor: VentaServicio(String idServicio, String idCliente, int cantidad, double precioUnitario, double totalVenta, Date fechaVenta)
        sampleVentas.add(new VentaServicio(idServicioEjemplo1, idClienteEjemplo, 2, 50.0, 100.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 15);
        sampleVentas.add(new VentaServicio(idServicioEjemplo2, idClienteEjemplo, 1, 75.0, 75.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 20);
        sampleVentas.add(new VentaServicio(idServicioEjemplo3, idClienteEjemplo, 1, 120.0, 120.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 22);
        sampleVentas.add(new VentaServicio(idServicioEjemplo1, idClienteEjemplo, 1, 50.0, 50.0, cal.getTime()));

        cal.set(Calendar.DAY_OF_MONTH, 25);
        sampleVentas.add(new VentaServicio(idServicioEjemplo6, idClienteEjemplo, 3, 50.5, 151.5, cal.getTime()));

        // Ventas para otro mes (si quieres probar el selector de mes)
        Calendar calPreviousMonth = Calendar.getInstance();
        calPreviousMonth.add(Calendar.MONTH, -1); // Un mes antes
        calPreviousMonth.set(Calendar.DAY_OF_MONTH, 5);
        sampleVentas.add(new VentaServicio(idServicioEjemplo4, idClienteEjemplo, 1, 90.0, 90.0, calPreviousMonth.getTime()));

        calPreviousMonth.set(Calendar.DAY_OF_MONTH, 12);
        sampleVentas.add(new VentaServicio(idServicioEjemplo5, idClienteEjemplo, 1, 200.0, 200.0, calPreviousMonth.getTime()));


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
    // *** FIN DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
}