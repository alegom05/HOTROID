package com.example.hotroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class InfoAccountUser extends AppCompatActivity {

    private TextView tvNombre, tvCorreo, tvTelefono, tvDni, tvNacimiento;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_info_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Referencias
        tvNombre = findViewById(R.id.tv_full_name);
        tvCorreo = findViewById(R.id.tv_email);
        tvTelefono = findViewById(R.id.tv_phone);
        tvDni = findViewById(R.id.tv_dni);
        tvNacimiento = findViewById(R.id.tv_birth_date);

        MaterialButton btnGuardar = findViewById(R.id.btn_save_changes);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();

        cargarDatosUsuario();

        // Listeners para editar campos
        findViewById(R.id.card_full_name).setOnClickListener(v -> mostrarDialogoEditar("Nombre Completo", tvNombre));
        findViewById(R.id.card_email).setOnClickListener(v -> mostrarDialogoEditar("Correo Electrónico", tvCorreo));
        findViewById(R.id.card_phone).setOnClickListener(v -> mostrarDialogoEditar("Teléfono", tvTelefono));
        findViewById(R.id.card_dni).setOnClickListener(v -> mostrarDialogoEditar("DNI", tvDni));
        findViewById(R.id.card_birth_date).setOnClickListener(v -> mostrarDialogoEditar("Fecha de Nacimiento", tvNacimiento));

        btnGuardar.setOnClickListener(v -> guardarCambios());

    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(uid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvNombre.setText(obtenerValor(documentSnapshot, "nombre") + " " + obtenerValor(documentSnapshot, "apellido"));
                        tvCorreo.setText(obtenerValor(documentSnapshot, "correo"));
                        tvTelefono.setText(obtenerValor(documentSnapshot, "telefono"));
                        tvDni.setText(obtenerValor(documentSnapshot, "dni"));
                        tvNacimiento.setText(obtenerValor(documentSnapshot, "fechaNacimiento"));
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show());
    }

    private String obtenerValor(DocumentSnapshot doc, String key) {
        String valor = doc.getString(key);
        return (valor == null || valor.trim().isEmpty()) ? "" : valor.trim();
    }

    private void mostrarDialogoEditar(String titulo, TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar " + titulo);

        final EditText input = new EditText(this);
        input.setText(textView.getText());
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            textView.setText(input.getText().toString().trim());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void guardarCambios() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", obtenerTextoLimpio(tvNombre.getText().toString().split(" ")[0]));
        datos.put("apellido", obtenerTextoLimpio(tvNombre.getText().toString().split(" ").length > 1 ? tvNombre.getText().toString().split(" ")[1] : ""));
        datos.put("correo", obtenerTextoLimpio(tvCorreo.getText().toString()));
        datos.put("telefono", obtenerTextoLimpio(tvTelefono.getText().toString()));
        datos.put("dni", obtenerTextoLimpio(tvDni.getText().toString()));
        datos.put("fechaNacimiento", obtenerTextoLimpio(tvNacimiento.getText().toString()));

        db.collection("usuarios").document(uid)
                .update(datos)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private String obtenerTextoLimpio(String texto) {
        return (texto == null) ? "" : texto.trim();
    }
}