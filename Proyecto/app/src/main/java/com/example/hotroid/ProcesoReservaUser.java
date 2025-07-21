package com.example.hotroid;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.bean.Room;
import com.example.hotroid.bean.RoomGroupOption;

import java.util.ArrayList;
import java.util.Date;

public class ProcesoReservaUser extends AppCompatActivity {

    private RoomGroupOption opcionSeleccionada;
    private ArrayList<Integer> roomNumbersSeleccionados;
    private Date fechaInicio, fechaFin;
    private int cantidadPersonas, ninios, numHabitaciones;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_proceso_reserva);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Log.d("ProcesoReserva", "Iniciando onCreate...");
// Recuperar datos enviados
        try {
            // Recuperar datos enviados
            opcionSeleccionada = getIntent().getParcelableExtra("opcionSeleccionada");
            Log.d("ProcesoReserva", "opcionSeleccionada: " + (opcionSeleccionada != null ? opcionSeleccionada.toString() : "null"));

            if (opcionSeleccionada != null && opcionSeleccionada.getHabitacionesSeleccionadas() != null) {
                for (Room room : opcionSeleccionada.getHabitacionesSeleccionadas()) {
                    Log.d("ProcesoReserva", "Imagen de habitación: " + room.getImageResourceName()); // cambia el método
                }
                Log.d("ProcesoReserva", "opcionSeleccionada tipo: " + opcionSeleccionada.getRoomType());
                Log.d("ProcesoReserva", "habitaciones necesarias: " + opcionSeleccionada.getHabitacionesNecesarias());
                Log.d("ProcesoReserva", "disponibles: " + opcionSeleccionada.getDisponibles());
            } else {
                Log.d("ProcesoReserva", "opcionSeleccionada o sus habitaciones son null");
            }
            roomNumbersSeleccionados = getIntent().getIntegerArrayListExtra("roomNumbersSeleccionados");
            Log.d("ProcesoReserva", "roomNumbersSeleccionados: " + roomNumbersSeleccionados);


            long inicioMillis = getIntent().getLongExtra("fechaInicio", -1);
            long finMillis = getIntent().getLongExtra("fechaFin", -1);
            Log.d("ProcesoReserva", "fechaInicioMillis: " + inicioMillis + ", fechaFinMillis: " + finMillis);

            fechaInicio = new Date(inicioMillis);
            fechaFin = new Date(finMillis);
            Log.d("ProcesoReserva", "fechaInicio: " + fechaInicio + ", fechaFin: " + fechaFin);

            cantidadPersonas = getIntent().getIntExtra("cantidadPersonas", 0);
            ninios = getIntent().getIntExtra("niniosSolicitados", 0);
            numHabitaciones = getIntent().getIntExtra("numHabitaciones", 0);
            Log.d("ProcesoReserva", "cantidadPersonas: " + cantidadPersonas + ", ninios: " + ninios + ", numHabitaciones: " + numHabitaciones);
        } catch (Exception e) {
            Log.e("ProcesoReserva", "Error al recuperar datos del intent", e);
        }


        // Cargar el primer fragmento solo si es la primera vez
        if (savedInstanceState == null) {
            try {
                Bundle bundle = new Bundle();
                bundle.putParcelable("opcionSeleccionada", opcionSeleccionada);
                bundle.putIntegerArrayList("roomNumbersSeleccionados", roomNumbersSeleccionados);
                bundle.putLong("fechaInicio", fechaInicio.getTime());
                bundle.putLong("fechaFin", fechaFin.getTime());
                bundle.putInt("cantidadPersonas", cantidadPersonas);
                bundle.putInt("niniosSolicitados", ninios);
                bundle.putInt("numHabitaciones", numHabitaciones);

                Paso1ReservacionFragment fragment = new Paso1ReservacionFragment();
                fragment.setArguments(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .commit();
                Log.d("ProcesoReserva", "Fragment cargado exitosamente");
            }catch (Exception e) {
                Log.e("ProcesoReserva", "Error al cargar fragmento", e);
            }
        }
    }

}