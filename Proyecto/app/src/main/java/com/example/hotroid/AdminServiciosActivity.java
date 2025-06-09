package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminServiciosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ServiciosAdapter adapter;
    private ArrayList<Servicios> serviciosList;

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
        recyclerView = findViewById(R.id.rvServicios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Initialize room data
        serviciosList = new ArrayList<>();

        // Lista 1: imagen de wifi
        ArrayList<Uri> imagenesWifi = new ArrayList<>();
        imagenesWifi.add(getUriFromDrawable(R.drawable.wifi));

        // Lista 2: imagen de desayuno buffet
        ArrayList<Uri> imagenesBuffet = new ArrayList<>();
        imagenesBuffet.add(getUriFromDrawable(R.drawable.buffet));

        // Lista 3: gimnasio
        ArrayList<Uri> imagenesGimnasio = new ArrayList<>();
        imagenesGimnasio.add(getUriFromDrawable(R.drawable.gimnasio));

        // Lista 4: piscina
        ArrayList<Uri> imagenesPiscina = new ArrayList<>();
        imagenesPiscina.add(getUriFromDrawable(R.drawable.piscina));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        serviciosList = new ArrayList<>();
        adapter = new ServiciosAdapter(serviciosList);
        recyclerView.setAdapter(adapter);




        // Set the adapter
        adapter = new ServiciosAdapter(serviciosList);
        recyclerView.setAdapter(adapter);

        // Set up the Register button click listener
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            // Open AdminNuevaHabitacionActivity when Register button is clicked
            Intent intent = new Intent(AdminServiciosActivity.this, AdminNuevoServicioActivity.class);
            startActivityForResult(intent, 100);

        });
        // Set up click listener for each service item
        adapter.setOnItemClickListener((position) -> {
            Servicios selectedServicio = serviciosList.get(position);

            // Create an Intent to open RoomDetailActivity
            Intent intent = new Intent(AdminServiciosActivity.this, AdminServiciosDetallesActivity.class);
            intent.putExtra("Service_name", selectedServicio.getNombre());
            intent.putExtra("Service_description", selectedServicio.getDescripcion());
            intent.putExtra("price", selectedServicio.getPrecio());
            intent.putExtra("documentId", selectedServicio.getDocumentId()); // 👈 esto es lo que necesitas

            ArrayList<String> uriStrings = new ArrayList<>();
            for (Uri uri : selectedServicio.getImagenes()) {
                uriStrings.add(uri.toString());
            }
            intent.putStringArrayListExtra("imagenes", uriStrings);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
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
    private Servicios convertirAFirebase(ServicioFirebase servicio) {
        ArrayList<Uri> imagenesUri = new ArrayList<>();
        for (String uriString : servicio.getImagenes()) {
            imagenesUri.add(Uri.parse(uriString));
        }

        Servicios servicioApp = new Servicios(
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getPrecio(),
                imagenesUri
        );
        servicioApp.setHabilitado(servicio.isHabilitado());
        return servicioApp;
    }
    @Override
    protected void onResume() {
        super.onResume();
        recargarServicios();
    }
    private void recargarServicios() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        serviciosList.clear();
        db.collection("servicios").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ServicioFirebase servicio = doc.toObject(ServicioFirebase.class);
                        Servicios servicioApp = convertirAFirebase(servicio);
                        servicioApp.setDocumentId(doc.getId());  // 💡 Aquí guardas el documentId real
                        serviciosList.add(servicioApp);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al recargar servicios", Toast.LENGTH_SHORT).show();
                });
    }

    private Uri getUriFromDrawable(int drawableId) {
        return Uri.parse("android.resource://" + getPackageName() + "/" + drawableId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String nombre = data.getStringExtra("nombre");
            String descripcion = data.getStringExtra("descripcion");
            String precio = data.getStringExtra("precio");
            ArrayList<String> uriStrings = data.getStringArrayListExtra("imagenes");

            ArrayList<Uri> imagenes = new ArrayList<>();
            for (String uriStr : uriStrings) {
                imagenes.add(Uri.parse(uriStr));
            }

            Servicios nuevoServicio = new Servicios(nombre, descripcion, precio, imagenes);
            serviciosList.add(nuevoServicio);
            adapter.notifyItemInserted(serviciosList.size() - 1);
            showNotification("Servicio creado", "El servicio \"" + nombre + "\" fue registrado con éxito.");
        }
    }
    private void showNotification(String title, String message) {
        String CHANNEL_ID = "servicios_channel";

        // Crear canal de notificación si aún no existe (solo para Android O en adelante)
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

        // Crear notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotroid_icon)  // Asegúrate de tener este ícono
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Mostrarla
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2, builder.build()); // ID arbitrario
    }


}