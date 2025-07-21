package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class AdminHabitacionDetallesActivity extends AppCompatActivity {
    private TextView tvRoomNumber, tvRoomType, tvCapacityAdults, tvCapacityChildren, tvArea, tvStatus; // Añadido tvStatus
    private Button btnEditarHabitacion;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_habitacion_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        tvRoomNumber = findViewById(R.id.tvRoomNumber);
        tvRoomType = findViewById(R.id.tvRoomType);
        tvCapacityAdults = findViewById(R.id.tvCapacityAdults);
        tvCapacityChildren = findViewById(R.id.tvCapacityChildren);
        tvArea = findViewById(R.id.tvArea);
        tvStatus = findViewById(R.id.tvStatus); // Inicializar tvStatus
        btnEditarHabitacion = findViewById(R.id.btnEditarHabitacion);

        roomId = getIntent().getStringExtra("ROOM_ID");
        int roomNumber = getIntent().getIntExtra("ROOM_NUMBER", 0);
        String roomType = getIntent().getStringExtra("ROOM_TYPE");
        int capacityAdults = getIntent().getIntExtra("CAPACITY_ADULTS", 0);
        int capacityChildren = getIntent().getIntExtra("CAPACITY_CHILDREN", 0);
        double area = getIntent().getDoubleExtra("AREA", 0.0);
        String status = getIntent().getStringExtra("STATUS"); // Recibir status

        tvRoomNumber.setText("Número de habitación: " + roomNumber);
        tvRoomType.setText("Tipo de habitación: " + roomType);
        tvCapacityAdults.setText("Capacidad Adultos: " + capacityAdults);
        tvCapacityChildren.setText("Capacidad Niños: " + capacityChildren);
        tvArea.setText(String.format(Locale.getDefault(), "Área: %.2f m²", area));
        String estadoFormateado = "Desconocido";
        if ("Available".equalsIgnoreCase(status)) {
            estadoFormateado = "Habilitada";
        } else if ("Unavailable".equalsIgnoreCase(status)) {
            estadoFormateado = "Deshabilitada";
        }
        tvStatus.setText("Estado: " + estadoFormateado);


        btnEditarHabitacion.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHabitacionDetallesActivity.this, AdminEditarHabitacionActivity.class);
            intent.putExtra("ROOM_ID", roomId);
            intent.putExtra("ROOM_NUMBER", roomNumber); // ya no es String, sino int
            intent.putExtra("ROOM_TYPE", roomType);
            intent.putExtra("CAPACITY_ADULTS", capacityAdults);
            intent.putExtra("CAPACITY_CHILDREN", capacityChildren);
            intent.putExtra("AREA", area);
            intent.putExtra("STATUS", status); // Pasar status a la actividad de edición
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentRegistros = new Intent(AdminHabitacionDetallesActivity.this, AdminActivity.class);
                    // Para evitar duplicados en la pila y volver al estado correcto
                    intentRegistros.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentRegistros);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminHabitacionDetallesActivity.this, AdminTaxistas.class);
                    intentUbicacion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    Intent intentCheckout = new Intent(AdminHabitacionDetallesActivity.this, AdminCheckout.class);
                    intentCheckout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCheckout);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentReportes = new Intent(AdminHabitacionDetallesActivity.this, AdminReportes.class);
                    intentReportes.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentReportes);
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}