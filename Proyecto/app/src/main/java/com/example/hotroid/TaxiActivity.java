package com.example.hotroid;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.TaxiAlertasAdapter;
import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TaxiActivity extends AppCompatActivity {

    private static final String TAG = "TaxiActivityDebug";

    private FirebaseFirestore db;
    private TaxiAlertasAdapter adapter;
    private List<TaxiAlertasBeans> listaAlertasOriginal; // Lista completa sin filtrar
    private List<TaxiAlertasBeans> listaAlertasFiltrada; // Lista que se muestra en el RecyclerView

    private EditText etBuscadorCliente; // SOLO para buscar clientes
    private Button btnLimpiar;
    private Spinner spinnerHoteles;     // Spinner para filtro de hoteles
    private Spinner spinnerAeropuertos; // Spinner para filtro de aeropuertos
    private TextView tvNoAlerts; // Para mostrar mensaje si no hay alertas
    private TextView tvTripStatus; // NEW: TextView para mostrar el estado del viaje

    private ListenerRegistration firestoreListener; // Listener para alertas "No asignado"
    private ListenerRegistration activeTripListener; // Listener para verificar viaje activo

    private boolean hasActiveTrip = false; // Bandera para indicar si hay un viaje activo

    // --- LISTAS PARA FILTRADO DE LUGARES DE INTERÉS EN LIMA (en minúsculas y normalizadas) ---
    private List<String> limaHotelsNormalized;
    private List<String> limaAirportsNormalized;

    // --- Variables para mantener el estado actual de los filtros ---
    private String currentClientSearchText = "";
    private String currentSelectedHotel = "Todos los Hoteles"; // Valor inicial del spinner de hoteles
    private String currentSelectedAirport = "Todos los Aeropuertos"; // Valor inicial del spinner de aeropuertos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main); // Usar taxi_main.xml

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        // Inicializar vistas
        CardView cardUsuario = findViewById(R.id.cardUsuario);
        RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        etBuscadorCliente = findViewById(R.id.etBuscadorCliente);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        spinnerHoteles = findViewById(R.id.spinnerHoteles);
        spinnerAeropuertos = findViewById(R.id.spinnerAeropuertos);
        tvNoAlerts = findViewById(R.id.tvNoAlerts);
        tvTripStatus = findViewById(R.id.tvTripStatus); // NEW: Inicializar tvTripStatus

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaAlertasOriginal = new ArrayList<>();
        listaAlertasFiltrada = new ArrayList<>();

        // --- INICIALIZAR LAS LISTAS DE HOTELES Y AEROPUERTOS DE LIMA ---
        // Estas listas contendrán los nombres NORMALIZADOS para la comparación
        initLimaLocations();

        // Configurar el adaptador con la lista filtrada y el listener de clic
        adapter = new TaxiAlertasAdapter(this, listaAlertasFiltrada, alerta -> {
            if (hasActiveTrip) {
                showActiveTripWarningDialog(); // Mostrar pop-up si hay un viaje activo
            } else {
                // Si no hay viaje activo, proceder a TaxiViaje
                Log.d(TAG, "Alerta seleccionada: " + alerta.getDocumentId());
                Intent intent = new Intent(TaxiActivity.this, TaxiViaje.class);
                intent.putExtra("documentId", alerta.getDocumentId());
                intent.putExtra("nombresCliente", alerta.getNombresCliente());
                intent.putExtra("apellidosCliente", alerta.getApellidosCliente());
                intent.putExtra("origen", alerta.getOrigen());
                intent.putExtra("destino", alerta.getDestino());
                intent.putExtra("timestamp", alerta.getTimestamp() != null ? alerta.getTimestamp().getTime() : 0);
                intent.putExtra("estadoViaje", alerta.getEstadoViaje());
                intent.putExtra("region", alerta.getRegion());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        // Listener para el CardView de usuario
        cardUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });

        // Configurar el buscador SOLO para cliente
        etBuscadorCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentClientSearchText = s.toString(); // Actualiza el texto de búsqueda de cliente
                applyFilters(); // Re-aplica todos los filtros
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnLimpiar.setOnClickListener(v -> {
            etBuscadorCliente.setText(""); // Limpia el texto del buscador de cliente
            spinnerHoteles.setSelection(0); // Resetea el spinner de hoteles a "Todos los Hoteles"
            spinnerAeropuertos.setSelection(0); // Resetea el spinner de aeropuertos a "Todos los Aeropuertos"
            // applyFilters() será llamado automáticamente por el TextWatcher o por los Spinner OnItemSelectedListener
        });

        // --- Configurar Spinner de Hoteles ---
        List<String> hotelOptions = new ArrayList<>();
        hotelOptions.add("Todos los Hoteles"); // Opción para no filtrar
        hotelOptions.addAll(limaHotelsNormalized); // Añadir todos los nombres de hoteles normalizados
        ArrayAdapter<String> hotelSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hotelOptions);
        hotelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHoteles.setAdapter(hotelSpinnerAdapter);

        spinnerHoteles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedHotel = parent.getItemAtPosition(position).toString();
                applyFilters(); // Re-aplica todos los filtros
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSelectedHotel = "Todos los Hoteles";
                applyFilters();
            }
        });

        // --- Configurar Spinner de Aeropuertos ---
        List<String> airportOptions = new ArrayList<>();
        airportOptions.add("Todos los Aeropuertos"); // Opción para no filtrar
        airportOptions.addAll(limaAirportsNormalized); // Añadir todos los nombres de aeropuertos normalizados
        ArrayAdapter<String> airportSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, airportOptions);
        airportSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAeropuertos.setAdapter(airportSpinnerAdapter);

        spinnerAeropuertos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSelectedAirport = parent.getItemAtPosition(position).toString();
                applyFilters(); // Re-aplica todos los filtros
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSelectedAirport = "Todos los Aeropuertos";
                applyFilters();
            }
        });

        // Configurar Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.wifi); // Marcar el ítem actual

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.wifi) {
                return true; // Ya estamos aquí
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

        // Configurar listeners de Firestore
        CollectionReference alertasRef = db.collection("alertas_taxi");

        // Listener para las alertas "No asignado" y de la región "Lima"
        Query queryNoAsignadoLima = alertasRef
                .whereEqualTo("estadoViaje", "No asignado")
                .whereEqualTo("region", "Lima") // FILTRO CLAVE PARA LIMA
                .orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreListener = queryNoAsignadoLima.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Error escuchando alertas de Firestore:", e);
                return;
            }

            if (snapshots != null) {
                listaAlertasOriginal.clear();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    TaxiAlertasBeans alerta = doc.toObject(TaxiAlertasBeans.class);
                    if (alerta != null) {
                        alerta.setDocumentId(doc.getId());
                        listaAlertasOriginal.add(alerta);
                    }
                }
                Log.d(TAG, "Alertas 'No asignado' (Lima) cargadas desde Firestore: " + listaAlertasOriginal.size());
                // Al cargar datos, re-aplicar todos los filtros para asegurar la coherencia
                applyFilters();
            }
        });

        // Listener para verificar si hay un viaje activo por el conductor
        activeTripListener = alertasRef
                .whereIn("estadoViaje", Arrays.asList("En camino", "Asignado", "Llegó a destino"))
                .limit(1) // Solo necesitamos saber si existe al menos uno
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error escuchando viajes activos:", e);
                        return;
                    }
                    hasActiveTrip = snapshots != null && !snapshots.isEmpty();
                    Log.d(TAG, "Estado de viaje activo actualizado: " + hasActiveTrip);
                    // NEW: Update the TextView for trip status
                    updateTripStatusText();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
            Log.d(TAG, "Listener de alertas de Firestore desregistrado.");
        }
        if (activeTripListener != null) {
            activeTripListener.remove();
            Log.d(TAG, "Listener de viaje activo desregistrado.");
        }
    }

    // --- MÉTODO PARA INICIALIZAR LUGARES DE INTERÉS (TODOS EN MINÚSCULAS Y NORMALIZADOS) ---
    // Esta lista NO es para usar directamente en el Spinner, sino para la lógica de filtrado.
    // Los nombres en esta lista deben ser el resultado de normalizar los nombres de Firestore.
    private void initLimaLocations() {
        // Nombres ya normalizados para comparación directa desde Firestore
        limaHotelsNormalized = new ArrayList<>();
        limaHotelsNormalized.add("libertador");
        limaHotelsNormalized.add("jw marriott hotel lima");
        limaHotelsNormalized.add("belmond miraflores park");
        limaHotelsNormalized.add("hilton lima miraflores");
        limaHotelsNormalized.add("ac hotel by marriott lima miraflores");
        limaHotelsNormalized.add("aloft lima miraflores");
        limaHotelsNormalized.add("radisson red miraflores");
        limaHotelsNormalized.add("swissotel lima");
        limaHotelsNormalized.add("pullman lima san isidro");
        limaHotelsNormalized.add("novotel lima san isidro");
        limaHotelsNormalized.add("costa del sol wyndham lima airport");

        limaAirportsNormalized = new ArrayList<>();
        limaAirportsNormalized.add("aeropuerto internacional jorge chávez");
        limaAirportsNormalized.add("aeropuerto de santa maría del mar");
        limaAirportsNormalized.add("base aérea las palmas");
        limaAirportsNormalized.add("aerodromo lib mandi de san bartolo");
    }

    /**
     * Aplica todos los filtros activos (búsqueda de cliente, filtro de hotel y filtro de aeropuerto).
     */
    private void applyFilters() {
        listaAlertasFiltrada.clear();

        // Si no hay alertas originales, no hay nada que filtrar.
        if (listaAlertasOriginal.isEmpty()) {
            adapter.updateList(listaAlertasFiltrada);
            updateNoAlertsVisibility();
            return;
        }

        String clientSearchTextLower = currentClientSearchText.toLowerCase(Locale.getDefault()).trim();
        String selectedHotelNormalized = normalizarNombreLugar(currentSelectedHotel); // Normalizar también la opción seleccionada del spinner
        String selectedAirportNormalized = normalizarNombreLugar(currentSelectedAirport); // Normalizar también la opción seleccionada del spinner

        for (TaxiAlertasBeans alerta : listaAlertasOriginal) {
            boolean matchesClient = true;
            boolean matchesHotelFilter = true;
            boolean matchesAirportFilter = true;

            // 1. Aplicar filtro de búsqueda de cliente
            if (!clientSearchTextLower.isEmpty()) {
                matchesClient = alerta.getNombresCliente().toLowerCase(Locale.getDefault()).contains(clientSearchTextLower) ||
                        alerta.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(clientSearchTextLower);
            }

            // 2. Aplicar filtro de hoteles
            // Solo si no se ha seleccionado "Todos los Hoteles"
            if (!currentSelectedHotel.equals("Todos los Hoteles")) {
                String origenNormalizado = normalizarNombreLugar(alerta.getOrigen());
                matchesHotelFilter = origenNormalizado.equals(selectedHotelNormalized);
            }

            // 3. Aplicar filtro de aeropuertos
            // Solo si no se ha seleccionado "Todos los Aeropuertos"
            if (!currentSelectedAirport.equals("Todos los Aeropuertos")) {
                String destinoNormalizado = normalizarNombreLugar(alerta.getDestino());
                matchesAirportFilter = destinoNormalizado.equals(selectedAirportNormalized);
            }

            // La alerta se muestra si PASA TODOS los filtros activos
            if (matchesClient && matchesHotelFilter && matchesAirportFilter) {
                listaAlertasFiltrada.add(alerta);
            }
        }

        adapter.updateList(listaAlertasFiltrada);
        updateNoAlertsVisibility();
    }

    // --- MÉTODO HELPER PARA NORMALIZAR NOMBRES DE LUGARES (DE FIREBASE O SPINNER) ---
    private String normalizarNombreLugar(String nombre) {
        if (nombre == null) return "";
        String normalized = nombre.toLowerCase(Locale.getDefault())
                .trim()
                .replaceAll("[^a-z0-9áéíóúüñ\\s]", "");

        // Mapeos específicos (asegúrate de que estos mapeos produzcan cadenas que EXISTAN
        // en tus listas limaHotelsNormalized y limaAirportsNormalized)
        if (normalized.contains("libertador") && normalized.contains("pucp")) {
            return "hotel libertador";
        } else if (normalized.contains("jorge chavez") || normalized.contains("jorge chvez")) {
            return "aeropuerto internacional jorge chávez";
        } else if (normalized.contains("base aerea") && normalized.contains("callao") && normalized.contains("grupo")) {
            return "base aérea del callao (grupo aéreo n° 8)";
        } else if (normalized.contains("lib mandi") || normalized.contains("san bartolo")) {
            return "aerodromo lib mandi de san bartolo";
        } else if (normalized.contains("swissotel")) {
            return "swissotel lima";
        } else if (normalized.contains("marriott") && normalized.contains("lima")) {
            if (normalized.contains("ac hotel")) return "ac hotel by marriott lima miraflores";
            return "jw marriott hotel lima";
        } else if (normalized.contains("dazzler") && normalized.contains("miraflores")) {
            return "hotel dazzler by wyndham lima miraflores";
        } else if (normalized.contains("wyndham") && normalized.contains("airport")) {
            return "costa del sol wyndham lima airport";
        }
        // Para las opciones "Todos los Hoteles" / "Todos los Aeropuertos", el método retornará
        // "todos los hoteles" / "todos los aeropuertos" respectivamente, lo cual es manejado
        // explícitamente en applyFilters() para no aplicar filtro.

        return normalized;
    }


    private void updateNoAlertsVisibility() {
        if (listaAlertasFiltrada.isEmpty()) {
            tvNoAlerts.setVisibility(View.VISIBLE);
        } else {
            tvNoAlerts.setVisibility(View.GONE);
        }
    }

    // NEW: Method to update the trip status TextView
    private void updateTripStatusText() {
        if (hasActiveTrip) {
            tvTripStatus.setText("Estado de Viaje: En curso");
            tvTripStatus.setTextColor(ContextCompat.getColor(this, R.color.verdejade)); // Optional: Change color for "En curso"
        } else {
            tvTripStatus.setText("Estado de Viaje: Libre");
            tvTripStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark)); // Optional: Revert to original color or set another
        }
    }

    // Método para mostrar el pop-up de advertencia
    private void showActiveTripWarningDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_active_trip);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);

        dialogMessage.setText("Tienes un viaje en proceso. No puedes aceptar nuevas alertas hasta finalizar el viaje actual.");

        dialogButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}