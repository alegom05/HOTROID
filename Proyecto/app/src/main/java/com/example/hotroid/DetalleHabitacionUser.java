package com.example.hotroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class DetalleHabitacionUser extends AppCompatActivity {

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

}