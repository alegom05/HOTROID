package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class AdminServiciosDetallesActivity extends AppCompatActivity {
    private TextView tvName, tvDescripcion, tvPrecio, tvHoraInicio, tvHoraFin, tvEstadoServicio;
    private LinearLayout contenedorImagenes;
    private boolean estadoHabilitado = true;

    // Keep original data for passing to edit activity
    private String originalServicioNombre;
    private String originalServicioDescripcion;
    private double originalPrecio;
    // Removed originalHorario as it's now split at the source
    private String originalHoraInicio; // This will directly hold the start time
    private String originalHoraFin;    // This will directly hold the end time
    private ArrayList<String> originalUriStrings;
    private String originalDocumentId;

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

        // Initialize TextViews
        tvName = findViewById(R.id.tvName);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        tvPrecio = findViewById(R.id.tvPrecio);
        tvHoraInicio = findViewById(R.id.tvHoraInicio);
        tvHoraFin = findViewById(R.id.tvHoraFin);
        tvEstadoServicio = findViewById(R.id.tvEstadoServicio);
        contenedorImagenes = findViewById(R.id.contenedorImagenes);
        MaterialButton btnEditarServicio = findViewById(R.id.btnEditarServicio);

        // Get data from Intent
        originalServicioNombre = getIntent().getStringExtra("Service_name");
        originalServicioDescripcion = getIntent().getStringExtra("Service_description");
        originalPrecio = getIntent().getDoubleExtra("price", 0.0);
        originalHoraInicio = getIntent().getStringExtra("hora_inicio");
        originalHoraFin = getIntent().getStringExtra("hora_fin");
        originalUriStrings = getIntent().getStringArrayListExtra("imagenes");
        originalDocumentId = getIntent().getStringExtra("documentId");
        estadoHabilitado = getIntent().getBooleanExtra("habilitado", true);

        // Set text for TextViews
        tvName.setText(originalServicioNombre);
        tvDescripcion.setText(originalServicioDescripcion);

        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        tvPrecio.setText("S/. " + decimalFormat.format(originalPrecio));

        // Displaying Hora Inicio and Hora Fin using the separate values
        // Aquí estás usando las variables originales que deben contener los datos recibidos.
        tvHoraInicio.setText(originalHoraInicio);
        tvHoraFin.setText(originalHoraFin);

        // Set Estado del Servicio
        if (estadoHabilitado) {
            tvEstadoServicio.setText("Habilitado");
            tvEstadoServicio.setTextColor(getResources().getColor(R.color.green_status));
        } else {
            tvEstadoServicio.setText("Deshabilitado");
            tvEstadoServicio.setTextColor(getResources().getColor(R.color.red_status));
        }

        // Load images
        if (originalUriStrings != null) {
            for (String uriStr : originalUriStrings) {
                ImageView imageView = new ImageView(this);
                Glide.with(this)
                        .load(uriStr)
                        .into(imageView);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                params.setMargins(8, 8, 8, 8);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                contenedorImagenes.addView(imageView);
            }
        }

        // Set OnClickListener for Edit Button
        btnEditarServicio.setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosDetallesActivity.this, AdminEditarServicioActivity.class);
            intent.putExtra("documentId", originalDocumentId);
            intent.putExtra("Service_name", originalServicioNombre);
            intent.putExtra("Service_description", originalServicioDescripcion);
            intent.putExtra("price", originalPrecio);
            // ¡ERROR AQUÍ! Se estaba usando 'servicio.getHoraInicio()' y 'servicio.getHoraFin()'
            // cuando 'servicio' no está definido en esta clase.
            // La corrección es usar las variables 'originalHoraInicio' y 'originalHoraFin'
            // que ya has recibido del Intent de la actividad anterior.
            intent.putExtra("hora_inicio", originalHoraInicio); // CORREGIDO
            intent.putExtra("hora_fin",  originalHoraFin);     // CORREGIDO
            intent.putStringArrayListExtra("imagenes", originalUriStrings);
            intent.putExtra("habilitado", estadoHabilitado);

            startActivityForResult(intent, 200);
        });

        // Bottom Navigation Listener (remains unchanged)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminServiciosDetallesActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminServiciosDetallesActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminServiciosDetallesActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.nav_reportes) {
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
            // Retrieve updated data
            String nuevoNombre = data.getStringExtra("nombre");
            String nuevaDescripcion = data.getStringExtra("descripcion");
            double nuevoPrecio = data.getDoubleExtra("precio", 0.0);
            String nuevaHoraInicio = data.getStringExtra("hora_inicio"); // Get updated separate start time
            String nuevaHoraFin = data.getStringExtra("hora_fin");     // Get updated separate end time
            ArrayList<String> nuevasImagenes = data.getStringArrayListExtra("imagenes");
            estadoHabilitado = data.getBooleanExtra("habilitado", true);

            // Update TextViews
            tvName.setText(nuevoNombre);
            tvDescripcion.setText(nuevaDescripcion);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            tvPrecio.setText("S/. " + decimalFormat.format(nuevoPrecio));

            // Display Hora Inicio and Hora Fin using the directly received value
            tvHoraInicio.setText(nuevaHoraInicio);
            tvHoraFin.setText(nuevaHoraFin);

            // Update Estado del Servicio
            if (estadoHabilitado) {
                tvEstadoServicio.setText("Habilitado");
                tvEstadoServicio.setTextColor(getResources().getColor(R.color.green_status));
            } else {
                tvEstadoServicio.setText("Deshabilitado");
                tvEstadoServicio.setTextColor(getResources().getColor(R.color.red_status));
            }

            // Clear and reload images
            contenedorImagenes.removeAllViews();
            if (nuevasImagenes != null) {
                for (String uriStr : nuevasImagenes) {
                    ImageView imageView = new ImageView(this);
                    Glide.with(this)
                            .load(uriStr)
                            .into(imageView);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                    params.setMargins(8, 8, 8, 8);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    contenedorImagenes.addView(imageView);
                }
            }

            // Update original data in this activity to reflect changes
            originalServicioNombre = nuevoNombre;
            originalServicioDescripcion = nuevaDescripcion;
            originalPrecio = nuevoPrecio;
            originalHoraInicio = nuevaHoraInicio; // Update the display vars
            originalHoraFin = nuevaHoraFin;     // Update the display vars
            originalUriStrings = nuevasImagenes;
            // originalDocumentId remains the same
        }
    }
}