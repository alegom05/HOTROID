package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Servicios;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdminServiciosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ServiciosAdapter adapter;
    private ArrayList<Servicios> serviciosList;
    private ArrayList<Servicios> originalServiciosList;
    private EditText etSearchServicio;
    private Button btnClearSearch;
    private Button btnSelectHoraInicio;
    private Button btnSelectHoraFin;
    // Removí los TextViews ya que no están en tu layout actual de AdminServicios
    // private TextView tvSelectedHoraInicio;
    // private TextView tvSelectedHoraFin;
    private FirebaseFirestore db;

    private String selectedHoraInicio = "";
    private String selectedHoraFin = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_servicios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rvServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearchServicio = findViewById(R.id.etSearchServicio);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnSelectHoraInicio = findViewById(R.id.btnSelectHoraInicio);
        btnSelectHoraFin = findViewById(R.id.btnSelectHoraFin);

        originalServiciosList = new ArrayList<>();
        serviciosList = new ArrayList<>();
        adapter = new ServiciosAdapter(serviciosList);
        recyclerView.setAdapter(adapter);

        // Puedes descomentar esto si necesitas añadir los servicios iniciales solo una vez.
        // addInitialServicesToFirestore();

        etSearchServicio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterServicios(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            etSearchServicio.setText("");
            filterServicios("");
            // Clear selected times when clearing search
            selectedHoraInicio = "";
            selectedHoraFin = "";
            btnSelectHoraInicio.setText("Hora Inicio");
            btnSelectHoraFin.setText("Hora Fin");
        });

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosActivity.this, AdminNuevoServicioActivity.class);
            // Si necesitas pasar las horas seleccionadas en esta actividad para prellenar
            // el formulario de nuevo servicio, las pasas aquí.
            intent.putExtra("horaInicio", selectedHoraInicio);
            intent.putExtra("horaFin", selectedHoraFin);
            startActivityForResult(intent, 100);
        });

        // Set listeners for new time selection buttons
        btnSelectHoraInicio.setOnClickListener(v -> showTimePickerDialog(true));
        btnSelectHoraFin.setOnClickListener(v -> showTimePickerDialog(false));

        adapter.setOnItemClickListener((position) -> {
            Servicios selectedServicio = serviciosList.get(position);

            Intent intent = new Intent(AdminServiciosActivity.this, AdminServiciosDetallesActivity.class);
            intent.putExtra("Service_name", selectedServicio.getNombre());
            intent.putExtra("Service_description", selectedServicio.getDescripcion());
            intent.putExtra("price", selectedServicio.getPrecio());
            // CORRECCIÓN: Usar las claves correctas para pasar los datos
            intent.putExtra("hora_inicio", selectedServicio.getHoraInicio()); // Clave corregida
            intent.putExtra("hora_fin", selectedServicio.getHoraFin());       // Clave corregida
            intent.putExtra("documentId", selectedServicio.getDocumentId());
            intent.putStringArrayListExtra("imagenes", selectedServicio.getImagenes());
            intent.putExtra("habilitado", selectedServicio.isHabilitado()); // Asegurarse de pasar el estado habilitado
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Obtener el ID del elemento seleccionado
            if (itemId == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminServiciosActivity.this, AdminActivity.class); // Asumo que AdminActivity es tu pantalla principal
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminServiciosActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminServiciosActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminServiciosActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    private void showTimePickerDialog(boolean isStartTime) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    if (isStartTime) {
                        selectedHoraInicio = formattedTime;
                        btnSelectHoraInicio.setText("Inicio: " + formattedTime);
                    } else {
                        selectedHoraFin = formattedTime;
                        btnSelectHoraFin.setText("Fin: " + formattedTime);
                    }
                    // Opcional: podrías aplicar un filtro aquí si el usuario selecciona una hora para buscar
                }, hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }


    private void addInitialServicesToFirestore() {
        db.collection("servicios").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Asegúrate de que el constructor de Servicios acepte horaInicio y horaFin
                        // Servicios(String nombre, String descripcion, double precio, String horaInicio, String horaFin, ArrayList<String> imagenes)
                        Servicios wifi = new Servicios("Wi-fi Premium", "Acceso a internet de alta velocidad en todo el hotel.", 0.00, "00:00", "23:59", getUriStringsFromDrawable(R.drawable.wifi));
                        Servicios buffet = new Servicios("Desayuno Buffet", "Variedad de opciones de desayuno continental y local.", 35.00, "06:00", "10:00", getUriStringsFromDrawable(R.drawable.buffet));
                        Servicios gimnasio = new Servicios("Gimnasio", "Acceso a equipos de cardio y pesas.", 0.00, "05:00", "23:00", getUriStringsFromDrawable(R.drawable.gimnasio));
                        Servicios piscina = new Servicios("Piscina Climatizada", "Piscina cubierta con temperatura controlada.", 0.00, "07:00", "22:00", getUriStringsFromDrawable(R.drawable.piscina));
                        Servicios karaoke = new Servicios("Sala de Karaoke", "Disfruta de una noche de diversión con amigos y familia.", 50.00, "19:00", "02:00", getUriStringsFromDrawable(R.drawable.karaoke));
                        Servicios lavanderia = new Servicios("Servicio de Lavandería", "Lavado y planchado de ropa personal.", 15.50, "08:00", "18:00", getUriStringsFromDrawable(R.drawable.lavanderia));
                        Servicios spa = new Servicios("Spa y Masajes", "Relájate con nuestros tratamientos y masajes profesionales.", 80.00, "09:00", "20:00", getUriStringsFromDrawable(R.drawable.spa));

                        addServiceToFirestore(wifi);
                        addServiceToFirestore(buffet);
                        addServiceToFirestore(gimnasio);
                        addServiceToFirestore(piscina);
                        addServiceToFirestore(karaoke);
                        addServiceToFirestore(lavanderia);
                        addServiceToFirestore(spa);

                        Toast.makeText(this, "Servicios iniciales añadidos a Firestore.", Toast.LENGTH_SHORT).show();
                        recargarServicios();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al verificar servicios iniciales: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addServiceToFirestore(Servicios servicio) {
        db.collection("servicios")
                .add(servicio)
                .addOnSuccessListener(documentReference -> {
                    servicio.setDocumentId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al añadir servicio " + servicio.getNombre() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private ArrayList<String> getUriStringsFromDrawable(int drawableId) {
        ArrayList<String> uriStrings = new ArrayList<>();
        // Asegúrate de que esta URL sea cargable por Glide o que tus imágenes estén en Cloudinary/Firebase Storage
        uriStrings.add(Uri.parse("android.resource://" + getPackageName() + "/" + drawableId).toString());
        return uriStrings;
    }

    private void filterServicios(String text) {
        serviciosList.clear();
        if (text.isEmpty()) {
            serviciosList.addAll(originalServiciosList);
        } else {
            text = text.toLowerCase(Locale.getDefault());
            for (Servicios servicio : originalServiciosList) {
                // Ahora busca también por horaInicio y horaFin
                if (servicio.getNombre().toLowerCase(Locale.getDefault()).contains(text) ||
                        (servicio.getHoraInicio() != null && servicio.getHoraInicio().toLowerCase(Locale.getDefault()).contains(text)) ||
                        (servicio.getHoraFin() != null && servicio.getHoraFin().toLowerCase(Locale.getDefault()).contains(text))) {
                    serviciosList.add(servicio);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarServicios();
    }

    private void recargarServicios() {
        db.collection("servicios").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    originalServiciosList.clear();
                    serviciosList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Servicios servicio = doc.toObject(Servicios.class);
                        servicio.setDocumentId(doc.getId());
                        originalServiciosList.add(servicio);
                    }
                    filterServicios(etSearchServicio.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al recargar servicios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String nombre = data.getStringExtra("nombre");
            String descripcion = data.getStringExtra("descripcion");
            // Asegúrate de que las claves aquí coincidan con lo que AdminNuevoServicioActivity devuelve
            String horaInicio = data.getStringExtra("hora_inicio"); // POSIBLE CAMBIO AQUÍ si NuevoServicio devuelve "hora_inicio"
            String horaFin = data.getStringExtra("hora_fin");       // POSIBLE CAMBIO AQUÍ si NuevoServicio devuelve "hora_fin"
            ArrayList<String> uriStrings = data.getStringArrayListExtra("imagenes");

            double precio = 0.0;
            // Si AdminNuevoServicioActivity devuelve el precio como double directamente, usa getDoubleExtra
            precio = data.getDoubleExtra("precio", 0.0); // OJO: Si AdminNuevoServicioActivity devuelve como String, mantén el parseo
            /*
            String precioStr = data.getStringExtra("precio");
            if (precioStr != null && !precioStr.isEmpty()) {
                try {
                    precio = Double.parseDouble(precioStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Error: El precio no es un número válido.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            */
            recargarServicios();
            showNotification("Servicio creado", "El servicio \"" + nombre + "\" fue registrado con éxito.");
        }
    }

    private void showNotification(String title, String message) {
        String CHANNEL_ID = "servicios_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Servicios";
            String description = "Canal para notificaciones de registro de servicios";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotroid_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(2, builder.build());
        } else {
//            Toast.makeText(this, "Permiso de notificaciones no concedido", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }
    }
}