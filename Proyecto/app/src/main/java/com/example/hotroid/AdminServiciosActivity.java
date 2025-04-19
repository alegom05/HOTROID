package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        serviciosList.add(new Servicios("WIFI", "descripcion", "2"));
        serviciosList.add(new Servicios("Desayuno Buffet", "descripcion", "2"));
        serviciosList.add(new Servicios("Gimnasio", "descripcion", "2"));
        serviciosList.add(new Servicios("Piscina", "descripcion", "2"));

        // Set the adapter
        adapter = new ServiciosAdapter(serviciosList);
        recyclerView.setAdapter(adapter);

        // Set up the Register button click listener
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            // Open AdminNuevaHabitacionActivity when Register button is clicked
            Intent intent = new Intent(AdminServiciosActivity.this, AdminNuevoServicioActivity.class);
            startActivity(intent);
        });
        // Set up click listener for each room item
        adapter.setOnItemClickListener((position) -> {
            Servicios selectedServicio = serviciosList.get(position);

            // Create an Intent to open RoomDetailActivity
            Intent intent = new Intent(AdminServiciosActivity.this, AdminServiciosDetallesActivity.class);
            intent.putExtra("Service_name", selectedServicio.getNombre());
            intent.putExtra("Service_description", selectedServicio.getDescripcion());
            intent.putExtra("price", selectedServicio.getPrecio());
            startActivity(intent);
        });
    }
}