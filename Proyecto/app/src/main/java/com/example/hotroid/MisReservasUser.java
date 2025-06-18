package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;
import com.example.hotroid.repository.ReservaRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MisReservasUser extends AppCompatActivity {

    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_mis_reservas);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        List<Reserva> reservas = obtenerReservas();
        List<Hotel> hoteles = obtenerHoteles(); // Ahora usa el método actualizado

        ReservaRepository reservaRepository = new ReservaRepository();
        List<ReservaConHotel> reservasConHotel = reservaRepository.obtenerReservasConHotel(reservas, hoteles);


        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel,"activo");
        Log.d("DebugReservas", "Cantidad activos: " + activos.size());
        List<ReservaConHotel> pasados = filtrarPorEstado(reservasConHotel,"pasado");
        List<ReservaConHotel> cancelados = filtrarPorEstado(reservasConHotel,"cancelado");

        List<List<ReservaConHotel>> listas = Arrays.asList(activos, pasados, cancelados);

        ReservasPagerAdapterUser adapter = new ReservasPagerAdapterUser(this, listas);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Activos"); break;
                case 1: tab.setText("Pasados"); break;
                case 2: tab.setText("Cancelados"); break;
            }
        }).attach();

    }

    private List<ReservaConHotel> filtrarPorEstado(List<ReservaConHotel> todas, String estadoDeseado) {
        List<ReservaConHotel> resultado = new ArrayList<>();
        for (ReservaConHotel rh : todas) {
            if (rh.getReserva() != null && rh.getReserva().getEstado() != null &&
                    rh.getReserva().getEstado().equalsIgnoreCase(estadoDeseado)) {
                resultado.add(rh);
            }
        }
        return resultado;
    }

    public static List<Hotel> obtenerHoteles() {
        List<Hotel> hoteles = new ArrayList<>();

        // HOTEL 1
        Hotel hotel1 = new Hotel();
        hotel1.setIdHotel("H1");
        hotel1.setName("Hotel Central");
        hotel1.setRating(4.5f);
        hotel1.setPrice(130.00);
        hotel1.setDireccion("Argentina");
        hotel1.setDireccionDetallada("High St 10, Old Town");
        hotel1.setDescription("Un hotel céntrico con todas las comodidades en Argentina."); // Añadir descripción
        hotel1.setImageResourceId(R.drawable.hotel_decameron); // Usar R.drawable
        hoteles.add(hotel1);

        // HOTEL 2
        Hotel hotel2 = new Hotel();
        hotel2.setIdHotel("H2");
        hotel2.setName("Hotel D'Cameron");
        hotel2.setRating(4.0f);
        hotel2.setPrice(123.00);
        hotel2.setDireccion("USA");
        hotel2.setDireccionDetallada("Av. del Prado 123, Centro Histórico");
        hotel2.setDescription("Ubicado en el corazón de USA, ideal para viajeros de negocios y placer."); // Añadir descripción
        hotel2.setImageResourceId(R.drawable.hotel_aranwa); // Usar R.drawable
        hoteles.add(hotel2);

        // HOTEL 3
        Hotel hotel3 = new Hotel();
        hotel3.setIdHotel("H3");
        hotel3.setName("Hotel Mar Azul");
        hotel3.setRating(3.8f);
        hotel3.setPrice(230.00);
        hotel3.setDireccion("Perú");
        hotel3.setDireccionDetallada("Paseo Marítimo 78, Playa Norte");
        hotel3.setDescription("Un encantador hotel frente al mar en Perú, perfecto para unas vacaciones relajantes."); // Añadir descripción
        hotel3.setImageResourceId(R.drawable.hotel_boca_raton); // Usar R.drawable
        hoteles.add(hotel3);

        return hoteles;
    }

    public static List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            reservas.add(new Reserva("R1", "U1", "H1", 2, 3, 1,
                    dateFormatter.parse("2025-05-01"), dateFormatter.parse("2025-05-03"),
                    "pasado", 160.0, true, true, 20.0, false,
                    dateFormatter.parse("2025-05-03"), "V1", "101", true));

            reservas.add(new Reserva("R2", "U1", "H2", 1, 2, 0,
                    dateFormatter.parse("2025-06-10"), dateFormatter.parse("2025-06-12"),
                    "activo", 240.0, false, false, 0.0, false,
                    null, null, "205", false));

            reservas.add(new Reserva("R3", "U1", "H3", 1, 2, 2,
                    dateFormatter.parse("2025-04-05"), dateFormatter.parse("2025-04-08"),
                    "cancelado", 0.0, false, false, 0.0, true,
                    dateFormatter.parse("2025-04-01"), null, null, false));

        } catch (ParseException e) {
            Log.e("MisReservasUser", "Error al parsear la fecha: " + e.getMessage());
        }
        return reservas;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_reservas_user);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = new Intent(this, ClienteActivity.class);

            if (itemId == R.id.nav_hoteles_user) {
                intent.putExtra("fragment_destino", "hoteles");
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                return true;
            } else if (itemId == R.id.nav_chat_user) {
                intent.putExtra("fragment_destino", "chat");
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                intent.putExtra("fragment_destino", "cuenta");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}