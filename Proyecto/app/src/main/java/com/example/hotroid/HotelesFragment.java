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
import com.google.android.material.datepicker.MaterialDatePicker;

import java.util.ArrayList;
import java.util.List;

public class HotelesFragment extends Fragment {

    private UserHotelesBinding binding;
    private int numHabitaciones = 1;
    private int numPersonas = 2;
    private int valoracionMinima = 3;
    private List<Hotel> hotelList;
    private HotelAdapter hotelAdapter;

    public HotelesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserHotelesBinding.inflate(inflater, container, false);

        configurarCalendario();
        configurarSelectorPersonas();
        configurarSelectorValoracion();
        cargarHotelesEstaticos();

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
            picker.show(getParentFragmentManager(), picker.toString());

            picker.addOnPositiveButtonClickListener(selection -> {
                String fecha = picker.getHeaderText(); // ej. "14 abr – 15 abr 2025"
                binding.selectedDatesText.setText(fecha);
            });
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
        // Aquí implementarías la lógica para realizar la búsqueda con los parámetros seleccionados
        String destino = binding.searchEditText.getText().toString();
        String fechas = binding.selectedDatesText.getText().toString();

        // Filtramos los hoteles por valoración
        filtrarHotelesPorValoracion();

        // Mostramos mensaje informativo
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
        // Inicializar la lista de hoteles
        hotelList = new ArrayList<>();

        // Agregar hoteles estáticos
        // Nota: Reemplaza R.drawable.hotel1, R.drawable.hotel2, etc. con tus propias imágenes
        hotelList.add(new Hotel("R001","Grand Hotel Madrid", 4.5f, "S/.145/noche","Madrid", R.drawable.hotel_decameron));
        hotelList.add(new Hotel("R002","Barcelona Royal Suite", 5.0f, "S/.210/noche","Barcelona", R.drawable.hotel_aranwa));
        hotelList.add(new Hotel("R003","Valencia Beach Resort", 4.0f, "S/.125/noche","Valencia", R.drawable.hotel_boca_raton));
        hotelList.add(new Hotel("R004","Sevilla Boutique Hotel", 3.5f, "S/.95/noche", "Sevilla",R.drawable.hotel_oro_verde));
        hotelList.add(new Hotel("R005","Granada Historic Palace", 4.8f, "S/.180/noche", "Inglaterra", R.drawable.hotel_sheraton));

        // Configurar el RecyclerView
        binding.hotelRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        hotelAdapter = new HotelAdapter(hotelList);
        binding.hotelRecyclerView.setAdapter(hotelAdapter);
    }

    private void filtrarHotelesPorValoracion() {
        // Filtrar hoteles por valoración mínima
        List<Hotel> hotelesFiltrados = new ArrayList<>();
        for (Hotel hotel : hotelList) {
            if (hotel.getRating() >= valoracionMinima) {
                hotelesFiltrados.add(hotel);
            }
        }

        // Actualizar el adaptador con la lista filtrada
        hotelAdapter = new HotelAdapter(hotelesFiltrados);
        binding.hotelRecyclerView.setAdapter(hotelAdapter);

        // Actualizar el título de resultados con el conteo
        binding.resultsTitle.setText("Hoteles recomendados (" + hotelesFiltrados.size() + ")");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}