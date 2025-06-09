package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiItem;
import com.example.hotroid.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TaxiActivity extends AppCompatActivity {

    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main);

            db = FirebaseFirestore.getInstance();



        UsuarioDto usuario = new UsuarioDto();
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
                .document("4eBr0Rr1SuUFavkn1Udn")
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

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));



        btnFinViaje.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(TaxiActivity.this, TaxiFin.class); // Redirige a TaxiCuenta
            startActivity(intent); // Inicia la nueva actividad
        });

        CardView cardTaxista = findViewById(R.id.cardTaxista);



        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.wifi);

        // Configurar RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerViewTaxi);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Lista de datos
        List<TaxiItem> taxiList = new ArrayList<>();
/*
        taxiList.add(new TaxiItem("Juan Pérez", "Disponible", R.drawable.taxi_image));
        taxiList.add(new TaxiItem("María López", "Ocupado", R.drawable.taxi_image));
        taxiList.add(new TaxiItem("Carlos García", "Disponible", R.drawable.taxi_image));
*/

        // Configurar el adaptador
        TaxiAdapter taxiAdapter = new TaxiAdapter(taxiList);
        recyclerView.setAdapter(taxiAdapter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) {
                return true;
            }
            else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiActivity.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            }
            else if (item.getItemId() == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiActivity.this, TaxiAlertas.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });
    }
}
