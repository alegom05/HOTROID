package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class FragmentReservaActivos extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_item_reserva_activo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Encuentra el CardView por su ID
        CardView cardHotelActivo = view.findViewById(R.id.cardHotelActivo);

        // Configura el listener de clic para el CardView
        cardHotelActivo.setOnClickListener(v -> {
            // Crea un Intent para iniciar la Activity de detalle
            Intent intent = new Intent(getActivity(), DetalleReservaActivo.class);

            // Añade datos extras al intent
            intent.putExtra("hotel_name", "Hotel Los Andes - Perú");
            intent.putExtra("room_details", "1 habitación, 2 adultos, 0 niños");
            intent.putExtra("status", "Confirmado");

            // Inicia la Activity
            startActivity(intent);
        });
    }
}