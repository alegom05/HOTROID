package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Importar Log para los mensajes
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
import com.google.firebase.firestore.FirebaseFirestore; // Importar FirebaseFirestore

import java.util.ArrayList;
import java.util.List;

public class AdminTaxistas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaxistaAdapter adapter;
    private List<Taxista> listaTaxistas; // Esta lista contendrá todos los taxistas inicialmente

    // Instancia de Firestore
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columnas

        // Inicializa Firestore
        db = FirebaseFirestore.getInstance();
        listaTaxistas = new ArrayList<>(); // Inicializa la lista vacía para llenarla desde Firestore o datos de prueba

        // --- Bloque de inicialización de datos de ejemplo (COMENTAR DESPUÉS DE LA PRIMERA EJECUCIÓN) ---
        // Descomenta este bloque, ejecuta la app UNA VEZ para guardar en Firestore, luego vuelve a comentarlo.
        /*
        listaTaxistas.add(new Taxista(
                "Carlos", "Alvarez", "DNI", "12345678", "1990-01-15",
                "carlos@example.com", "987654321", "Av. Siempre Viva 123",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista1, // Foto de perfil
                "ABC-123", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver, // Foto de vehículo
                "activado", "En Camino"
        ));
        listaTaxistas.add(new Taxista(
                "Alex", "Russo", "DNI", "87654321", "1988-05-20",
                "alex@example.com", "912345678", "Calle Falsa 456",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista2,
                "DEF-456", "android.resource://" + getPackageName() + "/" + R.drawable.carrito, // Otra foto de vehículo
                "activado", "Asignado"
        ));
        listaTaxistas.add(new Taxista(
                "Marcelo", "Vilca", "DNI", "11223344", "1992-11-01",
                "marcelo@example.com", "934567890", "Jr. Luna 789",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista3,
                "GHI-789", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver,
                "activado", "No Asignado"
        ));
        listaTaxistas.add(new Taxista(
                "Jaime", "Mora", "DNI", "99887766", "1985-03-10",
                "jaime@example.com", "956789012", "Av. Sol 101",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista4,
                "JKL-101", "android.resource://" + getPackageName() + "/" + R.drawable.carrito,
                "activado", "Llegó a Destino"
        ));
        listaTaxistas.add(new Taxista(
                "Arturo", "Delgado", "DNI", "55667788", "1995-07-25",
                "arturo@example.com", "978901234", "Pje. Estrella 202",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista5,
                "MNO-202", "android.resource://" + getPackageName() + "/" + R.drawable.car_taxi_driver,
                "desactivado", "No Asignado" // Forzado a "No Asignado"
        ));
        listaTaxistas.add(new Taxista(
                "Farith", "Puente", "DNI", "44332211", "1991-09-05",
                "farith@example.com", "990123456", "Cl. Diamante 303",
                "android.resource://" + getPackageName() + "/" + R.drawable.taxista6,
                "PQR-303", "android.resource://" + getPackageName() + "/" + R.drawable.carrito,
                "pendiente", "No Asignado" // Forzado a "No Asignado"
        ));

        // --- Código para añadir los taxistas a Firestore (Ejecutar solo una vez para poblar la DB) ---
        // Descomenta este bucle, ejecuta la app UNA VEZ para guardar en Firestore, luego vuelve a comentarlo.
        for (Taxista taxista : listaTaxistas) {
            db.collection("taxistas")
                .add(taxista)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AdminTaxistas", "Taxista añadido con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("AdminTaxistas", "Error al añadir taxista", e);
                });
        }
        // --- Fin del código para añadir a Firestore ---
        */
        // --- Fin del bloque de inicialización de datos de ejemplo ---


        // --- Cargar taxistas desde Firestore ---
        cargarTaxistasDesdeFirestore();
        // --- Fin de carga de taxistas desde Firestore ---

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
                filtrarTaxistas(estadoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no hay selección
            }
        });

        // Inicializar el adaptador con la lista vacía o lo que se cargue de Firestore
        adapter = new TaxistaAdapter(new ArrayList<>(), this); // Inicialmente vacío
        recyclerView.setAdapter(adapter);


        // --- Configuración de la barra de navegación inferior ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_taxistas);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            if (item.getItemId() == R.id.nav_registros) {
                intent = new Intent(AdminTaxistas.this, AdminActivity.class);
                startActivity(intent);
                finish(); // Finaliza la actividad actual para que no se apilen
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                // Ya estamos aquí, no hacemos nada o recargamos si es necesario
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

    /**
     * Carga los taxistas desde Firestore y actualiza el RecyclerView.
     */
    private void cargarTaxistasDesdeFirestore() {
        db.collection("taxistas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaTaxistas.clear(); // Limpia la lista existente
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Taxista taxista = document.toObject(Taxista.class);
                            listaTaxistas.add(taxista);
                        }
                        // Una vez cargados, actualiza el RecyclerView con todos los taxistas
                        adapter.actualizarLista(listaTaxistas); // Método a implementar en TaxistaAdapter
                        // Asegúrate de que el spinner refleje "Todos" después de la carga inicial
                        Spinner spinnerEstado = findViewById(R.id.spinnerEstado);
                        if (spinnerEstado != null) {
                            spinnerEstado.setSelection(0); // Selecciona la primera opción ("Todos")
                        }
                    } else {
                        Log.w("AdminTaxistas", "Error al obtener documentos: ", task.getException());
                    }
                });
    }

    /**
     * Filtra la lista de taxistas basada en el estado seleccionado y actualiza el adaptador.
     * @param estadoSeleccionado El estado de viaje por el que se va a filtrar.
     */
    private void filtrarTaxistas(String estadoSeleccionado) {
        List<Taxista> listaFiltrada = new ArrayList<>();
        if (estadoSeleccionado.equals("Todos")) {
            listaFiltrada.addAll(listaTaxistas);
        } else {
            for (Taxista t : listaTaxistas) {
                if (t.getEstadoDeViaje() != null && t.getEstadoDeViaje().equalsIgnoreCase(estadoSeleccionado)) {
                    listaFiltrada.add(t);
                }
            }
        }
        adapter.actualizarLista(listaFiltrada); // Método a implementar en TaxistaAdapter
    }

    /**
     * Método para abrir la pantalla de detalles de un taxista.
     * Pasa todos los datos necesarios del taxista al Intent.
     */
    // Este método debería ser un método público en TaxistaAdapter y ser llamado por el clickListener.
    // Sin embargo, si lo mantienes aquí, asegúrate de que el clickListener en el adaptador lo invoque.
    public void abrirDetalle(Taxista taxista) { // Cambiado a public para que TaxistaAdapter lo pueda llamar
        Intent intent = new Intent(AdminTaxistas.this, AdminTaxistaDetalles.class);
        // Es buena práctica pasar el ID de Firestore si lo tienes
        // intent.putExtra("taxista_firestore_id", taxista.getId()); // Si Taxista tiene un campo ID

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