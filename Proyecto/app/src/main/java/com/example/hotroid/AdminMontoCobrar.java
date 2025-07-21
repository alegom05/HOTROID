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

public class AdminMontoCobrar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_monto_cobrar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        TextView tvMontoTotal = findViewById(R.id.tvMontoTotal);

        double montoTotal = getIntent().getDoubleExtra("MONTO_TOTAL", 0.0);
        String cliente = getIntent().getStringExtra("CLIENT_NAME");
        String idCheckout = getIntent().getStringExtra("ID_CHECKOUT"); // <--- AÑADIR ESTA LÍNEA para recibir el ID

        tvMontoTotal.setText(String.format("S/. %.2f", montoTotal));
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMontoCobrar.this, AdminCheckoutCompletado.class);
            intent.putExtra("MONTO_TOTAL", montoTotal);
            intent.putExtra("CLIENT_NAME", cliente);
            intent.putExtra("ID_CHECKOUT", idCheckout); // <--- AÑADIR ESTA LÍNEA para pasar el ID
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Obtener el ID del item seleccionado
            if (itemId == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminMontoCobrar.this, AdminActivity.class);
                startActivity(intentInicio);
                finish(); // Añadir finish() para limpiar la pila
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminMontoCobrar.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                finish(); // Añadir finish()
                return true;
            } else if (itemId == R.id.nav_checkout) {
                // Si el usuario presiona el icono de checkout en la barra inferior,
                // queremos ir a la lista de checkouts (AdminCheckout), no volver a esta pantalla
                Intent intentCheckout = new Intent(AdminMontoCobrar.this, AdminCheckout.class);
                // Si es necesario limpiar el backstack al ir a la lista principal de checkouts
                intentCheckout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentCheckout);
                finish(); // Añadir finish()
                return true;
            } else if (itemId == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminMontoCobrar.this, AdminReportes.class);
                startActivity(intentAlertas);
                finish(); // Añadir finish()
                return true;
            } else {
                return false;
            }
        });
    }
}