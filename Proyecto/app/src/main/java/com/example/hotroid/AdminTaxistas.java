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
    private List<Taxista> listaTaxistas;

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

        listaTaxistas = new ArrayList<>();
        listaTaxistas.add(new Taxista("Carlos Alvarez", "En camino", R.drawable.taxista1));
        listaTaxistas.add(new Taxista("Alex Russo", "Asignado", R.drawable.taxista2));
        listaTaxistas.add(new Taxista("Marcelo Vilca", "No asignado", R.drawable.taxista3));
        listaTaxistas.add(new Taxista("Jaime Mora", "Llegó a destino", R.drawable.taxista4));
        listaTaxistas.add(new Taxista("Arturo Delgado", "Asignado", R.drawable.taxista5));
        listaTaxistas.add(new Taxista("Farith Puente", "En camino", R.drawable.taxista6));
        Spinner spinnerEstado = findViewById(R.id.spinnerEstado);

        spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estadoSeleccionado = parent.getItemAtPosition(position).toString();
                List<Taxista> listaFiltrada = new ArrayList<>();

                if (estadoSeleccionado.equals("Todos")) {
                    listaFiltrada.addAll(listaTaxistas);
                } else {
                    for (Taxista t : listaTaxistas) {
                        if (t.getEstado().equalsIgnoreCase(estadoSeleccionado)) {
                            listaFiltrada.add(t);
                        }
                    }
                }

                adapter = new TaxistaAdapter(listaFiltrada, AdminTaxistas.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.estados_taxistas, R.layout.spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterSpinner);

        adapter = new TaxistaAdapter(listaTaxistas, this);
        recyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminTaxistas.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminTaxistas.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminTaxistas.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminTaxistas.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });

    }
    private void abrirDetalle(Taxista taxista) {
        Intent intent = new Intent(AdminTaxistas.this, AdminTaxistaDetalles.class);
        intent.putExtra("nombre", taxista.getNombre());
        intent.putExtra("estado", taxista.getEstado());
        intent.putExtra("imagen", taxista.getImagenResId());
        intent.putExtra("dni", taxista.getDni());
        intent.putExtra("nacimiento", taxista.getNacimiento());
        intent.putExtra("correo", taxista.getCorreo());
        intent.putExtra("telefono", taxista.getTelefono());
        intent.putExtra("direccion", taxista.getDireccion());
        intent.putExtra("placa", taxista.getPlaca());
        intent.putExtra("fotoVehiculo", taxista.getVehiculoImagenResId());
        startActivity(intent);
    }


}