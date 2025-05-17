package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
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
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        // Inicializar vistas
        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        Button btnLimpiarFiltro = findViewById(R.id.btnLimpiarFiltro);
        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        // Inicializar la lista de eventos (aquí irían tus datos reales)
        listaEventos = new ArrayList<>();
        listaEventos.add(new Evento("15/5/2025", "Corte de energía en la torre A", "El Mirador"));
        listaEventos.add(new Evento("11/5/2025", "Caída de objeto en pasillo del restaurante", "Las Dunas"));
        listaEventos.add(new Evento("29/4/2025", "Fuga de agua en cuarto 210", "Costa del Mar"));

        // Inicializar y configurar el adaptador
        adapter = new EventoAdapter(this, listaEventos, evento -> SuperDetalleEvento(evento.getEvento()));
        recyclerEventos.setAdapter(adapter);

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                return true; // Ya estamos en esta actividad
            }
            return false;
        });

        // Configurar el selector de fecha
        etFiltroFecha.setOnClickListener(v -> mostrarDatePicker());

        // Configurar el botón limpiar filtro
        btnLimpiarFiltro.setOnClickListener(v -> {
            etFiltroFecha.setText("");
            filtrarEventosPorFecha(""); // Mostrar todos al limpiar
        });

        // Configurar click en la card del administrador
        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
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
            filtrarEventosPorFecha(fechaSeleccionada);
        }, año, mes, día);
        datePicker.show();
    }

    private void filtrarEventosPorFecha(String fechaFiltro) {
        List<Evento> listaFiltrada = new ArrayList<>();
        for (Evento evento : listaEventos) {
            if (evento.getFecha().equals(fechaFiltro) || fechaFiltro.isEmpty()) {
                listaFiltrada.add(evento);
            }
        }
        adapter.actualizarLista(listaFiltrada);
        if (listaFiltrada.isEmpty() && !fechaFiltro.isEmpty()) {
            Toast.makeText(this, "No hay eventos para esta fecha", Toast.LENGTH_SHORT).show();
        }
    }

    private void SuperDetalleEvento(String tituloEvento) {
        Intent intent = new Intent(this, SuperDetallesEventosActivity.class);
        intent.putExtra("titulo_evento", tituloEvento);
        startActivity(intent);
    }
}