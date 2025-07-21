package com.example.hotroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        // Recuperar datos enviados
        opcionSeleccionada = getIntent().getParcelableExtra("opcionSeleccionada");
        roomNumbersSeleccionados = getIntent().getIntegerArrayListExtra("roomNumbersSeleccionados");

        long inicioMillis = getIntent().getLongExtra("fechaInicio", -1);
        long finMillis = getIntent().getLongExtra("fechaFin", -1);
        fechaInicio = new Date(inicioMillis);
        fechaFin = new Date(finMillis);

        cantidadPersonas = getIntent().getIntExtra("cantidadPersonas", 0);
        ninios = getIntent().getIntExtra("niniosSolicitados",0);
        numHabitaciones=getIntent().getIntExtra("numHabitaciones",0);


        // Cargar el primer fragmento solo si es la primera vez
        if (savedInstanceState == null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("opcionSeleccionada", opcionSeleccionada);
            bundle.putIntegerArrayList("roomNumbersSeleccionados", roomNumbersSeleccionados);
            bundle.putLong("fechaInicio", inicioMillis);
            bundle.putLong("fechaFin", finMillis);
            bundle.putInt("cantidadPersonas", cantidadPersonas);
            bundle.putInt("niniosSolicitados", ninios);
            bundle.putInt("numHabitaciones", numHabitaciones);

            Paso1ReservacionFragment fragment = new Paso1ReservacionFragment();
            fragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

}