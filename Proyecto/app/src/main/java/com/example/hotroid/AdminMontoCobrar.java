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

public class AdminMontoCobrar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_monto_cobrar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        TextView tvMontoTotal = findViewById(R.id.tvMontoTotal);

        double montoTotal = getIntent().getDoubleExtra("MONTO_TOTAL", 0.0);
        tvMontoTotal.setText(String.format("S/. %.2f", montoTotal));
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMontoCobrar.this, AdminCheckoutCompletado.class);
            intent.putExtra("MONTO_TOTAL", montoTotal);
            startActivity(intent);
        });

    }
}