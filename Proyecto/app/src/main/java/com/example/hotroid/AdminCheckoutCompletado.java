package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminCheckoutCompletado extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout_completado);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvMontoFinal = findViewById(R.id.tvMontoFinal);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        // Obtener el monto total enviado desde la vista anterior
        double monto = getIntent().getDoubleExtra("MONTO_TOTAL", 0.0);
        tvMontoFinal.setText(String.format("S/. %.2f", monto));

        btnFinalizar.setOnClickListener(v -> {
            String cliente = getIntent().getStringExtra("CLIENT_NAME");
            String mensaje = "Se cobró S/. " + String.format("%.2f", monto) + " a " + cliente + " por su estadía.";
            showNotification("Checkout completado", mensaje);

            Intent intent = new Intent(AdminCheckoutCompletado.this, AdminCheckout.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finaliza esta actividad para que no se pueda volver con "Back"
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminCheckoutCompletado.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminCheckoutCompletado.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminCheckoutCompletado.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminCheckoutCompletado.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
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

        androidx.core.app.NotificationCompat.Builder builder = new androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)  // Asegúrate de tener este ícono
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        androidx.core.app.NotificationManagerCompat notificationManager = androidx.core.app.NotificationManagerCompat.from(this);
        notificationManager.notify(3, builder.build());  // ID arbitrario para la notificación
    }

}