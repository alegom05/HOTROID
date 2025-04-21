package com.example.hotroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

/*
 * A simple {@link Fragment} subclass.
 * Use the {@link Paso2ReservacionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Paso2ReservacionFragment extends Fragment {
    private SwitchMaterial  switchGimnasio, switchDesayuno, switchPiscina, switchParqueo;
    private TextView tvTotal;
    private MaterialButton btnSiguiente;

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    public Paso2ReservacionFragment() {
        // Required empty public constructor
    }

    /*
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Paso2ReservacionFragment.
     */
    // TODO: Rename and change types and number of parameters
    /*public static Paso2ReservacionFragment newInstance(String param1, String param2) {
        Paso2ReservacionFragment fragment = new Paso2ReservacionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }*/

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paso2_reservacion, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switchGimnasio = view.findViewById(R.id.switchGimnasio);
        switchDesayuno = view.findViewById(R.id.switchDesayuno);
        switchPiscina = view.findViewById(R.id.switchPiscina);
        switchParqueo = view.findViewById(R.id.switchParqueo);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnSiguiente = view.findViewById(R.id.btnSiguientePaso2);

        View.OnClickListener actualizarTotalListener = v -> actualizarTotal();
        switchGimnasio.setOnClickListener(actualizarTotalListener);
        switchDesayuno.setOnClickListener(actualizarTotalListener);
        switchPiscina.setOnClickListener(actualizarTotalListener);
        switchParqueo.setOnClickListener(actualizarTotalListener);

        btnSiguiente.setOnClickListener(v -> {
            Fragment paso3 = new Paso3ReservacionFragment();
            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, paso3);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        actualizarTotal();
    }

    private void actualizarTotal() {
        int total = 200; // Precio base
        if (switchGimnasio.isChecked()) total += 100;
        if (switchDesayuno.isChecked()) total += 80;
        if (switchPiscina.isChecked()) total += 100;
        if (switchParqueo.isChecked()) total += 100;

        tvTotal.setText("TOTAL\nPEN " + total);
    }

}