package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiItem; // Asegúrate de que esta clase exista
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaxiDashboardActivity extends AppCompatActivity {

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_dashboard); // Usamos el nuevo layout

        db = FirebaseFirestore.getInstance();

        // Código de Firebase Firestore (puedes moverlo a un lugar más apropiado si es una inicialización global)
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

        Button btnFinViaje = findViewById(R.id.btnFinViaje);

        // Configurar color de la barra de estado
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));

        btnFinViaje.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiFin.class); // Redirige a TaxiFin
            startActivity(intent); // Inicia la nueva actividad
        });

        CardView cardTaxista = findViewById(R.id.cardTaxista);
        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiDashboardActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notify); // Por ejemplo, el icono de notificaciones será ahora la "antigua" Home

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTaxi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lista de datos
        List<TaxiItem> taxiList = new ArrayList<>();
        // Puedes agregar datos de prueba aquí si es necesario
        // taxiList.add(new TaxiItem("Juan Pérez", "Disponible", R.drawable.taxi_image));
        // taxiList.add(new TaxiItem("María López", "Ocupado", R.drawable.taxi_image));
        // taxiList.add(new TaxiItem("Carlos García", "Disponible", R.drawable.taxi_image));

        // Configurar el adaptador
        TaxiAdapter taxiAdapter = new TaxiAdapter(taxiList); // Asegúrate de que TaxiAdapter exista
        recyclerView.setAdapter(taxiAdapter);

        // Listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.notify) { // Ahora este icono es la antigua Home (Dashboard)
                return true; // Ya estás en esta Activity
            } else if (item.getItemId() == R.id.wifi) { // Este icono ahora apunta a la nueva Home (Alertas)
                Intent intentAlertas = new Intent(TaxiDashboardActivity.this, TaxiActivity.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiDashboardActivity.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            return false;
        });
    }
}
