package com.example.hotroid;

import android.content.Intent;
import android.net.Uri; // Necesario para Uri
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide; // Importar Glide
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminTaxistaDetalles extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_taxista_detalles);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle clicks for admin profile card
        CardView cardAdmin = findViewById(R.id.cardPerfilAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminTaxistaDetalles.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        // Obtener los datos del Intent
        Intent intent = getIntent();
        String nombres = intent.getStringExtra("taxista_nombres");
        String apellidos = intent.getStringExtra("taxista_apellidos");
        String tipoDocumento = intent.getStringExtra("taxista_tipo_documento");
        String numeroDocumento = intent.getStringExtra("taxista_numero_documento");
        String nacimiento = intent.getStringExtra("taxista_nacimiento");
        String correo = intent.getStringExtra("taxista_correo");
        String telefono = intent.getStringExtra("taxista_telefono");
        String direccion = intent.getStringExtra("taxista_direccion");
        String placa = intent.getStringExtra("taxista_placa");
        String fotoPerfilUrl = intent.getStringExtra("taxista_foto_perfil_url");
        String fotoVehiculoUrl = intent.getStringExtra("taxista_foto_vehiculo_url");
        // No usaremos el estado aquí directamente para mostrarlo en el cuerpo principal,
        // pero lo extraemos por si lo necesitas en otra parte de la lógica.
        String estadoPrincipal = intent.getStringExtra("taxista_estado");
        String estadoDeViaje = intent.getStringExtra("taxista_estado_viaje");


        // Referencias a las vistas del layout del TAXISTA
        TextView tvNombres = findViewById(R.id.tvNombres);
        TextView tvApellidos = findViewById(R.id.tvApellidos);
        TextView tvTipoDocumento = findViewById(R.id.tvTipoDocumento);
        TextView tvNumeroDocumento = findViewById(R.id.tvNumeroDocumento);
        TextView tvNacimiento = findViewById(R.id.tvNacimiento);
        TextView tvCorreo = findViewById(R.id.tvCorreo);
        TextView tvTelefono = findViewById(R.id.tvTelefono);
        TextView tvDireccion = findViewById(R.id.tvDireccion);
        TextView tvPlaca = findViewById(R.id.tvPlaca);

        ImageView fotoPrincipal = findViewById(R.id.fotoPrincipal); // Foto de perfil del taxista
        ImageView fotoVehiculo = findViewById(R.id.fotoVehiculo);   // Foto del vehículo del taxista


        // Referencias a las vistas del layout del ADMINISTRADOR (card superior fijo)
        // Estas vistas tienen su contenido fijo en el XML, pero es buena práctica tener sus IDs
        TextView tvNombreAdmin = findViewById(R.id.tvNombreAdmin);
        TextView tvRolAdmin = findViewById(R.id.tvRolAdmin);
        ImageView imgFotoAdmin = findViewById(R.id.imgFotoAdmin);


        // Establecer los textos de los detalles del TAXISTA
        if (tvNombres != null) tvNombres.setText(nombres);
        if (tvApellidos != null) tvApellidos.setText(apellidos);
        if (tvTipoDocumento != null) tvTipoDocumento.setText(tipoDocumento); // Si el tipo de documento viene por Intent
        if (tvNumeroDocumento != null) tvNumeroDocumento.setText(numeroDocumento);
        if (tvNacimiento != null) tvNacimiento.setText(nacimiento);
        if (tvCorreo != null) tvCorreo.setText(correo);
        if (tvTelefono != null) tvTelefono.setText(telefono);
        if (tvDireccion != null) tvDireccion.setText(direccion);
        if (tvPlaca != null) tvPlaca.setText(placa);

        // Cargar fotos del TAXISTA con Glide
        if (fotoPrincipal != null && fotoPerfilUrl != null && !fotoPerfilUrl.isEmpty()) {
            Glide.with(this).load(Uri.parse(fotoPerfilUrl)).placeholder(R.drawable.ic_user_placeholder).error(R.drawable.ic_user_placeholder).into(fotoPrincipal);
        } else if (fotoPrincipal != null) {
            fotoPrincipal.setImageResource(R.drawable.ic_user_placeholder);
        }

        if (fotoVehiculo != null && fotoVehiculoUrl != null && !fotoVehiculoUrl.isEmpty()) {
            Glide.with(this).load(Uri.parse(fotoVehiculoUrl)).placeholder(R.drawable.car_taxi_driver).error(R.drawable.car_taxi_driver).into(fotoVehiculo);
        } else if (fotoVehiculo != null) {
            fotoVehiculo.setImageResource(R.drawable.car_taxi_driver);
        }


        // Configurar la barra de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent navIntent;
            if (item.getItemId() == R.id.nav_registros) {
                navIntent = new Intent(AdminTaxistaDetalles.this, AdminActivity.class);
                startActivity(navIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                // Ya estamos en AdminTaxistaDetalles, si queremos ir a la lista de taxistas
                navIntent = new Intent(AdminTaxistaDetalles.this, AdminTaxistas.class);
                startActivity(navIntent);
                // Opcional: finish() si no quieres mantener esta actividad en el back stack
                // finish();
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                navIntent = new Intent(AdminTaxistaDetalles.this, AdminCheckout.class);
                startActivity(navIntent);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                navIntent = new Intent(AdminTaxistaDetalles.this, AdminReportes.class);
                startActivity(navIntent);
                return true;
            } else {
                return false;
            }
        });
    }
}