package com.example.hotroid;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;

import com.example.hotroid.util.ReservacionTempData;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

public class Paso2ReservacionFragment extends Fragment {
    private SwitchMaterial switchGimnasio, switchDesayuno, switchPiscina, switchParqueo;
    private TextView tvTotal;
    private MaterialButton btnSiguiente;
    private double precioBase = 200; // Precio base por habitación

    public Paso2ReservacionFragment() {
        // Constructor vacío requerido
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar el layout para este fragmento
        return inflater.inflate(R.layout.fragment_paso2_reservacion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar componentes
        switchGimnasio = view.findViewById(R.id.switchGimnasio);
        switchDesayuno = view.findViewById(R.id.switchDesayuno);
        switchPiscina = view.findViewById(R.id.switchPiscina);
        switchParqueo = view.findViewById(R.id.switchParqueo);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnSiguiente = view.findViewById(R.id.btnConfirmarReserva);

        // Calcular el precio base según el número de habitaciones
        precioBase = 200 * ReservacionTempData.habitaciones;

        // Configurar listeners para los switches
        View.OnClickListener actualizarTotalListener = v -> actualizarTotal();
        switchGimnasio.setOnClickListener(actualizarTotalListener);
        switchDesayuno.setOnClickListener(actualizarTotalListener);
        switchPiscina.setOnClickListener(actualizarTotalListener);
        switchParqueo.setOnClickListener(actualizarTotalListener);

        // Configurar listener para el botón siguiente
        btnSiguiente.setOnClickListener(v -> {
            guardarDatosAdicionales();

            // Ir al paso 3
            Fragment paso3 = new Paso3ReservacionFragment();
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragmentContainer, paso3);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        actualizarTotal();
    }

    private void actualizarTotal() {
        double total = precioBase; // Precio base * número de habitaciones
        double adicionales = 0;

        // Sumar los servicios adicionales
        if (switchGimnasio.isChecked()) {
            total += 100;
            adicionales += 100;
        }
        if (switchDesayuno.isChecked()) {
            total += 80;
            adicionales += 80;
        }
        if (switchPiscina.isChecked()) {
            total += 100;
            adicionales += 100;
        }
        if (switchParqueo.isChecked()) {
            total += 100;
            adicionales += 100;
        }

        tvTotal.setText("TOTAL\nPEN " + total);

        // Actualizar datos temporales
        ReservacionTempData.precioBase = precioBase;
        ReservacionTempData.cobrosAdicionales = adicionales;
        ReservacionTempData.precioTotal = total;
    }

    private void guardarDatosAdicionales() {
        // Guardar selección de servicios adicionales
        ReservacionTempData.gimnasio = switchGimnasio.isChecked();
        ReservacionTempData.desayuno = switchDesayuno.isChecked();
        ReservacionTempData.piscina = switchPiscina.isChecked();
        ReservacionTempData.parqueo = switchParqueo.isChecked();
    }
}