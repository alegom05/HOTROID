package com.example.hotroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
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

import com.example.hotroid.bean.Room;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.hotroid.RoomFirebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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


        // Set the adapter
        roomList = new ArrayList<>();
        adapter = new HabitacionesAdapter(roomList);
        recyclerView.setAdapter(adapter);

        // Set up the Register button click listener
        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            // Open AdminNuevaHabitacionActivity when Register button is clicked
            Intent intent = new Intent(AdminHabitacionesActivity.this, AdminNuevaHabitacionActivity.class);
            startActivityForResult(intent, 100);  // Código arbitrario
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
    @Override
    protected void onResume() {
        super.onResume();
        recargarHabitacionesDesdeFirestore();
    }

    private void recargarHabitacionesDesdeFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        roomList.clear();

        db.collection("habitaciones").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        RoomFirebase rf = doc.toObject(RoomFirebase.class);
                        Room r = new Room(rf.getRoomNumber(), rf.getRoomType(), rf.getCapacityAdults(), rf.getCapacityChildren(), rf.getArea());
                        roomList.add(r);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar habitaciones", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }
    private void showNotification(String title, String message) {
        String CHANNEL_ID = "habitaciones_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal Habitaciones",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotroid_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(1, builder.build());
    }


}