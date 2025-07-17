package com.example.hotroid;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.hotroid.databinding.DialogoHabitacionesAdultosNiniosBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HabitacionesAdultosNiniosDialogoFragment extends BottomSheetDialogFragment {
    private DialogoHabitacionesAdultosNiniosBinding binding;
    private int currentRooms;
    private int currentAdults;
    private int currentChildren;
    private TextView tvHabitaciones, tvAdultos, tvNinos;
    private Button btnMinusHabitaciones, btnPlusHabitaciones;
    private Button btnMinusAdultos, btnPlusAdultos;
    private Button btnMinusNinos, btnPlusNinos;
    //    private OnValuesSelectedListener listener;
    //    public interface OnValuesSelectedListener {
//        void onValuesSelected(int rooms, int adults, int children);
//    }
//
//    public void setCurrentValues(int rooms, int adults, int children) {
//        this.currentRooms = rooms;
//        this.currentAdults = adults;
//        this.currentChildren = children;
//    }
    public HabitacionesAdultosNiniosDialogoFragment(int habitacionesInit, int adultosInit, int ninosInit) {
        this.currentRooms = habitacionesInit;
        this.currentAdults = adultosInit;
        this.currentChildren = ninosInit;
    }
//    public void setOnValuesSelectedListener(OnValuesSelectedListener listener) {
//        this.listener = listener;
//    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialogo_habitaciones_adultos_ninios, container, false);
        // Referencias
        tvHabitaciones = view.findViewById(R.id.roomsCountText);
        tvAdultos = view.findViewById(R.id.adultsCountText);
        tvNinos = view.findViewById(R.id.childrenCountText);
        btnMinusHabitaciones = view.findViewById(R.id.roomsMinusButton);
        btnPlusHabitaciones = view.findViewById(R.id.roomsPlusButton);
        btnMinusAdultos = view.findViewById(R.id.adultsMinusButton);
        btnPlusAdultos = view.findViewById(R.id.adultsPlusButton);
        btnMinusNinos = view.findViewById(R.id.childrenMinusButton);
        btnPlusNinos = view.findViewById(R.id.childrenPlusButton);
        updateUI();
        // Listeners
        btnPlusHabitaciones.setOnClickListener(v -> {
            if (currentRooms < 20) {
                currentRooms++;
                if (currentAdults < currentRooms) currentAdults = currentRooms;
                updateUI();
            }
        });
        btnMinusHabitaciones.setOnClickListener(v -> {
            if (currentRooms > 1) {
                currentRooms--;
                if (currentAdults < currentRooms) currentAdults = currentRooms;
                updateUI();
            }
        });
        btnPlusAdultos.setOnClickListener(v -> {
            if (currentAdults < 20) {
                currentAdults++;
                updateUI();
            }
        });
        btnMinusAdultos.setOnClickListener(v -> {
            if (currentAdults > currentRooms) {
                currentAdults--;
                updateUI();
            }
        });
        btnPlusNinos.setOnClickListener(v -> {
            if (currentChildren < 10) {
                currentChildren++;
                updateUI();
            }
        });
        btnMinusNinos.setOnClickListener(v -> {
            if (currentChildren > 0) {
                currentChildren--;
                updateUI();
            }
        });
        view.findViewById(R.id.btnConfirmar).setOnClickListener(v -> {
            // Lógica para retornar a tu Fragment o Activity (usa listener o interface)
            if (getActivity() instanceof HotelDetalladoUser) {
                ((HotelDetalladoUser) getActivity()).actualizarHuespedes(currentRooms, currentAdults, currentChildren);
            }
            dismiss();
        });
        return view;
    }
    private void updateUI() {
        tvHabitaciones.setText(String.valueOf(currentRooms));
        tvAdultos.setText(String.valueOf(currentAdults));
        tvNinos.setText(String.valueOf(currentChildren));
        btnMinusHabitaciones.setEnabled(currentRooms > 1);
        btnMinusAdultos.setEnabled(currentAdults > currentRooms);
        btnMinusNinos.setEnabled(currentChildren > 0);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
