package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminCheckoutDetalles extends AppCompatActivity {

    private TextView tvNumeroHabitacion, tvNombreCliente, tvCheckinDate, tvCheckoutDate, tvTarifaBase, tvMontoTotal;
    private LinearLayout contenedorTarifasAdicionales;
    private Button btnAgregarCobro, btnConfirmar;

    private List<EditText> listaMontosAdicionalesEditables;

    private double tarifaBaseInicial = 0.0;
    private double cobrosAdicionalesFijos = 0.0;

    private String currentCheckoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        tvNumeroHabitacion = findViewById(R.id.tvNumeroHabitacion);
        tvNombreCliente = findViewById(R.id.tvNombreCliente);
        tvCheckinDate = findViewById(R.id.tvCheckinDate);
        tvCheckoutDate = findViewById(R.id.tvCheckoutDate);
        tvTarifaBase = findViewById(R.id.tvTarifaBase);
        tvMontoTotal = findViewById(R.id.tvMontoTotal);
        contenedorTarifasAdicionales = findViewById(R.id.contenedorTarifasAdicionales);
        btnAgregarCobro = findViewById(R.id.btnAgregarCobro);
        btnConfirmar = findViewById(R.id.btnConfirmar);

        listaMontosAdicionalesEditables = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentCheckoutId = extras.getString("ID_CHECKOUT");
            String roomNumber = extras.getString("ROOM_NUMBER");
            String clientName = extras.getString("CLIENT_NAME");
            tarifaBaseInicial = extras.getDouble("BASE_RATE", 0.0);
            cobrosAdicionalesFijos = extras.getDouble("ADDITIONAL_CHARGES", 0.0);

            // Declare and assign checkinDateMillis and checkoutDateMillis here
            long checkinDateMillis = extras.getLong("CHECKIN_DATE", -1);
            long checkoutDateMillis = extras.getLong("CHECKOUT_DATE", -1);


            tvNumeroHabitacion.setText(roomNumber);
            tvNombreCliente.setText(clientName);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            if (checkinDateMillis != -1) {
                tvCheckinDate.setText(sdf.format(new Date(checkinDateMillis)));
            } else {
                tvCheckinDate.setText("N/A");
            }
            if (checkoutDateMillis != -1) {
                tvCheckoutDate.setText(sdf.format(new Date(checkoutDateMillis)));
            } else {
                tvCheckoutDate.setText("N/A");
            }

            tvTarifaBase.setText(String.format(Locale.getDefault(), "%.2f", tarifaBaseInicial));

            if (cobrosAdicionalesFijos > 0) {
                agregarFilaCobro(contenedorTarifasAdicionales, "Cobros adicionales iniciales", cobrosAdicionalesFijos, false);
            } else {
                agregarFilaCobro(contenedorTarifasAdicionales, "", 0.0, true);
            }

            calcularYActualizarMontoTotal();

        } else {
            Toast.makeText(this, "Error: No se recibieron los datos del checkout.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnAgregarCobro.setOnClickListener(v -> {
            agregarFilaCobro(contenedorTarifasAdicionales, "", 0.0, true);
            calcularYActualizarMontoTotal();
        });

        btnConfirmar.setOnClickListener(v -> {
            double montoFinalTotal = calcularMontoTotal();
            Intent intent = new Intent(AdminCheckoutDetalles.this, AdminMontoCobrar.class);
            intent.putExtra("MONTO_TOTAL", montoFinalTotal);
            intent.putExtra("CLIENT_NAME", tvNombreCliente.getText().toString());
            intent.putExtra("ID_CHECKOUT", currentCheckoutId);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentInicio = new Intent(AdminCheckoutDetalles.this, AdminActivity.class);
                    startActivity(intentInicio);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminCheckoutDetalles.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    Intent intentCheckout = new Intent(AdminCheckoutDetalles.this, AdminCheckout.class);
                    intentCheckout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentCheckout);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentAlertas = new Intent(AdminCheckoutDetalles.this, AdminReportes.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        } else {
            Log.e("AdminCheckoutDetalles", "BottomNavigationView con ID R.id.bottom_navigation no encontrada.");
        }
    }

    private void agregarFilaCobro(LinearLayout contenedor, String descripcionInicial, double montoInicial, boolean editable) {
        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 8, 0, 8);
        fila.setLayoutParams(layoutParams);

        EditText edtMonto = new EditText(this);
        edtMonto.setHint("0.00");
        edtMonto.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        edtMonto.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        edtMonto.setText(String.format(Locale.getDefault(), "%.2f", montoInicial));
        edtMonto.setEnabled(editable);

        EditText edtDescripcion = new EditText(this);
        edtDescripcion.setHint("Descripción del cobro");
        edtDescripcion.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        edtDescripcion.setText(descripcionInicial);
        edtDescripcion.setEnabled(editable);

        edtMonto.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                calcularYActualizarMontoTotal();
            }
        });

        fila.addView(edtMonto);
        fila.addView(edtDescripcion);

        contenedor.addView(fila);

        if (editable) {
            listaMontosAdicionalesEditables.add(edtMonto);
        }
    }

    private double calcularMontoTotal() {
        double total = tarifaBaseInicial + cobrosAdicionalesFijos;

        for (EditText edtMonto : listaMontosAdicionalesEditables) {
            try {
                String valorTexto = edtMonto.getText().toString();
                if (!valorTexto.isEmpty()) {
                    total += Double.parseDouble(valorTexto);
                }
            } catch (NumberFormatException e) {
                Log.e("AdminCheckoutDetalles", "Error de formato de número en cobro adicional: " + e.getMessage());
            }
        }
        return total;
    }

    private void calcularYActualizarMontoTotal() {
        double monto = calcularMontoTotal();
        tvMontoTotal.setText(String.format(Locale.getDefault(), "%.2f", monto));
    }
}