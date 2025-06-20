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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class TaxiDashboardActivity extends AppCompatActivity {

    private static final String TAG = "TaxiDashboardDebug";

    private FirebaseFirestore db;

    private ImageView imagenTaxistaCard;
    private TextView tvNombreTaxistaCard, tvRolTaxistaCard;

    private CardView cardViajeActual;
    private LinearLayout layoutNoViajeActual;
    private TextView tvClienteActual, tvOrigenActual, tvDestinoActual, tvTiempoActual, tvEstadoViajeActual;
    private Button btnFinViaje;
    private ListenerRegistration currentTripListener;

    private EditText etBuscadorTerminados;
    private Button btnLimpiarTerminados;

    private RecyclerView recyclerViajesTerminados;
    private TaxiAlertasAdapter adapterViajesTerminados;
    private List<TaxiAlertasBeans> listaViajesTerminadosOriginal;
    private List<TaxiAlertasBeans> listaViajesTerminadosFiltrada;
    private ListenerRegistration finishedTripsListener;

    private Random random = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_dashboard);

        db = FirebaseFirestore.getInstance();

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        imagenTaxistaCard = findViewById(R.id.imagenTaxistaCard);
        tvNombreTaxistaCard = findViewById(R.id.tvNombreTaxistaCard);
        tvRolTaxistaCard = findViewById(R.id.tvRolTaxistaCard);

        imagenTaxistaCard.setImageResource(R.drawable.taxipic1);
        tvNombreTaxistaCard.setText("Alejandro Gómez");
        tvRolTaxistaCard.setText("Taxista");

        CardView cardTaxista = findViewById(R.id.cardTaxista);
        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });

        cardViajeActual = findViewById(R.id.cardViajeActual);
        layoutNoViajeActual = findViewById(R.id.layoutNoViajeActual);
        tvClienteActual = findViewById(R.id.tvClienteActual);
        tvOrigenActual = findViewById(R.id.tvOrigenActual);
        tvDestinoActual = findViewById(R.id.tvDestinoActual);
        tvTiempoActual = findViewById(R.id.tvTiempoActual);
        tvEstadoViajeActual = findViewById(R.id.tvEstadoViajeActual);
        btnFinViaje = findViewById(R.id.btnFinViaje);

        btnFinViaje.setOnClickListener(v -> {
            db.collection("alertas_taxi")
                    .whereIn("estadoViaje", Arrays.asList("Asignado", "En viaje", "Llegó a origen", "Llegó a destino"))
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                            String tripId = doc.getId();
                            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiFin.class);
                            intent.putExtra("documentId", tripId);
                            startActivity(intent);
                        } else {
                            Toast.makeText(TaxiDashboardActivity.this, "No hay viaje actual para finalizar.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error al obtener viaje para btnFinViaje: " + e.getMessage());
                        Toast.makeText(TaxiDashboardActivity.this, "Error al preparar finalización del viaje.", Toast.LENGTH_SHORT).show();
                    });
        });

        cardViajeActual.setOnClickListener(v -> {
            if (cardViajeActual.getVisibility() == View.VISIBLE && layoutNoViajeActual.getVisibility() == View.GONE) {
                db.collection("alertas_taxi")
                        .whereIn("estadoViaje", Arrays.asList("Asignado", "En viaje", "Llegó a origen", "Llegó a destino"))
                        .limit(1)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                                String tripId = doc.getId();
                                TaxiAlertasBeans currentTrip = doc.toObject(TaxiAlertasBeans.class);
                                if (currentTrip != null) {
                                    currentTrip.setDocumentId(tripId);
                                    Intent intent = new Intent(TaxiDashboardActivity.this, TaxiViaje.class);
                                    intent.putExtra("documentId", tripId);
                                    intent.putExtra("nombresCliente", currentTrip.getNombresCliente());
                                    intent.putExtra("apellidosCliente", currentTrip.getApellidosCliente());
                                    intent.putExtra("origen", currentTrip.getOrigen());
                                    intent.putExtra("destino", currentTrip.getDestino());
                                    intent.putExtra("timestamp", currentTrip.getTimestamp() != null ? currentTrip.getTimestamp().getTime() : 0);
                                    intent.putExtra("estadoViaje", currentTrip.getEstadoViaje());
                                    intent.putExtra("region", currentTrip.getRegion());
                                    startActivity(intent);
                                }
                            } else {
                                Log.w(TAG, "Clic en cardViajeActual pero no se encontró viaje activo.");
                                Toast.makeText(TaxiDashboardActivity.this, "No hay viaje activo para ver detalles.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al intentar ver detalles del viaje activo: " + e.getMessage());
                            Toast.makeText(TaxiDashboardActivity.this, "Error al cargar detalles del viaje.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(TaxiDashboardActivity.this, "No hay viaje activo para ver detalles.", Toast.LENGTH_SHORT).show();
            }
        });


        currentTripListener = db.collection("alertas_taxi")
                .whereIn("estadoViaje", Arrays.asList("Asignado", "En viaje", "Llegó a origen", "Llegó a destino"))
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error escuchando viaje actual para UI:", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        DocumentSnapshot doc = snapshots.getDocuments().get(0);
                        TaxiAlertasBeans currentTrip = doc.toObject(TaxiAlertasBeans.class);
                        if (currentTrip != null) {
                            currentTrip.setDocumentId(doc.getId());

                            tvClienteActual.setText(currentTrip.getNombresCliente() + " " + currentTrip.getApellidosCliente());
                            tvOrigenActual.setText("Origen: " + currentTrip.getOrigen());
                            tvDestinoActual.setText("Destino: " + currentTrip.getDestino());
                            tvTiempoActual.setText(currentTrip.getTiempoTranscurrido());

                            tvEstadoViajeActual.setText("Estado: " + currentTrip.getEstadoViaje());
                            int colorRes;
                            switch (currentTrip.getEstadoViaje()) {
                                case "Asignado":
                                case "En viaje":
                                case "Llegó a origen":
                                    colorRes = android.R.color.holo_blue_dark;
                                    break;
                                case "Llegó a destino":
                                    colorRes = R.color.naranja;
                                    break;
                                default:
                                    colorRes = android.R.color.black;
                            }
                            tvEstadoViajeActual.setTextColor(ContextCompat.getColor(this, colorRes));

                            cardViajeActual.setVisibility(View.VISIBLE);
                            layoutNoViajeActual.setVisibility(View.GONE);
                            if ("Llegó a destino".equals(currentTrip.getEstadoViaje())) {
                                btnFinViaje.setVisibility(View.VISIBLE);
                                btnFinViaje.setText("Escanear QR / Finalizar");
                            } else {
                                btnFinViaje.setVisibility(View.GONE);
                            }
                            Log.d(TAG, "UI de viaje actual actualizada: " + currentTrip.getDocumentId() + " - " + currentTrip.getEstadoViaje());
                        }
                    } else {
                        cardViajeActual.setVisibility(View.GONE);
                        layoutNoViajeActual.setVisibility(View.VISIBLE);
                        btnFinViaje.setVisibility(View.GONE);
                        Log.d(TAG, "UI: No hay viajes 'Asignados'/'En viaje'/'Llegó a origen'/'Llegó a destino' actualmente.");
                    }
                });

        recyclerViajesTerminados = findViewById(R.id.recyclerViajesTerminados);
        recyclerViajesTerminados.setLayoutManager(new LinearLayoutManager(this));

        listaViajesTerminadosOriginal = new ArrayList<>();
        listaViajesTerminadosFiltrada = new ArrayList<>();
        adapterViajesTerminados = new TaxiAlertasAdapter(this, listaViajesTerminadosFiltrada);
        recyclerViajesTerminados.setAdapter(adapterViajesTerminados);

        etBuscadorTerminados = findViewById(R.id.etBuscadorTerminados);
        btnLimpiarTerminados = findViewById(R.id.btnLimpiarTerminados);

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

        finishedTripsListener = db.collection("alertas_taxi")
                .whereEqualTo("estadoViaje", "Completado")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error escuchando viajes terminados:", e);
                        return;
                    }

                    if (snapshots != null) {
                        listaViajesTerminadosOriginal.clear();
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            TaxiAlertasBeans alerta = doc.toObject(TaxiAlertasBeans.class);
                            if (alerta != null) {
                                alerta.setDocumentId(doc.getId());
                                listaViajesTerminadosOriginal.add(alerta);
                            }
                        }
                        Log.d(TAG, "Viajes terminados cargados desde Firestore: " + listaViajesTerminadosOriginal.size());
                        filtrarViajesTerminados(etBuscadorTerminados.getText().toString());
                    }
                });

        // generarAlertasEnFirestore(); // Descomenta y usa esto solo una vez para poblar tu BD, luego comenta de nuevo

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notify);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent targetIntent;
            if (itemId == R.id.notify) {
                return true;
            } else if (itemId == R.id.wifi) {
                Log.d(TAG, "Navegando a TaxiActivity (Alertas) desde Dashboard.");
                targetIntent = new Intent(TaxiDashboardActivity.this, TaxiActivity.class);
            } else if (itemId == R.id.location) {
                Log.d(TAG, "Navegando a TaxiLocation (Ubicación) desde Dashboard.");
                targetIntent = new Intent(TaxiDashboardActivity.this, TaxiLocation.class);
            } else {
                return false;
            }
            targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(targetIntent);
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentTripListener != null) {
            currentTripListener.remove();
            Log.d(TAG, "Listener de viaje actual (UI) desregistrado.");
        }
        if (finishedTripsListener != null) {
            finishedTripsListener.remove();
            Log.d(TAG, "Listener de viajes terminados desregistrado.");
        }
    }

    private void generarAlertasEnFirestore() {
        CollectionReference clientesRef = db.collection("clientes");
        CollectionReference hotelesRef = db.collection("hoteles");
        CollectionReference alertasRef = db.collection("alertas_taxi");

        Log.d(TAG, "Iniciando generación de alertas de prueba desde DB...");

        alertasRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().isEmpty()) {
                    Log.d(TAG, "No hay documentos existentes en alertas_taxi. Procediendo a crear nuevas.");
                    fetchDataAndCreateAlerts(clientesRef, hotelesRef, alertasRef);
                } else {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Documento eliminado: " + document.getId()))
                                .addOnFailureListener(e -> Log.e(TAG, "Error al eliminar documento: " + document.getId(), e));
                    }
                    new android.os.Handler().postDelayed(
                            () -> fetchDataAndCreateAlerts(clientesRef, hotelesRef, alertasRef),
                            2000
                    );
                }
            } else {
                Log.e(TAG, "Error al obtener documentos para eliminación en alertas_taxi: ", task.getException());
                fetchDataAndCreateAlerts(clientesRef, hotelesRef, alertasRef);
            }
        });
    }

    private void fetchDataAndCreateAlerts(CollectionReference clientesRef, CollectionReference hotelesRef, CollectionReference alertasRef) {
        final List<Map<String, String>> fetchedClientes = new ArrayList<>();
        final String HOTEL_LIBERTADOR_NAME_IN_DB = "Libertador";

        clientesRef.get().addOnCompleteListener(taskClientes -> {
            if (taskClientes.isSuccessful()) {
                for (QueryDocumentSnapshot doc : taskClientes.getResult()) {
                    Map<String, Object> clienteData = doc.getData();
                    if (clienteData.containsKey("nombres") && clienteData.containsKey("apellidos")) {
                        Map<String, String> cliente = new HashMap<>();
                        cliente.put("nombres", (String) clienteData.get("nombres"));
                        cliente.put("apellidos", (String) clienteData.get("apellidos"));
                        fetchedClientes.add(cliente);
                    } else {
                        Log.w(TAG, "Documento de cliente sin 'nombres' o 'apellidos': " + doc.getId());
                    }
                }
                Log.d(TAG, "Clientes obtenidos de Firestore: " + fetchedClientes.size());

                hotelesRef.whereEqualTo("name", HOTEL_LIBERTADOR_NAME_IN_DB).get().addOnCompleteListener(taskHoteles -> {
                    if (taskHoteles.isSuccessful()) {
                        String hotelLibertadorOrigen = null;
                        for (QueryDocumentSnapshot doc : taskHoteles.getResult()) {
                            hotelLibertadorOrigen = doc.getString("name");
                            Log.d(TAG, "Hotel Libertador obtenido: " + hotelLibertadorOrigen);
                            break;
                        }

                        if (!fetchedClientes.isEmpty() && hotelLibertadorOrigen != null) {
                            crearNuevasAlertas(alertasRef, fetchedClientes, hotelLibertadorOrigen);
                        } else {
                            Toast.makeText(this, "No se pudieron obtener suficientes datos de clientes o hotel.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "No hay clientes disponibles o el Hotel Libertador no fue encontrado en Firestore con el campo 'name'.");
                        }
                    } else {
                        Log.e(TAG, "Error al obtener hoteles de Firestore: ", taskHoteles.getException());
                        Toast.makeText(this, "Error al obtener hoteles.", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Log.e(TAG, "Error al obtener clientes de Firestore: ", taskClientes.getException());
                Toast.makeText(this, "Error al obtener clientes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearNuevasAlertas(CollectionReference alertasRef, List<Map<String, String>> clientes, String hotelLibertadorOrigen) {
        List<String> aeropuertosCusco = Arrays.asList(
                "Aeropuerto Internacional Alejandro Velasco Astete (CUZ)",
                "Aeropuerto de Chinchero (CUZ)"
        );
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            Map<String, String> cliente = clientes.get(random.nextInt(clientes.size()));
            String aeropuertoDestinoCusco = aeropuertosCusco.get(random.nextInt(aeropuertosCusco.size()));

            TaxiAlertasBeans alerta = new TaxiAlertasBeans(
                    cliente.get("nombres"),
                    cliente.get("apellidos"),
                    hotelLibertadorOrigen,
                    aeropuertoDestinoCusco,
                    null,
                    "En camino",
                    "Cusco"
            );
            guardarAlertaEnFirestore(alertasRef, alerta);
        }
        Toast.makeText(this, "7 Alertas de Cusco generadas desde DB y subidas a Firestore.", Toast.LENGTH_LONG).show();
    }

    private void guardarAlertaEnFirestore(CollectionReference alertasRef, TaxiAlertasBeans alerta) {
        alertasRef.add(alerta)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Alerta guardada con ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al guardar alerta: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al guardar alerta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

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
                        alerta.getEstadoViaje().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        (alerta.getRegion() != null && alerta.getRegion().toLowerCase(Locale.getDefault()).contains(searchTextLower))) {
                    listaViajesTerminadosFiltrada.add(alerta);
                }
            }
        }
        adapterViajesTerminados.notifyDataSetChanged();
    }
}