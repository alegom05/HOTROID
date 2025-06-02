package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etFiltroFecha;
    private EditText etBuscador;
    private Button btnLimpiarGeneralSearch; // Renamed for clarity to avoid conflict
    private Button btnLimpiarFiltroFecha; // New button for date filter
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos;
    private List<Evento> filteredEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        btnLimpiarFiltroFecha = findViewById(R.id.btnLimpiarFiltroFecha); // Correct ID for date filter clear button

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiarGeneralSearch = findViewById(R.id.btnLimpiar); // Correct ID for general search clear button

        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        listaEventos = new ArrayList<>();
        // Now, the 'evento' field will just contain the description,
        // and 'hotel' will contain the hotel name. The adapter will combine them.
        listaEventos.add(new Evento("15/5/2025", "Corte de energía en la torre A", "Oro Verde"));
        listaEventos.add(new Evento("11/5/2025", "Caída de objeto en pasillo del restaurante", "Las Dunas"));
        listaEventos.add(new Evento("29/4/2025", "Fuga de agua en cuarto 210", "Costa del Mar"));
        listaEventos.add(new Evento("10/5/2025", "Incendio menor en cocina", "Sauce Resort"));
        listaEventos.add(new Evento("15/5/2025", "Problemas de internet en el lobby", "Oro Verde"));


        filteredEventList = new ArrayList<>(listaEventos);

        adapter = new EventoAdapter(this, filteredEventList, evento -> SuperDetalleEvento(evento.getEvento() + " en el hotel " + evento.getHotel())); // Pass the combined string to detail
        recyclerEventos.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                return true;
            }
            return false;
        });

        etFiltroFecha.setOnClickListener(v -> mostrarDatePicker());

        // Assign the click listener to the *correct* button for clearing date filter
        btnLimpiarFiltroFecha.setOnClickListener(v -> {
            etFiltroFecha.setText("");
            applyFilters();
        });

        setupGeneralSearchFunctionality();

        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
        });
    }

    private void setupGeneralSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Assign the click listener to the *correct* button for clearing general search
        btnLimpiarGeneralSearch.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            applyFilters();
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            etFiltroFecha.setText(fechaSeleccionada);
            applyFilters();
        }, año, mes, día);
        datePicker.show();
    }

    private void applyFilters() {
        filteredEventList.clear();
        String fechaFiltro = etFiltroFecha.getText().toString().trim();
        String searchText = etBuscador.getText().toString().toLowerCase().trim();

        for (Evento evento : listaEventos) {
            boolean matchesDate = fechaFiltro.isEmpty() || evento.getFecha().equals(fechaFiltro);
            // Search in both event description and hotel name
            // The combined string for search is currentEvento.getEvento() + " en el hotel " + currentEvento.getHotel()
            String combinedTextForSearch = evento.getEvento() + " en el hotel " + evento.getHotel();

            boolean matchesSearch = searchText.isEmpty() ||
                    combinedTextForSearch.toLowerCase().contains(searchText);


            if (matchesDate && matchesSearch) {
                filteredEventList.add(evento);
            }
        }
        adapter.actualizarLista(filteredEventList);

        if (filteredEventList.isEmpty() && (!fechaFiltro.isEmpty() || !searchText.isEmpty())) {
            Toast.makeText(this, "No hay eventos que coincidan con los filtros", Toast.LENGTH_SHORT).show();
        }
    }

    private void SuperDetalleEvento(String tituloEvento) {
        Intent intent = new Intent(this, SuperDetallesEventosActivity.class);
        intent.putExtra("titulo_evento", tituloEvento); // Pass the combined string to the detail activity
        startActivity(intent);
    }

}