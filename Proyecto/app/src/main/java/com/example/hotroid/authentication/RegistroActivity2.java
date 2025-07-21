package com.example.hotroid.authentication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat; // Import for NotificationCompat
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.ClienteActivity;
import com.example.hotroid.R;
// import com.example.hotroid.TaxiActivity; // No longer directly redirecting here for Taxista
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

    private String nombre, apellido, dni, correo, idRol;

    // Notification Channel ID
    private static final String CHANNEL_ID = "registration_channel";
    private static final int NOTIFICATION_ID = 1;

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
        idRol = getIntent().getStringExtra("idRol");

        // Mostrar resumen
        binding.tvUserInfo.setText(nombre + " " + apellido + " • DNI: " + dni + " • " + correo + " • Rol: " + idRol);

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
                        personaMap.put("idRol", idRol);

                        firestore.collection("usuarios").document(user.getUid())
                                .set(personaMap)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                                    // *** LÓGICA DE REDIRECCIÓN CONDICIONAL Y NOTIFICACIÓN ***
                                    if ("Cliente".equals(idRol)) {
                                        startActivity(new Intent(this, ClienteActivity.class));
                                        finishAffinity();
                                    } else if ("Taxista".equals(idRol)) {
                                        // Specific logic for Taxista: Show message and redirect to LoginActivity
                                        String message = "Su solicitud ha sido enviada al administrador del sistema.";
                                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                                        sendRegistrationNotification(message); // Send the notification

                                        // Redirect to LoginActivity and clear back stack
                                        Intent intent = new Intent(RegistroActivity2.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra("from_registration", true); // <--- ADD THIS LINE
                                        startActivity(intent);
                                    } else {
                                        // This is a fallback case if the role is neither "Cliente" nor "Taxista"
                                        Toast.makeText(this, "Rol desconocido, redireccionando a inicio.", Toast.LENGTH_SHORT).show();
                                        // You can decide where to go by default, e.g., to ClienteActivity or LoginActivity
                                        startActivity(new Intent(this, LoginActivity.class)); // Redirect to Login as default unknown
                                        finishAffinity();
                                    }
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
            Toast.makeText(this, "La contraseña no cumple con los requisitos: mínimo 8 caracteres, al menos una mayúscula y un número.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Creates and displays a notification.
     * @param message The message to display in the notification.
     */
    private void sendRegistrationNotification(String message) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Registration Status";
            String description = "Notifications for user registration status.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You need to have an icon in your drawables
                .setContentTitle("Registro de Taxista")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Automatically closes the notification when the user taps it

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}