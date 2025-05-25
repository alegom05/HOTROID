package com.example.hotroid;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SuperDetallesClienteActivadoActivity extends AppCompatActivity {

    private int clientPosition;
    private String[] clientDataFull; // Para almacenar todos los datos del cliente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_cliente_activado);

        Intent incomingIntent = getIntent();
        clientPosition = incomingIntent.getIntExtra("client_position", -1);
        clientDataFull = incomingIntent.getStringArrayExtra("client_data_full");

        TextView tvTituloDetalleCliente = findViewById(R.id.tvTituloDetalleCliente);
        TextView tvNombreAdminActual = findViewById(R.id.tvNombreAdminActual);
        TextView tvUsuarioDetalle = findViewById(R.id.tvUsuarioDetalle);
        TextView tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        TextView tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        TextView tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        TextView tvNumDocumentoDetalle = findViewById(R.id.tvNumDocumentoDetalle);
        TextView tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        TextView tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        TextView tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        TextView tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        ImageView fotoPrincipal = findViewById(R.id.fotoPrincipal);
        Button btnDesactivar = findViewById(R.id.btnDesactivar);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        CardView cardPerfil = findViewById(R.id.cardPerfil);

        // Actualizar el título de la actividad
        if (tvTituloDetalleCliente != null) {
            tvTituloDetalleCliente.setText("Detalle Cliente");
        }

        // Mostrar el nombre del administrador actual en la card de perfil superior (hardcoded como en tu admin)
        if (tvNombreAdminActual != null) {
            tvNombreAdminActual.setText("Pedro Bustamante");
        }

        if (clientDataFull != null && clientDataFull.length >= 9) { // Asegura que el array tenga al menos los datos básicos
            if (tvUsuarioDetalle != null) tvUsuarioDetalle.setText(clientDataFull[0]); // Nombre Completo (Usuario)
            if (tvNombresDetalle != null) tvNombresDetalle.setText(clientDataFull[1]);
            if (tvApellidosDetalle != null) tvApellidosDetalle.setText(clientDataFull[2]);
            if (tvTipoDocumentoDetalle != null) tvTipoDocumentoDetalle.setText(clientDataFull[3]);
            if (tvNumDocumentoDetalle != null) tvNumDocumentoDetalle.setText(clientDataFull[4]);
            if (tvFechaNacimientoDetalle != null) tvFechaNacimientoDetalle.setText(clientDataFull[5]);
            if (tvCorreoDetalle != null) tvCorreoDetalle.setText(clientDataFull[6]);
            if (tvTelefonoDetalle != null) tvTelefonoDetalle.setText(clientDataFull[7]);
            if (tvDomicilioDetalle != null) tvDomicilioDetalle.setText(clientDataFull[8]);


        } else {
            Toast.makeText(this, "Cliente no encontrado o datos incompletos.", Toast.LENGTH_SHORT).show();
            // Establecer textos por defecto o vacíos si los datos no son válidos
            if (tvUsuarioDetalle != null) tvUsuarioDetalle.setText("N/A");
            if (tvNombresDetalle != null) tvNombresDetalle.setText("N/A");
            if (tvApellidosDetalle != null) tvApellidosDetalle.setText("N/A");
            if (tvTipoDocumentoDetalle != null) tvTipoDocumentoDetalle.setText("N/A");
            if (tvNumDocumentoDetalle != null) tvNumDocumentoDetalle.setText("N/A");
            if (tvFechaNacimientoDetalle != null) tvFechaNacimientoDetalle.setText("N/A");
            if (tvCorreoDetalle != null) tvCorreoDetalle.setText("N/A");
            if (tvTelefonoDetalle != null) tvTelefonoDetalle.setText("N/A");
            if (tvDomicilioDetalle != null) tvDomicilioDetalle.setText("N/A");
        }

        if (btnDesactivar != null) {
            btnDesactivar.setOnClickListener(v -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "desactivado");
                resultIntent.putExtra("client_position", clientPosition);
                // No es necesario pasar "client_name" si la lista principal puede obtenerlo por posición
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        }

        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    startActivity(new Intent(SuperDetallesClienteActivadoActivity.this, SuperActivity.class));
                    finish(); // Cierra esta actividad al navegar
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    startActivity(new Intent(SuperDetallesClienteActivadoActivity.this, SuperUsuariosActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    startActivity(new Intent(SuperDetallesClienteActivadoActivity.this, SuperEventosActivity.class));
                    finish(); // Cierra esta actividad al navegar
                    return true;
                }
                return false;
            });
        }

        if (cardPerfil != null) {
            cardPerfil.setOnClickListener(v -> {
                startActivity(new Intent(SuperDetallesClienteActivadoActivity.this, SuperCuentaActivity.class));
            });
        }
    }
}