package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    private List<Evento> listaEventos;
    private List<Evento> filteredEventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_eventos);

        etFiltroFecha = findViewById(R.id.etFiltroFecha);
        btnLimpiarFiltroFecha = findViewById(R.id.btnLimpiarFiltroFecha);

        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiarGeneralSearch = findViewById(R.id.btnLimpiar);

        recyclerEventos = findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(this));

        CardView cardSuper = findViewById(R.id.cardSuper);

        listaEventos = new ArrayList<>();
        // Updated with more detailed and creative descriptions
        listaEventos.add(new Evento("15/5/2025", "Corte de energía en la torre A", "Oro Verde",
                "**Detalle:** A las 23:00, la torre A del Hotel Oro Verde experimentó un corte total de energía, afectando todas las habitaciones del piso 5 al 10, así como los ascensores y el sistema de climatización. La causa preliminar apunta a una sobrecarga en el transformador principal de la torre. El generador de emergencia se activó parcialmente, pero no logró suplir toda la demanda. \n\n**Impacto:** Huéspedes en los pisos afectados expresaron incomodidad y algunos tuvieron que ser reubicados. La recepción se vio colapsada por llamadas. \n\n**Acciones:** El equipo técnico ya está en el lugar evaluando la magnitud del daño y se ha solicitado apoyo de una empresa externa especializada para agilizar la reparación. Se estima un tiempo de resolución de 4-6 horas. Se ofreció compensación (desayuno gratuito/descuento) a los afectados."));
        listaEventos.add(new Evento("11/5/2025", "Caída de objeto en pasillo del restaurante", "Las Dunas",
                "**Detalle:** Aproximadamente a las 14:30, un gran jarrón decorativo de cerámica, que adornaba el pasillo principal que conduce al restaurante 'El Oasis' del Hotel Las Dunas, se desprendió de su base y cayó al suelo. El impacto provocó la fragmentación del jarrón en múltiples piezas y daños menores en el revestimiento del piso. \n\n**Impacto:** Aunque no hubo heridos, el incidente causó un gran estruendo y asustó a varios huéspedes y personal que se encontraban cerca. El área fue acordonada inmediatamente por seguridad. \n\n**Acciones:** El equipo de limpieza procedió a retirar los escombros y se inició una investigación para determinar la causa del desprendimiento. Se revisarán todos los elementos decorativos colgantes o inestables en el hotel. La gerencia ha dispuesto la instalación de nuevas medidas de sujeción para futuros adornos."));
        listaEventos.add(new Evento("29/4/2025", "Fuga de agua en cuarto 210", "Costa del Mar",
                "**Detalle:** A las 08:15, el personal de limpieza del Hotel Costa del Mar descubrió una considerable fuga de agua en el baño del cuarto 210. La fuente de la fuga se identificó como una tubería corroída detrás del lavamanos, lo que ha provocado una acumulación de agua debajo del mueble y una filtración visible en la alfombra de la habitación. \n\n**Impacto:** La habitación fue declarada inoperable y el huésped fue trasladado a una habitación superior de cortesía. Existe un riesgo potencial de daño estructural a la pared y al piso si no se repara pronto. \n\n**Acciones:** El equipo de mantenimiento cerró la llave de paso de agua de la habitación y procedió a demoler parcialmente la pared para acceder a la tubería. Se espera que la reparación tome al menos 24 horas. Se ha programado la limpieza profunda y secado de la alfombra."));
        listaEventos.add(new Evento("10/5/2025", "Incendio menor en cocina", "Sauce Resort",
                "**Detalle:** A las 05:45 de la mañana, un conato de incendio se registró en el área de la freidora de la cocina principal del Hotel Sauce Resort. La causa se atribuye a un sobrecalentamiento del aceite que provocó llamas. El sistema de rociadores automáticos se activó de inmediato, conteniendo el fuego y evitando su propagación. \n\n**Impacto:** Aunque el incendio fue menor, generó una gran cantidad de humo, lo que activó las alarmas de incendios en todo el hotel por precaución. No hubo heridos. El personal de cocina fue evacuado momentáneamente. \n\n**Acciones:** Los bomberos llegaron rápidamente para verificar la situación y ventilar el área. Se realizó una inspección de seguridad en todo el sistema de cocina y se reforzaron los protocolos de uso de freidoras con el personal. La cocina reabrió a las 08:00 tras una limpieza exhaustiva."));
        listaEventos.add(new Evento("15/5/2025", "Problemas de internet en el lobby", "Oro Verde",
                "**Detalle:** Desde las 10:00 AM, el Hotel Oro Verde ha estado experimentando interrupciones intermitentes y una baja velocidad en el servicio de internet Wi-Fi en el área del lobby y salones de conferencias adyacentes. Se observa que la señal se pierde y recupera constantemente. \n\n**Impacto:** Esto está afectando a los huéspedes que intentan trabajar o comunicarse, causando quejas en recepción, especialmente de viajeros de negocios. Las tablets de check-in/out también se vieron ralentizadas. \n\n**Acciones:** El departamento de TI ha reiniciado los routers principales varias veces sin éxito. Se ha contactado al proveedor de servicios de internet, que ha confirmado una incidencia regional. Se espera que un técnico de la operadora visite el hotel en las próximas 3 horas para diagnosticar y resolver el problema. Se han habilitado puntos de acceso alternativos con menor capacidad para necesidades urgentes."));


        filteredEventList = new ArrayList<>(listaEventos);

        adapter = new EventoAdapter(this, filteredEventList, evento -> SuperDetalleEvento(evento));
        recyclerEventos.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_eventos);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                startActivity(new Intent(this, SuperActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                startActivity(new Intent(this, SuperUsuariosActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
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

        for (Evento evento : listaEventos) {
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
        intent.putExtra("event_descripcion", evento.getDescripcion()); // Pass the detailed description
        startActivity(intent);
    }

}