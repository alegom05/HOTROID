package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etFiltroFecha;
    private LinearLayout evento1, evento2, evento3;
    private TextView tvFechaEvento1, tvFechaEvento2, tvFechaEvento3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        // Inicializar vistas
        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        Button btnLimpiarFiltro = findViewById(R.id.btnLimpiarFiltro);
        evento1 = findViewById(R.id.evento1);
        evento2 = findViewById(R.id.evento2);
        evento3 = findViewById(R.id.evento3);
        tvFechaEvento1 = findViewById(R.id.tvFechaEvento1);
        tvFechaEvento2 = findViewById(R.id.tvFechaEvento2);
        tvFechaEvento3 = findViewById(R.id.tvFechaEvento3);
        CardView cardSuper = findViewById(R.id.cardSuper);

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
            mostrarTodosLosEventos();
        });

        // Configurar clicks en los eventos
        evento1.setOnClickListener(v -> abrirDetalleEvento("Corte de energía en la torre A"));
        evento2.setOnClickListener(v -> abrirDetalleEvento("Caída de objeto en pasillo del restaurante"));
        evento3.setOnClickListener(v -> abrirDetalleEvento("Fuga de agua en cuarto 210"));

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

    private void filtrarEventosPorFecha(String fechaSeleccionada) {
        // Obtener las fechas de cada evento
        String fechaEvento1 = tvFechaEvento1.getText().toString();
        String fechaEvento2 = tvFechaEvento2.getText().toString();
        String fechaEvento3 = tvFechaEvento3.getText().toString();

        // Comparar fechas y mostrar/ocultar eventos
        evento1.setVisibility(fechaEvento1.equals(fechaSeleccionada) ? View.VISIBLE : View.GONE);
        evento2.setVisibility(fechaEvento2.equals(fechaSeleccionada) ? View.VISIBLE : View.GONE);
        evento3.setVisibility(fechaEvento3.equals(fechaSeleccionada) ? View.VISIBLE : View.GONE);

        // Mostrar mensaje si no hay eventos para la fecha seleccionada
        if (evento1.getVisibility() != View.VISIBLE &&
                evento2.getVisibility() != View.VISIBLE &&
                evento3.getVisibility() != View.VISIBLE) {
            Toast.makeText(this, "No hay eventos para esta fecha", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarTodosLosEventos() {
        evento1.setVisibility(View.VISIBLE);
        evento2.setVisibility(View.VISIBLE);
        evento3.setVisibility(View.VISIBLE);
    }

    private void abrirDetalleEvento(String tituloEvento) {
        Intent intent = new Intent(this, SuperDetallesEventosActivity.class);
        intent.putExtra("titulo_evento", tituloEvento);
        startActivity(intent);
    }
}