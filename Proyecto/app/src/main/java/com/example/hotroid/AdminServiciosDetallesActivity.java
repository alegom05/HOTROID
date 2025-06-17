package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.DecimalFormat; // Import for formatting
import java.util.ArrayList;

public class AdminServiciosDetallesActivity extends AppCompatActivity {
    private TextView tvName, tvDescripcion, tvPrecio, tvHorario;
    private LinearLayout contenedorImagenes;
    private boolean estadoHabilitado = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_servicios_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvName = findViewById(R.id.tvName);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvHorario = findViewById(R.id.tvHorario);
        contenedorImagenes = findViewById(R.id.contenedorImagenes);


        String servicioNombre = getIntent().getStringExtra("Service_name");
        String servicioDescripcion = getIntent().getStringExtra("Service_description");
        // --- FIX: Get price as double ---
        double precio = getIntent().getDoubleExtra("price", 0.0); // Default to 0.0 if not found
        // ---------------------------------
        String horario = getIntent().getStringExtra("schedule");
        ArrayList<String> uriStrings = getIntent().getStringArrayListExtra("imagenes");
        String documentId = getIntent().getStringExtra("documentId");


        tvName.setText(servicioNombre);
        tvDescripcion.setText(servicioDescripcion);
        // --- FIX: Format double price for display ---
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        tvPrecio.setText("S/. " + decimalFormat.format(precio));
        // ---------------------------------------------
        tvHorario.setText(horario);

        if (uriStrings != null) {
            for (String uriStr : uriStrings) {
                Uri uri = Uri.parse(uriStr);
                ImageView imageView = new ImageView(this);
                imageView.setImageURI(uri);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                contenedorImagenes.addView(imageView);
            }
        }

        findViewById(R.id.btnEditarHabitacion).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosDetallesActivity.this, AdminEditarServicioActivity.class);
            intent.putExtra("documentId", documentId);
            intent.putExtra("Service_name", servicioNombre);
            intent.putExtra("Service_description", servicioDescripcion);
            // --- FIX: Pass price as double ---
            intent.putExtra("price", precio);
            // ---------------------------------
            intent.putExtra("schedule", horario);
            intent.putStringArrayListExtra("imagenes", uriStrings);
            intent.putExtra("habilitado", estadoHabilitado);

            startActivityForResult(intent, 200);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminServiciosDetallesActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminServiciosDetallesActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminServiciosDetallesActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminServiciosDetallesActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            String nuevoNombre = data.getStringExtra("nombre");
            String nuevaDescripcion = data.getStringExtra("descripcion");
            // --- FIX: Get updated price as double ---
            double nuevoPrecio = data.getDoubleExtra("precio", 0.0);
            // ----------------------------------------
            String nuevoHorario = data.getStringExtra("horario");
            ArrayList<String> nuevasImagenes = data.getStringArrayListExtra("imagenes");
            estadoHabilitado = data.getBooleanExtra("habilitado", true);


            tvName.setText(nuevoNombre);
            tvDescripcion.setText(nuevaDescripcion);
            // --- FIX: Format updated double price for display ---
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            tvPrecio.setText("S/. " + decimalFormat.format(nuevoPrecio));
            // ----------------------------------------------------
            tvHorario.setText(nuevoHorario);

            contenedorImagenes.removeAllViews();
            if (nuevasImagenes != null) {
                for (String uriStr : nuevasImagenes) {
                    Uri uri = Uri.parse(uriStr);
                    ImageView imageView = new ImageView(this);
                    imageView.setImageURI(uri);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                    params.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    contenedorImagenes.addView(imageView);
                }
            }
        }
    }
}