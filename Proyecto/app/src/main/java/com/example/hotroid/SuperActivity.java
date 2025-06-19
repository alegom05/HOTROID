package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

        setupSearch();
        setupBottomNavigation();
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

                                    if (hotel.getImageName() != null && !hotel.getImageName().isEmpty()) {
                                        int resId = getResources().getIdentifier(
                                                hotel.getImageName().toLowerCase(Locale.getDefault()),
                                                "drawable",
                                                getPackageName()
                                        );
                                        hotel.setImageResourceId(resId != 0 ? resId : R.drawable.placeholder_hotel);
                                    } else {
                                        hotel.setImageResourceId(R.drawable.placeholder_hotel);
                                    }

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
        Log.d(TAG, "Agregando los 7 hoteles iniciales...");

        List<Hotel> initialHotels = new ArrayList<>();

        // Hotel 1: Boca Raton
        Hotel bocaRaton = new Hotel();
        bocaRaton.setName("Boca Raton");
        bocaRaton.setDireccion("Tarapoto, San Martín");
        bocaRaton.setDireccionDetallada("Jr. San Pablo de la Cruz 273");
        bocaRaton.setPrice(150.00);
        bocaRaton.setRating(3.8f);
        bocaRaton.setDescription("El Hotel Boca Raton te ofrece una experiencia única en Tarapoto, con habitaciones confortables y un ambiente acogedor, ideal para explorar la selva peruana.");
        bocaRaton.setImageName("hotel_boca_raton");
        initialHotels.add(bocaRaton);

        // Hotel 2: Oro Verde
        Hotel oroVerde = new Hotel();
        oroVerde.setName("Oro Verde");
        oroVerde.setDireccion("Iquitos, Loreto");
        oroVerde.setDireccionDetallada("Av. Abelardo Quiñones Km 2.8");
        oroVerde.setPrice(450.00);
        oroVerde.setRating(4.2f);
        oroVerde.setDescription("Descubre la Amazonía desde el Hotel Oro Verde en Iquitos. Ofrece comodidades modernas y acceso fácil a las maravillas naturales de la selva.");
        oroVerde.setImageName("hotel_oro_verde");
        initialHotels.add(oroVerde);

        // Hotel 3: Libertador
        Hotel libertador = new Hotel();
        libertador.setName("Libertador");
        libertador.setDireccion("Machu Picchu, Cusco");
        libertador.setDireccionDetallada("Jr. Waynapicchu s/n");
        libertador.setPrice(567.50);
        libertador.setRating(4.7f);
        libertador.setDescription("Situado en el corazón de Machu Picchu, el Hotel Libertador ofrece vistas espectaculares y un servicio excepcional, ideal para tu aventura inca.");
        libertador.setImageName("hotel_libertador");
        initialHotels.add(libertador);

        // Hotel 4: Sonesta
        Hotel sonesta = new Hotel();
        sonesta.setName("Sonesta");
        sonesta.setDireccion("Arequipa");
        sonesta.setDireccionDetallada("Urb. Santa Rosa G-10");
        sonesta.setPrice(231.80);
        sonesta.setRating(3.9f);
        sonesta.setDescription("El Hotel Sonesta en Arequipa combina la elegancia colonial con el confort moderno, perfecto para explorar la Ciudad Blanca.");
        sonesta.setImageName("hotel_sonesta");
        initialHotels.add(sonesta);

        // Hotel 5: Decameron
        Hotel decameron = new Hotel();
        decameron.setName("Decameron");
        decameron.setDireccion("Punta Sal, Piura");
        decameron.setDireccionDetallada("Km 1192 Panamericana Norte");
        decameron.setPrice(989.90);
        decameron.setRating(4.0f);
        decameron.setDescription("Disfruta de unas vacaciones todo incluido en el Hotel Decameron Punta Sal. Playas paradisíacas y actividades para toda la familia.");
        decameron.setImageName("hotel_decameron");
        initialHotels.add(decameron);

        // Hotel 6: Aranwa
        Hotel aranwa = new Hotel();
        aranwa.setName("Aranwa");
        aranwa.setDireccion("Paracas, Ica");
        aranwa.setDireccionDetallada("Av. Paracas Lote F");
        aranwa.setPrice(756.40);
        aranwa.setRating(4.5f);
        aranwa.setDescription("El Hotel Aranwa en Paracas ofrece un oasis de tranquilidad con vistas al mar, ideal para relajarse y explorar la Reserva de Paracas.");
        aranwa.setImageName("hotel_aranwa");
        initialHotels.add(aranwa);

        // Hotel 7: Costa del Sol
        Hotel costaDelSol = new Hotel();
        costaDelSol.setName("Costa del Sol");
        costaDelSol.setDireccion("Trujillo, La Libertad");
        costaDelSol.setDireccionDetallada("Av. Mansiche 790");
        costaDelSol.setPrice(546.80);
        costaDelSol.setRating(4.1f);
        costaDelSol.setDescription("El Hotel Costa del Sol en Trujillo es el punto de partida perfecto para descubrir la cultura Moche y Chimú, con confort y excelentes servicios.");
        costaDelSol.setImageName("hotel_costa_sol");
        initialHotels.add(costaDelSol);

        CollectionReference hotelesRef = db.collection("hoteles");
        int[] hotelsAddedCount = {0};
        for (Hotel hotel : initialHotels) {
            hotelesRef.add(hotel)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Hotel agregado a Firestore: " + hotel.getName());
                            hotelsAddedCount[0]++;
                            if (hotelsAddedCount[0] == initialHotels.size()) {
                                Log.d(TAG, "Todos los hoteles iniciales han sido agregados. Cargando desde Firestore...");
                                new Handler().postDelayed(SuperActivity.this::loadHotelsFromFirestore, 1000);
                            }
                        } else {
                            Log.e(TAG, "Error al agregar hotel " + hotel.getName() + ": " + task.getException().getMessage(), task.getException());
                        }
                    });
        }
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
            etSearchHotel.setText("");
            hotelAdapter.clearFilter();
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