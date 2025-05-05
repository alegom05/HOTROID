package com.example.hotroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.hotroid.databinding.UserEditAccountBinding;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;



public class EditAccountUser extends AppCompatActivity {
    // Variables de clase para Calendar y SimpleDateFormat
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    // Variable para el view binding
    private UserEditAccountBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Inicializar las variables Calendar y SimpleDateFormat
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "ES"));

        // Inicializar el binding
        binding = UserEditAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar la UI inicial
        setupUI();

        // Configurar los listeners
        setupListeners();
    }

    private void setupUI() {
        // Configurar la interfaz inicial con datos de ejemplo
        binding.etNombreCompleto.setText("Jean Paul Quispe");
        binding.etTelefono.setText("999 299 299");
        binding.etCorreo.setText("jp@gmail.com");
        binding.etNacimiento.setText("17/07/2000");
        binding.etDni.setText("43612945");
    }

    private void setupListeners() {
        // Listener para el campo de fecha de nacimiento
        binding.etNacimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
        // Listener para el botón de guardar
        binding.btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfileData();
            }
        });
        // Listener para editar la imagen de perfil
        /*binding.fabEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implementar lógica para cambiar imagen de perfil
                Toast.makeText(EditAccountUser.this, "Cambiar imagen de perfil", Toast.LENGTH_SHORT).show();
            }
        });*/

        // Configurar navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_hoteles_user) {
                Toast.makeText(EditAccountUser.this, "Hoteles", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_reservas_user) {
                Toast.makeText(EditAccountUser.this, "Reservas", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_chat_user) {
                Toast.makeText(EditAccountUser.this, "Chat", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_cuenta) {
                // Ya estamos en cuenta
                return true;
            }
            return false;
        });

        /*ya luego modificar para usar una clase utilitaria que repita el uso de meú en todas las vistas necesarias y su uso sea común
        public class NavigationUtils {
            public static void setupBottomNavigation(BottomNavigationView bottomNav, Context context, int currentItemId) {
                bottomNav.setOnItemSelectedListener(item ->
         */
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    binding.etNacimiento.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveProfileData() {
        // Validar datos
        String nombre = binding.etNombreCompleto.getText().toString().trim();
        String telefono = binding.etTelefono.getText().toString().trim();
        String correo = binding.etCorreo.getText().toString().trim();

        if (nombre.isEmpty() || telefono.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de correo
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.setError("Formato de correo inválido");
            return;
        } else {
            binding.tilCorreo.setError(null);
        }

        // Aquí implementarías la lógica para guardar los datos en tu base de datos
        // Por ahora, solo mostraremos un mensaje de éxito
        Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
    }
}