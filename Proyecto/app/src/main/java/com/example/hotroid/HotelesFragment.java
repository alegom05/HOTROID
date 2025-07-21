package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.databinding.UserHotelesBinding;
import com.example.hotroid.bean.Hotel;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker.Builder;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.datepicker.CalendarConstraints;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HotelesFragment extends Fragment {

    private static final String TAG = "HotelesFragment";
    private UserHotelesBinding binding;
    private int numHabitaciones = 1;
    private int numPersonas = 1;
    private int valoracionMinima = 2;
    private List<Hotel> hotelList;
    private List<Hotel> hotelesOriginales;
    private HotelAdapter hotelAdapter;
    private FirebaseFirestore db;
    private String destino = "";
    private RecyclerView recyclerView;
    private EditText ciudadInput;
    private int niniosSolicitados = 0;
    private Date fechaInicio;
    private Date fechaFin;
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY = 500; // 500ms de delay para búsqueda

    public HotelesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserHotelesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        hotelList = new ArrayList<>();
        hotelesOriginales = new ArrayList<>();
        hotelAdapter = new HotelAdapter(hotelList, getContext());

        hotelAdapter.setOnHotelClickListener((hotel,precioMinimo) -> {
            Intent intent = new Intent(getContext(), HotelDetalladoUser.class);
            intent.putExtra("hotelId", hotel.getIdHotel());
            intent.putExtra("hotelName", hotel.getName());        // Agregar nombre
            intent.putExtra("hotelRating", hotel.getRating());    // Agregar rating
            intent.putExtra("hotelDireccion", hotel.getDireccion()); // Agregar dirección(ciudad)
            intent.putExtra("precioMinimo", precioMinimo);
            // Pasar también las fechas seleccionadas
            if (fechaInicio != null && fechaFin != null) {
                intent.putExtra("fechaInicio", fechaInicio.getTime());
                intent.putExtra("fechaFin", fechaFin.getTime());
            }
            // Pasar datos de la búsqueda
            intent.putExtra("numHabitaciones", numHabitaciones);
            intent.putExtra("numPersonas", numPersonas);
            intent.putExtra("niniosSolicitados", niniosSolicitados);
            // O cualquier otra información que quieras pasar
//            intent.putExtra("valoracionMinima", valoracionMinima); // Por si lo necesitas
            startActivity(intent);
        });

        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.hotelRecyclerView.setAdapter(hotelAdapter);

        // Inicializar fechas por defecto
        inicializarFechasDefault();
        configurarCalendario();
        configurarSelectorPersonas();
        configurarSelectorValoracion();
        // Mostrar valores iniciales en el campo de habitaciones y personas
        actualizarTextoHabitacionesPersonas();
        actualizarTextoValoracion();

        cargarHoteles();

//        // Buscador en tiempo real
//        binding.searchEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Cancelar búsqueda anterior si existe
//                if (searchRunnable != null) {
//                    searchHandler.removeCallbacks(searchRunnable);
//                }
//                // Crear nueva búsqueda con delay
//                searchRunnable = () -> filtrarHotelesporUbicacion();
//                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });

        // Botón de búsqueda con filtros
        binding.searchButton.setOnClickListener(v -> {
            // Cancelar búsqueda automática si está pendiente
//            if (searchRunnable != null) {
//                searchHandler.removeCallbacks(searchRunnable);
//            }
            mostrarResumenBusqueda();
        });
    }


    // Método para inicializar fechas por defecto (hoy y mañana)
    private void inicializarFechasDefault() {
        Calendar calendar = Calendar.getInstance();
        fechaInicio = calendar.getTime(); // Hoy

        calendar.add(Calendar.DAY_OF_YEAR, 1);
        fechaFin = calendar.getTime(); // Mañana

        // Mostrar las fechas por defecto en el campo
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
        String texto = sdf.format(fechaInicio) + " - " + sdf.format(fechaFin);
        binding.selectedDatesText.setText(texto);
    }
    private void cargarHoteles() {
        // Mostrar indicador de carga si existe
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        db.collection("hoteles").get().addOnCompleteListener(task -> {
            if (binding.progressBar != null) {
                binding.progressBar.setVisibility(View.GONE);
            }
            if (task.isSuccessful()) {
                hotelList.clear();
                hotelesOriginales.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Hotel hotel = doc.toObject(Hotel.class);
                    hotel.setIdHotel(doc.getId());
                    hotelList.add(hotel);
                }
                // Ordenar hoteles por rating descendente y ciudad ascendente
                Collections.sort(hotelList, (h1, h2) -> {
                    int compareRating = Float.compare(h2.getRating(), h1.getRating()); // rating descendente
                    if (compareRating != 0) return compareRating;
                    return h1.getDireccion().compareToIgnoreCase(h2.getDireccion()); // ciudad ascendente
                });
                hotelesOriginales.addAll(hotelList); // Guardar copia original
                hotelAdapter.actualizarLista(hotelList);
//                // Aplicar filtros iniciales si hay algún criterio
//                if (!binding.searchEditText.getText().toString().trim().isEmpty()) {
//                    filtrarHoteles();
//                }else {
//                    // Manejo de errores
//                    Log.e(TAG, "Error al cargar hoteles", task.getException());
//                    // Mostrar mensaje de error al usuario
//                    if (getContext() != null) {
//                        Toast.makeText(getContext(), "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
//                    }
//                }
            }else {
                Log.e(TAG, "Error al cargar hoteles", task.getException());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error al cargar hoteles", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filtrarHoteles() {
        String query = binding.searchEditText.getText().toString().toLowerCase().trim();
        int habitaciones = numHabitaciones;
        int adultos = numPersonas;
        int ninos = niniosSolicitados;
        int valoracionMinimaLocal = valoracionMinima;
        // Si no hay criterios de búsqueda, mostrar todos los hoteles
        if (query.isEmpty() && valoracionMinimaLocal <= 1 &&
                numHabitaciones == 1 && numPersonas == 1 && niniosSolicitados == 0 &&
                fechaInicio!= null && fechaFin!= null) {
            List<Hotel> copia = new ArrayList<>(hotelesOriginales);
            Collections.sort(copia, (h1,h2) ->{
                int cmp = Float.compare(h2.getRating(), h1.getRating()); // Descendente
                if(cmp!=0) return cmp;
                return h1.getDireccion().compareToIgnoreCase(h2.getDireccion()); // Ascendente por ciudad
            });
            hotelAdapter.actualizarLista(copia);
            return;
        }
        // Mostrar indicador de carga durante filtrado
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        // Usar lista original para filtrar
        List<Hotel> hotelesParaFiltrar = new ArrayList<>(hotelesOriginales);

        //manejando con hilos para aliviar carga
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(hotelList.size());
        List<Hotel> hotelesFiltrados = new ArrayList<>();

        for (Hotel hotel : hotelList) {
            executor.execute(() -> {
                try {
                    verificarDisponibilidad(hotel, habitaciones, adultos, ninos, disponible -> {
                        synchronized (hotelesFiltrados) {
                            //                        boolean nombreCoincide = hotel.getName().toLowerCase().contains(query);
                            //                        boolean ciudadCoincide = hotel.getDireccion().toLowerCase().contains(query);
                            //                        boolean valoracionCumple = hotel.getRating() >= valoracionMinimaLocal;
                            boolean nombreCoincide = query.isEmpty() || hotel.getName().toLowerCase().contains(query);
                            boolean ciudadCoincide = query.isEmpty() || hotel.getDireccion().toLowerCase().contains(query);
                            boolean valoracionCumple = hotel.getRating() >= valoracionMinimaLocal;
                            if ((nombreCoincide || ciudadCoincide) && disponible && valoracionCumple
                            ) {
                                hotelesFiltrados.add(hotel);
                            }
                        }
                        latch.countDown();
                    });
                }catch (Exception e){
                    Log.e(TAG, "Error en filtrado de hotel: " + hotel.getName(), e);
                    latch.countDown();
                }
            });

        }

        new Thread(() -> {
            try {
                latch.await();
                requireActivity().runOnUiThread(() -> {
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    hotelAdapter.actualizarLista(hotelesFiltrados);
                    // Mostrar mensaje si no hay resultados
                    if (hotelesFiltrados.isEmpty()) {
                        Toast.makeText(getContext(), "No se encontraron hoteles con los criterios seleccionados", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "Error en filtrado de hoteles", e);
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
            }finally {
                executor.shutdown();
            }
        }).start();
//        hotelAdapter.actualizarLista(hotelesFiltrados);
    }

    // Verifica si el hotel tiene capacidad suficiente (puede incluir verificación con Firestore de habitaciones y fechas)
    private void verificarDisponibilidad(Hotel hotel, int habitaciones, int adultos, int ninos, DisponibilidadCallback callback) {
        // TODO: Aquí puedes conectar la lógica real de disponibilidad usando Firestore
        db.collection("habitaciones")
                .whereEqualTo("idHotel", hotel.getIdHotel())
                .whereEqualTo("status", "Available")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<String> habitacionesCandidatas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        try {
                            Long capacidadAdultosLong = doc.getLong("capacidad.capacityAdults");
                            Long capacidadNinosLong = doc.getLong("capacidad.capacityChildren");

                            if (capacidadAdultosLong != null && capacidadNinosLong != null) {
                                int capacidadAdultos = capacidadAdultosLong.intValue();
                                int capacidadNinos = capacidadNinosLong.intValue();

                                if (capacidadAdultos >= adultos && capacidadNinos >= ninos) {
                                    String roomNumber = doc.getString("roomNumber");
                                    if (roomNumber != null) {
                                        habitacionesCandidatas.add(roomNumber);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando habitación", e);
                        }
                    }

                    if (habitacionesCandidatas.isEmpty()) {
                        callback.onResultado(false);
                        return;
                    }
                    verificarReservas(hotel, habitacionesCandidatas, habitaciones, callback);
                })
                .addOnFailureListener(e ->{
                    Log.e(TAG, "Error verificando disponibilidad", e);
                    callback.onResultado(false);
                });
    }

    private void verificarReservas(Hotel hotel, List<String> habitacionesCandidatas, int habitacionesNecesarias, DisponibilidadCallback callback) {
        db.collection("reservas")
                .whereEqualTo("idHotel", hotel.getIdHotel())
                .whereEqualTo("estado", "activo")
                .get()
                .addOnSuccessListener(reservaSnapshot -> {
                    Set<String> habitacionesOcupadas = new HashSet<>();

                    for (QueryDocumentSnapshot reservaDoc : reservaSnapshot) {
                        try {
                            Timestamp inicio = reservaDoc.getTimestamp("fechaInicio");
                            Timestamp fin = reservaDoc.getTimestamp("fechaFin");

                            // Validar si hay cruce de fechas con las del selector
                            if (inicio != null && fin != null && fechaInicio != null && fechaFin != null) {
                                boolean fechasSeCruzan = !(fechaFin.before(inicio.toDate()) || fechaInicio.after(fin.toDate()));

                                if (fechasSeCruzan) {
                                    Object roomNumberObj = reservaDoc.get("roomNumber");
                                    if (roomNumberObj instanceof List) {
                                        List<String> ocupadas = (List<String>) roomNumberObj;
                                        habitacionesOcupadas.addAll(ocupadas);
                                    } else if (roomNumberObj instanceof String) {
                                        habitacionesOcupadas.add((String) roomNumberObj);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error procesando reserva", e);
                        }
                    }

                    // Filtrar habitaciones candidatas que no están ocupadas
                    List<String> disponibles = new ArrayList<>();
                    for (String room : habitacionesCandidatas) {
                        if (!habitacionesOcupadas.contains(room)) {
                            disponibles.add(room);
                        }
                    }

                    callback.onResultado(disponibles.size() >= habitacionesNecesarias);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error verificando reservas", e);
                    callback.onResultado(false);
                });
    }

    interface DisponibilidadCallback {
        void onResultado(boolean disponible);
    }

    // Método para obtener el ID del recurso de imagen basado en el nombre
//    private int getImageResourceId(String imageName) {
//        // Mapear nombres de imágenes a recursos
//        switch (imageName) {
//            case "hotel_costa_sol":
//                return R.drawable.hotel_costa_sol;
//            case "hotel_boca_raton":
//                return R.drawable.hotel_boca_raton;
//            case "hotel_decameron":
//                return R.drawable.hotel_decameron;
//            case "hotel_aranwa":
//                return R.drawable.hotel_aranwa;
//            case "hotel_oro_verde":
//                return R.drawable.hotel_oro_verde;
//            case "hotel_sheraton":
//                return R.drawable.hotel_sheraton;
//            default:
//                // Imagen por defecto si no hay coincidencia
//                return R.drawable.hotel_decameron;
//        }
//    }

    private void configurarCalendario() {
        binding.datePickerLayout.setOnClickListener(v -> {
            MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Selecciona fechas de estancia");
            // Establecer fecha predeterminada (inicio: hoy, fin: mañana)
            long today = MaterialDatePicker.todayInUtcMilliseconds();
            long tomorrow = today + (24 * 60 * 60 * 1000);
//            long inicioMillis = fechaInicio.getTime();
//            long finMillis = fechaFin.getTime();
            androidx.core.util.Pair<Long, Long> defaultDateRange =
                    new androidx.core.util.Pair<>(today, tomorrow);
            builder.setSelection(defaultDateRange);

            // Restringir fechas pasadas
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now());
            builder.setCalendarConstraints(constraintsBuilder.build());
            MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();
            datePicker.show(getParentFragmentManager(), datePicker.toString());

            datePicker.addOnPositiveButtonClickListener(selection -> {
                Long inicio = selection.first;
                Long fin = selection.second;

                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM", new Locale("es", "ES"));
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                String texto = sdf.format(new Date(inicio)) + " - " + sdf.format(new Date(fin));
                binding.selectedDatesText.setText(texto);

                // Guarda internamente para usar al filtrar
                this.fechaInicio = new Date(inicio);
                this.fechaFin = new Date(fin);
            });

//            picker.show(getParentFragmentManager(), picker.toString()); // Mover show() aquí después de addOnPositiveButtonClickListener
        });
    }


    private void configurarSelectorPersonas() {
        binding.roomGuestsLayout.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialogo_personas_habitaciones, null);

            NumberPicker habitacionesPicker = dialogView.findViewById(R.id.npHabitaciones);
            NumberPicker personasPicker = dialogView.findViewById(R.id.npPersonas);

            habitacionesPicker.setMinValue(1);
            habitacionesPicker.setMaxValue(10);
            habitacionesPicker.setValue(numHabitaciones);

            personasPicker.setMinValue(numHabitaciones);
            personasPicker.setMaxValue(10);
            personasPicker.setValue(numPersonas);

            // Cuando cambian las habitaciones, actualizar el mínimo de personas
            habitacionesPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
                personasPicker.setMinValue(newVal);
                if (personasPicker.getValue() < newVal) {
                    personasPicker.setValue(newVal);
                }
            });

            new AlertDialog.Builder(requireContext())
                    .setTitle("Habitaciones y Huéspedes")
                    .setView(dialogView)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        numHabitaciones = habitacionesPicker.getValue();
                        numPersonas = personasPicker.getValue();
                        actualizarTextoHabitacionesPersonas();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void configurarSelectorValoracion() {
        binding.ratingLayout.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.user_rating_selector, null);

            RatingBar ratingBar = dialogView.findViewById(R.id.ratingSelector);
            TextView ratingValue = dialogView.findViewById(R.id.ratingValue);

            // Establecer valor inicial
            ratingBar.setRating(valoracionMinima);
            ratingValue.setText(String.valueOf(valoracionMinima));

            // Configurar listener para actualizar el texto mientras se cambia la valoración
            ratingBar.setOnRatingBarChangeListener((rBar, rating, fromUser) -> {
                int ratingInt = Math.round(rating);
                ratingValue.setText(String.valueOf(ratingInt));
            });

            new AlertDialog.Builder(requireContext())
                    .setTitle("Valoración mínima")
                    .setView(dialogView)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        valoracionMinima = Math.round(ratingBar.getRating());
                        actualizarTextoValoracion();

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Establecer texto inicial
        actualizarTextoValoracion();
    }

    private void actualizarTextoHabitacionesPersonas() {
        binding.roomGuestsText.setText(
                numHabitaciones + " habitación" + (numHabitaciones > 1 ? "es" : "") +
                        " · " + numPersonas + " adulto" + (numPersonas > 1 ? "s" : ""));
    }

    private void actualizarTextoValoracion() {
        binding.ratingValueText.setText(valoracionMinima + " estrella" + (valoracionMinima > 1 ? "s" : "") + " o más");
    }

    private void mostrarResumenBusqueda() {
        // Obtener la consulta de búsqueda
        String query = binding.searchEditText.getText().toString().trim();
        String destino = query.isEmpty() ? "Todos los destinos" : query;

        // Formatear fechas para mostrar
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        String fechas = sdf.format(fechaInicio) + " - " + sdf.format(fechaFin);

        // Crear mensaje de resumen
        String mensaje = "Buscando en: " + destino + "\n" +
                "Fechas: " + fechas + "\n" +
                "Habitaciones: " + numHabitaciones + "\n" +
                "Adultos: " + numPersonas + "\n" +
                "Valoración mínima: " + valoracionMinima + " estrella" + (valoracionMinima > 1 ? "s" : "");

        // Mostrar resumen de búsqueda
        new AlertDialog.Builder(requireContext())
                .setTitle("Resumen de búsqueda")
                .setMessage(mensaje)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    // Confirmar y ejecutar búsqueda
                    filtrarHoteles();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

//    private void realizarBusqueda() {
//        destino = binding.searchEditText.getText().toString().trim();
//        String fechas = binding.selectedDatesText.getText().toString();
//
//        // Mostrar indicador de búsqueda
//        Toast.makeText(requireContext(), "Buscando hoteles...", Toast.LENGTH_SHORT).show();
//
//        // Si hay un destino específico, filtrar por ubicación
//        if (!destino.isEmpty()) {
//            filtrarHotelesPorUbicacion(destino);
//        } else {
//            // Si no hay destino, solo filtrar por valoración
//            filtrarHotelesPorValoracion();
//        }
//
//        // Mostrar resumen de búsqueda
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Búsqueda iniciada")
//                .setMessage("Buscando en: " + (destino.isEmpty() ? "Todos los destinos" : destino) + "\n" +
//                        "Fechas: " + fechas + "\n" +
//                        "Habitaciones: " + numHabitaciones + "\n" +
//                        "Adultos: " + numPersonas + "\n" +
//                        "Valoración mínima: " + valoracionMinima + " estrellas")
//                .setPositiveButton("Aceptar", null)
//                .show();
//    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Limpiar handler para evitar memory leaks
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}