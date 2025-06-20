package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Importar para la imagen del taxista en la card
import android.widget.TextView;
import android.widget.Toast; // Se mantiene por si se usa en otros Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore; // Se mantiene, aunque el bloque de test está comentado

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaxiDashboardActivity extends AppCompatActivity {

    FirebaseFirestore db; // Instancia de Firestore (mantenida por compatibilidad)

    // Componentes de la Card "Taxista"
    private ImageView imagenTaxistaCard;
    private TextView tvNombreTaxistaCard, tvRolTaxistaCard;

    // Componentes de la Card "Viaje Actual"
    private TextView tvClienteActual, tvOrigenActual, tvDestinoActual, tvTiempoActual, tvEstadoViajeActual;
    private Button btnFinViaje; // Mantener este ID

    // Componentes del buscador de "Viajes Terminados"
    private EditText etBuscadorTerminados;
    private Button btnLimpiarTerminados;

    // Componentes del RecyclerView de "Viajes Terminados"
    private RecyclerView recyclerViajesTerminados;
    private TaxiAlertasAdapter adapterViajesTerminados;
    private List<TaxiAlertasBeans> listaViajesTerminadosOriginal;
    private List<TaxiAlertasBeans> listaViajesTerminadosFiltrada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_dashboard);

        db = FirebaseFirestore.getInstance();

        // Bloque de código de Firebase de prueba (comentado, puedes descomentar o eliminar si es necesario)
        /*
        UsuarioDto usuario = new UsuarioDto(); // Asegúrate de que UsuarioDto exista
        usuario.setNombre("Juan");
        usuario.setCorreo("juan.perez@pucp.edu.pe");
        usuario.setDni("12345678");
        db.collection("usuarios")
                .add(usuario)
                .addOnSuccessListener(unused -> {
                    Log.d("msg-test","Data guardada exitosamente");
                })
                .addOnFailureListener(e -> e.printStackTrace());

        db.collection("usuarios")
                .document("4eBr0Rr1SuUFavkn1Udn") // Asegúrate de que este ID de documento sea válido
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d("msg-test", "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d("nsg-test", "No such document");
                        }
                    } else {
                        Log.d("msg-test", "get failed with ", task.getException());
                    }
                });
        */

        // Configurar color de la barra de estado
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        // --- INICIO: Lógica para la Card "Taxista" ---
        imagenTaxistaCard = findViewById(R.id.imagenTaxistaCard);
        tvNombreTaxistaCard = findViewById(R.id.tvNombreTaxistaCard);
        tvRolTaxistaCard = findViewById(R.id.tvRolTaxistaCard);

        // Asignar datos estáticos del taxista
        imagenTaxistaCard.setImageResource(R.drawable.taxipic1); // Asegúrate de tener este drawable
        tvNombreTaxistaCard.setText("Alejandro Gómez");
        tvRolTaxistaCard.setText("Taxista");

        CardView cardTaxista = findViewById(R.id.cardTaxista);
        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });
        // --- FIN: Lógica para la Card "Taxista" ---


        // --- INICIO: Lógica para la Card "Viaje Actual" ---
        tvClienteActual = findViewById(R.id.tvClienteActual);
        tvOrigenActual = findViewById(R.id.tvOrigenActual);
        tvDestinoActual = findViewById(R.id.tvDestinoActual);
        tvTiempoActual = findViewById(R.id.tvTiempoActual);
        tvEstadoViajeActual = findViewById(R.id.tvEstadoViajeActual);
        btnFinViaje = findViewById(R.id.btnFinViaje); // ID del botón

        // Datos estáticos para el Viaje Actual
        tvClienteActual.setText("Fernando Vargas");
        tvOrigenActual.setText("Hotel Libertador");
        tvDestinoActual.setText("Aeropuerto Internacional Alejandro Velasco Astete (CUZ)");
        tvTiempoActual.setText("Hace 30 min"); // Tiempo estático por ahora
        tvEstadoViajeActual.setText("Estado: En Curso"); // Estado estático
        tvEstadoViajeActual.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark)); // Ejemplo de color para "En curso"

        // *** CAMBIO AQUÍ: Navegación a TaxiFin.java y eliminación de Toast ***
        btnFinViaje.setOnClickListener(v -> {
            // Acción para finalizar el viaje: navegar a TaxiFin
            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiFin.class);
            startActivity(intent);
            // Ya no se muestra el Toast "Viaje actual finalizado" aquí.
        });
        // --- FIN: Lógica para la Card "Viaje Actual" ---


        // --- INICIO: Lógica para la Card "Viajes Terminados" con Buscador ---
        recyclerViajesTerminados = findViewById(R.id.recyclerViajesTerminados);
        recyclerViajesTerminados.setLayoutManager(new LinearLayoutManager(this));

        // Datos estáticos para "Viajes Terminados" (usando TaxiAlertasBeans)
        listaViajesTerminadosOriginal = new ArrayList<>();
        long now = System.currentTimeMillis();

        listaViajesTerminadosOriginal.add(new TaxiAlertasBeans("Roberto", "Núñez Prado", "Hotel Costa del Sol", "Aeropuerto Internacional Jorge Chávez (LIM)", new Date(now - (150 * 60 * 1000)), "Llegó a destino"));
        listaViajesTerminadosOriginal.add(new TaxiAlertasBeans("Camila", "Flores Soto", "Hotel Sonesta", "Aeropuerto Internacional Jorge Chávez (LIM)", new Date(now - (200 * 60 * 1000)), "Llegó a destino"));
        listaViajesTerminadosOriginal.add(new TaxiAlertasBeans("Diego", "Castro Ramos", "Hotel Aranwa", "Aeropuerto Internacional Jorge Chávez (LIM)", new Date(now - (250 * 60 * 1000)), "Llegó a destino"));
        listaViajesTerminadosOriginal.add(new TaxiAlertasBeans("Luciana", "Vásquez Díaz", "Hotel Decameron", "Aeropuerto Internacional Jorge Chávez (LIM)", new Date(now - (300 * 60 * 1000)), "Llegó a destino"));

        listaViajesTerminadosFiltrada = new ArrayList<>(listaViajesTerminadosOriginal);
        adapterViajesTerminados = new TaxiAlertasAdapter(this, listaViajesTerminadosFiltrada);
        recyclerViajesTerminados.setAdapter(adapterViajesTerminados);

        // Inicializar elementos del buscador
        etBuscadorTerminados = findViewById(R.id.etBuscadorTerminados);
        btnLimpiarTerminados = findViewById(R.id.btnLimpiarTerminados);

        // Lógica del buscador
        etBuscadorTerminados.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarViajesTerminados(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnLimpiarTerminados.setOnClickListener(v -> {
            etBuscadorTerminados.setText("");
            filtrarViajesTerminados("");
        });
        // --- FIN: Lógica para la Card "Viajes Terminados" ---


        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notify); // Asignado a Dashboard

        // Listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.notify) {
                return true; // Ya estás en esta Activity
            } else if (itemId == R.id.wifi) {
                Intent intentAlertas = new Intent(TaxiDashboardActivity.this, TaxiActivity.class);
                startActivity(intentAlertas);
                return true;
            } else if (itemId == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiDashboardActivity.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            return false;
        });
    }

    /**
     * Filtra la lista de viajes terminados basándose en el texto de búsqueda.
     * Busca coincidencias en los nombres del cliente, apellidos, origen, destino o estado del viaje.
     * @param textoBusqueda El texto introducido por el usuario en el buscador.
     */
    private void filtrarViajesTerminados(String textoBusqueda) {
        listaViajesTerminadosFiltrada.clear();

        if (textoBusqueda.isEmpty()) {
            listaViajesTerminadosFiltrada.addAll(listaViajesTerminadosOriginal);
        } else {
            String searchTextLower = textoBusqueda.toLowerCase(Locale.getDefault());
            for (TaxiAlertasBeans alerta : listaViajesTerminadosOriginal) {
                if (alerta.getNombresCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getOrigen().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getDestino().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getEstadoViaje().toLowerCase(Locale.getDefault()).contains(searchTextLower)) {
                    listaViajesTerminadosFiltrada.add(alerta);
                }
            }
        }
        adapterViajesTerminados.notifyDataSetChanged();
    }
}