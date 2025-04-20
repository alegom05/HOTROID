package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class DetalleHabitacionUser extends AppCompatActivity {
    private TextView roomGuestsText, precioText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_detalle_habitacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarCarruselImagenes();
        configurarSelectorHabitaciones();

        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        MaterialButton btnReservar = findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
            Intent intent = new Intent(DetalleHabitacionUser.this, ProcesoReservaUser.class);
            startActivity(intent);
        });

    }

    private void inicializarCarruselImagenes() {
        ViewPager2 viewPager = findViewById(R.id.viewPagerImagenes);
        CircleIndicator3 indicator = findViewById(R.id.indicadorImagenes);

        List<Integer> imagenes = Arrays.asList(
                R.drawable.hotel_room,
                R.drawable.hotel_room_doble,
                R.drawable.hotel_room_deluxe
        );

        ImagenHabitacionAdapterUser adapter = new ImagenHabitacionAdapterUser(imagenes);
        viewPager.setAdapter(adapter);
        indicator.setViewPager(viewPager);
    }
    private void configurarSelectorHabitaciones() {
        LinearLayout roomGuestsLayout = findViewById(R.id.roomGuestsLayout);
        roomGuestsText = findViewById(R.id.roomGuestsText);
        precioText = findViewById(R.id.tvPrecio);

        roomGuestsLayout.setOnClickListener(v -> mostrarDialogoHabitaciones());
    }

    private void mostrarDialogoHabitaciones() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_habitaciones, null);
        NumberPicker npHabitaciones = dialogView.findViewById(R.id.npHabitaciones);

        npHabitaciones.setMinValue(1);
        npHabitaciones.setMaxValue(3);
        npHabitaciones.setValue(1);

        new AlertDialog.Builder(this)
                .setTitle("Selecciona cantidad de habitaciones")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    int cantidad = npHabitaciones.getValue();
                    roomGuestsText.setText(cantidad + (cantidad == 1 ? " habitación" : " habitaciones"));

                    int precio = (cantidad == 1) ? 1200 : (cantidad == 2) ? 1500 : 1800;
                    precioText.setText("S/. " + precio);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }


}