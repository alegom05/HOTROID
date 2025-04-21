package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminCheckoutDetalles extends AppCompatActivity {

    private TextView tvNumeroHabitacion, tvNombreCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar los campos de texto
        tvNumeroHabitacion = findViewById(R.id.tvNumeroHabitacion);
        tvNombreCliente = findViewById(R.id.tvNombreCliente);


        // Obtener los datos de la habitación pasados a través del Intent
        String roomNumber = getIntent().getStringExtra("ROOM_NUMBER");
        String clientName = getIntent().getStringExtra("CLIENT_NAME");

        // Establecer los datos en los TextViews
        tvNumeroHabitacion.setText(roomNumber);
        tvNombreCliente.setText(clientName);

        LinearLayout contenedor = findViewById(R.id.contenedorTarifasAdicionales);
        Button btnAgregar = findViewById(R.id.btnAgregarCobro);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        // Agrega una primera fila al iniciar
        agregarFilaCobro(contenedor);

        btnAgregar.setOnClickListener(v -> {
            agregarFilaCobro(contenedor);
        });
        btnConfirmar.setOnClickListener(v -> {
            double tarifaBase = 0.0;
            try {
                tarifaBase = Double.parseDouble(((TextView) findViewById(R.id.tvTarifaBase)).getText().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            double totalAdicional = 0.0;

            // Recorremos todos los hijos del contenedor
            for (int i = 0; i < contenedor.getChildCount(); i++) {
                LinearLayout fila = (LinearLayout) contenedor.getChildAt(i);
                EditText edtMonto = (EditText) fila.getChildAt(0); // Primer hijo: monto
                try {
                    String valorTexto = edtMonto.getText().toString();
                    if (!valorTexto.isEmpty()) {
                        totalAdicional += Double.parseDouble(valorTexto);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            double montoTotal = tarifaBase + totalAdicional;

            Intent intent = new Intent(AdminCheckoutDetalles.this, AdminMontoCobrar.class);
            intent.putExtra("MONTO_TOTAL", montoTotal);
            startActivity(intent);
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminCheckoutDetalles.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminCheckoutDetalles.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminCheckoutDetalles.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminCheckoutDetalles.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
    private void agregarFilaCobro(LinearLayout contenedor) {
        EditText edtMonto = new EditText(this);
        edtMonto.setHint("00.00");
        edtMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edtMonto.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        EditText edtDescripcion = new EditText(this);
        edtDescripcion.setHint("Escriba aquí");
        edtDescripcion.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        fila.setPadding(0, 8, 0, 8);

        fila.addView(edtMonto);
        fila.addView(edtDescripcion);

        contenedor.addView(fila);
    }
}