package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotroid.bean.RoomGroupOption;
import com.example.hotroid.util.ReservacionTempData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Paso1ReservacionFragment extends Fragment {

    private EditText nombreEditText, apellidoEditText, dniEditText;
    private EditText numeroTarjetaEditText, fechaVencimientoEditText, cvvEditText;
    private SwitchMaterial switchGimnasio, switchDesayuno, switchPiscina, switchParqueo;

    private RoomGroupOption opcionSeleccionada;
    private ArrayList<Integer> roomNumbersSeleccionados;
    private Date fechaInicio, fechaFin;
    private int cantidadPersonas, niniosSolicitados, numHabitaciones;
    private DatePicker fechaInicioPicker, fechaFinPicker;
    private NumberPicker adultosPicker, ninosPicker, habitacionesPicker;
    //private TextInputEditText nombreEditText, apellidoEditText;
    private MaterialButton btnSiguiente;
//    public Paso1ReservacionFragment() {
//        // Required empty public constructor
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_paso1_reservacion, container, false);
    }
    @Override
    public void  onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperar argumentos
        Bundle args = getArguments();
        if (args != null) {
            opcionSeleccionada = args.getParcelable("opcionSeleccionada");
            roomNumbersSeleccionados = args.getIntegerArrayList("roomNumbersSeleccionados");
            fechaInicio = new Date(args.getLong("fechaInicio"));
            fechaFin = new Date(args.getLong("fechaFin"));
            cantidadPersonas = args.getInt("cantidadPersonas");
            niniosSolicitados = args.getInt("niniosSolicitados");
            numHabitaciones = args.getInt("numHabitaciones");
        }

        // Inicializar componentes
        inicializarComponentes(view);

        btnSiguiente.setOnClickListener(v -> {
            // Guardar datos en el objeto temporal
//            guardarDatosReservaTemporal();
            // Puedes validar campos aquí si deseas
            String nombre = nombreEditText.getText().toString().trim();
            String apellido = apellidoEditText.getText().toString().trim();
            String dni = dniEditText.getText().toString().trim();
            String numeroTarjeta = numeroTarjetaEditText.getText().toString().trim();
            String fechaVencimiento = fechaVencimientoEditText.getText().toString().trim();
            String cvv = cvvEditText.getText().toString().trim();

            boolean gimnasio = switchGimnasio.isChecked();
            boolean desayuno = switchDesayuno.isChecked();
            boolean piscina = switchPiscina.isChecked();
            boolean parqueo = switchParqueo.isChecked();

            // Ejemplo simple: mostrar los datos en log
            android.util.Log.d("Paso1", "Nombre: " + nombre + ", Apellido: " + apellido);
            android.util.Log.d("Paso1", "Servicios: GYM=" + gimnasio + ", DES=" + desayuno +
                    ", PIS=" + piscina + ", PAR=" + parqueo);

            // Ir al paso 2
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new Paso2ReservacionFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }

    private void inicializarComponentes(View view) {
        // Referencias a vistas originales
        nombreEditText = view.findViewById(R.id.nombreEditText);
        apellidoEditText = view.findViewById(R.id.apellidoEditText);
        dniEditText = view.findViewById(R.id.dniEditText);

        numeroTarjetaEditText = view.findViewById(R.id.numeroTarjetaEditText);
        fechaVencimientoEditText = view.findViewById(R.id.fechaVencimientoEditText);
        cvvEditText = view.findViewById(R.id.cvvEditText);

        switchGimnasio = view.findViewById(R.id.switchGimnasio);
        switchDesayuno = view.findViewById(R.id.switchDesayuno);
        switchPiscina = view.findViewById(R.id.switchPiscina);
        switchParqueo = view.findViewById(R.id.switchParqueo);

//        // Referencias a nuevos componentes
//        fechaInicioPicker = view.findViewById(R.id.fechaInicioPicker);
//        fechaFinPicker = view.findViewById(R.id.fechaFinPicker);
//        adultosPicker = view.findViewById(R.id.adultosPicker);
//        ninosPicker = view.findViewById(R.id.ninosPicker);
//        habitacionesPicker = view.findViewById(R.id.habitacionesPicker);
        btnSiguiente = view.findViewById(R.id.btnSiguientePaso1);

//        // Configurar límites para los pickers
//        adultosPicker.setMinValue(1);
//        adultosPicker.setMaxValue(5);
//        ninosPicker.setMinValue(0);
//        ninosPicker.setMaxValue(4);
//        habitacionesPicker.setMinValue(1);
//        habitacionesPicker.setMaxValue(3);

        // Configurar fecha mínima para los datepickers (fecha actual)
//        Calendar calendar = Calendar.getInstance();
//        fechaInicioPicker.setMinDate(calendar.getTimeInMillis());
//        calendar.add(Calendar.DATE, 1);
//        fechaFinPicker.setMinDate(calendar.getTimeInMillis());
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