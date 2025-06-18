package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotroid.databinding.UserHotelesBinding;
import com.example.hotroid.bean.Hotel;
// NOTA: Si tienes un HotelAdapter específico para el cliente, asegúrate de que el import sea correcto.
// Si tu HotelAdapter para clientes está en com.example.hotroid.adapter.HotelAdapter, mantén ese.
// Si lo renombraste para admin, este import debe ser el de cliente.
import com.example.hotroid.HotelAdapter; // Asumiendo que este es el HotelAdapter para clientes
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelesFragment extends Fragment {

    private UserHotelesBinding binding;
    private int numHabitaciones = 1;
    private int numPersonas = 2;
    private int valoracionMinima = 3;
    private List<Hotel> hotelList;
    private HotelAdapter hotelAdapter; // Usa el HotelAdapter para clientes

    public HotelesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserHotelesBinding.inflate(inflater, container, false);

        configurarCalendario();
        configurarSelectorPersonas();
        configurarSelectorValoracion();
        cargarHotelesEstaticos(); // Esta función ahora usa setters en lugar del constructor largo

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
        String destino = binding.searchEditText.getText().toString();
        String fechas = binding.selectedDatesText.getText().toString();

        filtrarHotelesPorValoracion();

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

    private void cargarHotelesEstaticos() {
        hotelList = new ArrayList<>();

        // HOTEL 1: Grand Hotel Madrid
        Hotel hotel1 = new Hotel();
        hotel1.setIdHotel("R001"); // Aunque no se use en Firestore para esta vista, mantenemos la consistencia
        hotel1.setName("Grand Hotel Madrid");
        hotel1.setRating(4.5f);
        hotel1.setPrice(223.00);
        hotel1.setDireccion("Madrid");
        hotel1.setDireccionDetallada("Av. del Prado 123, Centro Histórico");
        hotel1.setDescription("Un hotel elegante en el corazón de Madrid, cerca de los principales museos y atracciones.");
        hotel1.setImageResourceId(R.drawable.hotel_decameron); // Usar R.drawable
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
        hotel2.setImageResourceId(R.drawable.hotel_aranwa); // Usar R.drawable
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
        hotel3.setImageResourceId(R.drawable.hotel_boca_raton); // Usar R.drawable
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
        hotel4.setImageResourceId(R.drawable.hotel_oro_verde); // Usar R.drawable
        hotelList.add(hotel4);

        // HOTEL 5: Granada Historic Palace
        Hotel hotel5 = new Hotel();
        hotel5.setIdHotel("R005");
        hotel5.setName("Granada Historic Palace");
        hotel5.setRating(4.8f);
        hotel5.setPrice(356.60);
        hotel5.setDireccion("Inglaterra"); // Aquí la dirección está como "Inglaterra", lo mantengo según tu código.
        hotel5.setDireccionDetallada("High St 10, Old Town");
        hotel5.setDescription("Un palacio convertido en hotel, ofreciendo una estancia lujosa y con historia en Granada.");
        hotel5.setImageResourceId(R.drawable.hotel_sheraton); // Usar R.drawable
        hotelList.add(hotel5);


        // Configurar el RecyclerView
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        hotelAdapter = new HotelAdapter(hotelList); // Asegúrate de que este HotelAdapter sea el de cliente
        binding.hotelRecyclerView.setAdapter(hotelAdapter);
    }

    private void filtrarHotelesPorValoracion() {
        List<Hotel> hotelesFiltrados = new ArrayList<>();
        for (Hotel hotel : hotelList) {
            if (hotel.getRating() >= valoracionMinima) {
                hotelesFiltrados.add(hotel);
            }
        }

        hotelAdapter = new HotelAdapter(hotelesFiltrados); // Se crea un nuevo adaptador con la lista filtrada
        binding.hotelRecyclerView.setAdapter(hotelAdapter);

        binding.resultsTitle.setText("Hoteles recomendados (" + hotelesFiltrados.size() + ")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}