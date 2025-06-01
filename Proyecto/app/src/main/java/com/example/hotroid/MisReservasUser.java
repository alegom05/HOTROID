package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MisReservasUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_mis_reservas);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

        //llamando a los datos que se supone están en la bd
        List<Reserva> reservas = obtenerReservas();
        List<Hotel> hoteles = obtenerHoteles();

        // Une reservas con hoteles
        ReservaRepository reservaRepository = new ReservaRepository();
        List<ReservaConHotel> reservasConHotel = reservaRepository.obtenerReservasConHotel(reservas, hoteles);


        // Preparas los datos separados por estado
        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel,"activo");
        List<ReservaConHotel> pasados = filtrarPorEstado(reservasConHotel,"pasado");
        List<ReservaConHotel> cancelados = filtrarPorEstado(reservasConHotel,"cancelado");

        List<List<ReservaConHotel>> listas = Arrays.asList(activos, pasados, cancelados);

        ReservasPagerAdapterUser adapter = new ReservasPagerAdapterUser(this, listas);
        viewPager.setAdapter(adapter);

// Conecta con tabs
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Activos"); break;
                case 1: tab.setText("Pasados"); break;
                case 2: tab.setText("Cancelados"); break;
            }
        }).attach();

    }
    //filtrado de reservas por los estados activo, pasados y cancelados
    private List<ReservaConHotel> filtrarPorEstado(List<ReservaConHotel> todas, String estadoDeseado) {
        List<ReservaConHotel> resultado = new ArrayList<>();
        for (ReservaConHotel rh : todas) {
            if (rh.getReserva().getEstado().equalsIgnoreCase(estadoDeseado)) {
                resultado.add(rh);
            }
        }
        return resultado;
    }

    //data falsa de las reservas con los hoteles
    /*hotelList.add(new Hotel("R001","Grand Hotel Madrid", 4.5f, "S/.145/noche","Madrid", R.drawable.hotel_decameron));
        hotelList.add(new Hotel("R002","Barcelona Royal Suite", 5.0f, "S/.210/noche","Barcelona", R.drawable.hotel_aranwa));
        hotelList.add(new Hotel("R003","Valencia Beach Resort", 4.0f, "S/.125/noche","Valencia", R.drawable.hotel_boca_raton));
        hotelList.add(new Hotel("R004","Sevilla Boutique Hotel", 3.5f, "S/.95/noche", "Sevilla",R.drawable.hotel_oro_verde));
        hotelList.add(new Hotel("R005","Granada Historic Palace", 4.8f, "S/.180/noche", "Inglaterra", R.drawable.hotel_sheraton));
*/
    public static List<Hotel> obtenerHoteles() {
        List<Hotel> hoteles = new ArrayList<>();
        hoteles.add(new Hotel("H1", "Hotel Central", 4.5f, "$80", "Argentina", R.drawable.hotel_decameron));
        hoteles.add(new Hotel("H2", "Hotel D'Cameron", 4.0f, "$120", "USA", R.drawable.hotel_aranwa));
        hoteles.add(new Hotel("H3", "Hotel Mar Azul", 3.8f, "$70", "Perú", R.drawable.hotel_boca_raton));
        return hoteles;
    }

    public static List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        reservas.add(new Reserva("R1", "H1", "U1", 2, 3, 1, "2025-05-01", "2025-05-03", "pasado", 160));
        reservas.add(new Reserva("R2", "H2", "U1", 1, 2, 0, "2025-06-10", "2025-06-12", "activo", 240));
        reservas.add(new Reserva("R3", "H3", "U1", 1, 2, 2, "2025-04-05", "2025-04-08", "cancelado", 0));
        return reservas;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_reservas_user); // selecciona el tab actual

        bottomNavigation.setOnItemSelectedListener(item -> {
            //Fragment selectedFragment = null;
            int itemId = item.getItemId();
            Intent intent = new Intent(this, ClienteActivity.class);

            if (itemId == R.id.nav_hoteles_user) {
                intent.putExtra("fragment_destino", "hoteles");
                startActivity(intent);                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                return true; // O true, depende si quieres que el botón quede resaltado o no
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