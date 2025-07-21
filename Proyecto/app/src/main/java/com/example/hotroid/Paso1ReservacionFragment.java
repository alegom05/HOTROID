package com.example.hotroid;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotroid.bean.RoomGroupOption;
import com.example.hotroid.util.ReservacionTempData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private FirebaseFirestore db;
    private FirebaseAuth auth;
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
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        // Formato para fecha vencimiento MM/AA
        fechaVencimientoEditText.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting && s.length() == 2 && before == 0) {
                    isFormatting = true;
                    fechaVencimientoEditText.setText(s + "/");
                    fechaVencimientoEditText.setSelection(3);
                    isFormatting = false;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
        // Validar 16 dígitos en número de tarjeta
        numeroTarjetaEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});


        // Obtener datos de usuario y rellenar campos
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (uid != null) {
            db.collection("usuarios").document(uid).get().addOnSuccessListener(document -> {
                if (document.exists()) {
                    String nombre = document.getString("nombre");
                    String apellido = document.getString("apellido");
                    String dni = document.getString("dni");

                    nombreEditText.setText(nombre);
                    apellidoEditText.setText(apellido);

                    if (dni != null && !dni.isEmpty()) {
                        dniEditText.setText(dni);
                        dniEditText.setEnabled(false); // Bloquear edición
                    } else {
                        dniEditText.setEnabled(true); // Permitir edición
                    }
                }
            });
        }
        // Acción del botón continuar
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

            // Validaciones básicas
            if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() ||
                    numeroTarjeta.length() != 16 || !fechaVencimiento.matches("\\d{2}/\\d{2}") ||
                    cvv.length() < 3) {
                Toast.makeText(requireContext(), "Por favor completa todos los campos correctamente.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ejemplo simple: mostrar los datos en log
            android.util.Log.d("Paso1", "Nombre: " + nombre + ", Apellido: " + apellido);
            android.util.Log.d("Paso1", "Servicios: GYM=" + gimnasio + ", DES=" + desayuno +
                    ", PIS=" + piscina + ", PAR=" + parqueo);

//            // Obtener idHotel desde una habitación
            String idHotel = null;
            if (opcionSeleccionada != null && opcionSeleccionada.getHabitacionesSeleccionadas() != null && !opcionSeleccionada.getHabitacionesSeleccionadas().isEmpty()) {
                idHotel = opcionSeleccionada.getHabitacionesSeleccionadas().get(0).getHotelId();
            }

            // Enviar datos al siguiente paso
            Bundle data = new Bundle();
            data.putParcelable("opcionSeleccionada", opcionSeleccionada);
            data.putIntegerArrayList("roomNumbersSeleccionados", roomNumbersSeleccionados);
            data.putLong("fechaInicio", fechaInicio.getTime());
            data.putLong("fechaFin", fechaFin.getTime());
            data.putInt("cantidadPersonas", cantidadPersonas);
            data.putInt("niniosSolicitados", niniosSolicitados);
            data.putInt("numHabitaciones", numHabitaciones);
            data.putString("nombre", nombre);
            data.putString("apellido", apellido);
            data.putString("dni", dni);
            data.putString("numeroTarjeta", numeroTarjeta);
            data.putString("fechaVencimiento", fechaVencimiento);
            data.putString("cvv", cvv);
            data.putBoolean("gimnasio", gimnasio);
            data.putBoolean("desayuno", desayuno);
            data.putBoolean("piscina", piscina);
            data.putBoolean("parqueo", parqueo);
            data.putString("idHotel", idHotel);

            Paso2ReservacionFragment paso2 = new Paso2ReservacionFragment();
            paso2.setArguments(data);
            // Ir al paso 2
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, paso2)
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

        btnSiguiente = view.findViewById(R.id.btnSiguientePaso1);


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