package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText; // Importar EditText
import android.widget.Button; // Importar Button para el botón Limpiar
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Taxista;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminTaxistas extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TaxistaAdapter adapter;
    private List<Taxista> listaTaxistas; // Lista original, no filtrada, de todos los taxistas
    private FirebaseFirestore db;

    private EditText etBuscador; // Cambiado de etSearch a etBuscador
    private Button btnLimpiar; // Declarar el botón Limpiar
    private Spinner spinnerEstado;
    private String currentSearchText = ""; // Variable para mantener el texto de búsqueda actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_taxistas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerTaxistas);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        db = FirebaseFirestore.getInstance();
        listaTaxistas = new ArrayList<>(); // Inicializa la lista que contendrá todos los taxistas

        adapter = new TaxistaAdapter(new ArrayList<>(), this); // Inicializar con una lista vacía
        recyclerView.setAdapter(adapter);

        // --- Configuración del buscador (EditText y Botón Limpiar) ---
        etBuscador = findViewById(R.id.etBuscador); // Referencia al EditText
        btnLimpiar = findViewById(R.id.btnLimpiar); // Referencia al botón Limpiar

        // Añadir TextWatcher para filtrar mientras el usuario escribe
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchText = s.toString(); // Actualizar el texto de búsqueda actual
                // Llamar al filtro con el estado actual del spinner y el nuevo texto de búsqueda
                String estadoSeleccionado = spinnerEstado.getSelectedItem().toString();
                filtrarTaxistas(estadoSeleccionado, currentSearchText);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementación aquí
            }
        });

        // Listener para el botón "Limpiar"
        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText(""); // Limpiar el texto del buscador
            currentSearchText = ""; // Resetear la variable de búsqueda
            // Recargar el filtro para mostrar todos los taxistas del estado seleccionado
            String estadoSeleccionado = spinnerEstado.getSelectedItem().toString();
            filtrarTaxistas(estadoSeleccionado, currentSearchText);
        });
        // --- Fin de configuración del buscador ---


        cargarTaxistasDesdeFirestore();

        // Spinner setup
        spinnerEstado = findViewById(R.id.spinnerEstado); // Inicializar spinnerEstado aquí
        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(
                this, R.array.estados_taxistas, R.layout.spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEstado.setAdapter(adapterSpinner);

        spinnerEstado.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String estadoSeleccionado = parent.getItemAtPosition(position).toString();
                // Llamar al filtro con el nuevo estado del spinner y el texto de búsqueda actual
                filtrarTaxistas(estadoSeleccionado, currentSearchText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona nada
            }
        });

        // Handle clicks for admin profile card
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminTaxistas.this, AdminCuentaActivity.class);
            startActivity(intent);
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
        // Recargar datos para asegurar que la lista esté actualizada y los filtros se apliquen
        cargarTaxistasDesdeFirestore();
        // Opcional: Limpiar el buscador cada vez que la actividad vuelve a primer plano
        etBuscador.setText("");
        currentSearchText = "";
    }

    private void cargarTaxistasDesdeFirestore() {
        db.collection("taxistas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaTaxistas.clear(); // Limpiar la lista antes de añadir nuevos datos
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Taxista taxista = document.toObject(Taxista.class);
                            // Si necesitas el ID de Firestore en tu objeto Taxista:
                            // taxista.setFirestoreId(document.getId());
                            listaTaxistas.add(taxista);
                        }
                        Log.d("AdminTaxistas", "Loaded " + listaTaxistas.size() + " taxistas from Firestore.");

                        // Después de cargar, aplicar los filtros actuales (spinner y búsqueda)
                        String estadoSeleccionado = (spinnerEstado != null && spinnerEstado.getSelectedItem() != null) ? spinnerEstado.getSelectedItem().toString() : "Todos";
                        filtrarTaxistas(estadoSeleccionado, currentSearchText);

                    } else {
                        Log.w("AdminTaxistas", "Error al obtener documentos: ", task.getException());
                    }
                });
    }

    // MODIFICADO: El método filtrarTaxistas recibe el estado seleccionado y el texto de búsqueda
    private void filtrarTaxistas(String estadoSeleccionado, String searchText) {
        List<Taxista> listaFiltradaTemporal = new ArrayList<>();

        // Paso 1: Filtrar por estado desde la lista original de todos los taxistas
        if (estadoSeleccionado.equals("Todos") || estadoSeleccionado.equals("Todos los estados") || estadoSeleccionado.equals("all")) { // Asegúrate de que "Todos" coincida con tu array
            listaFiltradaTemporal.addAll(listaTaxistas);
        } else {
            for (Taxista t : listaTaxistas) {
                if (t.getEstadoDeViaje() != null && t.getEstadoDeViaje().equalsIgnoreCase(estadoSeleccionado)) {
                    listaFiltradaTemporal.add(t);
                }
            }
        }

        // Paso 2: Aplicar el filtro de búsqueda a la lista ya filtrada por estado
        List<Taxista> listaFinalFiltrada = new ArrayList<>();
        if (searchText == null || searchText.trim().isEmpty()) {
            listaFinalFiltrada.addAll(listaFiltradaTemporal);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase().trim();
            for (Taxista t : listaFiltradaTemporal) {
                // Se busca en nombres, apellidos, DNI, o placa
                if ((t.getNombres() != null && t.getNombres().toLowerCase().contains(lowerCaseSearchText)) ||
                        (t.getApellidos() != null && t.getApellidos().toLowerCase().contains(lowerCaseSearchText)) ||
                        (t.getPlaca() != null && t.getPlaca().toLowerCase().contains(lowerCaseSearchText)) ||
                        (t.getNumeroDocumento() != null && t.getNumeroDocumento().toLowerCase().contains(lowerCaseSearchText)))
                {
                    listaFinalFiltrada.add(t);
                }
            }
        }
        Log.d("AdminTaxistas", "Filtered list size: " + listaFinalFiltrada.size() + " for state: " + estadoSeleccionado + " and search: '" + searchText + "'");
        adapter.actualizarLista(listaFinalFiltrada);
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