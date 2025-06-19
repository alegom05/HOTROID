package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.Room;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random; // Para generar áreas aleatorias

public class AdminHabitacionesActivity extends AppCompatActivity {

    private RecyclerView rvHabitaciones;
    private HabitacionesAdapter adapter;
    private List<Room> roomList;
    private List<Room> originalRoomList;
    private FirebaseFirestore db;
    private List<Reserva> reservasList;
    private EditText etBuscadorHabitaciones;
    private Button btnLimpiarBuscadorHabitaciones;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_habitaciones);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        roomList = new ArrayList<>();
        originalRoomList = new ArrayList<>();
        reservasList = new ArrayList<>();

        rvHabitaciones = findViewById(R.id.rvHabitaciones);
        rvHabitaciones.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HabitacionesAdapter(roomList);
        rvHabitaciones.setAdapter(adapter);

        etBuscadorHabitaciones = findViewById(R.id.etBuscadorHabitaciones);
        btnLimpiarBuscadorHabitaciones = findViewById(R.id.btnLimpiarBuscadorHabitaciones);
        btnRegistrar = findViewById(R.id.btnRegistrarNuevaHabitacion);

        // --- Lógica del buscador ---
        etBuscadorHabitaciones.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterRooms(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimpiarBuscadorHabitaciones.setOnClickListener(v -> {
            etBuscadorHabitaciones.setText("");
        });

        btnRegistrar.setOnClickListener(v -> {
            Toast.makeText(AdminHabitacionesActivity.this, "Navegar a la pantalla de registro de habitación", Toast.LENGTH_SHORT).show();
             Intent intent = new Intent(AdminHabitacionesActivity.this, AdminNuevaHabitacionActivity.class);
            startActivity(intent);
        });

        loadReservasThenGenerateAndLoadRooms();

        adapter.setOnItemClickListener(new HabitacionesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Room clickedRoom = roomList.get(position);

                Intent intent = new Intent(AdminHabitacionesActivity.this,AdminHabitacionDetallesActivity.class);
                intent.putExtra("ROOM_ID", clickedRoom.getId());
                intent.putExtra("ROOM_NUMBER", clickedRoom.getRoomNumber());
                intent.putExtra("ROOM_TYPE", clickedRoom.getRoomType());
                intent.putExtra("CAPACITY_ADULTS", clickedRoom.getCapacityAdults()); // Pasar como int
                intent.putExtra("CAPACITY_CHILDREN", clickedRoom.getCapacityChildren()); // Pasar como int
                intent.putExtra("AREA", clickedRoom.getArea()); // ¡Pasar como double!
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_registros);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminHabitacionesActivity.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    Intent intentCheckout = new Intent(AdminHabitacionesActivity.this, AdminCheckout.class);
                    startActivity(intentCheckout);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentAlertas = new Intent(AdminHabitacionesActivity.this, AdminReportes.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        } else {
            Log.e("AdminHabitacionesActivity", "BottomNavigationView con ID R.id.bottom_navigation no encontrada.");
        }
    }

    private void loadReservasThenGenerateAndLoadRooms() {
        db.collection("reservas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reservasList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reserva reserva = document.toObject(Reserva.class);
                                reservasList.add(reserva);
                            }
                            Log.d("AdminHabitacionesActivity", "Reservas cargadas exitosamente: " + reservasList.size());
                            // --- ¡DESCOMENTAR SOLO PARA LA PRIMERA EJECUCIÓN! ---
                            // generateAndSaveRoomsFromReservas();
                            // --- COMENTAR DESPUÉS DE LA PRIMERA EJECUCIÓN EXITOSA ---
                            loadRoomsFromFirestore();
                        } else {
                            Log.w("AdminHabitacionesActivity", "Error al obtener documentos de reservas: ", task.getException());
                            Toast.makeText(AdminHabitacionesActivity.this, "Error al cargar reservas: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void generateAndSaveRoomsFromReservas() {
        if (reservasList.isEmpty()) {
            Log.w("AdminHabitacionesActivity", "No hay reservas para generar habitaciones.");
            Toast.makeText(AdminHabitacionesActivity.this, "No hay reservas para generar habitaciones.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> existingRoomNumbers = new ArrayList<>();
        db.collection("habitaciones")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            existingRoomNumbers.add(document.getString("roomNumber"));
                        }
                    }

                    Random random = new Random(); // Para generar áreas aleatorias
                    for (Reserva reserva : reservasList) {
                        if (!existingRoomNumbers.contains(reserva.getRoomNumber())) {
                            String roomType;
                            if (reserva.getPrecioTotal() >= 1000) {
                                roomType = "Suite Lujo";
                            } else if (reserva.getPrecioTotal() >= 500) {
                                roomType = "Doble Superior";
                            } else {
                                roomType = "Estándar";
                            }

                            // Generar un área aleatoria para el ejemplo (entre 20 y 60 m²)
                            double area = 20.0 + (60.0 - 20.0) * random.nextDouble();
                            area = Math.round(area * 100.0) / 100.0; // Redondear a 2 decimales

                            Room newRoom = new Room(
                                    null,
                                    reserva.getRoomNumber(),
                                    roomType,
                                    reserva.getAdultos(),
                                    reserva.getNinos(),
                                    area // ¡Pasado como double!
                            );

                            db.collection("habitaciones")
                                    .add(newRoom)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("AdminHabitacionesActivity", "Habitación generada y guardada con ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w("AdminHabitacionesActivity", "Error al añadir habitación", e);
                                        Toast.makeText(AdminHabitacionesActivity.this, "Error al generar habitación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }

    private void loadRoomsFromFirestore() {
        db.collection("habitaciones")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            roomList.clear();
                            originalRoomList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Room room = document.toObject(Room.class);
                                roomList.add(room);
                            }
                            originalRoomList.addAll(roomList);
                            adapter.notifyDataSetChanged();
                            Log.d("AdminHabitacionesActivity", "Habitaciones cargadas: " + roomList.size());
                            if (roomList.isEmpty()) {
                                Toast.makeText(AdminHabitacionesActivity.this, "No hay habitaciones registradas.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("AdminHabitacionesActivity", "Error al cargar habitaciones: ", task.getException());
                            Toast.makeText(AdminHabitacionesActivity.this, "Error al cargar habitaciones: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void filterRooms(String searchText) {
        List<Room> filteredList = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredList.addAll(originalRoomList);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());
            for (Room room : originalRoomList) {
                // Filtrar por número de habitación o tipo de habitación
                // Ahora convertimos el número de habitación a String para la comparación
                if (String.valueOf(room.getRoomNumber()).toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText) ||
                        room.getRoomType().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText)) {
                    filteredList.add(room);
                }
            }
        }
        adapter.updateList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoomsFromFirestore();
    }
}