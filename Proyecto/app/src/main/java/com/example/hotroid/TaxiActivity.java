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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaxiActivity extends AppCompatActivity {

    FirebaseFirestore db;
    private TaxiAlertasAdapter adapter;
    private List<TaxiAlertasBeans> listaAlertasOriginal;
    private List<TaxiAlertasBeans> listaAlertasFiltrada;

    private EditText etBuscador;
    private Button btnLimpiar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main);

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        CardView cardUsuario = findViewById(R.id.cardUsuario);
        cardUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // --- INICIO: Generación de Datos Estáticos con Timestamps y Estado de Viaje (Capitalización Consistente) ---
        listaAlertasOriginal = new ArrayList<>();
        long now = System.currentTimeMillis();

        // Notificaciones con Nombres/Apellidos, Origen, Destino, Timestamp y ESTADO DE VIAJE:
        // Aseguramos la capitalización exacta: "Asignado", "En camino", "Llegó a destino"
        listaAlertasOriginal.add(new TaxiAlertasBeans("Mauricio", "Guerra Sanchez", "Hotel Costa del Sol", "Aeropuerto Internacional Cap. FAP Carlos Martínez de Pinillos (TRU)", new Date(now - (7 * 60 * 1000)), "Asignado"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Lisa", "Cáceres Vega", "Hotel Sonesta", "Aeropuerto Internacional Rodríguez Ballón (AQP)", new Date(now - (25 * 60 * 1000)), "Asignado"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Sol", "Díaz Rojas", "Hotel Decameron", "Aeropuerto Capitán FAP Pedro Canga Rodríguez (TBP)", new Date(now - (50 * 60 * 1000)), "Asignado"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Juan", "Perez Luna", "Hotel Oro Verde", "Aeropuerto Internacional Coronel FAP Francisco Secada Vignetta (IQT)", new Date(now - (75 * 60 * 1000)), "En camino"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Maria", "Lopez Cruz", "Hotel Aranwa", "Aeropuerto Capitán FAP Renán Elías Olivera (PIO)", new Date(now - (100 * 60 * 1000)), "Asignado"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Carlos", "Garcia Ramos", "Hotel Boca Raton", "Aeropuerto Cadete FAP Guillermo del Castillo Paredes (TPP)", new Date(now - (130 * 60 * 1000)), "Llegó a destino"));
        listaAlertasOriginal.add(new TaxiAlertasBeans("Laura", "Fernandez Soto", "Hotel Libertador", "Aeropuerto Internacional Alejandro Velasco Astete (CUZ)", new Date(now - (1 * 60 * 1000)), "Asignado"));
        // --- FIN: Generación de Datos Estáticos ---

        listaAlertasFiltrada = new ArrayList<>(listaAlertasOriginal);
        adapter = new TaxiAlertasAdapter(this, listaAlertasFiltrada);
        recyclerView.setAdapter(adapter);

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int  count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int  before, int count) {
                filtrarNotificaciones(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText("");
            filtrarNotificaciones("");
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.wifi);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.wifi) {
                return true;
            } else if (itemId == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiActivity.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.notify) {
                Intent intentDashboard = new Intent(TaxiActivity.this, TaxiDashboardActivity.class);
                startActivity(intentDashboard);
                return true;
            }
            return false;
        });
    }

    /**
     * Filtra la lista de notificaciones basándose en el texto de búsqueda.
     * Busca coincidencias en los nombres del cliente, apellidos del cliente, origen, destino o estado del viaje.
     * @param textoBusqueda El texto introducido por el usuario en el buscador.
     */
    private void filtrarNotificaciones(String textoBusqueda) {
        listaAlertasFiltrada.clear();

        if (textoBusqueda.isEmpty()) {
            listaAlertasFiltrada.addAll(listaAlertasOriginal);
        } else {
            String searchTextLower = textoBusqueda.toLowerCase(Locale.getDefault());
            for (TaxiAlertasBeans alerta : listaAlertasOriginal) {
                // El filtro sigue siendo case-insensitive para la búsqueda
                if (alerta.getNombresCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getOrigen().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getDestino().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getEstadoViaje().toLowerCase(Locale.getDefault()).contains(searchTextLower)) {
                    listaAlertasFiltrada.add(alerta);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
