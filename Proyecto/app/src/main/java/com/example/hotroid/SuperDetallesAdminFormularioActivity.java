package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ImageView; // Asegúrate de que este import esté presente

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SuperDetallesAdminFormularioActivity extends AppCompatActivity {

    private EditText etFechaNacimiento;
    private EditText etNombre;
    private EditText etApellido;
    private Spinner spTipoDocumento;
    private EditText etNumDocumento;
    private EditText etCorreo;
    private EditText etTelefono;
    private EditText etDireccion;
    private Spinner spHotel;
    private ImageView ivFotoPerfil; // Para manejar la imagen de perfil (si existe en tu layout)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin_formulario); // Asegúrate de que este layout exista

        // Referencias a los elementos de la UI
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        spTipoDocumento = findViewById(R.id.spTipoDocumento);
        etNumDocumento = findViewById(R.id.etNumDocumento);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        spHotel = findViewById(R.id.spHotel);
        // ivFotoPerfil = findViewById(R.id.ivFotoPerfil); // Descomentar si tienes un ImageView con este ID para la foto de perfil en el formulario

        // Spinner Tipo de Documento
        List<String> tiposDocumento = Arrays.asList("DNI", "Pasaporte", "Carnet de Extranjería");
        ArrayAdapter<String> adapterDocumento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDocumento);
        adapterDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoDocumento.setAdapter(adapterDocumento);

        // Spinner de Hoteles
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

        // Manejo de la fecha de nacimiento (DatePickerDialog)
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        // Botón de Registrar
        Button btnRegistrar = findViewById(R.id.btnRegistrar); // Asegúrate de que este ID exista en tu layout
        btnRegistrar.setOnClickListener(v -> {
            // Validar y recopilar datos
            if (validarCampos()) {
                String nombre = etNombre.getText().toString().trim();
                String apellido = etApellido.getText().toString().trim();
                String tipoDocumento = spTipoDocumento.getSelectedItem().toString();
                String numDocumento = etNumDocumento.getText().toString().trim();
                String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
                String correo = etCorreo.getText().toString().trim();
                String telefono = etTelefono.getText().toString().trim();
                String direccion = etDireccion.getText().toString().trim();
                String hotelAsignado = spHotel.getSelectedItem().toString();
                String nombreCompleto = nombre + " " + apellido; // Crear el "Usuario" como lo tienes en SuperListaAdminActivity

                // Crear un array de String con todos los datos
                // Asegúrate de que el orden y la cantidad de campos coincidan con lo que esperas en SuperListaAdminActivity
                // Esto permite pasar todos los datos para una futura visualización en detalles, incluso si la lista principal
                // solo muestra un subconjunto de ellos.
                String[] nuevoAdminData = new String[]{
                        nombreCompleto,   // 0: Nombre Completo (para mostrar en la lista)
                        nombre,           // 1: Nombres
                        apellido,         // 2: Apellidos
                        tipoDocumento,    // 3: Tipo de Documento
                        numDocumento,     // 4: Número de Documento
                        fechaNacimiento,  // 5: Fecha de Nacimiento
                        correo,           // 6: Correo Electrónico
                        telefono,         // 7: Teléfono
                        direccion,        // 8: Dirección
                        hotelAsignado     // 9: Hotel Asignado
                };

                // Enviar los datos de vuelta a SuperListaAdminActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "registrado");
                resultIntent.putExtra("nuevo_admin_data", nuevoAdminData); // Pasa el array completo de datos
                setResult(RESULT_OK, resultIntent);
                finish(); // Cierra esta actividad y regresa a la anterior
            }
        });

        // Configuración del CardView de perfil (el de "Pedro Bustamante")
        CardView cardPerfil = findViewById(R.id.cardPerfil); // Asegúrate de que este ID exista en tu layout
        cardPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesAdminFormularioActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        // BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperActivity.class));
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperUsuariosActivity.class));
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperEventosActivity.class));
                return true;
            }
            return false;
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formatear la fecha a un string deseado, ej. "DD/MM/YYYY"
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
            etFechaNacimiento.setText(fecha);
        }, año, mes, día);
        datePicker.show();
    }

    // Validación básica de campos del formulario
    private boolean validarCampos() {
        boolean isValid = true;
        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etApellido.getText().toString().trim().isEmpty()) {
            etApellido.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etNumDocumento.getText().toString().trim().isEmpty()) {
            etNumDocumento.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etFechaNacimiento.getText().toString().trim().isEmpty()) {
            etFechaNacimiento.setError("Seleccione una fecha");
            isValid = false;
        }
        if (etCorreo.getText().toString().trim().isEmpty()) {
            etCorreo.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etTelefono.getText().toString().trim().isEmpty()) {
            etTelefono.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etDireccion.getText().toString().trim().isEmpty()) {
            etDireccion.setError("Este campo es obligatorio");
            isValid = false;
        }
        // Puedes añadir más validaciones según sea necesario (formato de correo, número de teléfono, etc.)
        return isValid;
    }
}