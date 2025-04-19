package com.example.hotroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DetalleReservaActivo extends Fragment {

    private TextView tvHotelName, tvRoomDetails, tvStatus;
    private TextView tvCheckIn, tvCheckOut, tvReservationCode;

    public DetalleReservaActivo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.user_detalle_reserva_activo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvHotelName = view.findViewById(R.id.tvHotelNameDetail);
        tvRoomDetails = view.findViewById(R.id.tvRoomDetailsDetail);
        tvStatus = view.findViewById(R.id.tvStatusDetail);
        tvCheckIn = view.findViewById(R.id.tvCheckInDate);
        tvCheckOut = view.findViewById(R.id.tvCheckOutDate);
        tvReservationCode = view.findViewById(R.id.tvReservationCode);

        // Get arguments if available
        if (getArguments() != null) {
            String hotelName = getArguments().getString("hotel_name", "");
            String roomDetails = getArguments().getString("room_details", "");
            String status = getArguments().getString("status", "");

            // Set values to views
            tvHotelName.setText(hotelName);
            tvRoomDetails.setText(roomDetails);
            tvStatus.setText("Estado: " + status);

            // For demo purposes, set some default values
            tvCheckIn.setText("Check-in: 22/04/2025");
            tvCheckOut.setText("Check-out: 27/04/2025");
            tvReservationCode.setText("Código de reserva: RES123456");
        }
    }
}