package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.hotroid.bean.Reserva;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperReservasActivity extends AppCompatActivity {

    private TextView tvHotelNombre;
    private LinearLayout llReservasContainer;
    private EditText etSearchUser;
    private Button btnLimpiar;

    private List<Reserva> allReservas; // Todas las reservas del hotel
    private List<Reserva> filteredReservas; // Reservas filtradas por búsqueda
    private FirebaseFirestore db;
    private String selectedHotelId;
    private String selectedHotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_reservas_hotel);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        allReservas = new ArrayList<>();
        filteredReservas = new ArrayList<>();

        // Obtener datos del hotel del Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("hotel_id")) {
            selectedHotelId = intent.getStringExtra("hotel_id");
            selectedHotelName = intent.getStringExtra("hotel_name");
        } else {
            Toast.makeText(this, "Error: No se recibió información del hotel", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Inicializar vistas
        tvHotelNombre = findViewById(R.id.tvHotelNombre);
        llReservasContainer = findViewById(R.id.llReservasContainer);
        etSearchUser = findViewById(R.id.etSearchUser);
        btnLimpiar = findViewById(R.id.btnClearSearch);

        tvHotelNombre.setText("Reservas para " + selectedHotelName);

        // Configurar buscador
        setupSearch();

        // Cargar reservas
        loadReservasFromFirestore();

        // Configurar navegación
        setupBottomNavigation();
        setupCardSuper();
    }

    private void loadReservasFromFirestore() {
        db.collection("reservas")
                .whereEqualTo("idHotel", selectedHotelId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allReservas.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Reserva reserva = document.toObject(Reserva.class);
                            reserva.setIdReserva(document.getId());
                            allReservas.add(reserva);
                        }
                        filteredReservas = new ArrayList<>(allReservas);
                        displayReservas(filteredReservas);
                    } else {
                        Log.e("SuperReservas", "Error al cargar reservas", task.getException());
                        Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupSearch() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReservas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimpiar.setOnClickListener(v -> {
            etSearchUser.setText("");
            filterReservas("");
        });
    }

    private void filterReservas(String searchText) {
        filteredReservas.clear();

        if (searchText.isEmpty()) {
            filteredReservas.addAll(allReservas);
        } else {
            String searchLower = searchText.toLowerCase(Locale.getDefault());
            for (Reserva reserva : allReservas) {
                if (reserva.getNombresCliente().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        reserva.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(searchLower) ||
                        reserva.getRoomNumber().toLowerCase(Locale.getDefault()).contains(searchLower)) {
                    filteredReservas.add(reserva);
                }
            }
        }
        displayReservas(filteredReservas);
    }

    private void displayReservas(List<Reserva> reservas) {
        llReservasContainer.removeAllViews();

        if (reservas.isEmpty()) {
            TextView noResults = new TextView(this);
            noResults.setText("No se encontraron reservas");
            noResults.setTextSize(16);
            noResults.setGravity(android.view.Gravity.CENTER);
            llReservasContainer.addView(noResults);
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Reserva reserva : reservas) {
            View itemView = inflater.inflate(R.layout.item_reserva_card, llReservasContainer, false);

            TextView tvCliente = itemView.findViewById(R.id.tvClienteCard);
            TextView tvFechas = itemView.findViewById(R.id.tvFechaReservaCard);
            TextView tvHabitacion = itemView.findViewById(R.id.tvHabitacionCard);
            TextView tvPrecio = itemView.findViewById(R.id.tvPrecioTotalCard);

            tvCliente.setText(reserva.getNombresCliente() + " " + reserva.getApellidosCliente());
            tvFechas.setText("Estancia: " + dateFormat.format(reserva.getFechaInicio()) + " - " + dateFormat.format(reserva.getFechaFin()));
            tvHabitacion.setText(String.format(Locale.getDefault(),
                    "Habitaciones: %d | Adultos: %d | Niños: %d | Hab. Nro: %s",
                    reserva.getHabitaciones(), reserva.getAdultos(), reserva.getNinos(), reserva.getRoomNumber()));
            tvPrecio.setText(String.format(Locale.getDefault(), "Total: S/ %.2f", reserva.getPrecioTotal()));

            llReservasContainer.addView(itemView);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperListaClientesActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(this, SuperEventosActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupCardSuper() {
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
        });
    }
}