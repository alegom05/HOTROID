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
import java.util.concurrent.atomic.AtomicInteger; // Para el contador en la carga de datos

public class AdminVentasUsuario extends AppCompatActivity {

    private static final String TAG = "AdminVentasUsuario";

    private FirebaseFirestore db;
    private CollectionReference ventasServiciosRef; // Añadido
    private CollectionReference clientesRef; // Añadido
    private RecyclerView recyclerView;
    private VentaClienteAdapter adapter;
    private List<VentaClienteConsolidado> ventasConsolidadas;
    private TextView tvMesSeleccionado;
    private ImageView ivMonthPicker;

    private Calendar selectedCalendar; // Para almacenar el mes y año seleccionados

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
        ventasServiciosRef = db.collection("ventas_servicios"); // Inicializado
        clientesRef = db.collection("clientes"); // Inicializado

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

        // *** TEMPORARY: Call this method to add sample data ***
        // Descomenta la siguiente línea solo para añadir los datos de prueba.
        // Luego, vuelve a comentarla o bórrala después de la primera ejecución.
        // addSampleVentasServicioData(); // Asegúrate de tener servicios y clientes reales en DB antes de esto.

        // Cargar los datos inicialmente para el mes actual
        loadVentasUsuarioData(); // Cargar datos de ventas por usuario

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
                // Si tienes una actividad "AdminReportes" más general, la iniciarías aquí:
                // startActivity(new Intent(AdminVentasUsuario.this, AdminReportes.class));
                return true;
            }
            return false;
        });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")); // Corregido el formato para mostrar el año
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
                        List<Task<Void>> clientFetchTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                VentaServicio venta = document.toObject(VentaServicio.class);
                                String clientId = venta.getIdCliente();
                                double totalVenta = venta.getTotalVenta();

                                VentaClienteConsolidado consolidatedVenta = ventasConsolidadoMap.get(clientId);
                                if (consolidatedVenta == null) {
                                    // El nombre se buscará después, por ahora un placeholder vacío
                                    consolidatedVenta = new VentaClienteConsolidado(clientId, "", 0.0);
                                    ventasConsolidadoMap.put(clientId, consolidatedVenta);
                                }
                                consolidatedVenta.addMonto(totalVenta); // Usando el método addMonto

                            } catch (Exception e) {
                                Log.e(TAG, "Error al parsear documento o procesar venta: " + document.getId(), e);
                            }
                        }

                        // Ahora, buscar los nombres de los clientes para las ventas consolidadas
                        for (VentaClienteConsolidado consolidated : ventasConsolidadoMap.values()) {
                            // Solo si el nombre aún no ha sido establecido (debería estar vacío al inicio)
                            if (consolidated.getNombreCompletoCliente().isEmpty()) {
                                Task<Void> fetchTask = clientesRef.document(consolidated.getIdCliente()).get()
                                        .addOnSuccessListener(clientDocument -> {
                                            if (clientDocument.exists()) {
                                                Cliente cliente = clientDocument.toObject(Cliente.class);
                                                if (cliente != null) {
                                                    // Asumiendo que Cliente tiene getNombres() y getApellidos()
                                                    String nombreCompleto = cliente.getNombres() + " " + cliente.getApellidos();
                                                    consolidated.setNombreCompletoCliente(nombreCompleto);
                                                }
                                            } else {
                                                Log.w(TAG, "Cliente con ID " + consolidated.getIdCliente() + " no encontrado en Firestore.");
                                                consolidated.setNombreCompletoCliente("Cliente Desconocido (ID: " + consolidated.getIdCliente() + ")");
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error al obtener cliente con ID: " + consolidated.getIdCliente() + ", " + e.getMessage());
                                            consolidated.setNombreCompletoCliente("Error al cargar cliente (ID: " + consolidated.getIdCliente() + ")");
                                        })
                                        .continueWith(task1 -> null); // El lambda para continueWith debe retornar un valor (en este caso null para Task<Void>)
                                clientFetchTasks.add(fetchTask);
                            }
                        }

                        // Usar Tasks.whenAll para esperar que todas las tareas de obtención de clientes se completen
                        if (!clientFetchTasks.isEmpty()) {
                            Tasks.whenAll(clientFetchTasks)
                                    .addOnCompleteListener(allTasks -> {
                                        if (allTasks.isSuccessful()) {
                                            Log.d(TAG, "Todos los nombres de clientes obtenidos. Actualizando UI.");
                                            ventasConsolidadas.clear();
                                            ventasConsolidadas.addAll(ventasConsolidadoMap.values());
                                            sortAndDisplayVentas();
                                        } else {
                                            Log.e(TAG, "Algunas tareas de obtención de nombres de clientes fallaron: ", allTasks.getException());
                                            // Aunque algunas fallen, actualizamos con los datos que tenemos
                                            ventasConsolidadas.clear();
                                            ventasConsolidadas.addAll(ventasConsolidadoMap.values());
                                            sortAndDisplayVentas();
                                        }
                                    });
                        } else {
                            // No hay clientes que buscar, simplemente actualizar la UI
                            Log.d(TAG, "No hay clientes en ventas para buscar nombres. Actualizando UI directamente.");
                            ventasConsolidadas.clear();
                            ventasConsolidadas.addAll(ventasConsolidadoMap.values());
                            sortAndDisplayVentas();
                        }

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

            SimpleDateFormat sdfReportMonth = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES")); // Corregido el formato
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
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener un icono
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
    // Puedes comentar o eliminar estos métodos después de usarlos.
    // Asegúrate de que los IDs de cliente y servicio sean reales en tu base de datos.
    // ************************************************************
    private void addSampleVentasServicioData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<VentaServicio> sampleVentas = new ArrayList<>();

        // --- IMPORTANTE: Reemplaza estos con tus IDs REALES de CLIENTES de Firestore ---
        // Estos IDs de cliente son consistentes con tus capturas de pantalla de Firebase.
        // Asumiendo que estos son los IDs de tus documentos en la colección 'clientes'.
        String CLIENT_ID_PAMELA = "4WtmgavW2xpZtLclbg76"; // Tu ID de Pamela
        String CLIENT_ID_ALONSO = "5PxcEUoWep0JVe67PZQy"; // ID de Alonso
        String CLIENT_ID_ROBERT = "JQSEUylORLpKdyAH2Vt3"; // ID de Robert
        String CLIENT_ID_RUBEN = "YgFzSkts9pMY3FoVGprg";  // ID de Ruben
        String CLIENT_ID_GUMERCINDO = "ijAo74LEw6ANJRDGB8D5"; // ID de Gumercindo

        // --- IMPORTANTE: Reemplaza estos con tus IDs REALES de SERVICIOS de Firestore ---
        // He usado los IDs que aparecen en tus capturas de la colección 'servicios'.
        // Verifica que estos IDs sean EXACTOS a los de tus documentos en la colección 'servicios'.
        String SERVICE_ID_LAVANDERIA = "L5tMZ01Lzflhbr279Y9o"; //
        String SERVICE_ID_JARDINERIA = "AySL6OmQ0uvut09LzWcd"; //
        String SERVICE_ID_ELECTRICIDAD = "CXVY2UDbI5IhfOF5nyZH"; //
        String SERVICE_ID_FONTANERIA = "8FtghYALwtcjjn9T7TNh"; //
        String SERVICE_ID_PINTURA = "MHDuY7FWio4kGoclrOij"; //
        String SERVICE_ID_KARAOKE = "65QQCSuDAC9Sra1P6PnR"; //
        String SERVICE_ID_GYM = "G6SmZnjshIeY55vxw7vn"; //


        Calendar cal = Calendar.getInstance();

        // --- Datos de Ventas para Abril 2025 (mezcla de clientes y servicios) ---
        cal.set(2025, Calendar.APRIL, 5, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_LAVANDERIA, CLIENT_ID_PAMELA, 2, 50.0, 100.0, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 20, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_KARAOKE, CLIENT_ID_PAMELA, 1, 50.5, 50.5, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 22, 11, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_GYM, CLIENT_ID_ALONSO, 3, 50.0, 150.0, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 8, 11, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_JARDINERIA, CLIENT_ID_ALONSO, 1, 75.0, 75.0, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 12, 14, 30, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_ELECTRICIDAD, CLIENT_ID_ROBERT, 1, 120.0, 120.0, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 15, 9, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_PINTURA, CLIENT_ID_RUBEN, 1, 200.0, 200.0, cal.getTime()));
        cal.set(2025, Calendar.APRIL, 18, 16, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_FONTANERIA, CLIENT_ID_GUMERCINDO, 1, 90.0, 90.0, cal.getTime()));


        // --- Datos de Ventas para Mayo 2025 (mezcla de clientes y servicios) ---
        cal.set(2025, Calendar.MAY, 15, 17, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_GYM, CLIENT_ID_PAMELA, 3, 50.0, 150.0, cal.getTime()));
        cal.set(2025, Calendar.MAY, 20, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_KARAOKE, CLIENT_ID_ROBERT, 1, 50.5, 50.5, cal.getTime()));
        cal.set(2025, Calendar.MAY, 25, 11, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_LAVANDERIA, CLIENT_ID_PAMELA, 1, 120.0, 120.0, cal.getTime()));
        cal.set(2025, Calendar.MAY, 3, 9, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_JARDINERIA, CLIENT_ID_ROBERT, 2, 75.0, 150.0, cal.getTime()));
        cal.set(2025, Calendar.MAY, 7, 13, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_ELECTRICIDAD, CLIENT_ID_RUBEN, 1, 120.0, 120.0, cal.getTime()));
        cal.set(2025, Calendar.MAY, 10, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_PINTURA, CLIENT_ID_GUMERCINDO, 1, 200.0, 200.0, cal.getTime()));
        cal.set(2025, Calendar.MAY, 18, 8, 30, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_FONTANERIA, CLIENT_ID_ALONSO, 1, 90.0, 90.0, cal.getTime()));


        // --- Datos de Ventas para Junio 2025 (mezcla de clientes y servicios) ---
        cal.set(2025, Calendar.JUNE, 1, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_LAVANDERIA, CLIENT_ID_ROBERT, 1, 50.0, 50.0, cal.getTime()));
        cal.set(2025, Calendar.JUNE, 16, 14, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_GYM, CLIENT_ID_ALONSO, 2, 50.0, 100.0, cal.getTime()));
        cal.set(2025, Calendar.JUNE, 20, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_KARAOKE, CLIENT_ID_ROBERT, 1, 50.5, 50.5, cal.getTime()));
        cal.set(2025, Calendar.JUNE, 5, 15, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_JARDINERIA, CLIENT_ID_RUBEN, 1, 75.0, 75.0, cal.getTime()));
        cal.set(2025, Calendar.JUNE, 10, 11, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_ELECTRICIDAD, CLIENT_ID_GUMERCINDO, 2, 120.0, 240.0, cal.getTime()));
        cal.set(2025, Calendar.JUNE, 14, 10, 0, 0);
        sampleVentas.add(new VentaServicio(null, SERVICE_ID_PINTURA, CLIENT_ID_PAMELA, 1, 200.0, 200.0, cal.getTime()));


        // Añadir todas las ventas a Firestore
        for (VentaServicio venta : sampleVentas) {
            db.collection("ventas_servicios").add(venta)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Venta de servicio añadida con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al añadir venta de servicio: " + e.getMessage(), e);
                    });
        }
        Toast.makeText(this, "Añadiendo ventas de servicio de ejemplo...", Toast.LENGTH_SHORT).show();
    }
    // ************************************************************
    // *** FIN DE MÉTODOS PARA AÑADIR DATOS DE PRUEBA (TEMPORAL) ***
    // ************************************************************
}