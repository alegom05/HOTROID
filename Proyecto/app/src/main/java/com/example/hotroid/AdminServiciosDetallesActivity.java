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

import java.util.ArrayList;

public class AdminServiciosDetallesActivity extends AppCompatActivity {
    private TextView tvName, tvDescripcion, tvPrecio;
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
        contenedorImagenes = findViewById(R.id.contenedorImagenes);


        // Obtener los datos de la habitación pasados a través del Intent
        String servicioNombre = getIntent().getStringExtra("Service_name");
        String servicioDescripcion = getIntent().getStringExtra("Service_description");
        String precio = getIntent().getStringExtra("price");
        ArrayList<String> uriStrings = getIntent().getStringArrayListExtra("imagenes");

        // Establecer los datos en los TextViews
        tvName.setText(servicioNombre);
        tvDescripcion.setText(servicioDescripcion);
        tvPrecio.setText("S/. " + precio);

        // Mostrar las imágenes
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

        // Acción para editar la habitación
        findViewById(R.id.btnEditarHabitacion).setOnClickListener(v -> {
            Intent intent = new Intent(AdminServiciosDetallesActivity.this, AdminEditarServicioActivity.class);
            intent.putExtra("Service_name", servicioNombre);
            intent.putExtra("Service_description", servicioDescripcion);
            intent.putExtra("price", precio);
            intent.putStringArrayListExtra("imagenes", uriStrings);
            intent.putExtra("habilitado", estadoHabilitado);

            startActivityForResult(intent, 200);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
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
            // Obtener los datos editados
            String nuevoNombre = data.getStringExtra("nombre");
            String nuevaDescripcion = data.getStringExtra("descripcion");
            String nuevoPrecio = data.getStringExtra("precio");
            ArrayList<String> nuevasImagenes = data.getStringArrayListExtra("imagenes");
            estadoHabilitado = data.getBooleanExtra("habilitado", true);


            // Actualizar los TextViews
            tvName.setText(nuevoNombre);
            tvDescripcion.setText(nuevaDescripcion);
            tvPrecio.setText("S/. " + nuevoPrecio);

            // Actualizar las imágenes
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