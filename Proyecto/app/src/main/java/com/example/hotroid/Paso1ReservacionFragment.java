package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import androidx.annotation.Nullable;


public class Paso1ReservacionFragment extends Fragment {
    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_paso1_reservacion, container, false);

        Button btnSiguiente = view.findViewById(R.id.btnSiguientePaso1);
        btnSiguiente.setOnClickListener(v -> {
            //al paso 2
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new Paso2ReservacionFragment())
                    .addToBackStack(null) //permite regresar
                    .commit();
        });
        return view;
    }
}
