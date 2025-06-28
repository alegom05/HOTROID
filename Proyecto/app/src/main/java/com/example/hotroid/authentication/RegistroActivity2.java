package com.example.hotroid.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.ClienteActivity;
import com.example.hotroid.R;
import com.example.hotroid.databinding.ActivityRegistro2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistroActivity2 extends AppCompatActivity {
    private ActivityRegistro2Binding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String nombre, apellido, dni, correo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistro2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Recibir datos del paso anterior
        nombre = getIntent().getStringExtra("nombre");
        apellido = getIntent().getStringExtra("apellido");
        dni = getIntent().getStringExtra("dni");
        correo = getIntent().getStringExtra("correo");

        // Mostrar resumen
        binding.tvUserInfo.setText(nombre + " " + apellido + " • DNI: " + dni + " • " + correo);

        // Botón retroceso
        binding.btnBack.setOnClickListener(v -> finish());

        // Crear cuenta
        binding.btnCreateAccount.setOnClickListener(v -> {
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (!validarPassword(password, confirmPassword)) return;

            auth.createUserWithEmailAndPassword(correo, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        Map<String, Object> personaMap = new HashMap<>();
                        personaMap.put("nombre", nombre);
                        personaMap.put("apellido", apellido);
                        personaMap.put("dni", dni);
                        personaMap.put("correo", correo);
                        personaMap.put("idRol", "Cliente");

                        firestore.collection("usuarios").document(user.getUid())
                                .set(personaMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, ClienteActivity.class));
                                    finishAffinity();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                } else {
                    Toast.makeText(this, "Error al crear usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private boolean validarPassword(String pass, String confirm) {
        if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (pass.length() < 8 || !pass.matches(".*[A-Z].*") || !pass.matches(".*[0-9].*")) {
            Toast.makeText(this, "La contraseña no cumple con los requisitos", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}