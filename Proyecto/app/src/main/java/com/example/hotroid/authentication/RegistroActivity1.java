package com.example.hotroid.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter; // Import for ArrayAdapter
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.R;
import com.example.hotroid.databinding.ActivityRegistro1Binding;

public class RegistroActivity1 extends AppCompatActivity {

    private ActivityRegistro1Binding binding;
    private String selectedRole; // To store the selected role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistro1Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup the Role Spinner
        String[] roles = new String[]{"Cliente", "Taxista"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line, // Or use a custom layout if needed
                roles
        );
        binding.etRole.setAdapter(adapter);

        // Set a listener for when an item is selected in the dropdown
        binding.etRole.setOnItemClickListener((parent, view, position, id) -> {
            selectedRole = (String) parent.getItemAtPosition(position);
        });

        // Botón para retroceder
        binding.btnBack.setOnClickListener(v -> finish());

        // Botón CONTINUAR
        binding.btnContinue.setOnClickListener(v -> {
            String nombre = binding.etFirstName.getText().toString().trim();
            String apellido = binding.etLastName.getText().toString().trim();
            String dni = binding.etDni.getText().toString().trim();
            String correo = binding.etEmail.getText().toString().trim();

            // Validate that a role has been selected
            if (selectedRole == null || selectedRole.isEmpty()) {
                Toast.makeText(this, "Por favor, selecciona tu rol", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validarCampos(nombre, apellido, dni, correo)) return;

            // Pasar al segundo paso con los datos
            Intent intent = new Intent(RegistroActivity1.this, RegistroActivity2.class);
            intent.putExtra("nombre", nombre);
            intent.putExtra("apellido", apellido);
            intent.putExtra("dni", dni);
            intent.putExtra("correo", correo);
            intent.putExtra("idRol", selectedRole); // Pass the selected role
            startActivity(intent);
        });
    }

    private boolean validarCampos(String nombre, String apellido, String dni, String correo) {
        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || correo.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Modified DNI validation to allow 8 or 9 digits
        if (dni.length() != 8 && dni.length() != 9) {
            Toast.makeText(this, "El DNI debe tener 8 o 9 dígitos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!TextUtils.isDigitsOnly(dni)) {
            Toast.makeText(this, "El DNI debe contener solo números", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}