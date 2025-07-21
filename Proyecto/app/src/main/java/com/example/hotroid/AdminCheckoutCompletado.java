package com.example.hotroid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale; // <--- Asegúrate de tener esta importación

public class AdminCheckoutCompletado extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout_completado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        TextView tvMontoFinal = findViewById(R.id.tvMontoFinal);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        double monto = getIntent().getDoubleExtra("MONTO_TOTAL", 0.0);
        String cliente = getIntent().getStringExtra("CLIENT_NAME");
        String idCheckout = getIntent().getStringExtra("ID_CHECKOUT");

        tvMontoFinal.setText(String.format(Locale.getDefault(), "S/. %.2f", monto)); // Usar Locale.getDefault()

        btnFinalizar.setOnClickListener(v -> {
            if (idCheckout != null && !idCheckout.isEmpty()) {
                eliminarCheckoutDeFirestore(idCheckout, monto, cliente);
            } else {
                Toast.makeText(AdminCheckoutCompletado.this, "Error: No se pudo obtener el ID del checkout para eliminar.", Toast.LENGTH_LONG).show();
                navigateToAdminCheckout();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminCheckoutCompletado.this, AdminActivity.class);
                startActivity(intentInicio);
                finish();
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminCheckoutCompletado.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                finish();
                return true;
            } else if (itemId == R.id.nav_checkout) {
                navigateToAdminCheckout();
                return true;
            } else if (itemId == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminCheckoutCompletado.this, AdminReportes.class);
                startActivity(intentAlertas);
                finish();
                return true;
            } else {
                return false;
            }
        });
    }

    private void eliminarCheckoutDeFirestore(String idCheckout, double monto, String cliente) {
        db.collection("checkouts").document(idCheckout)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("AdminCheckoutCompletado", "Checkout eliminado de Firestore: " + idCheckout);
                            String mensaje = "Se cobró S/. " + String.format(Locale.getDefault(), "%.2f", monto) + " a " + cliente + " por su estadía.";
                            showNotification("Checkout completado", mensaje);
                            Toast.makeText(AdminCheckoutCompletado.this, "Checkout completado y eliminado.", Toast.LENGTH_SHORT).show();
                            navigateToAdminCheckout();
                        } else {
                            Log.w("AdminCheckoutCompletado", "Error al eliminar checkout de Firestore: ", task.getException());
                            Toast.makeText(AdminCheckoutCompletado.this, "Error al completar checkout: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            navigateToAdminCheckout();
                        }
                    }
                });
    }

    private void navigateToAdminCheckout() {
        Intent intent = new Intent(AdminCheckoutCompletado.this, AdminCheckout.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showNotification(String title, String message) {
        String CHANNEL_ID = "checkout_channel";

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Checkout";
            String description = "Canal para notificaciones de pagos por checkout";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_hotroid_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // Si no tienes permiso de notificaciones, se solicita
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                        1001);
//            }
//        }
        // Verifica si tienes el permiso
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(3, builder.build());
        } else {
            // Si no tienes permiso, sale mensaje
            Toast.makeText(this, "Permiso de notificaciones no concedido", Toast.LENGTH_SHORT).show();
        }
    }
}