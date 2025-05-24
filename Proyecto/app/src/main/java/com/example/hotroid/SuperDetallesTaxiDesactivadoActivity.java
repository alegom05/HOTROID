package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesTaxiDesactivadoActivity extends AppCompatActivity {

    private int taxiPosition;
    private String[] taxiDataFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_taxi_desactivado);

        Intent incomingIntent = getIntent();
        taxiPosition = incomingIntent.getIntExtra("taxi_position", -1);
        taxiDataFull = incomingIntent.getStringArrayExtra("taxi_data_full");

        // Referencias a los TextViews y ImageView en el layout
        TextView tvTituloDetalleTaxi = findViewById(R.id.tvTituloDetalleTaxi);
        TextView tvNombreAdminActual = findViewById(R.id.tvNombreAdminActual);
        TextView tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        TextView tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        TextView tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        TextView tvNumDocumentoDetalle = findViewById(R.id.tvNumDocumentoDetalle);
        TextView tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        TextView tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        TextView tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        TextView tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        TextView tvPlacaVehiculoDetalle = findViewById(R.id.tvPlacaVehiculoDetalle);
        ImageView fotoPrincipal = findViewById(R.id.fotoPrincipal);
        ImageView fotoVehiculo = findViewById(R.id.fotoVehiculo);

        Button btnActivar = findViewById(R.id.btnActivar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        CardView cardPerfil = findViewById(R.id.cardPerfil);

        // Actualizar el título de la actividad
        if (tvTituloDetalleTaxi != null) {
            tvTituloDetalleTaxi.setText("Detalle Taxista");
        }

        // Mostrar el nombre del administrador actual en la card de perfil superior
        if (tvNombreAdminActual != null) {
            tvNombreAdminActual.setText("Pedro Bustamante");
        }

        // Rellenar los datos del taxista
        if (taxiDataFull != null && taxiDataFull.length >= 11) {
            if (tvNombresDetalle != null) tvNombresDetalle.setText(taxiDataFull[1]);
            if (tvApellidosDetalle != null) tvApellidosDetalle.setText(taxiDataFull[2]);
            if (tvTipoDocumentoDetalle != null) tvTipoDocumentoDetalle.setText(taxiDataFull[3]);
            if (tvNumDocumentoDetalle != null) tvNumDocumentoDetalle.setText(taxiDataFull[4]);
            if (tvFechaNacimientoDetalle != null) tvFechaNacimientoDetalle.setText(taxiDataFull[5]);
            if (tvCorreoDetalle != null) tvCorreoDetalle.setText(taxiDataFull[6]);
            if (tvTelefonoDetalle != null) tvTelefonoDetalle.setText(taxiDataFull[7]);
            if (tvDomicilioDetalle != null) tvDomicilioDetalle.setText(taxiDataFull[8]);
            if (tvPlacaVehiculoDetalle != null) tvPlacaVehiculoDetalle.setText(taxiDataFull[9]);

            // Asignar imágenes
            if (fotoPrincipal != null) fotoPrincipal.setImageResource(R.drawable.foto_admin);
            if (fotoVehiculo != null) fotoVehiculo.setImageResource(R.drawable.carrito);
        } else {
            Toast.makeText(this, "Taxista no encontrado o datos incompletos.", Toast.LENGTH_SHORT).show();
            if (tvNombresDetalle != null) tvNombresDetalle.setText("N/A");
            if (tvApellidosDetalle != null) tvApellidosDetalle.setText("N/A");
            if (tvTipoDocumentoDetalle != null) tvTipoDocumentoDetalle.setText("N/A");
            if (tvNumDocumentoDetalle != null) tvNumDocumentoDetalle.setText("N/A");
            if (tvFechaNacimientoDetalle != null) tvFechaNacimientoDetalle.setText("N/A");
            if (tvCorreoDetalle != null) tvCorreoDetalle.setText("N/A");
            if (tvTelefonoDetalle != null) tvTelefonoDetalle.setText("N/A");
            if (tvDomicilioDetalle != null) tvDomicilioDetalle.setText("N/A");
            if (tvPlacaVehiculoDetalle != null) tvPlacaVehiculoDetalle.setText("N/A");
        }

        // Click listener para el botón Activar
        if (btnActivar != null) {
            btnActivar.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "activado");
                resultIntent.putExtra("taxi_position", taxiPosition);
                if (taxiDataFull != null) {
                    resultIntent.putExtra("taxi_name", taxiDataFull[1] + " " + taxiDataFull[2]);
                }
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }

        // Configuración de la barra de navegación inferior
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    startActivity(new Intent(SuperDetallesTaxiDesactivadoActivity.this, SuperActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    startActivity(new Intent(SuperDetallesTaxiDesactivadoActivity.this, SuperEventosActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }

        // Click listener para la card de perfil superior
        if (cardPerfil != null) {
            cardPerfil.setOnClickListener(v -> {
                startActivity(new Intent(SuperDetallesTaxiDesactivadoActivity.this, SuperCuentaActivity.class));
            });
        }
    }
}