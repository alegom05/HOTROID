package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Make sure this import is present
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

import com.example.hotroid.bean.Evento;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etBuscador;
    private Button btnLimpiar; // Renamed for clarity in Java
    private Button btnPickStartDate, btnPickEndDate;
    private Spinner spinnerHotels;
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos;
    private List<Evento> filteredEventList;

    private FirebaseFirestore db;
    private Date selectedStartDate;
    private Date selectedEndDate;
    private String selectedHotel; // This will hold the selected hotel, or null if "Seleccionar Hotel" or "Limpiar" is used.
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        Log.d("SuperEventosActivity", "onCreate: Activity started");

        db = FirebaseFirestore.getInstance();

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnFiltrar); // Still references R.id.btnFiltrar from XML
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);
        spinnerHotels = findViewById(R.id.spinnerHotels);

        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        listaEventos = new ArrayList<>();
        filteredEventList = new ArrayList<>();

        adapter = new EventoAdapter(this, filteredEventList, this::SuperDetalleEvento);
        recyclerEventos.setAdapter(adapter);
        Log.d("SuperEventosActivity", "RecyclerView adapter set.");


        // --- COMENTADO: Descomenta la siguiente línea solo si necesitas guardar eventos iniciales por primera vez.
        // Después de la primera ejecución exitosa, vuelve a comentarla para evitar duplicados en Firestore. ---
        // saveInitialEventsToFirestore(); // ONLY RUN ONCE TO POPULATE FIRESTORE!

        loadEventsFromFirestore(); // This is where data loading begins

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                return true;
            }
            return false;
        });

        setupFilterFunctionality(); // This now handles the "Limpiar" button
        setupDatePickers();
        setupHotelSpinner(); // This will set selectedHotel to "Seleccionar Hotel" initially

        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
        });
    }

    private void saveInitialEventsToFirestore() {
        Log.d("SuperEventosActivity", "Attempting to save initial events to Firestore...");
        List<Evento> initialEvents = new ArrayList<>();

        // It's CRITICAL that the hotel names here EXACTLY match the ones in R.array.hotel_options
        // "Hotel Oro Verde", "Hotel Decameron", "Hotel Libertador", "Hotel Costa del Sol",
        // "Hotel Sonesta", "Sauce Resort", "Boca Ratón", "Aranwa Paracas"
        initialEvents.add(new Evento("14/06/2025", "Conferencia Tech Annual", "Aranwa Paracas", "Una conferencia anual de tecnología con ponentes internacionales."));
        initialEvents.add(new Evento("20/06/2025", "Festival Gastronómico", "Hotel Decameron", "Un festival para degustar la gastronomía local e internacional."));
        initialEvents.add(new Evento("01/07/2025", "Seminario de Marketing Digital", "Boca Ratón", "Aprende las últimas estrategias de marketing digital."));
        initialEvents.add(new Evento("10/07/2025", "Concierto de Jazz", "Hotel Libertador", "Una noche inolvidable con los mejores artistas de jazz."));
        initialEvents.add(new Evento("15/07/2025", "Exposición de Arte Moderno", "Hotel Costa del Sol", "Descubre las obras de artistas contemporáneos."));
        initialEvents.add(new Evento("25/07/2025", "Competencia de Natación", "Hotel Sonesta", "Evento deportivo con participación de clubes locales."));
        initialEvents.add(new Evento("05/08/2025", "Taller de Fotografía", "Aranwa Paracas", "Aprende técnicas avanzadas de fotografía con profesionales."));
        initialEvents.add(new Evento("12/08/2025", "Noche de Talentos", "Hotel Decameron", "Presentaciones de canto, baile y comedia por talentos emergentes."));
        initialEvents.add(new Evento("20/08/2025", "Degustación de Vinos", "Boca Ratón", "Experimenta los sabores de los mejores viñedos de la región."));
        initialEvents.add(new Evento("01/09/2025", "Clase Maestra de Cocina Peruana", "Hotel Libertador", "Aprende a preparar platos tradicionales peruanos con chefs expertos."));
        initialEvents.add(new Evento("10/09/2025", "Feria de Libros y Autores", "Hotel Costa del Sol", "Encuentra tus próximos libros favoritos y conoce a sus creadores."));
        initialEvents.add(new Evento("18/09/2025", "Maratón de Videojuegos", "Hotel Sonesta", "Compite en tus videojuegos favoritos y gana premios."));
        initialEvents.add(new Evento("25/09/2025", "Show de Magia e Ilusionismo", "Aranwa Paracas", "Una noche llena de asombro y misterio."));
        initialEvents.add(new Evento("03/10/2025", "Festival de Cerveza Artesanal", "Hotel Decameron", "Descubre una variedad de cervezas artesanales locales e internacionales."));
        initialEvents.add(new Evento("12/10/2025", "Día de la Familia con Juegos", "Boca Ratón", "Actividades y juegos para todas las edades."));
        initialEvents.add(new Evento("20/10/2025", "Torneo de Ajedrez", "Hotel Libertador", "Pon a prueba tu estrategia en un torneo de ajedrez competitivo."));
        initialEvents.add(new Evento("28/10/2025", "Noche de Disfraces (Halloween)", "Hotel Costa del Sol", "Celebra Halloween con música, concursos de disfraces y diversión."));
        initialEvents.add(new Evento("05/11/2025", "Conferencia de Desarrollo Personal", "Hotel Sonesta", "Motívate y aprende herramientas para tu crecimiento personal."));
        initialEvents.add(new Evento("15/11/2025", "Mercado Navideño Anticipado", "Aranwa Paracas", "Encuentra regalos únicos y disfruta del ambiente festivo."));
        initialEvents.add(new Evento("22/11/2025", "Competencia de Baile Urbano", "Hotel Decameron", "Demuestra tus mejores pasos y compite por el primer lugar."));
        initialEvents.add(new Evento("01/12/2025", "Cena de Gala de Fin de Año", "Boca Ratón", "Despide el año con una cena elegante y música en vivo."));
        initialEvents.add(new Evento("10/12/2025", "Lectura de Cuentos Infantiles", "Hotel Libertador", "Un evento mágico para los más pequeños con cuentacuentos."));
        initialEvents.add(new Evento("18/12/2025", "Concierto de Villancicos", "Hotel Costa del Sol", "Disfruta de la música navideña con coros locales."));
        initialEvents.add(new Evento("24/12/2025", "Cena de Nochebuena Especial", "Hotel Sonesta", "Una cena festiva para celebrar la Nochebuena en un ambiente acogedor."));
        initialEvents.add(new Evento("31/12/2025", "Fiesta de Año Nuevo", "Aranwa Paracas", "Celebra la llegada del nuevo año con una gran fiesta y fuegos artificiales."));
        initialEvents.add(new Evento("05/01/2026", "Torneo de Tenis de Mesa", "Hotel Decameron", "Demuestra tus habilidades en este divertido torneo."));
        initialEvents.add(new Evento("12/01/2026", "Clase de Yoga al Aire Libre", "Boca Ratón", "Relájate y recarga energías con una sesión de yoga."));
        initialEvents.add(new Evento("20/01/2026", "Noche de Karaoke", "Hotel Libertador", "Libera tu estrella interior y canta tus canciones favoritas."));
        initialEvents.add(new Evento("28/01/2026", "Taller de Coctelería", "Hotel Costa del Sol", "Aprende a preparar deliciosos cócteles con bartenders expertos."));
        initialEvents.add(new Evento("05/02/2026", "Desfile de Moda Sostenible", "Hotel Sonesta", "Descubre las últimas tendencias en moda ética y sostenible."));
        initialEvents.add(new Evento("14/02/2026", "Cena Romántica de San Valentín", "Aranwa Paracas", "Celebra el amor con una cena especial y ambiente romántico."));
        initialEvents.add(new Evento("22/02/2026", "Carnaval de Verano", "Hotel Decameron", "Disfruta de un colorido carnaval con música y bailes."));
        initialEvents.add(new Evento("01/03/2026", "Conferencia sobre Historia Local", "Boca Ratón", "Explora la rica historia y cultura de la región."));
        initialEvents.add(new Evento("10/03/2026", "Festival de Cortometrajes", "Hotel Libertador", "Disfruta de una selección de cortometrajes independientes."));
        initialEvents.add(new Evento("18/03/2026", "Taller de Jardinería Urbana", "Hotel Costa del Sol", "Aprende a crear tu propio oasis verde en la ciudad."));
        initialEvents.add(new Evento("25/03/2026", "Noche de Observación de Estrellas", "Hotel Sonesta", "Admira el cielo nocturno y aprende sobre astronomía."));
        initialEvents.add(new Evento("01/04/2026", "Día de Bromas y Risas (April Fools)", "Aranwa Paracas", "Un día para la diversión y las sorpresas."));
        initialEvents.add(new Evento("10/04/2026", "Retiro de Bienestar y Mindfulness", "Hotel Decameron", "Encuentra la paz interior y relájate en este retiro."));
        initialEvents.add(new Evento("18/04/2026", "Feria de Artesanías Locales", "Boca Ratón", "Apoya a los artesanos locales y encuentra piezas únicas."));
        initialEvents.add(new Evento("25/04/2026", "Concurso de Talentos para Niños", "Hotel Libertador", "Los más pequeños demuestran sus habilidades en un show divertido."));
        initialEvents.add(new Evento("01/05/2026", "Celebración del Día del Trabajo", "Hotel Costa del Sol", "Eventos especiales para conmemorar el Día del Trabajo."));
        initialEvents.add(new Evento("10/05/2026", "Brunch Especial Día de la Madre", "Hotel Sonesta", "Un brunch delicioso para celebrar a mamá."));
        initialEvents.add(new Evento("01/06/2025", "Clase de Cocina Molecular", "Hotel Oro Verde", "Experimenta con técnicas de cocina de vanguardia."));
        initialEvents.add(new Evento("08/06/2025", "Noche de Cine Clásico", "Hotel Oro Verde", "Disfruta de películas icónicas en un ambiente retro."));


        for (Evento evento : initialEvents) {
            db.collection("eventos")
                    .add(evento)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Evento guardado con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error al guardar evento", e);
                        Toast.makeText(SuperEventosActivity.this, "Error al guardar un evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadEventsFromFirestore() {
        Log.d("SuperEventosActivity", "loadEventsFromFirestore: Fetching events from Firestore...");
        db.collection("eventos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            listaEventos.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Evento evento = document.toObject(Evento.class);
                                listaEventos.add(evento);
                                Log.d("SuperEventosActivity", "Loaded event: " + evento.getEvento() + " at " + evento.getFecha());
                            }
                            Log.d("SuperEventosActivity", "Events loaded successfully. Total: " + listaEventos.size());
                            applyFilters(); // Apply filters immediately after loading
                        } else {
                            Log.w("SuperEventosActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(SuperEventosActivity.this, "Error al cargar eventos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupFilterFunctionality() {
        // Change the listener for the "Limpiar" button
        btnLimpiar.setOnClickListener(v -> {
            Log.d("SuperEventosActivity", "Limpiar button clicked. Resetting filters.");
            // 1. Clear search text
            etBuscador.setText("");
            // 2. Clear date selections
            selectedStartDate = null;
            selectedEndDate = null;
            btnPickStartDate.setText("Fecha Inicio"); // Reset button text
            btnPickEndDate.setText("Fecha Fin");     // Reset button text
            // 3. Reset hotel spinner to the first item ("Seleccionar Hotel")
            spinnerHotels.setSelection(0);
            // 4. Apply filters (which will now show all events if no filters are active)
            applyFilters();
            Toast.makeText(this, "Filtros limpiados, mostrando todos los eventos.", Toast.LENGTH_SHORT).show();
        });

        // Optional: Apply filters as user types in search bar
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                Log.d("SuperEventosActivity", "Search text changed: " + s.toString());
                applyFilters(); // Apply filters whenever text changes
            }
        });
    }

    private void setupDatePickers() {
        btnPickStartDate.setOnClickListener(v -> showDatePicker(true));
        btnPickEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(final boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year1, monthOfYear, dayOfMonth);
                    if (isStartDate) {
                        selectedCal.set(Calendar.HOUR_OF_DAY, 0);
                        selectedCal.set(Calendar.MINUTE, 0);
                        selectedCal.set(Calendar.SECOND, 0);
                        selectedCal.set(Calendar.MILLISECOND, 0);
                        selectedStartDate = selectedCal.getTime();
                        btnPickStartDate.setText(dateFormat.format(selectedStartDate));
                        Log.d("SuperEventosActivity", "Selected Start Date: " + dateFormat.format(selectedStartDate));
                    } else {
                        selectedCal.set(Calendar.HOUR_OF_DAY, 23);
                        selectedCal.set(Calendar.MINUTE, 59);
                        selectedCal.set(Calendar.SECOND, 59);
                        selectedCal.set(Calendar.MILLISECOND, 999);
                        selectedEndDate = selectedCal.getTime();
                        btnPickEndDate.setText(dateFormat.format(selectedEndDate));
                        Log.d("SuperEventosActivity", "Selected End Date: " + dateFormat.format(selectedEndDate));
                    }
                    applyFilters(); // Apply filters immediately after date selection
                }, year, month, day);

        datePickerDialog.show();
    }

    private void setupHotelSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hotel_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHotels.setAdapter(adapter);

        spinnerHotels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String itemSelected = parent.getItemAtPosition(position).toString();
                Log.d("SuperEventosActivity", "Spinner item selected: " + itemSelected);

                // If "Seleccionar Hotel" is chosen, treat it as no specific hotel filter.
                if (itemSelected.equals(getResources().getString(R.string.seleccione_el_hotel))) { // Using resource string for robustness
                    selectedHotel = null; // No hotel filter
                    Log.d("SuperEventosActivity", "Hotel filter set to: NONE (Seleccionar Hotel)");
                } else {
                    selectedHotel = itemSelected;
                    Log.d("SuperEventosActivity", "Hotel filter set to: " + selectedHotel);
                }
                applyFilters(); // Apply filters whenever spinner selection changes
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedHotel = null; // If nothing is selected, clear the filter
                Log.d("SuperEventosActivity", "No hotel selected, filter cleared.");
                applyFilters();
            }
        });
        // Set initial selection, which will trigger onItemSelected for the first item ("Seleccionar Hotel")
        // This ensures selectedHotel is initialized correctly even if user doesn't interact.
        spinnerHotels.setSelection(0);
    }


    private void applyFilters() {
        filteredEventList.clear();
        String searchText = etBuscador.getText().toString().toLowerCase(Locale.getDefault()).trim();
        Log.d("SuperEventosActivity", "Applying filters: Search='" + searchText + "', Hotel='" + selectedHotel + "', StartDate=" + (selectedStartDate != null ? dateFormat.format(selectedStartDate) : "null") + ", EndDate=" + (selectedEndDate != null ? dateFormat.format(selectedEndDate) : "null"));
        Log.d("SuperEventosActivity", "Total events in listaEventos before filtering: " + listaEventos.size());


        for (Evento evento : listaEventos) {
            boolean matchesSearch = true;
            boolean matchesDateRange = true;
            boolean matchesHotel = true;

            // Filtro de búsqueda general (basado en evento, hotel, descripción)
            if (!searchText.isEmpty()) {
                String description = (evento.getDescripcion() != null) ? evento.getDescripcion() : "";
                String combinedTextForSearch = (evento.getEvento() + " " + evento.getHotel() + " " + description).toLowerCase(Locale.getDefault());
                matchesSearch = combinedTextForSearch.contains(searchText);
                // Log.d("SuperEventosActivity", "Event: " + evento.getEvento() + " matches search: " + matchesSearch);
            }

            // Filtro de rango de fechas
            if (selectedStartDate != null || selectedEndDate != null) {
                try {
                    Date eventDate = dateFormat.parse(evento.getFecha());
                    // Log.d("SuperEventosActivity", "Event date parsed: " + eventDate);

                    if (selectedStartDate != null && selectedEndDate != null) {
                        // Event date must be >= start date AND <= end date
                        matchesDateRange = !eventDate.before(selectedStartDate) && !eventDate.after(selectedEndDate);
                    } else if (selectedStartDate != null) {
                        // Only start date selected: eventDate must be on or after start date
                        matchesDateRange = !eventDate.before(selectedStartDate);
                    } else if (selectedEndDate != null) {
                        // Only end date selected: eventDate must be on or before end date
                        matchesDateRange = !eventDate.after(selectedEndDate);
                    }
                    // Log.d("SuperEventosActivity", "Event: " + evento.getEvento() + " matches date range: " + matchesDateRange);

                } catch (ParseException e) {
                    Log.e("SuperEventosActivity", "Error parsing event date: " + evento.getFecha(), e);
                    matchesDateRange = false; // If date cannot be parsed, it doesn't match
                }
            }

            // Filtro por hotel
            if (selectedHotel != null && !selectedHotel.equals("Seleccionar Hotel")) { // Only apply filter if a specific hotel is selected
                matchesHotel = evento.getHotel().equalsIgnoreCase(selectedHotel);
                // Log.d("SuperEventosActivity", "Event: " + evento.getEvento() + " matches hotel: " + matchesHotel);
            } else {
                matchesHotel = true; // If selectedHotel is null or "Seleccionar Hotel", all hotels match
            }

            if (matchesSearch && matchesDateRange && matchesHotel) {
                filteredEventList.add(evento);
                // Log.d("SuperEventosActivity", "Event ADDED to filteredList: " + evento.getEvento());
            } else {
                // Log.d("SuperEventosActivity", "Event SKIPPED: " + evento.getEvento() + " (Search:" + matchesSearch + ", Date:" + matchesDateRange + ", Hotel:" + matchesHotel + ")");
            }
        }
        adapter.actualizarLista(filteredEventList);
        Log.d("SuperEventosActivity", "Filtered list updated. Size: " + filteredEventList.size());


        // --- Toast messages for user feedback ---
        if (filteredEventList.isEmpty()) {
            boolean hasActiveFilters = !searchText.isEmpty() || selectedStartDate != null || selectedEndDate != null || (selectedHotel != null && !selectedHotel.equals(getResources().getString(R.string.seleccione_el_hotel)));
            if (hasActiveFilters) {
                Log.d("SuperEventosActivity", "No events found with applied filters.");
            } else {
                // This toast will only show if list is empty and no filters are active (meaning, no data in Firestore)
                Toast.makeText(this, "No hay eventos para mostrar (base de datos vacía o error de carga).", Toast.LENGTH_LONG).show();
                Log.d("SuperEventosActivity", "No events found (no filters active, likely empty DB).");
            }
        }
    }

    private void SuperDetalleEvento(Evento evento) {
        Intent intent = new Intent(this, SuperDetallesEventosActivity.class);
        intent.putExtra("event_fecha", evento.getFecha());
        intent.putExtra("event_titulo", evento.getEvento());
        intent.putExtra("event_hotel", evento.getHotel());
        intent.putExtra("event_descripcion", evento.getDescripcion());
        startActivity(intent);
    }
}