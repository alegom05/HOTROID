package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class Paso3ReservacionFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paso3_reservacion, container, false);

        Button btnReservar = view.findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            // Lógica de finalización de reserva (posteriormente guardar en BD)
            Toast.makeText(requireContext(), "Reserva completada con éxito", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}
