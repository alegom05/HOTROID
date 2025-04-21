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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AdminHabitacionesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitacionesAdapter adapter;
    private ArrayList<Room> roomList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_habitaciones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        recyclerView = findViewById(R.id.rvHabitaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize room data
        roomList = new ArrayList<>();
        roomList.add(new Room("101", "Standard", "2", "2", "25"));
        roomList.add(new Room("102", "Económica", "2", "1", "20"));
        roomList.add(new Room("103", "Standard", "2", "3", "30"));
        roomList.add(new Room("104", "Lux", "2", "2", "35"));

        // Set the adapter
        adapter = new HabitacionesAdapter(roomList);
        recyclerView.setAdapter(adapter);

        // Set up the Register button click listener
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            // Open AdminNuevaHabitacionActivity when Register button is clicked
            Intent intent = new Intent(AdminHabitacionesActivity.this, AdminNuevaHabitacionActivity.class);
            startActivity(intent);
        });
        // Set up click listener for each room item
        adapter.setOnItemClickListener((position) -> {
            Room selectedRoom = roomList.get(position);

            // Create an Intent to open RoomDetailActivity
            Intent intent = new Intent(AdminHabitacionesActivity.this, AdminHabitacionDetallesActivity.class);
            intent.putExtra("ROOM_NUMBER", selectedRoom.getRoomNumber());
            intent.putExtra("ROOM_TYPE", selectedRoom.getRoomType());
            // Optionally add capacity and area
            intent.putExtra("CAPACITY", selectedRoom.getCapacityAdults() + " Adultos, " + selectedRoom.getCapacityChildren() + " Niños");
            intent.putExtra("AREA", selectedRoom.getArea());
            startActivity(intent);
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminHabitacionesActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminHabitacionesActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminHabitacionesActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminHabitacionesActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
}