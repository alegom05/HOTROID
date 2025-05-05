package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String estado = intent.getStringExtra("estado");
        int imagen = intent.getIntExtra("imagen", R.drawable.foto_admin);
        String dni = intent.getStringExtra("dni");
        String nacimiento = intent.getStringExtra("nacimiento");
        String correo = intent.getStringExtra("correo");
        String telefono = intent.getStringExtra("telefono");
        String direccion = intent.getStringExtra("direccion");
        String placa = intent.getStringExtra("placa");
        int fotoVehiculo = intent.getIntExtra("fotoVehiculo", R.drawable.carrito);

        ((TextView) findViewById(R.id.tvCuenta)).setText(nombre);
        ((ImageView) findViewById(R.id.fotoPrincipal)).setImageResource(imagen);
        ((ImageView) findViewById(R.id.fotoVehiculo)).setImageResource(fotoVehiculo);

        // Reemplaza los textos fijos por dinámicos
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("nombres")).setText(nombre.split(" ")[0]);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("apellidos")).setText(nombre.substring(nombre.indexOf(" ") + 1));
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("dni")).setText(dni);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("nacimiento")).setText(nacimiento);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("correo")).setText(correo);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("telefono")).setText(telefono);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("direccion")).setText(direccion);
        ((TextView) findViewById(R.id.layoutInfo).findViewWithTag("placa")).setText(placa);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminTaxistaDetalles.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminTaxistaDetalles.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminTaxistaDetalles.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminTaxistaDetalles.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
}