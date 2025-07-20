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
import android.app.Dialog; // Importar Dialog
import android.graphics.drawable.ColorDrawable; // Para fondo transparente del diálogo

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

public class TaxiActivity extends AppCompatActivity {

    private static final String TAG = "TaxiActivityDebug";

    private FirebaseFirestore db;
    private TaxiAlertasAdapter adapter;
    private List<TaxiAlertasBeans> listaAlertasOriginal;
    private List<TaxiAlertasBeans> listaAlertasFiltrada;

    private EditText etBuscador;
    private Button btnLimpiar;
    private ListenerRegistration firestoreListener;
    private ListenerRegistration activeTripListener; // Nuevo listener para viaje activo

    private boolean hasActiveTrip = false; // Bandera para indicar si hay un viaje activo

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
        // Pasamos una lambda al adaptador para manejar el clic en la tarjeta
        adapter = new TaxiAlertasAdapter(this, listaAlertasFiltrada, alerta -> {
            if (hasActiveTrip) {
                showActiveTripWarningDialog(); // Mostrar pop-up si hay un viaje activo
            } else {
                // Si no hay viaje activo, proceder a TaxiViaje
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

        // Listener para las alertas "No asignado"
        Query queryNoAsignado = alertasRef
                .whereEqualTo("estadoViaje", "No asignado")
                .whereEqualTo("region", "Cusco")
                .orderBy("timestamp", Query.Direction.DESCENDING);

        firestoreListener = queryNoAsignado.addSnapshotListener((snapshots, e) -> {
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
                Log.d(TAG, "Alertas 'No asignado' (Cusco) cargadas desde Firestore: " + listaAlertasOriginal.size());
                filtrarNotificaciones(etBuscador.getText().toString());
            }
        });

        // Nuevo Listener para verificar si hay un viaje activo por el conductor
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
            activeTripListener.remove(); // Desregistrar el nuevo listener
            Log.d(TAG, "Listener de viaje activo desregistrado.");
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

    // Método para mostrar el pop-up de advertencia
    private void showActiveTripWarningDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false); // No se puede cerrar tocando fuera
        dialog.setContentView(R.layout.dialog_active_trip); // Usar el layout personalizado

        // Configurar fondo transparente para que se vea el CardView redondeado
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView dialogMessage = dialog.findViewById(R.id.dialogMessage);
        Button dialogButton = dialog.findViewById(R.id.dialogButtonOK);

        dialogMessage.setText("Tienes un viaje en proceso. No puedes aceptar nuevas alertas hasta finalizar el viaje actual.");

        dialogButton.setOnClickListener(v -> dialog.dismiss()); // Cerrar el diálogo al hacer clic en OK

        dialog.show();
    }
}