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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Importar EditText
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

import com.example.hotroid.VentaClienteAdapter; // Usar VentaClienteAdapter
import com.example.hotroid.bean.Cliente; // Asegúrate de que esta clase exista y tenga los campos correctos (nombres, apellidos)
import com.example.hotroid.bean.VentaServicio; // Asegúrate de que esta clase exista y tenga los campos correctos (idCliente, totalVenta)
import com.example.hotroid.bean.VentaClienteConsolidado; // Asegúrate de que esta clase exista y tenga los métodos correctos (addMonto, getMontoTotal)
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
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
import com.itextpdf.text.pdf.PdfWriter;
import java.util.List; // Importar java.util.List explícitamente
import com.itextpdf.text.ListItem; // Importar ListItem de iText
// No se necesita importar com.itextpdf.text.List con alias, se usará el nombre completo

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
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminVentasUsuario extends AppCompatActivity {

    private static final String TAG = "AdminVentasUsuario";
    private static final int STORAGE_PERMISSION_CODE = 1;

    private FirebaseFirestore db;
    private CollectionReference ventasServiciosRef;
    private CollectionReference clientesRef;
    private CollectionReference serviciosRef;
    private RecyclerView recyclerView;
    private VentaClienteAdapter adapter;
    private List<VentaClienteConsolidado> ventasConsolidadas;
    private TextView tvMesSeleccionado;
    private ImageView ivMonthPicker;
    private EditText etBuscador; // Declaración del EditText para el buscador
    private Button btnLimpiar; // Declaración del Button para limpiar

    private Calendar selectedCalendar;

    private List<String> allClientIds = new ArrayList<>();
    private List<String> allServiceIds = new ArrayList<>();
    private Map<String, String> clientNamesById = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_ventas_usuario);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        ventasServiciosRef = db.collection("ventas_servicios");
        clientesRef = db.collection("clientes");
        serviciosRef = db.collection("servicios");

        recyclerView = findViewById(R.id.recyclerVentasUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasConsolidadas = new ArrayList<>();
        adapter = new VentaClienteAdapter(ventasConsolidadas); // Usar el nuevo adaptador
        recyclerView.setAdapter(adapter);

        tvMesSeleccionado = findViewById(R.id.tvMesSeleccionado);
        ivMonthPicker = findViewById(R.id.ivMonthPicker);
        etBuscador = findViewById(R.id.etBuscador); // Vinculamos el EditText
        btnLimpiar = findViewById(R.id.btnLimpiar); // Vinculamos el Button

        selectedCalendar = Calendar.getInstance();
        updateMonthDisplay();

        ivMonthPicker.setOnClickListener(v -> showMonthYearPicker());

        // Lógica del buscador
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
                btnLimpiar.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText(""); // Limpia el texto del buscador
            // El TextWatcher se encargará de volver a filtrar y ocultar el botón
        });


        // --- INICIO: SOLICITUD DE PERMISO DE ALMACENAMIENTO ---
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
        // --- FIN: SOLICITUD DE PERMISO DE ALMACENAMIENTO ---

        loadFirebaseDataAndThenProceed();

        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVentasUsuario.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        Button btnGenerarPdf = findViewById(R.id.btnGenerarPdf);
        btnGenerarPdf.setOnClickListener(v -> generatePdfReport());


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_reportes);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                startActivity(new Intent(AdminVentasUsuario.this, AdminActivity.class));
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                startActivity(new Intent(AdminVentasUsuario.this, AdminTaxistas.class));
                return true;
            } else if (itemId == R.id.nav_checkout) {
                startActivity(new Intent(AdminVentasUsuario.this, AdminCheckout.class));
                return true;
            } else if (itemId == R.id.nav_reportes) {
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

    private void loadFirebaseDataAndThenProceed() {
        // Carga clientes y servicios en paralelo
        Task<QuerySnapshot> loadClientsTask = clientesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allClientIds.clear();
                clientNamesById.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String clientId = document.getId();
                    allClientIds.add(clientId);
                    String nombres = document.getString("nombres");
                    String apellidos = document.getString("apellidos");
                    if (nombres != null && apellidos != null) {
                        clientNamesById.put(clientId, nombres + " " + apellidos);
                    } else {
                        clientNamesById.put(clientId, "Cliente ID: " + clientId);
                    }
                }
                Log.d(TAG, "Clientes cargados: " + allClientIds.size());
            } else {
                Log.e(TAG, "Error cargando clientes: ", task.getException());
                Toast.makeText(this, "Error al cargar datos de clientes.", Toast.LENGTH_SHORT).show();
            }
        });

        Task<QuerySnapshot> loadServicesTask = serviciosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allServiceIds.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String serviceId = document.getId();
                    allServiceIds.add(serviceId);
                }
                Log.d(TAG, "Servicios cargados: " + allServiceIds.size());
            } else {
                Log.e(TAG, "Error cargando servicios: ", task.getException());
                Toast.makeText(this, "Error al cargar datos de servicios.", Toast.LENGTH_SHORT).show();
            }
        });

        // Espera a que ambas tareas de carga de IDs se completen
        Tasks.whenAll(loadClientsTask, loadServicesTask)
                .addOnCompleteListener(allTasks -> {
                    if (allTasks.isSuccessful()) {
                        Log.d(TAG, "Todos los IDs de Firebase (clientes y servicios) cargados con éxito.");
                        // *** DESCOMENTA LA SIGUIENTE LÍNEA SOLO UNA VEZ PARA AÑADIR DATOS DE PRUEBA ***
                        // addSampleVentasServicioData(); // Llamar solo para poblar datos de prueba
                        loadVentasUsuarioData(); // Cargar los datos de ventas una vez que los IDs estén disponibles
                    } else {
                        Log.e(TAG, "Falló la carga de algunos IDs de Firebase. La aplicación podría no funcionar correctamente.", allTasks.getException());
                        Toast.makeText(this, "No se pudieron cargar todos los datos iniciales.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        tvMesSeleccionado.setText(sdf.format(selectedCalendar.getTime()));
    }

    private void showMonthYearPicker() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);

        DatePickerDialog picker = new DatePickerDialog(AdminVentasUsuario.this,
                AlertDialog.THEME_HOLO_LIGHT,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, selectedYear);
                    selectedCalendar.set(Calendar.MONTH, selectedMonth);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, 1); // Establecer al primer día del mes

                    updateMonthDisplay();
                    loadVentasUsuarioData();
                }, year, month, 1);

        // Ocultar el selector de día
        if (picker.getDatePicker() != null) {
            int dayId = getResources().getIdentifier("day", "id", "android");
            if (dayId != 0) {
                View daySpinner = picker.getDatePicker().findViewById(dayId);
                if (daySpinner != null) {
                    daySpinner.setVisibility(View.GONE);
                }
            }
        }
        picker.show();
    }


    private void loadVentasUsuarioData() {
        Calendar startCalendar = (Calendar) selectedCalendar.clone();
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        Date startDate = startCalendar.getTime();

        Calendar endCalendar = (Calendar) selectedCalendar.clone();
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 999);
        Date endDate = endCalendar.getTime();

        Log.d(TAG, "Cargando datos para el rango: " + startDate + " a " + endDate);

        ventasServiciosRef.whereGreaterThanOrEqualTo("fechaVenta", startDate)
                .whereLessThanOrEqualTo("fechaVenta", endDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, VentaClienteConsolidado> ventasConsolidadoMap = new HashMap<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                VentaServicio venta = document.toObject(VentaServicio.class);
                                String clientId = venta.getIdCliente();
                                double totalVenta = venta.getTotalVenta();

                                VentaClienteConsolidado consolidatedVenta = ventasConsolidadoMap.get(clientId);
                                if (consolidatedVenta == null) {
                                    String nombreCompleto = clientNamesById.getOrDefault(clientId, "Cliente Desconocido (ID: " + clientId + ")");
                                    consolidatedVenta = new VentaClienteConsolidado(clientId, nombreCompleto, 0.0);
                                    ventasConsolidadoMap.put(clientId, consolidatedVenta);
                                }
                                consolidatedVenta.addMonto(totalVenta);

                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear documento o procesar venta: " + document.getId(), e);
                            }
                        }

                        Log.d(TAG, "Nombres de clientes ya disponibles. Actualizando UI con ventas consolidadas.");
                        ventasConsolidadas.clear();
                        ventasConsolidadas.addAll(ventasConsolidadoMap.values());
                        sortAndDisplayVentas();

                    } else {
                        Log.e(TAG, "Error obteniendo documentos de ventas: ", task.getException());
                        Toast.makeText(AdminVentasUsuario.this, "Error cargando ventas: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        ventasConsolidadas.clear();
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void sortAndDisplayVentas() {
        Collections.sort(ventasConsolidadas, (v1, v2) -> Double.compare(v2.getMontoTotal(), v1.getMontoTotal()));
        adapter.setListaVentasCliente(new ArrayList<>(ventasConsolidadas)); // Usar setListaVentasCliente del adaptador
        Log.d(TAG, "Ventas consolidadas y actualizadas en UI. Cantidad: " + ventasConsolidadas.size());
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
            SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMM_MMMM yyyy", new Locale("es", "ES"));
            String fileName = "Reporte_Ventas_Usuarios_" + sdfFileName.format(selectedCalendar.getTime()) + ".pdf";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

            } else {
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
            Font itemHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font itemFont = new Font(Font.FontFamily.HELVETICA, 12);

            // Título principal
            Paragraph title = new Paragraph("Reporte de Venta por Usuarios", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            // Subtítulo con el mes del reporte
            SimpleDateFormat sdfReportMonth = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
            Paragraph subtitle = new Paragraph("Mes: " + sdfReportMonth.format(selectedCalendar.getTime()), subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(20f);
            document.add(subtitle);

            // Generar PDF como una lista de ítems (como cards)
            DecimalFormat df = new DecimalFormat("0.00");

            // Usamos el nombre completo para evitar conflictos: com.itextpdf.text.List
            com.itextpdf.text.List pdfList = new com.itextpdf.text.List(false, 15);

            for (VentaClienteConsolidado venta : ventasConsolidadas) {
                // Cada usuario es un ListItem
                ListItem userItem = new ListItem();
                userItem.add(new Paragraph("Cliente: " + venta.getNombreCompletoCliente(), itemHeaderFont));
                userItem.add(new Paragraph("  Servicios Comprados: " + venta.getCantidadTotalServicios(), itemFont));
                userItem.add(new Paragraph("  Monto Total Gastado (S/.): " + df.format(venta.getMontoTotal()), itemFont));

                pdfList.add(userItem);
            }
            document.add(pdfList);


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

    private PdfPCell createCenteredCell(String content, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(content, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
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
        String CHANNEL_ID = "pdf_user_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificación PDF de Usuarios";
            String description = "Canal para notificaciones de PDFs de ventas por usuario";
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
                .setContentTitle("PDF de Reporte de Ventas por Usuario Generado")
                .setContentText("Haz clic para abrir el reporte de ventas por usuario: " + fileName)
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
        notificationManagerCompat.notify(101, builder.build());
    }

    private void addSampleVentasServicioData() {
        if (allClientIds.isEmpty() || allServiceIds.isEmpty()) {
            Log.e(TAG, "No se pueden añadir ventas de ejemplo: Faltan IDs de clientes o servicios. " +
                    "Asegúrate de que las colecciones 'clientes' y 'servicios' tengan documentos en Firestore.");
            Toast.makeText(this, "Asegúrate de tener clientes y servicios en Firestore antes de añadir datos de muestra.", Toast.LENGTH_LONG).show();
            return;
        }

        Random random = new Random();
        List<VentaServicio> sampleVentas = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        int numberOfSales = 20;

        for (int i = 0; i < numberOfSales; i++) {
            String randomClientId = allClientIds.get(random.nextInt(allClientIds.size()));
            String randomServiceId = allServiceIds.get(random.nextInt(allServiceIds.size()));

            double precioUnitario = 20.0 + (180.0 * random.nextDouble());
            int cantidad = 1 + random.nextInt(3);
            double totalVenta = precioUnitario * cantidad;

            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -random.nextInt(3));
            cal.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
            cal.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(10));
            cal.set(Calendar.MINUTE, random.nextInt(60));
            cal.set(Calendar.SECOND, random.nextInt(60));

            sampleVentas.add(new VentaServicio(randomServiceId, randomClientId, cantidad, precioUnitario, totalVenta, cal.getTime()));
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        int totalSalesToProcess = sampleVentas.size();

        if (totalSalesToProcess == 0) {
            Toast.makeText(this, "No se generaron ventas de servicio de ejemplo para añadir.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (VentaServicio venta : sampleVentas) {
            db.collection("ventas_servicios").add(venta)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Venta de servicio añadida con ID: " + documentReference.getId());
                        successCount.incrementAndGet();
                        if (successCount.get() + failureCount.get() == totalSalesToProcess) {
                            Toast.makeText(AdminVentasUsuario.this, "Añadidos " + successCount.get() + " ventas de servicio de ejemplo.", Toast.LENGTH_SHORT).show();
                            loadVentasUsuarioData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir venta de servicio: " + e.getMessage(), e);
                        failureCount.incrementAndGet();
                        if (successCount.get() + failureCount.get() == totalSalesToProcess) {
                            Toast.makeText(AdminVentasUsuario.this, "Añadidos " + successCount.get() + " ventas de servicio de ejemplo con " + failureCount.get() + " errores.", Toast.LENGTH_LONG).show();
                            loadVentasUsuarioData();
                        }
                    });
        }
        Toast.makeText(this, "Intentando añadir " + totalSalesToProcess + " ventas de servicio de ejemplo...", Toast.LENGTH_SHORT).show();
    }
}