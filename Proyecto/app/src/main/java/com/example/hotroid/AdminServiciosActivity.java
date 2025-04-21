package com.example.hotroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // Ahora agrega los servicios con sus respectivas imágenes
        serviciosList.add(new Servicios("WIFI", "descripcion", "2", imagenesWifi));
        serviciosList.add(new Servicios("Desayuno Buffet", "descripcion", "2", imagenesBuffet));
        serviciosList.add(new Servicios("Gimnasio", "descripcion", "2", imagenesGimnasio));
        serviciosList.add(new Servicios("Piscina", "descripcion", "2", imagenesPiscina));


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
        }
    }

}