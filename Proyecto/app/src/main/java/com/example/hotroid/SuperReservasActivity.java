package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.hotroid.bean.Reserva;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperReservasActivity extends AppCompatActivity {

    private TextView tvHotelNombre;
    private LinearLayout llReservasContainer;
    private EditText etSearchUser;
    private Button btnLimpiar;
    private Spinner spinnerAdults, spinnerChildren, spinnerRooms;
    private Button btnPickStartDate, btnPickEndDate;

    private List<Reserva> allReservas; // Todas las reservas del hotel
    private List<Reserva> filteredReservas; // Reservas filtradas por búsqueda
    private FirebaseFirestore db;
    private String selectedHotelId;
    private String selectedHotelName;

    private Date selectedStartDate;
    private Date selectedEndDate;
    private int selectedAdults = 0; // 0 for "Any"
    private int selectedChildren = 0; // 0 for "Any"
    private int selectedRooms = 0; // 0 for "Any"

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
        spinnerAdults = findViewById(R.id.spinnerAdults);
        spinnerChildren = findViewById(R.id.spinnerChildren);
        spinnerRooms = findViewById(R.id.spinnerRooms);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);

        tvHotelNombre.setText("Reservas para " + selectedHotelName);

        // Configurar Spinners
        setupSpinners();
        // Configurar Date Pickers
        setupDatePickers();
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
                        // Apply filters after loading all reservations
                        applyAllFilters();
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
                applyAllFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimpiar.setOnClickListener(v -> {
            etSearchUser.setText("");
            spinnerAdults.setSelection(0);
            spinnerChildren.setSelection(0);
            spinnerRooms.setSelection(0);
            selectedStartDate = null;
            selectedEndDate = null;
            btnPickStartDate.setText("Fecha Inicio");
            btnPickEndDate.setText("Fecha Fin");
            applyAllFilters();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.number_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAdults.setAdapter(adapter);
        spinnerChildren.setAdapter(adapter);
        spinnerRooms.setAdapter(adapter);

        spinnerAdults.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAdults = position; // 0 for "Any", 1-10 for numbers
                applyAllFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerChildren.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChildren = position; // 0 for "Any", 1-10 for numbers
                applyAllFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerRooms.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRooms = position; // 0 for "Any", 1-10 for numbers
                applyAllFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupDatePickers() {
        Calendar calendar = Calendar.getInstance();

        btnPickStartDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SuperReservasActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year1, monthOfYear, dayOfMonth);
                        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCal.set(Calendar.MINUTE, 0);
                        selectedCal.set(Calendar.SECOND, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);
                        selectedStartDate = selectedCal.getTime();
                        btnPickStartDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedStartDate));
                        applyAllFilters();
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(SuperReservasActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        Calendar selectedCal = Calendar.getInstance();
                        selectedCal.set(year1, monthOfYear, dayOfMonth);
                        selectedCal.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCal.set(Calendar.MINUTE, 59);
                        selectedCal.set(Calendar.SECOND, 59);
                        selectedCal.set(Calendar.MILLISECOND, 999);
                        selectedEndDate = selectedCal.getTime();
                        btnPickEndDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedEndDate));
                        applyAllFilters();
                    }, year, month, day);
            datePickerDialog.show();
        });
    }

    private void applyAllFilters() {
        filteredReservas.clear();
        String searchText = etSearchUser.getText().toString().toLowerCase(Locale.getDefault());

        for (Reserva reserva : allReservas) {
            boolean matchesSearch = true;
            boolean matchesAdults = true;
            boolean matchesChildren = true;
            boolean matchesRooms = true;
            boolean matchesDateRange = true;

            // Search by name or room number
            if (!searchText.isEmpty()) {
                matchesSearch = reserva.getNombresCliente().toLowerCase(Locale.getDefault()).contains(searchText) ||
                        reserva.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(searchText) ||
                        reserva.getRoomNumber().toLowerCase(Locale.getDefault()).contains(searchText);
            }

            // Filter by adults
            if (selectedAdults > 0) {
                matchesAdults = reserva.getAdultos() == selectedAdults;
            }

            // Filter by children
            if (selectedChildren > 0) {
                matchesChildren = reserva.getNinos() == selectedChildren;
            }

            // Filter by rooms
            if (selectedRooms > 0) {
                matchesRooms = reserva.getHabitaciones() == selectedRooms;
            }

            // Filter by date range
            if (selectedStartDate != null && selectedEndDate != null) {
                matchesDateRange = !reserva.getFechaInicio().after(selectedEndDate) &&
                        !reserva.getFechaFin().before(selectedStartDate);
            } else if (selectedStartDate != null) {
                matchesDateRange = !reserva.getFechaFin().before(selectedStartDate);
            } else if (selectedEndDate != null) {
                matchesDateRange = !reserva.getFechaInicio().after(selectedEndDate);
            }


            if (matchesSearch && matchesAdults && matchesChildren && matchesRooms && matchesDateRange) {
                filteredReservas.add(reserva);
            }
        }
        displayReservas(filteredReservas);
    }


    private void displayReservas(List<Reserva> reservas) {
        llReservasContainer.removeAllViews();

        if (reservas.isEmpty()) {
            TextView noResults = new TextView(this);
            noResults.setText("No se encontraron reservas con los filtros aplicados.");
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