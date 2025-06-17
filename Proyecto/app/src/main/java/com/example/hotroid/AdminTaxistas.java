package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import this

import java.util.ArrayList;
import java.util.List;

public class AdminTaxistas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaxistaAdapter adapter;
    private List<Taxista> listaTaxistas;

    private FirebaseFirestore db;

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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();
        listaTaxistas = new ArrayList<>();

        // Initialize the adapter with an empty list FIRST
        // This prevents null pointer issues if Firestore load is slow
        adapter = new TaxistaAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // --- IMPORTANT: Ensure this block is COMMENTED OUT if data is already in Firestore ---
        // If you need to populate data, use the addInitialTaxisToFirestore() in SuperListaTaxisActivity
        // or ensure this specific block runs only once.
        /*
        // Example: To add one taxista if database is empty for testing,
        // you might use a flag in SharedPreferences or check DB size first.
        // For production, this should not be in onCreate().
        Log.d("AdminTaxistas", "Attempting to add initial taxistas if not present...");
        db.collection("taxistas").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().isEmpty()) {
                List<Taxista> initialDummyTaxistas = Arrays.asList(
                    new Taxista(
                            "Carlos Josue", "Alvarez Retes", "DNI", "12345678", "1990-01-15",
                            "carlos.alvarez@gmail.com", "987654321", "Av. Siempre Viva 123",
                            "android.resource://" + getPackageName() + "/" + R.drawable.taxista1, // Foto de perfil
                            "ABC-123", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver, // Foto de vehículo
                            "activado", "En Camino"
                    ),
                    new Taxista(
                            "Alex David", "Russo Vera", "DNI", "87654321", "1988-05-20",
                            "alex.russo@gmail.com", "912345678", "Calle Falsa 456",
                            "android.resource://" + getPackageName() + "/" + R.drawable.taxista2,
                            "DEF-456", "android.resource://" + getPackageName() + "/" + R.drawable.carrito, // Otra foto de vehículo
                            "activado", "Asignado"
                    )
                    // ... add all your 6 taxistas here if you want to use this block for initial setup
                );

                for (Taxista taxista : initialDummyTaxistas) {
                    db.collection("taxistas")
                        .add(taxista)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("AdminTaxistas", "Initial Taxista added with ID: " + documentReference.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.w("AdminTaxistas", "Error adding initial taxista", e);
                        });
                }
            } else if (task.isSuccessful()) {
                Log.d("AdminTaxistas", "Database already contains taxistas. Skipping initial add.");
            } else {
                Log.e("AdminTaxistas", "Error checking database for initial data: ", task.getException());
            }
        });
        */
        // --- End of initial data population block ---


        cargarTaxistasDesdeFirestore(); // This should be the main way to load data

        // Spinner setup
        Spinner spinnerEstado = findViewById(R.id.spinnerEstado);
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.estados_taxistas, R.layout.spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterSpinner);

        spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estadoSeleccionado = parent.getItemAtPosition(position).toString();
                filtrarTaxistas(estadoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No doing nothing if nothing is selected
            }
        });

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_registros) {
                intent = new Intent(AdminTaxistas.this, AdminActivity.class);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                intent = new Intent(AdminTaxistas.this, AdminCheckout.class);
                startActivity(intent);
                finish();
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                intent = new Intent(AdminTaxistas.this, AdminReportes.class);
                startActivity(intent);
                finish();
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // It's good practice to reload data on resume if changes might have occurred
        // (e.g., from AdminTaxistaDetalles). This will ensure the list is always fresh.
        cargarTaxistasDesdeFirestore();
    }

    private void cargarTaxistasDesdeFirestore() {
        db.collection("taxistas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaTaxistas.clear(); // CRUCIAL: Clear the list before adding
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Taxista taxista = document.toObject(Taxista.class);
                            // IMPORTANT: If you need the Firestore ID in your Taxista object for updates/deletes,
                            // make sure to set it here! Your Taxista class must have a `firestoreId` field and setter.
                            // taxista.setFirestoreId(document.getId());
                            listaTaxistas.add(taxista);
                        }
                        Log.d("AdminTaxistas", "Loaded " + listaTaxistas.size() + " taxistas from Firestore.");
                        // After loading, apply the current filter or display all
                        Spinner spinnerEstado = findViewById(R.id.spinnerEstado);
                        if (spinnerEstado != null) {
                            String selectedState = spinnerEstado.getSelectedItem().toString();
                            filtrarTaxistas(selectedState); // Filter based on current spinner selection
                        } else {
                            adapter.actualizarLista(listaTaxistas); // If spinner not ready, show all
                        }
                    } else {
                        Log.w("AdminTaxistas", "Error al obtener documentos: ", task.getException());
                    }
                });
    }

    private void filtrarTaxistas(String estadoSeleccionado) {
        List<Taxista> listaFiltrada = new ArrayList<>();
        if (estadoSeleccionado.equals("Todos") || estadoSeleccionado.equals("Todos los estados")) { // Make sure "Todos" matches your R.array.estados_taxistas
            listaFiltrada.addAll(listaTaxistas);
        } else {
            for (Taxista t : listaTaxistas) {
                // Ensure null check and case-insensitive comparison
                if (t.getEstadoDeViaje() != null && t.getEstadoDeViaje().equalsIgnoreCase(estadoSeleccionado)) {
                    listaFiltrada.add(t);
                }
            }
        }
        Log.d("AdminTaxistas", "Filtered list size: " + listaFiltrada.size() + " for state: " + estadoSeleccionado);
        adapter.actualizarLista(listaFiltrada);
    }

    public void abrirDetalle(Taxista taxista) {
        Intent intent = new Intent(AdminTaxistas.this, AdminTaxistaDetalles.class);

        // IMPORTANT: Pass the Firestore ID if you have it, it's crucial for details/updates
        // Add `taxista.setFirestoreId(document.getId());` in `cargarTaxistasDesdeFirestore()`
        // and ensure your `Taxista` bean has `private String firestoreId;` and its getter/setter.
        // Then uncomment the line below:
        // intent.putExtra("taxista_firestore_id", taxista.getFirestoreId());

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