package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Hotel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperActivity extends AppCompatActivity {

    private static final String TAG = "SuperActivity";
    private FirebaseFirestore db;
    private RecyclerView recyclerViewHotels;
    private SuperHotelAdapter hotelAdapter;
    private List<Hotel> hotelList;
    private EditText etSearchHotel;
    private Button btnClearSearch;
    private Spinner spinnerCityFilter;
    private String[] cities = {"Todas las ciudades", "Arequipa", "Cusco", "Iquitos", "Paracas", "Punta Sal", "Tarapoto", "Trujillo"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_main);

        db = FirebaseFirestore.getInstance();
        hotelList = new ArrayList<>();
        hotelAdapter = new SuperHotelAdapter(this, hotelList);

        recyclerViewHotels = findViewById(R.id.recyclerViewHotels);
        recyclerViewHotels.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHotels.setAdapter(hotelAdapter);
        recyclerViewHotels.setHasFixedSize(true);

        new Handler().postDelayed(this::checkAndLoadHotels, 500);

        setupCityFilter();
        setupSearch();
        setupBottomNavigation();

        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intent = new Intent(SuperActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });
    }

    private void setupCityFilter() {
        spinnerCityFilter = findViewById(R.id.spinnerCityFilter);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCityFilter.setAdapter(cityAdapter);

        spinnerCityFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = cities[position];
                if (position == 0) { // "Todas las ciudades"
                    hotelAdapter.clearCityFilter();
                } else {
                    hotelAdapter.filterByCity(selectedCity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });
    }

    private void checkAndLoadHotels() {
        Log.d(TAG, "Verificando existencia de hoteles en Firestore...");
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No hay documentos en la colección 'hoteles'. Agregando datos iniciales...");
                            addInitialHotels();
                        } else {
                            Log.d(TAG, "Documentos encontrados en 'hoteles'. Cargando hoteles...");
                            loadHotelsFromFirestore();
                        }
                    } else {
                        Log.e(TAG, "Error al verificar la colección 'hoteles': " + task.getException().getMessage());
                        Toast.makeText(this, "Error al verificar hoteles: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadHotelsFromFirestore() {
        Log.d(TAG, "Iniciando carga de hoteles desde Firestore...");
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Hotel> fetchedHotels = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Hotel hotel = document.toObject(Hotel.class);
                                    hotel.setIdHotel(document.getId());
                                    fetchedHotels.add(hotel);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al procesar documento de hotel " + document.getId() + ": " + e.getMessage(), e);
                                }
                            }

                            hotelAdapter.setHotels(fetchedHotels);
                            Log.d(TAG, "Total de hoteles cargados: " + fetchedHotels.size());

                            if (fetchedHotels.isEmpty()) {
                                Toast.makeText(SuperActivity.this, "No se encontraron hoteles en Firestore.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error al obtener documentos de hoteles: " + task.getException().getMessage(), task.getException());
                            Toast.makeText(SuperActivity.this, "Error al cargar hoteles: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void addInitialHotels() {
        // ... (mantén tu método addInitialHotels existente sin cambios) ...
    }

    private void setupSearch() {
        etSearchHotel = findViewById(R.id.etSearchHotel);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        etSearchHotel.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                hotelAdapter.filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> {
            // Limpiar el texto de búsqueda
            etSearchHotel.setText("");

            // Restablecer el filtro de ciudad a "Todas las ciudades"
            spinnerCityFilter.setSelection(0);

            // Limpiar todos los filtros en el adaptador
            hotelAdapter.clearAllFilters();
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(SuperActivity.this, SuperUsuariosActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperActivity.this, SuperEventosActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHotelsFromFirestore();
    }
}