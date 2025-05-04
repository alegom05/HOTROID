package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class SuperDetallesAdminFormularioActivity extends AppCompatActivity {
    private EditText etFechaNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin_formulario);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Spinner Tipo de Documento
        Spinner spTipoDocumento = findViewById(R.id.spTipoDocumento);
        List<String> tiposDocumento = Arrays.asList("DNI", "Pasaporte", "Carnet de Extranjería");
        ArrayAdapter<String> adapterDocumento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDocumento);
        adapterDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoDocumento.setAdapter(adapterDocumento);

        // Spinner de Hoteles
        Spinner spHotel = findViewById(R.id.spHotel);
        List<String> hoteles = Arrays.asList(
                "Hotel Miraflores Park - Lima",
                "JW Marriott - Lima",
                "Hotel Libertador - Arequipa",
                "Casa Andina Premium - Cusco",
                "Hotel Paracas - Paracas",
                "Costa del Sol Wyndham - Trujillo",
                "Inkaterra Machu Picchu Pueblo - Cusco",
                "Hotel Novotel - Cusco",
                "Sonesta Hotel - El Olivar, Lima",
                "Hotel Taypikala - Puno"
        );
        ArrayAdapter<String> adapterHoteles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoteles);
        adapterHoteles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHotel.setAdapter(adapterHoteles);

        // Manejo de la fecha de nacimiento
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());


        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperDetallesAdminFormularioActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesAdminFormularioActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperDetallesAdminFormularioActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesAdminFormularioActivity.this, SuperListaAdminActivity.class);
            startActivity(intent);
        });

        CardView cardSuper2 = findViewById(R.id.cardPerfil);
        cardSuper2.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesAdminFormularioActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = dayOfMonth + "/" + (month + 1) + "/" + year;
            etFechaNacimiento.setText(fecha);
        }, año, mes, día);
        datePicker.show();
    }
}