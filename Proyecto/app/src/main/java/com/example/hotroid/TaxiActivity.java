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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;

public class TaxiActivity extends AppCompatActivity {

    private static final String TAG = "TaxiActivityDebug";

    private FirebaseFirestore db;
    private TaxiAlertasAdapter adapter;
    private List<TaxiAlertasBeans> listaAlertasOriginal;
    private List<TaxiAlertasBeans> listaAlertasFiltrada;

    private EditText etBuscador;
    private Button btnLimpiar;
    private ListenerRegistration firestoreListener;

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

        listaAlertasOriginal = new ArrayList<>();
        listaAlertasFiltrada = new ArrayList<>();
        adapter = new TaxiAlertasAdapter(this, listaAlertasFiltrada);
        recyclerView.setAdapter(adapter);

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
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

        CollectionReference alertasRef = db.collection("alertas_taxi");

        Query query = alertasRef
                .whereEqualTo("estadoViaje", "En camino")
                .whereEqualTo("region", "Cusco")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreListener = query.addSnapshotListener((snapshots, e) -> {
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
                Log.d(TAG, "Alertas 'En camino' (Cusco) cargadas desde Firestore: " + listaAlertasOriginal.size());
                filtrarNotificaciones(etBuscador.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (firestoreListener != null) {
            firestoreListener.remove();
            Log.d(TAG, "Listener de alertas de Firestore desregistrado.");
        }
    }

    private void filtrarNotificaciones(String textoBusqueda) {
        listaAlertasFiltrada.clear();

        if (textoBusqueda.isEmpty()) {
            listaAlertasFiltrada.addAll(listaAlertasOriginal);
        } else {
            String searchTextLower = textoBusqueda.toLowerCase(Locale.getDefault());
            for (TaxiAlertasBeans alerta : listaAlertasOriginal) {
                if (alerta.getNombresCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getApellidosCliente().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getOrigen().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getDestino().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        alerta.getEstadoViaje().toLowerCase(Locale.getDefault()).contains(searchTextLower) ||
                        (alerta.getRegion() != null && alerta.getRegion().toLowerCase(Locale.getDefault()).contains(searchTextLower))) {
                    listaAlertasFiltrada.add(alerta);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}