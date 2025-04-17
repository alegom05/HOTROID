package com.example.hotroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.hotroid.databinding.UserHotelesBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class HotelesFragment extends Fragment {

    private UserHotelesBinding binding;
    private int numHabitaciones = 1;
    private int numPersonas = 2;

    public HotelesFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserHotelesBinding.inflate(inflater, container, false);

        configurarCalendario();
        configurarSelectorPersonas();
        configurarValoracionSlider();

        return binding.getRoot();
    }

    private void configurarCalendario() {
        binding.datePickerLayout.setOnClickListener(v -> {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Selecciona las fechas");

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
                        binding.roomGuestsText.setText(
                                numHabitaciones + " habitación" + (numHabitaciones > 1 ? "es" : "") +
                                        " · " + numPersonas + " adulto" + (numPersonas > 1 ? "s" : ""));
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void configurarValoracionSlider() {
        Slider slider = new Slider(requireContext());
        slider.setValueFrom(1f);
        slider.setValueTo(5f);
        slider.setStepSize(1f);
        slider.setValue(5f);

        // Puedes usar este slider programáticamente si decides mostrarlo en un popup
        // Aquí se asume que usas el RecyclerView para mostrar las opciones gráficas de valoración.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
