package com.example.hotroid;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Evento;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etBuscador;
    private Button btnLimpiar, btnPickStartDate, btnPickEndDate;
    private Spinner spinnerHotels;
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos;
    private List<Evento> filteredEventList;
    private FirebaseFirestore db;
    private Date selectedStartDate, selectedEndDate;
    private String selectedHotel;
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        // Inicialización de Firebase
        db = FirebaseFirestore.getInstance();

        // Inicialización de vistas
        initViews();

        // Configuración del RecyclerView
        setupRecyclerView();

        // Cargar eventos desde Firestore
        loadEventsFromFirestore();

        // Configuración de listeners
        setupListeners();

        // Configuración del BottomNavigationView
        setupBottomNavigation();

        // Configuración del CardView de perfil
        setupProfileCard();
    }

    private void initViews() {
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnFiltrar);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        spinnerHotels = findViewById(R.id.spinnerHotels);
        recyclerEventos = findViewById(R.id.recyclerEventos);
    }

    private void setupRecyclerView() {
        listaEventos = new ArrayList<>();
        filteredEventList = new ArrayList<>();
        adapter = new EventoAdapter(this, filteredEventList, this::onEventoClick);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));
        recyclerEventos.setAdapter(adapter);
    }

    private void loadEventsFromFirestore() {
        db.collection("eventos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaEventos.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Evento evento = document.toObject(Evento.class);
                                evento.setId(document.getId());
                                listaEventos.add(evento);
                                Log.d("FirestoreData", "Evento cargado: " + evento.getEvento());
                            } catch (Exception e) {
                                Log.e("FirestoreError", "Error al parsear evento", e);
                            }
                        }
                        applyFilters();
                    } else {
                        Log.e("FirestoreError", "Error al cargar eventos", task.getException());
                        Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupListeners() {
        // Listener para el botón de limpiar
        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            selectedStartDate = null;
            selectedEndDate = null;
            btnPickStartDate.setText("Fecha Inicio");
            btnPickEndDate.setText("Fecha Fin");
            spinnerHotels.setSelection(0);
            applyFilters();
        });

        // Listener para el buscador
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                applyFilters();
            }
        });

        // Listeners para los selectores de fecha
        btnPickStartDate.setOnClickListener(v -> showDateTimePicker(true));
        btnPickEndDate.setOnClickListener(v -> showDateTimePicker(false));

        // Configuración del Spinner de hoteles
        setupHotelSpinner();
    }

    private void showDateTimePicker(final boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(year, month, day);
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hour, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hour);
                                calendar.set(Calendar.MINUTE, minute);
                                Date selectedDate = calendar.getTime();

                                if (isStartDate) {
                                    selectedStartDate = selectedDate;
                                    btnPickStartDate.setText(dateTimeFormat.format(selectedStartDate));
                                } else {
                                    selectedEndDate = selectedDate;
                                    btnPickEndDate.setText(dateTimeFormat.format(selectedEndDate));
                                }
                                applyFilters();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupHotelSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hotel_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHotels.setAdapter(adapter);

        spinnerHotels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHotel = position == 0 ? null : parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHotel = null;
            }
        });
    }

    private void applyFilters() {
        filteredEventList.clear();
        String searchText = etBuscador.getText().toString().toLowerCase(Locale.getDefault()).trim();

        for (Evento evento : listaEventos) {
            boolean matchesSearch = searchText.isEmpty() ||
                    evento.getEvento().toLowerCase().contains(searchText) ||
                    (evento.getDescripcion() != null && evento.getDescripcion().toLowerCase().contains(searchText)) ||
                    evento.getHotel().toLowerCase().contains(searchText);

            boolean matchesDateRange = true;
            if (selectedStartDate != null || selectedEndDate != null) {
                Date eventDate = evento.getFechaHora().toDate();

                if (selectedStartDate != null && selectedEndDate != null) {
                    matchesDateRange = !eventDate.before(selectedStartDate) && !eventDate.after(selectedEndDate);
                } else if (selectedStartDate != null) {
                    matchesDateRange = !eventDate.before(selectedStartDate);
                } else if (selectedEndDate != null) {
                    matchesDateRange = !eventDate.after(selectedEndDate);
                }
            }

            boolean matchesHotel = selectedHotel == null ||
                    evento.getHotel().equalsIgnoreCase(selectedHotel);

            if (matchesSearch && matchesDateRange && matchesHotel) {
                filteredEventList.add(evento);
            }
        }

        adapter.actualizarLista(filteredEventList);

    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                return true;
            }
            return false;
        });
    }

    private void setupProfileCard() {
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
        });
    }

    private void onEventoClick(Evento evento) {
        Intent intent = new Intent(this, SuperDetallesEventosActivity.class);
        intent.putExtra("event_id", evento.getId());
        intent.putExtra("event_titulo", evento.getEvento());
        intent.putExtra("event_hotel", evento.getHotel());
        intent.putExtra("event_descripcion", evento.getDescripcion());

        // Formatear fecha para mostrar en detalles
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        intent.putExtra("event_fecha", sdf.format(evento.getFechaHora().toDate()));

        startActivity(intent);
    }
}