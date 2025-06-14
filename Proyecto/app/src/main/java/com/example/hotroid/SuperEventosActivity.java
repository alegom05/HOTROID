package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etFiltroFecha;
    private EditText etBuscador;
    private Button btnLimpiarGeneralSearch;
    private Button btnLimpiarFiltroFecha;
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos;
    private List<Evento> filteredEventList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        db = FirebaseFirestore.getInstance();

        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        btnLimpiarFiltroFecha = findViewById(R.id.btnLimpiarFiltroFecha);

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiarGeneralSearch = findViewById(R.id.btnLimpiar);

        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        listaEventos = new ArrayList<>();
        filteredEventList = new ArrayList<>();

        // Inicializar el adaptador con la lista vacía al principio
        // Los datos se cargarán desde Firestore y luego se actualizará el adaptador
        adapter = new EventoAdapter(this, filteredEventList, this::SuperDetalleEvento);
        recyclerEventos.setAdapter(adapter);

        // Puedes descomentar la siguiente línea si necesitas guardar eventos iniciales por primera vez.
        // Después de la primera ejecución exitosa, vuelve a comentarla para evitar duplicados.
        // saveInitialEventsToFirestore();

        // Cargar eventos desde Firestore al iniciar la actividad
        loadEventsFromFirestore();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                overridePendingTransition(0, 0); // Evitar animacion
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                overridePendingTransition(0, 0); // Evitar animacion
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                return true; // Ya estás en esta actividad
            }
            return false;
        });

        etFiltroFecha.setOnClickListener(v -> mostrarDatePicker());

        btnLimpiarFiltroFecha.setOnClickListener(v -> {
            etFiltroFecha.setText("");
            applyFilters();
        });

        setupGeneralSearchFunctionality();

        cardSuper.setOnClickListener(v -> {
            startActivity(new Intent(this, SuperCuentaActivity.class));
        });
    }

    // --- COMENTADO: Este método es solo para la primera carga de datos a Firestore.
    // Una vez que tus datos estén en Firestore, puedes mantener este método comentado. ---
    private void saveInitialEventsToFirestore() {
        List<Evento> initialEvents = new ArrayList<>();

        // --- EVENTOS INICIALES (DESCOMENTAR SOLO PARA LA PRIMERA VEZ QUE QUIERAS POBLAR FIRESTORE) ---
        // Descomentar y ejecutar la app una vez, luego volver a comentar.
        /*
        initialEvents.add(new Evento("14/06/2025", "Conferencia Tech Annual", "Hotel Aranwa Paracas", "Una conferencia anual de tecnología con ponentes internacionales."));
        initialEvents.add(new Evento("20/06/2025", "Festival Gastronómico", "Hotel Decameron", "Un festival para degustar la gastronomía local e internacional."));
        initialEvents.add(new Evento("01/07/2025", "Seminario de Marketing Digital", "Boca Ratón", "Aprende las últimas estrategias de marketing digital."));
        initialEvents.add(new Evento("10/07/2025", "Concierto de Jazz", "Libertador", "Una noche inolvidable con los mejores artistas de jazz."));
        initialEvents.add(new Evento("15/07/2025", "Exposición de Arte Moderno", "Costa del Sol", "Descubre las obras de artistas contemporáneos."));
        initialEvents.add(new Evento("25/07/2025", "Competencia de Natación", "Sonesta", "Evento deportivo con participación de clubes locales."));
        initialEvents.add(new Evento("05/08/2025", "Taller de Fotografía", "Hotel Aranwa Paracas", "Aprende técnicas avanzadas de fotografía con profesionales."));
        initialEvents.add(new Evento("12/08/2025", "Noche de Talentos", "Hotel Decameron", "Presentaciones de canto, baile y comedia por talentos emergentes."));
        initialEvents.add(new Evento("20/08/2025", "Degustación de Vinos", "Boca Ratón", "Experimenta los sabores de los mejores viñedos de la región."));
        initialEvents.add(new Evento("01/09/2025", "Clase Maestra de Cocina Peruana", "Libertador", "Aprende a preparar platos tradicionales peruanos con chefs expertos."));
        initialEvents.add(new Evento("10/09/2025", "Feria de Libros y Autores", "Costa del Sol", "Encuentra tus próximos libros favoritos y conoce a sus creadores."));
        initialEvents.add(new Evento("18/09/2025", "Maratón de Videojuegos", "Sonesta", "Compite en tus videojuegos favoritos y gana premios."));
        initialEvents.add(new Evento("25/09/2025", "Show de Magia e Ilusionismo", "Hotel Aranwa Paracas", "Una noche llena de asombro y misterio."));
        initialEvents.add(new Evento("03/10/2025", "Festival de Cerveza Artesanal", "Hotel Decameron", "Descubre una variedad de cervezas artesanales locales e internacionales."));
        initialEvents.add(new Evento("12/10/2025", "Día de la Familia con Juegos", "Boca Ratón", "Actividades y juegos para todas las edades."));
        initialEvents.add(new Evento("20/10/2025", "Torneo de Ajedrez", "Libertador", "Pon a prueba tu estrategia en un torneo de ajedrez competitivo."));
        initialEvents.add(new Evento("28/10/2025", "Noche de Disfraces (Halloween)", "Costa del Sol", "Celebra Halloween con música, concursos de disfraces y diversión."));
        initialEvents.add(new Evento("05/11/2025", "Conferencia de Desarrollo Personal", "Sonesta", "Motívate y aprende herramientas para tu crecimiento personal."));
        initialEvents.add(new Evento("15/11/2025", "Mercado Navideño Anticipado", "Hotel Aranwa Paracas", "Encuentra regalos únicos y disfruta del ambiente festivo."));
        initialEvents.add(new Evento("22/11/2025", "Competencia de Baile Urbano", "Hotel Decameron", "Demuestra tus mejores pasos y compite por el primer lugar."));
        initialEvents.add(new Evento("01/12/2025", "Cena de Gala de Fin de Año", "Boca Ratón", "Despide el año con una cena elegante y música en vivo."));
        initialEvents.add(new Evento("10/12/2025", "Lectura de Cuentos Infantiles", "Libertador", "Un evento mágico para los más pequeños con cuentacuentos."));
        initialEvents.add(new Evento("18/12/2025", "Concierto de Villancicos", "Costa del Sol", "Disfruta de la música navideña con coros locales."));
        initialEvents.add(new Evento("24/12/2025", "Cena de Nochebuena Especial", "Sonesta", "Una cena festiva para celebrar la Nochebuena en un ambiente acogedor."));
        initialEvents.add(new Evento("31/12/2025", "Fiesta de Año Nuevo", "Hotel Aranwa Paracas", "Celebra la llegada del nuevo año con una gran fiesta y fuegos artificiales."));
        initialEvents.add(new Evento("05/01/2026", "Torneo de Tenis de Mesa", "Hotel Decameron", "Demuestra tus habilidades en este divertido torneo."));
        initialEvents.add(new Evento("12/01/2026", "Clase de Yoga al Aire Libre", "Boca Ratón", "Relájate y recarga energías con una sesión de yoga."));
        initialEvents.add(new Evento("20/01/2026", "Noche de Karaoke", "Libertador", "Libera tu estrella interior y canta tus canciones favoritas."));
        initialEvents.add(new Evento("28/01/2026", "Taller de Coctelería", "Costa del Sol", "Aprende a preparar deliciosos cócteles con bartenders expertos."));
        initialEvents.add(new Evento("05/02/2026", "Desfile de Moda Sostenible", "Sonesta", "Descubre las últimas tendencias en moda ética y sostenible."));
        initialEvents.add(new Evento("14/02/2026", "Cena Romántica de San Valentín", "Hotel Aranwa Paracas", "Celebra el amor con una cena especial y ambiente romántico."));
        initialEvents.add(new Evento("22/02/2026", "Carnaval de Verano", "Hotel Decameron", "Disfruta de un colorido carnaval con música y bailes."));
        initialEvents.add(new Evento("01/03/2026", "Conferencia sobre Historia Local", "Boca Ratón", "Explora la rica historia y cultura de la región."));
        initialEvents.add(new Evento("10/03/2026", "Festival de Cortometrajes", "Libertador", "Disfruta de una selección de cortometrajes independientes."));
        initialEvents.add(new Evento("18/03/2026", "Taller de Jardinería Urbana", "Costa del Sol", "Aprende a crear tu propio oasis verde en la ciudad."));
        initialEvents.add(new Evento("25/03/2026", "Noche de Observación de Estrellas", "Sonesta", "Admira el cielo nocturno y aprende sobre astronomía."));
        initialEvents.add(new Evento("01/04/2026", "Día de Bromas y Risas (April Fools)", "Hotel Aranwa Paracas", "Un día para la diversión y las sorpresas."));
        initialEvents.add(new Evento("10/04/2026", "Retiro de Bienestar y Mindfulness", "Hotel Decameron", "Encuentra la paz interior y relájate en este retiro."));
        initialEvents.add(new Evento("18/04/2026", "Feria de Artesanías Locales", "Boca Ratón", "Apoya a los artesanos locales y encuentra piezas únicas."));
        initialEvents.add(new Evento("25/04/2026", "Concurso de Talentos para Niños", "Libertador", "Los más pequeños demuestran sus habilidades en un show divertido."));
        initialEvents.add(new Evento("01/05/2026", "Celebración del Día del Trabajo", "Costa del Sol", "Eventos especiales para conmemorar el Día del Trabajo."));
        initialEvents.add(new Evento("10/05/2026", "Brunch Especial Día de la Madre", "Sonesta", "Un brunch delicioso para celebrar a mamá."));
        */
        // --- FIN DE EVENTOS INICIALES ---

        // Puedes añadir aquí algunos eventos de prueba sin comentar si quieres ver algo al inicio
        // y aún no has configurado tu base de datos de Firestore.
        // Después de implementar Firestore, asegúrate de que estos también provengan de allí.
        initialEvents.add(new Evento("14/06/2025", "Conferencia de Verano", "Hotel Aranwa", "Un evento para desarrolladores y profesionales de TI."));
        initialEvents.add(new Evento("20/06/2025", "Concierto Acústico Noche de Luna", "Hotel Decameron", "Disfruta de la música en vivo bajo las estrellas."));
        initialEvents.add(new Evento("05/07/2025", "Taller de Cocina Peruana Fusión", "Boca Ratón", "Aprende a preparar platos innovadores con un toque peruano."));


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
        db.collection("eventos")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            listaEventos.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Evento evento = document.toObject(Evento.class);
                                // Si tu clase Evento tiene un campo 'id' y lo mapeas desde el DocumentId
                                // evento.setId(document.getId()); // Descomentar si aplicable
                                listaEventos.add(evento);
                            }
                            Log.d("Firestore", "Eventos cargados exitosamente: " + listaEventos.size());
                            applyFilters(); // Aplicar filtros para mostrar los datos cargados
                        } else {
                            Log.w("Firestore", "Error al obtener documentos: ", task.getException());
                            Toast.makeText(SuperEventosActivity.this, "Error al cargar eventos: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupGeneralSearchFunctionality() {
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLimpiarGeneralSearch.setOnClickListener(v -> {
            etBuscador.setText("");
            etBuscador.clearFocus();
            applyFilters();
        });
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            etFiltroFecha.setText(fechaSeleccionada);
            applyFilters();
        }, año, mes, día);
        datePicker.show();
    }

    private void applyFilters() {
        filteredEventList.clear();
        String fechaFiltro = etFiltroFecha.getText().toString().trim();
        String searchText = etBuscador.getText().toString().toLowerCase(Locale.getDefault()).trim();

        for (Evento evento : listaEventos) {
            boolean matchesDate = fechaFiltro.isEmpty() || evento.getFecha().equals(fechaFiltro);

            String combinedTextForSearch = (evento.getEvento() + " " + evento.getHotel() + " " + evento.getDescripcion()).toLowerCase(Locale.getDefault());

            boolean matchesSearch = searchText.isEmpty() || combinedTextForSearch.contains(searchText);

            if (matchesDate && matchesSearch) {
                filteredEventList.add(evento);
            }
        }
        adapter.actualizarLista(filteredEventList);

        if (filteredEventList.isEmpty() && (!fechaFiltro.isEmpty() || !searchText.isEmpty())) {
            Toast.makeText(this, "No hay eventos que coincidan con los filtros", Toast.LENGTH_SHORT).show();
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