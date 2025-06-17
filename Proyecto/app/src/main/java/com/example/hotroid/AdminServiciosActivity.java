package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.util.Locale;

public class AdminServiciosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ServiciosAdapter adapter;
    private ArrayList<Servicios> serviciosList;
    private ArrayList<Servicios> originalServiciosList;
    private EditText etSearchServicio;
    private Button btnClearSearch;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_servicios);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.rvServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etSearchServicio = findViewById(R.id.etSearchServicio);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        originalServiciosList = new ArrayList<>();
        serviciosList = new ArrayList<>();
        adapter = new ServiciosAdapter(serviciosList);
        recyclerView.setAdapter(adapter);

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
        });

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosActivity.this, AdminNuevoServicioActivity.class);
            startActivityForResult(intent, 100);
        });

        adapter.setOnItemClickListener((position) -> {
            Servicios selectedServicio = serviciosList.get(position);

            Intent intent = new Intent(AdminServiciosActivity.this, AdminServiciosDetallesActivity.class);
            intent.putExtra("Service_name", selectedServicio.getNombre());
            intent.putExtra("Service_description", selectedServicio.getDescripcion());
            // --- FIX: Pass price as double ---
            intent.putExtra("price", selectedServicio.getPrecio());
            // ---------------------------------
            intent.putExtra("schedule", selectedServicio.getHorario());
            intent.putExtra("documentId", selectedServicio.getDocumentId());
            intent.putStringArrayListExtra("imagenes", selectedServicio.getImagenes());
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminServiciosActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminServiciosActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminServiciosActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminServiciosActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    private void addInitialServicesToFirestore() {
        db.collection("servicios").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        ArrayList<String> noImages = new ArrayList<>();

                        // --- FIX: Use double for price ---
                        Servicios wifi = new Servicios("Wi-fi Premium", "Acceso a internet de alta velocidad en todo el hotel.", 0.00, "24/7", getUriStringsFromDrawable(R.drawable.wifi));
                        Servicios buffet = new Servicios("Desayuno Buffet", "Variedad de opciones de desayuno continental y local.", 35.00, "6:00 AM - 10:00 AM", getUriStringsFromDrawable(R.drawable.buffet));
                        Servicios gimnasio = new Servicios("Gimnasio", "Acceso a equipos de cardio y pesas.", 0.00, "5:00 AM - 11:00 PM", getUriStringsFromDrawable(R.drawable.gimnasio));
                        Servicios piscina = new Servicios("Piscina Climatizada", "Piscina cubierta con temperatura controlada.", 0.00, "7:00 AM - 10:00 PM", getUriStringsFromDrawable(R.drawable.piscina));
                        Servicios karaoke = new Servicios("Sala de Karaoke", "Disfruta de una noche de diversión con amigos y familia.", 50.00, "7:00 PM - 2:00 AM", getUriStringsFromDrawable(R.drawable.karaoke));
                        Servicios lavanderia = new Servicios("Servicio de Lavandería", "Lavado y planchado de ropa personal.", 15.50, "8:00 AM - 6:00 PM", getUriStringsFromDrawable(R.drawable.lavanderia)); // Example decimal price
                        Servicios spa = new Servicios("Spa y Masajes", "Relájate con nuestros tratamientos y masajes profesionales.", 80.00, "9:00 AM - 8:00 PM", getUriStringsFromDrawable(R.drawable.spa));
                        // ---------------------------------

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
                if (servicio.getNombre().toLowerCase(Locale.getDefault()).contains(text) ||
                        (servicio.getHorario() != null && servicio.getHorario().toLowerCase(Locale.getDefault()).contains(text))) {
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
            String horario = data.getStringExtra("horario");
            ArrayList<String> uriStrings = data.getStringArrayListExtra("imagenes");

            // --- FIX: Parse price String to double and handle potential errors ---
            double precio = 0.0; // Default value
            String precioStr = data.getStringExtra("precio");
            if (precioStr != null && !precioStr.isEmpty()) {
                try {
                    precio = Double.parseDouble(precioStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Error: El precio no es un número válido.", Toast.LENGTH_SHORT).show();
                    return; // Stop if price is invalid
                }
            }
            // -------------------------------------------------------------------

            Servicios nuevoServicio = new Servicios(nombre, descripcion, precio, horario, uriStrings);

            db.collection("servicios").add(nuevoServicio)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Servicio registrado en Firestore.", Toast.LENGTH_SHORT).show();
                        recargarServicios();
                        showNotification("Servicio creado", "El servicio \"" + nombre + "\" fue registrado con éxito.");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al registrar servicio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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
        notificationManager.notify(2, builder.build());
    }
}