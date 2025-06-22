package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotroid.databinding.UserHotelesBinding;
import com.example.hotroid.bean.Hotel;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelesFragment extends Fragment {

    private static final String TAG = "HotelesFragment";
    private UserHotelesBinding binding;
    private int numHabitaciones = 1;
    private int numPersonas = 2;
    private int valoracionMinima = 3;
    private List<Hotel> hotelList;
    private HotelAdapter hotelAdapter;
    private FirebaseFirestore db;
    private String destino = "";

    public HotelesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserHotelesBinding.inflate(inflater, container, false);

        // Inicializar Firebase Firestore
        db = FirebaseFirestore.getInstance();

        configurarCalendario();
        configurarSelectorPersonas();
        configurarSelectorValoracion();

        // Inicializar lista de hoteles vacía
        hotelList = new ArrayList<>();

        // Configurar RecyclerView
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        hotelAdapter = new HotelAdapter(hotelList);
        binding.hotelRecyclerView.setAdapter(hotelAdapter);

        // Cargar hoteles desde Firestore
        cargarHotelesDinamicos();

        // Animación de entrada
        binding.searchCard.setAlpha(0f);
        binding.searchCard.animate().alpha(1f).setDuration(500).start();

        // Configurar botón de búsqueda
        binding.searchButton.setOnClickListener(v -> realizarBusqueda());

        //inserccion del enlace a notificaciones
        binding.notificationIcon.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificacionesUser.class));
        });

        return binding.getRoot();
    }

    private void cargarHotelesDinamicos() {
        // Mostrar indicador de carga
        Toast.makeText(requireContext(), "Cargando hoteles...", Toast.LENGTH_SHORT).show();

        // Limpiar lista actual
        hotelList.clear();

        // Consultar colección de hoteles en Firestore
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Crear objeto Hotel desde documento Firestore
                                String id = document.getId();
                                String nombre = document.getString("name");
                                String descripcion = document.getString("description");
                                String direccion = document.getString("direccion");
                                String direccionDetallada = document.getString("direccionDetallada");
                                double price = document.getDouble("price");
                                double ratingDouble = document.getDouble("rating");
                                float rating = (float) ratingDouble;
                                String imageName = document.getString("imageName");

                                // Crear objeto Hotel
                                Hotel hotel = new Hotel();
                                hotel.setIdHotel(id);
                                hotel.setName(nombre);
                                hotel.setDescription(descripcion);
                                hotel.setDireccion(direccion);
                                hotel.setDireccionDetallada(direccionDetallada);
                                hotel.setPrice(price);
                                hotel.setRating(rating);

                                // Determinar el recurso de imagen basado en el nombre de la imagen
                                int imageResourceId = getImageResourceId(imageName);
                                hotel.setImageResourceId(imageResourceId);

                                // Agregar a la lista
                                hotelList.add(hotel);

                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar documento: " + document.getId(), e);
                            }
                        }

                        // Actualizar adaptador y título
                        hotelAdapter = new HotelAdapter(hotelList);
                        binding.hotelRecyclerView.setAdapter(hotelAdapter);
                        binding.resultsTitle.setText("Hoteles recomendados (" + hotelList.size() + ")");

                    } else {
                        Log.e(TAG, "Error al obtener documentos: ", task.getException());
                        Toast.makeText(requireContext(), "Error al cargar hoteles", Toast.LENGTH_SHORT).show();

                        // Si hay error, cargar datos estáticos como respaldo
                        cargarHotelesEstaticos();
                    }
                });
    }

    // Método para obtener el ID del recurso de imagen basado en el nombre
    private int getImageResourceId(String imageName) {
        // Mapear nombres de imágenes a recursos
        switch (imageName) {
            case "hotel_costa_sol":
                return R.drawable.hotel_costa_sol;
            case "hotel_boca_raton":
                return R.drawable.hotel_boca_raton;
            case "hotel_decameron":
                return R.drawable.hotel_decameron;
            case "hotel_aranwa":
                return R.drawable.hotel_aranwa;
            case "hotel_oro_verde":
                return R.drawable.hotel_oro_verde;
            case "hotel_sheraton":
                return R.drawable.hotel_sheraton;
            default:
                // Imagen por defecto si no hay coincidencia
                return R.drawable.hotel_decameron;
        }
    }

    private void configurarCalendario() {
        binding.datePickerLayout.setOnClickListener(v -> {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Selecciona las fechas");

            // Establecer fecha predeterminada (inicio: hoy, fin: mañana)
            long today = System.currentTimeMillis();
            long tomorrow = today + (24 * 60 * 60 * 1000);
            androidx.core.util.Pair<Long, Long> defaultDateRange =
                    new androidx.core.util.Pair<>(today, tomorrow);
            builder.setSelection(defaultDateRange);

            MaterialDatePicker<?> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                String fecha = picker.getHeaderText(); // ej. "14 abr – 15 abr 2025"
                binding.selectedDatesText.setText(fecha);
            });
            picker.show(getParentFragmentManager(), picker.toString()); // Mover show() aquí después de addOnPositiveButtonClickListener
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

            personasPicker.setMinValue(1);
            personasPicker.setMaxValue(10);
            personasPicker.setValue(numPersonas);

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
                        filtrarHotelesPorValoracion();
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
        binding.ratingValueText.setText(valoracionMinima + " estrellas o más");
    }

    private void realizarBusqueda() {
        destino = binding.searchEditText.getText().toString().trim();
        String fechas = binding.selectedDatesText.getText().toString();

        // Mostrar indicador de búsqueda
        Toast.makeText(requireContext(), "Buscando hoteles...", Toast.LENGTH_SHORT).show();

        // Si hay un destino específico, filtrar por ubicación
        if (!destino.isEmpty()) {
            filtrarHotelesPorUbicacion(destino);
        } else {
            // Si no hay destino, solo filtrar por valoración
            filtrarHotelesPorValoracion();
        }

        // Mostrar resumen de búsqueda
        new AlertDialog.Builder(requireContext())
                .setTitle("Búsqueda iniciada")
                .setMessage("Buscando en: " + (destino.isEmpty() ? "Todos los destinos" : destino) + "\n" +
                        "Fechas: " + fechas + "\n" +
                        "Habitaciones: " + numHabitaciones + "\n" +
                        "Adultos: " + numPersonas + "\n" +
                        "Valoración mínima: " + valoracionMinima + " estrellas")
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void filtrarHotelesPorUbicacion(String ubicacion) {
        // Crear consulta para buscar hoteles que contengan la ubicación especificada
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Hotel> hotelesFiltrados = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                String nombre = document.getString("name");
                                String direccion = document.getString("direccion");
                                String descripcion = document.getString("description");

                                // Comprobar si la ubicación coincide con algún campo relevante
                                if ((direccion != null && direccion.toLowerCase().contains(ubicacion.toLowerCase())) ||
                                        (nombre != null && nombre.toLowerCase().contains(ubicacion.toLowerCase())) ||
                                        (descripcion != null && descripcion.toLowerCase().contains(ubicacion.toLowerCase()))) {

                                    // Crear objeto Hotel desde documento Firestore
                                    String id = document.getId();
                                    String direccionDetallada = document.getString("direccionDetallada");
                                    double price = document.getDouble("price");
                                    double ratingDouble = document.getDouble("rating");
                                    float rating = (float) ratingDouble;
                                    String imageName = document.getString("imageName");

                                    // Filtrar también por valoración mínima
                                    if (rating >= valoracionMinima) {
                                        // Crear objeto Hotel
                                        Hotel hotel = new Hotel();
                                        hotel.setIdHotel(id);
                                        hotel.setName(nombre);
                                        hotel.setDescription(descripcion);
                                        hotel.setDireccion(direccion);
                                        hotel.setDireccionDetallada(direccionDetallada);
                                        hotel.setPrice(price);
                                        hotel.setRating(rating);

                                        // Determinar el recurso de imagen basado en el nombre de la imagen
                                        int imageResourceId = getImageResourceId(imageName);
                                        hotel.setImageResourceId(imageResourceId);

                                        // Agregar a la lista
                                        hotelesFiltrados.add(hotel);
                                    }
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar documento: " + document.getId(), e);
                            }
                        }

                        // Actualizar adaptador y título
                        hotelAdapter = new HotelAdapter(hotelesFiltrados);
                        binding.hotelRecyclerView.setAdapter(hotelAdapter);
                        binding.resultsTitle.setText("Hoteles en " + ubicacion + " (" + hotelesFiltrados.size() + ")");

                    } else {
                        Log.e(TAG, "Error al obtener documentos: ", task.getException());
                        Toast.makeText(requireContext(), "Error al buscar hoteles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filtrarHotelesPorValoracion() {
        // Consultar todos los hoteles y filtrar por valoración mínima
        db.collection("hoteles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Hotel> hotelesFiltrados = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Obtener rating del documento
                                double ratingDouble = document.getDouble("rating");
                                float rating = (float) ratingDouble;

                                // Filtrar por valoración mínima
                                if (rating >= valoracionMinima) {
                                    // Crear objeto Hotel desde documento Firestore
                                    String id = document.getId();
                                    String nombre = document.getString("name");
                                    String descripcion = document.getString("description");
                                    String direccion = document.getString("direccion");
                                    String direccionDetallada = document.getString("direccionDetallada");
                                    double price = document.getDouble("price");
                                    String imageName = document.getString("imageName");

                                    // Crear objeto Hotel
                                    Hotel hotel = new Hotel();
                                    hotel.setIdHotel(id);
                                    hotel.setName(nombre);
                                    hotel.setDescription(descripcion);
                                    hotel.setDireccion(direccion);
                                    hotel.setDireccionDetallada(direccionDetallada);
                                    hotel.setPrice(price);
                                    hotel.setRating(rating);

                                    // Determinar el recurso de imagen basado en el nombre de la imagen
                                    int imageResourceId = getImageResourceId(imageName);
                                    hotel.setImageResourceId(imageResourceId);

                                    // Agregar a la lista
                                    hotelesFiltrados.add(hotel);
                                }

                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar documento: " + document.getId(), e);
                            }
                        }

                        // Actualizar adaptador y título
                        hotelAdapter = new HotelAdapter(hotelesFiltrados);
                        binding.hotelRecyclerView.setAdapter(hotelAdapter);
                        binding.resultsTitle.setText("Hoteles recomendados (" + hotelesFiltrados.size() + ")");

                    } else {
                        Log.e(TAG, "Error al obtener documentos: ", task.getException());
                        Toast.makeText(requireContext(), "Error al filtrar hoteles", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método de respaldo para cargar datos estáticos si falla la conexión a Firestore
    private void cargarHotelesEstaticos() {
        hotelList = new ArrayList<>();

        // HOTEL 1: Grand Hotel Madrid
        Hotel hotel1 = new Hotel();
        hotel1.setIdHotel("R001");
        hotel1.setName("Grand Hotel Madrid");
        hotel1.setRating(4.5f);
        hotel1.setPrice(223.00);
        hotel1.setDireccion("Madrid");
        hotel1.setDireccionDetallada("Av. del Prado 123, Centro Histórico");
        hotel1.setDescription("Un hotel elegante en el corazón de Madrid, cerca de los principales museos y atracciones.");
        hotel1.setImageResourceId(R.drawable.hotel_decameron);
        hotelList.add(hotel1);

        // HOTEL 2: Barcelona Royal Suite
        Hotel hotel2 = new Hotel();
        hotel2.setIdHotel("R002");
        hotel2.setName("Barcelona Royal Suite");
        hotel2.setRating(5.0f);
        hotel2.setPrice(135.50);
        hotel2.setDireccion("Barcelona");
        hotel2.setDireccionDetallada("Calle Mallorca 456, Eixample");
        hotel2.setDescription("Lujo y confort en el centro de Barcelona, con suites espaciosas y servicios exclusivos.");
        hotel2.setImageResourceId(R.drawable.hotel_aranwa);
        hotelList.add(hotel2);

        // HOTEL 3: Valencia Beach Resort
        Hotel hotel3 = new Hotel();
        hotel3.setIdHotel("R003");
        hotel3.setName("Valencia Beach Resort");
        hotel3.setRating(4.0f);
        hotel3.setPrice(356.70);
        hotel3.setDireccion("Valencia");
        hotel3.setDireccionDetallada("Paseo Marítimo 78, Playa Norte");
        hotel3.setDescription("Disfruta del sol y la playa en este resort moderno con acceso directo a la costa valenciana.");
        hotel3.setImageResourceId(R.drawable.hotel_boca_raton);
        hotelList.add(hotel3);

        // HOTEL 4: Sevilla Boutique Hotel
        Hotel hotel4 = new Hotel();
        hotel4.setIdHotel("R004");
        hotel4.setName("Sevilla Boutique Hotel");
        hotel4.setRating(3.5f);
        hotel4.setPrice(930.00);
        hotel4.setDireccion("Sevilla");
        hotel4.setDireccionDetallada("Calle Sierpes 22, Casco Antiguo");
        hotel4.setDescription("Un encantador hotel boutique en el histórico barrio de Sevilla, perfecto para explorar a pie.");
        hotel4.setImageResourceId(R.drawable.hotel_oro_verde);
        hotelList.add(hotel4);

        // HOTEL 5: Granada Historic Palace
        Hotel hotel5 = new Hotel();
        hotel5.setIdHotel("R005");
        hotel5.setName("Granada Historic Palace");
        hotel5.setRating(4.8f);
        hotel5.setPrice(356.60);
        hotel5.setDireccion("Granada");
        hotel5.setDireccionDetallada("Calle Alhamar 15, Centro");
        hotel5.setDescription("Un palacio convertido en hotel, ofreciendo una estancia lujosa y con historia en Granada.");
        hotel5.setImageResourceId(R.drawable.hotel_sheraton);
        hotelList.add(hotel5);

        // Configurar el RecyclerView
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        hotelAdapter = new HotelAdapter(hotelList);
        binding.hotelRecyclerView.setAdapter(hotelAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}