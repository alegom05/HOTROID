package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotroid.util.ReservacionTempData;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Date;

public class Paso1ReservacionFragment extends Fragment {
    private DatePicker fechaInicioPicker, fechaFinPicker;
    private NumberPicker adultosPicker, ninosPicker, habitacionesPicker;
    private TextInputEditText nombreEditText, apellidoEditText;
    private Button btnSiguiente;

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paso1_reservacion, container, false);

        // Inicializar componentes
        inicializarComponentes(view);

        btnSiguiente.setOnClickListener(v -> {
            // Guardar datos en el objeto temporal
            guardarDatosReservaTemporal();

            // Ir al paso 2
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new Paso2ReservacionFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void inicializarComponentes(View view) {
        // Referencias a vistas originales
        nombreEditText = view.findViewById(R.id.nombreEditText);
        apellidoEditText = view.findViewById(R.id.apellidoEditText);

        // Referencias a nuevos componentes
        fechaInicioPicker = view.findViewById(R.id.fechaInicioPicker);
        fechaFinPicker = view.findViewById(R.id.fechaFinPicker);
        adultosPicker = view.findViewById(R.id.adultosPicker);
        ninosPicker = view.findViewById(R.id.ninosPicker);
        habitacionesPicker = view.findViewById(R.id.habitacionesPicker);
        btnSiguiente = view.findViewById(R.id.btnSiguientePaso1);

        // Configurar límites para los pickers
        adultosPicker.setMinValue(1);
        adultosPicker.setMaxValue(5);
        ninosPicker.setMinValue(0);
        ninosPicker.setMaxValue(4);
        habitacionesPicker.setMinValue(1);
        habitacionesPicker.setMaxValue(3);

        // Configurar fecha mínima para los datepickers (fecha actual)
        Calendar calendar = Calendar.getInstance();
        fechaInicioPicker.setMinDate(calendar.getTimeInMillis());
        calendar.add(Calendar.DATE, 1);
        fechaFinPicker.setMinDate(calendar.getTimeInMillis());
    }

    private void guardarDatosReservaTemporal() {
        // Obtener nombre y apellido
        String nombre = nombreEditText.getText().toString();
        String apellido = apellidoEditText.getText().toString();

        // Obtener fechas
        Calendar calendarInicio = Calendar.getInstance();
        calendarInicio.set(fechaInicioPicker.getYear(), fechaInicioPicker.getMonth(),
                fechaInicioPicker.getDayOfMonth());
        Date fechaInicio = calendarInicio.getTime();

        Calendar calendarFin = Calendar.getInstance();
        calendarFin.set(fechaFinPicker.getYear(), fechaFinPicker.getMonth(),
                fechaFinPicker.getDayOfMonth());
        Date fechaFin = calendarFin.getTime();

        // Obtener conteos
        int adultos = adultosPicker.getValue();
        int ninos = ninosPicker.getValue();
        int habitaciones = habitacionesPicker.getValue();

        // Guardar en objeto temporal
        ReservacionTempData.nombresCliente = nombre;
        ReservacionTempData.apellidosCliente = apellido;
        ReservacionTempData.fechaInicio = fechaInicio;
        ReservacionTempData.fechaFin = fechaFin;
        ReservacionTempData.adultos = adultos;
        ReservacionTempData.ninos = ninos;
        ReservacionTempData.habitaciones = habitaciones;

        // Usar el hotel seleccionado previamente (se asumiría que viene de una pantalla anterior)
        // O se podría obtener de los extras del intent
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            ReservacionTempData.idHotel = extras.getString("idHotel", "");
            ReservacionTempData.nombreHotel = extras.getString("nombreHotel", "");
        }
    }
}