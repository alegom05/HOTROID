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
            Intent intent = new Intent(AdminCheckoutCompletado.this, AdminCheckout.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finaliza esta actividad para que no se pueda volver con "Back"
        });
    }
}