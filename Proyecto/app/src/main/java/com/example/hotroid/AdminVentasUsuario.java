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

import com.example.hotroid.VentaClienteAdapter; // Asegúrate que esta ruta es correcta (ej: com.example.hotroid.adapters.VentaClienteAdapter si está en 'adapters')
import com.example.hotroid.bean.Cliente; // Tu clase Cliente
import com.example.hotroid.bean.VentaServicio; // Clase de VentaServicio existente
import com.example.hotroid.bean.VentaClienteConsolidado; // Nueva clase bean
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks; // Importar para Tasks.whenAll
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference; // Importar CollectionReference
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
import java.util.Random; // Para generar números aleatorios
import java.util.concurrent.atomic.AtomicInteger; // Para el contador en la carga de datos

public class AdminVentasUsuario extends AppCompatActivity {

    private static final String TAG = "AdminVentasUsuario";

    private FirebaseFirestore db;
    private CollectionReference ventasServiciosRef;
    private CollectionReference clientesRef;
    private CollectionReference serviciosRef; // Referencia a la colección de servicios
    private RecyclerView recyclerView;
    private VentaClienteAdapter adapter;
    private List<VentaClienteConsolidado> ventasConsolidadas;
    private TextView tvMesSeleccionado;
    private ImageView ivMonthPicker;

    private Calendar selectedCalendar; // Para almacenar el mes y año seleccionados

    // Listas para almacenar todos los IDs de clientes y servicios obtenidos de Firestore para selección aleatoria
    private List<String> allClientIds = new ArrayList<>();
    private List<String> allServiceIds = new ArrayList<>();

    // Mapa para almacenar el nombre completo del cliente por su ID (para la visualización en RecyclerView y PDF)
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
        serviciosRef = db.collection("servicios"); // Inicializado

        recyclerView = findViewById(R.id.recyclerVentasUsuario);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ventasConsolidadas = new ArrayList<>();
        adapter = new VentaClienteAdapter(ventasConsolidadas);
        recyclerView.setAdapter(adapter);

        tvMesSeleccionado = findViewById(R.id.tvMesSeleccionado);
        ivMonthPicker = findViewById(R.id.ivMonthPicker);

        // Inicializar con el mes y año actuales
        selectedCalendar = Calendar.getInstance();
        updateMonthDisplay(); // Actualizar el TextView con el mes actual

        // Listener para abrir el DatePicker (para seleccionar mes/año)
        ivMonthPicker.setOnClickListener(v -> showMonthYearPicker());

        // *** PASO CLAVE: Cargar los IDs de clientes y servicios PRIMERO ***
        // Después de cargar los IDs, se puede llamar a addSampleVentasServicioData() (si es necesario)
        // y luego loadVentasUsuarioData().
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
                // Si esta es la actividad de reportes principal, simplemente retorna true para quedarte aquí.
                return true;
            }
            return false;
        });
    }

    /**
     * Carga todos los IDs de clientes y servicios de Firestore en listas
     * para su uso posterior (e.g., generación de datos de muestra, mapeo de nombres).
     * Después de que se completan las cargas, procede a llamar a otros métodos.
     */
    private void loadFirebaseDataAndThenProceed() {
        // Cargar IDs y nombres de clientes
        Task<QuerySnapshot> loadClientsTask = clientesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allClientIds.clear(); // Limpiar lista de IDs anteriores
                clientNamesById.clear(); // Limpiar mapa de nombres anteriores
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String clientId = document.getId();
                    allClientIds.add(clientId); // Añadir el ID a la lista de IDs disponibles
                    String nombres = document.getString("nombres");
                    String apellidos = document.getString("apellidos");
                    if (nombres != null && apellidos != null) {
                        // Guardar el nombre completo asociado al ID para búsquedas rápidas
                        clientNamesById.put(clientId, nombres + " " + apellidos);
                    }
                }
                Log.d(TAG, "Clientes cargados: " + allClientIds.size());
            } else {
                Log.e(TAG, "Error cargando clientes: ", task.getException());
                Toast.makeText(this, "Error al cargar datos de clientes.", Toast.LENGTH_SHORT).show();
            }
        });

        // Cargar IDs de servicios
        Task<QuerySnapshot> loadServicesTask = serviciosRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allServiceIds.clear(); // Limpiar lista de IDs anteriores
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String serviceId = document.getId();
                    allServiceIds.add(serviceId); // Añadir el ID a la lista de IDs disponibles
                }
                Log.d(TAG, "Servicios cargados: " + allServiceIds.size());
            } else {
                Log.e(TAG, "Error cargando servicios: ", task.getException());
                Toast.makeText(this, "Error al cargar datos de servicios.", Toast.LENGTH_SHORT).show();
            }
        });

        // Usar Tasks.whenAll para esperar que ambas cargas de datos se completen
        Tasks.whenAll(loadClientsTask, loadServicesTask)
                .addOnCompleteListener(allTasks -> {
                    if (allTasks.isSuccessful()) {
                        Log.d(TAG, "Todos los IDs de Firebase (clientes y servicios) cargados con éxito.");
                        // *** DESCOMENTA LA SIGUIENTE LÍNEA SOLO UNA VEZ PARA AÑADIR DATOS DE PRUEBA ***
                        // Luego, vuelve a comentarla o bórrala para evitar duplicados.
                        // addSampleVentasServicioData();

                        // Cargar los datos de ventas para el mes actual una vez que los IDs están disponibles
                        loadVentasUsuarioData();
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
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, 1); // Establece el día al primero del mes

                    updateMonthDisplay();
                    loadVentasUsuarioData(); // Recargar datos para el nuevo mes
                }, year, month, 1); // Se pasa 1 como día inicial

        // Intenta ocultar el día del DatePickerDialog
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
        // Usa el selectedCalendar para el rango de fechas
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
                                    // Obtener el nombre del cliente directamente del mapa clientNamesById
                                    String nombreCompleto = clientNamesById.getOrDefault(clientId, "Cliente Desconocido (ID: " + clientId + ")");
                                    consolidatedVenta = new VentaClienteConsolidado(clientId, nombreCompleto, 0.0);
                                    ventasConsolidadoMap.put(clientId, consolidatedVenta);
                                }
                                consolidatedVenta.addMonto(totalVenta); // Usando el método addMonto

                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear documento o procesar venta: " + document.getId(), e);
                            }
                        }

                        // Los nombres de los clientes ya fueron obtenidos y mapeados en clientNamesById
                        // por loadFirebaseDataAndThenProceed(), por lo que no necesitamos un bucle
                        // adicional ni Tasks.whenAll para obtener los nombres aquí.
                        Log.d(TAG, "Nombres de clientes ya disponibles. Actualizando UI con ventas consolidadas.");
                        ventasConsolidadas.clear();
                        ventasConsolidadas.addAll(ventasConsolidadoMap.values());
                        sortAndDisplayVentas();

                    } else {
                        Log.e(TAG, "Error obteniendo documentos de ventas: ", task.getException());
                        Toast.makeText(AdminVentasUsuario.this, "Error cargando ventas: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        ventasConsolidadas.clear(); // Limpiar datos antiguos
                        adapter.notifyDataSetChanged(); // Actualizar UI para mostrar vacío
                    }
                });
    }

    private void sortAndDisplayVentas() {
        // Ordenar las ventas consolidadas por monto total (descendente)
        Collections.sort(ventasConsolidadas, (v1, v2) -> Double.compare(v2.getMontoTotal(), v1.getMontoTotal()));
        adapter.notifyDataSetChanged(); // Actualizar RecyclerView
        Log.d(TAG, "Ventas consolidadas y actualizadas en UI. Cantidad: " + ventasConsolidadas.size());
    }

    // ************************************************************
    // *** MÉTODOS PARA LA GENERACIÓN DE PDF - INICIO ***
    // ************************************************************

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

            SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMM_MMMM", new Locale("es", "ES"));
            String fileName = "Reporte_Ventas_Usuarios_" + sdfFileName.format(selectedCalendar.getTime()) + ".pdf";
            File file = new File(pdfDir, fileName);

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

            Paragraph title = new Paragraph("Reporte de Venta por Usuarios", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat sdfReportMonth = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
            Paragraph subtitle = new Paragraph("Mes: " + sdfReportMonth.format(selectedCalendar.getTime()), subtitleFont);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(20f);
            document.add(subtitle);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 2f});
            table.setSpacingBefore(10f);

            table.addCell(createCenteredCell("Cliente", headerFont));
            table.addCell(createCenteredCell("Monto Total (S/.)", headerFont));

            DecimalFormat df = new DecimalFormat("0.00");

            for (VentaClienteConsolidado venta : ventasConsolidadas) {
                table.addCell(createCenteredCell(venta.getNombreCompletoCliente(), cellFont));
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
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener un icono apropiado
                .setContentTitle("PDF de Reporte de Ventas por Usuario Generado")
                .setContentText("Haz clic para abrir el reporte de ventas por usuario.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(101, builder.build());
    }

    // ************************************************************
    // *** MÉTODOS PARA LA GENERACIÓN DE PDF - FIN ***
    // ************************************************************


    // ************************************************************
    // *** INICIO DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    // ************************************************************
    /**
     * Añade datos de ventas de servicio de ejemplo a Firestore utilizando IDs
     * de clientes y servicios existentes y aleatorios de las listas previamente cargadas.
     * Este método solo debe llamarse una vez para poblar la base de datos de prueba.
     */
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

        // Número de ventas de ejemplo a generar (puedes ajustar este número)
        int numberOfSales = 20;

        for (int i = 0; i < numberOfSales; i++) {
            // Seleccionar un cliente y un servicio aleatorios de las listas cargadas
            String randomClientId = allClientIds.get(random.nextInt(allClientIds.size()));
            String randomServiceId = allServiceIds.get(random.nextInt(allServiceIds.size()));

            // Generar un precio y cantidad aleatorios
            double precioUnitario = 20.0 + (180.0 * random.nextDouble()); // Precio entre 20.0 y 200.0
            int cantidad = 1 + random.nextInt(3); // Cantidad entre 1 y 3
            double totalVenta = precioUnitario * cantidad;

            // Generar una fecha aleatoria en los últimos 3 meses
            cal.setTime(new Date()); // Empieza con la fecha y hora actuales
            // Retrocede hasta 2 meses (0, 1 o 2 meses atrás)
            cal.add(Calendar.MONTH, -random.nextInt(3));
            // Establece un día aleatorio dentro del mes seleccionado
            cal.set(Calendar.DAY_OF_MONTH, 1 + random.nextInt(cal.getActualMaximum(Calendar.DAY_OF_MONTH)));
            // Establece una hora aleatoria de 8 AM a 5 PM
            cal.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(10)); // 8 (inclusive) a 17 (exclusive) -> 8 a 16
            cal.set(Calendar.MINUTE, random.nextInt(60));
            cal.set(Calendar.SECOND, random.nextInt(60));


            sampleVentas.add(new VentaServicio(null, randomServiceId, randomClientId, cantidad, precioUnitario, totalVenta, cal.getTime()));
        }

        // Añadir todas las ventas generadas a Firestore
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
                        // Si todas las operaciones se han completado (éxitos + fallos == total), mostrar Toast
                        if (successCount.get() + failureCount.get() == totalSalesToProcess) {
                            Toast.makeText(AdminVentasUsuario.this, "Añadidos " + successCount.get() + " ventas de servicio de ejemplo.", Toast.LENGTH_SHORT).show();
                            loadVentasUsuarioData(); // Recargar los datos para mostrar las nuevas ventas
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir venta de servicio: " + e.getMessage(), e);
                        failureCount.incrementAndGet();
                        // Si todas las operaciones se han completado, mostrar Toast (incluso con errores)
                        if (successCount.get() + failureCount.get() == totalSalesToProcess) {
                            Toast.makeText(AdminVentasUsuario.this, "Añadidos " + successCount.get() + " ventas de servicio de ejemplo con " + failureCount.get() + " errores.", Toast.LENGTH_LONG).show();
                            loadVentasUsuarioData(); // Recargar los datos (mostrará lo que se haya añadido)
                        }
                    });
        }
        Toast.makeText(this, "Intentando añadir " + totalSalesToProcess + " ventas de servicio de ejemplo...", Toast.LENGTH_SHORT).show();
    }
    // ************************************************************
    // *** FIN DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    // ************************************************************
}