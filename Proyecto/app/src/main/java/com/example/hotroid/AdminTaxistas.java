package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Taxista;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminTaxistas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaxistaAdapter adapter;
    private List<Taxista> listaTaxistas; // Esta lista contendrá todos los taxistas inicialmente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_taxistas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerTaxistas);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        // --- Inicialización de la lista de taxistas con datos de ejemplo actualizados ---
        listaTaxistas = new ArrayList<>();
        // Usaremos las URLs de recursos drawable para las fotos de perfil y vehículo

        // Taxista 1: Activado, En Camino (foto de perfil y foto de vehículo)
        listaTaxistas.add(new Taxista(
                "Carlos", "Alvarez", "DNI", "12345678", "1990-01-15",
                "carlos@example.com", "987654321", "Av. Siempre Viva 123",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista1, // Foto de perfil
                "ABC-123", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver, // Foto de vehículo
                "activado", "En Camino"
        ));
        // Taxista 2: Activado, Asignado
        listaTaxistas.add(new Taxista(
                "Alex", "Russo", "DNI", "87654321", "1988-05-20",
                "alex@example.com", "912345678", "Calle Falsa 456",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista2,
                "DEF-456", "android.resource://" + getPackageName() + "/" + R.drawable.carrito, // Otra foto de vehículo
                "activado", "Asignado"
        ));
        // Taxista 3: Activado, No Asignado
        listaTaxistas.add(new Taxista(
                "Marcelo", "Vilca", "DNI", "11223344", "1992-11-01",
                "marcelo@example.com", "934567890", "Jr. Luna 789",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista3,
                "GHI-789", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver,
                "activado", "No Asignado"
        ));
        // Taxista 4: Activado, Llegó a Destino
        listaTaxistas.add(new Taxista(
                "Jaime", "Mora", "DNI", "99887766", "1985-03-10",
                "jaime@example.com", "956789012", "Av. Sol 101",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista4,
                "JKL-101", "android.resource://" + getPackageName() + "/" + R.drawable.carrito,
                "activado", "Llegó a Destino"
        ));
        // Taxista 5: Desactivado, DEBE SER "No Asignado"
        listaTaxistas.add(new Taxista(
                "Arturo", "Delgado", "DNI", "55667788", "1995-07-25",
                "arturo@example.com", "978901234", "Pje. Estrella 202",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista5,
                "MNO-202", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver,
                "desactivado", "No Asignado" // Forzado a "No Asignado"
        ));
        // Taxista 6: Pendiente, DEBE SER "No Asignado"
        listaTaxistas.add(new Taxista(
                "Farith", "Puente", "DNI", "44332211", "1991-09-05",
                "farith@example.com", "990123456", "Cl. Diamante 303",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista6,
                "PQR-303", "android.resource://" + getPackageName() + "/" + R.drawable.carrito,
                "pendiente", "No Asignado" // Forzado a "No Asignado"
        ));
        // --- Fin de la inicialización de la lista de taxistas ---

        // Configuración del Spinner para filtrar por el estado de viaje
        Spinner spinnerEstado = findViewById(R.id.spinnerEstado);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.estados_taxistas, R.layout.spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterSpinner);

        spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estadoSeleccionado = parent.getItemAtPosition(position).toString();
                List<Taxista> listaFiltrada = new ArrayList<>();

                if (estadoSeleccionado.equals("Todos")) {
                    listaFiltrada.addAll(listaTaxistas);
                } else {
                    for (Taxista t : listaTaxistas) {
                        // --- ¡CORRECCIÓN AQUÍ! Filtrar por el campo 'estadoDeViaje' ---
                        if (t.getEstadoDeViaje().equalsIgnoreCase(estadoSeleccionado)) {
                            listaFiltrada.add(t);
                        }
                    }
                }
                adapter = new TaxistaAdapter(listaFiltrada, AdminTaxistas.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no hay selección
            }
        });

        // Inicializar el adaptador con la lista completa de taxistas al inicio
        adapter = new TaxistaAdapter(listaTaxistas, this);
        recyclerView.setAdapter(adapter);

        // --- Configuración de la barra de navegación inferior ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_registros) {
                intent = new Intent(AdminTaxistas.this, AdminActivity.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                intent = new Intent(AdminTaxistas.this, AdminCheckout.class);
                startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                intent = new Intent(AdminTaxistas.this, AdminReportes.class);
                startActivity(intent);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Método para abrir la pantalla de detalles de un taxista.
     * Pasa todos los datos necesarios del taxista al Intent.
     */
    private void abrirDetalle(Taxista taxista) {
        Intent intent = new Intent(AdminTaxistas.this, AdminTaxistaDetalles.class);
        intent.putExtra("taxista_firestore_id", "some_id_if_from_firebase");
        intent.putExtra("taxista_nombres", taxista.getNombres());
        intent.putExtra("taxista_apellidos", taxista.getApellidos());
        intent.putExtra("taxista_tipo_documento", taxista.getTipoDocumento());
        intent.putExtra("taxista_numero_documento", taxista.getNumeroDocumento());
        intent.putExtra("taxista_nacimiento", taxista.getNacimiento());
        intent.putExtra("taxista_correo", taxista.getCorreo());
        intent.putExtra("taxista_telefono", taxista.getTelefono());
        intent.putExtra("taxista_direccion", taxista.getDireccion());
        intent.putExtra("taxista_placa", taxista.getPlaca());
        intent.putExtra("taxista_foto_perfil_url", taxista.getFotoPerfilUrl());
        intent.putExtra("taxista_foto_vehiculo_url", taxista.getFotoVehiculoUrl());
        intent.putExtra("taxista_estado", taxista.getEstado());
        intent.putExtra("taxista_estado_viaje", taxista.getEstadoDeViaje());

        startActivity(intent);
    }
}