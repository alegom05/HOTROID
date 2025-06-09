package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // Importar Log para depuración
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull; // Para anotaciones de no-nulo
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Evento;
import com.google.android.gms.tasks.OnCompleteListener; // Importar para OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener; // Importar para OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener; // Importar para OnSuccessListener
import com.google.android.gms.tasks.Task; // Importar para Task
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore; // Importar FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot; // Importar QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot; // Importar QuerySnapshot

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SuperEventosActivity extends AppCompatActivity {

    private EditText etFiltroFecha;
    private EditText etBuscador;
    private Button btnLimpiarGeneralSearch;
    private Button btnLimpiarFiltroFecha;
    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos; // Esta lista ahora se llenará desde Firestore
    private List<Evento> filteredEventList;

    // Declarar instancia de FirebaseFirestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        btnLimpiarFiltroFecha = findViewById(R.id.btnLimpiarFiltroFecha);

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiarGeneralSearch = findViewById(R.id.btnLimpiar); // Asumiendo que este es el ID del botón limpiar general

        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        listaEventos = new ArrayList<>();
        filteredEventList = new ArrayList<>();

        // 1. Guardar eventos iniciales a Firestore (Ejecutar solo una vez para poblar la BD)
        // Puedes comentar o eliminar esta llamada después de la primera ejecución exitosa
        saveInitialEventsToFirestore();

        // 2. Leer eventos desde Firestore al iniciar la actividad
        loadEventsFromFirestore();

        // Inicializar el adaptador con la lista vacía o con los datos cargados (que se actualizarán después de la carga de Firestore)
        adapter = new EventoAdapter(this, filteredEventList, this::SuperDetalleEvento); // Usando referencia a método
        recyclerEventos.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Obtener el ID del elemento seleccionado
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                return true;
            } else if (itemId == R.id.nav_eventos) {
                // Ya estamos en SuperEventosActivity, no es necesario iniciarla de nuevo.
                // Si la idea es "refrescar", podrías recargar los eventos de Firestore.
                // startActivity(new Intent(SuperEventosActivity.this, SuperEventosActivity.class));
                return true;
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

    // Método para guardar la lista inicial de eventos en Firestore
    private void saveInitialEventsToFirestore() {
        List<Evento> initialEvents = new ArrayList<>();
        initialEvents.add(new Evento("15/5/2025", "Corte de energía en la torre A", "Oro Verde",
                "**Detalle:** A las 23:00, la torre A del Hotel Oro Verde experimentó un corte total de energía, afectando todas las habitaciones del piso 5 al 10, así como los ascensores y el sistema de climatización. La causa preliminar apunta a una sobrecarga en el transformador principal de la torre. El generador de emergencia se activó parcialmente, pero no logró suplir toda la demanda. \n\n**Impacto:** Huéspedes en los pisos afectados expresaron incomodidad y algunos tuvieron que ser reubicados. La recepción se vio colapsada por llamadas. \n\n**Acciones:** El equipo técnico ya está en el lugar evaluando la magnitud del daño y se ha solicitado apoyo de una empresa externa especializada para agilizar la reparación. Se estima un tiempo de resolución de 4-6 horas. Se ofreció compensación (desayuno gratuito/descuento) a los afectados."));
        initialEvents.add(new Evento("11/5/2025", "Caída de objeto en pasillo del restaurante", "Las Dunas",
                "**Detalle:** Aproximadamente a las 14:30, un gran jarrón decorativo de cerámica, que adornaba el pasillo principal que conduce al restaurante 'El Oasis' del Hotel Las Dunas, se desprendió de su base y cayó al suelo. El impacto provocó la fragmentación del jarrón en múltiples piezas y daños menores en el revestimiento del piso. \n\n**Impacto:** Aunque no hubo heridos, el incidente causó un gran estruendo y asustó a varios huéspedes y personal que se encontraban cerca. El área fue acordonada inmediatamente por seguridad. \n\n**Acciones:** El equipo de limpieza procedió a retirar los escombros y se inició una investigación para determinar la causa del desprendimiento. Se revisarán todos los elementos decorativos colgantes o inestables en el hotel. La gerencia ha dispuesto la instalación de nuevas medidas de sujeción para futuros adornos."));
        initialEvents.add(new Evento("29/4/2025", "Fuga de agua en cuarto 210", "Costa del Mar",
                "**Detalle:** A las 08:15, el personal de limpieza del Hotel Costa del Mar descubrió una considerable fuga de agua en el baño del cuarto 210. La fuente de la fuga se identificó como una tubería corroída detrás del lavamanos, lo que ha provocado una acumulación de agua debajo del mueble y una filtración visible en la alfombra de la habitación. \n\n**Impacto:** La habitación fue declarada inoperable y el huésped fue trasladado a una habitación superior de cortesía. Existe un riesgo potencial de daño estructural a la pared y al piso si no se repara pronto. \n\n**Acciones:** El equipo de mantenimiento cerró la llave de paso de agua de la habitación y procedió a demoler parcialmente la pared para acceder a la tubería. Se espera que la reparación tome al menos 24 horas. Se ha programado la limpieza profunda y secado de la alfombra."));
        initialEvents.add(new Evento("10/5/2025", "Incendio menor en cocina", "Sauce Resort",
                "**Detalle:** A las 05:45 de la mañana, un conato de incendio se registró en el área de la freidora de la cocina principal del Hotel Sauce Resort. La causa se atribuye a un sobrecalentamiento del aceite que provocó llamas. El sistema de rociadores automáticos se activó de inmediato, conteniendo el fuego y evitando su propagación. \n\n**Impacto:** Aunque el incendio fue menor, generó una gran cantidad de humo, lo que activó las alarmas de incendios en todo el hotel por precaución. No hubo heridos. El personal de cocina fue evacuado momentáneamente. \n\n**Acciones:** Los bomberos llegaron rápidamente para verificar la situación y ventilar el área. Se realizó una inspección de seguridad en todo el sistema de cocina y se reforzaron los protocolos de uso de freidoras con el personal. La cocina reabrió a las 08:00 tras una limpieza exhaustiva."));
        initialEvents.add(new Evento("15/5/2025", "Problemas de internet en el lobby", "Oro Verde",
                "**Detalle:** Desde las 10:00 AM, el Hotel Oro Verde ha estado experimentando interrupciones intermitentes y una baja velocidad en el servicio de internet Wi-Fi en el área del lobby y salones de conferencias adyacentes. Se observa que la señal se pierde y recupera constantemente. \n\n**Impacto:** Esto está afectando a los huéspedes que intentan trabajar o comunicarse, causando quejas en recepción, especialmente de viajeros de negocios. Las tablets de check-in/out también se vieron ralentizadas. \n\n**Acciones:** El departamento de TI ha reiniciado los routers principales varias veces sin éxito. Se ha contactado al proveedor de servicios de internet, que ha confirmado una incidencia regional. Se espera que un técnico de la operadora visite el hotel en las próximas 3 horas para diagnosticar y resolver el problema. Se han habilitado puntos de acceso alternativos con menor capacidad para necesidades urgentes."));

        for (Evento evento : initialEvents) {
            db.collection("eventos") // Nombre de tu colección en Firestore
                    .add(evento) // Usar .add() para que Firestore genere un ID de documento automáticamente
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Evento guardado con ID: " + documentReference.getId());
                        // Opcional: puedes cargar los eventos inmediatamente después de guardar el último
                        // if (initialEvents.indexOf(evento) == initialEvents.size() - 1) {
                        //     loadEventsFromFirestore();
                        // }
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error al guardar evento", e);
                        Toast.makeText(SuperEventosActivity.this, "Error al guardar un evento: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para cargar eventos desde Firestore
    private void loadEventsFromFirestore() {
        db.collection("eventos") // Nombre de tu colección en Firestore
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            listaEventos.clear(); // Limpiar la lista existente
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Evento evento = document.toObject(Evento.class); // Mapear el documento a un objeto Evento
                                // Si usas @DocumentId en tu clase Evento y quieres el ID del documento
                                // evento.setId(document.getId());
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
            String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
            etFiltroFecha.setText(fechaSeleccionada);
            applyFilters();
        }, año, mes, día);
        datePicker.show();
    }

    private void applyFilters() {
        filteredEventList.clear();
        String fechaFiltro = etFiltroFecha.getText().toString().trim();
        String searchText = etBuscador.getText().toString().toLowerCase().trim();

        for (Evento evento : listaEventos) { // Ahora listaEventos contiene datos de Firestore
            boolean matchesDate = fechaFiltro.isEmpty() || evento.getFecha().equals(fechaFiltro);

            // Search in event description, hotel name, AND detailed description
            String combinedTextForSearch = (evento.getEvento() + " " + evento.getHotel() + " " + evento.getDescripcion()).toLowerCase();

            boolean matchesSearch = searchText.isEmpty() ||
                    combinedTextForSearch.contains(searchText);


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