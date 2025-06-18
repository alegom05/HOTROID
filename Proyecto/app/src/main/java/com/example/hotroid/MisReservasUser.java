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

import java.text.ParseException; // Importar para el manejo de excepciones de parseo de fecha
import java.text.SimpleDateFormat; // Importar para el formateo de fecha
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date; // Importar java.util.Date
import java.util.List;
import java.util.Locale; // Importar para especificar el locale en SimpleDateFormat

public class MisReservasUser extends AppCompatActivity {

    // Se recomienda que SimpleDateFormat sea una instancia de clase si se usa repetidamente
    // o declararla dentro del método si solo se usa una vez.
    // Aquí la hacemos una instancia para consistencia y evitar recrearla.
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

        //llamando a los datos que se supone están en la bd
        List<Reserva> reservas = obtenerReservas();
        List<Hotel> hoteles = obtenerHoteles();

        // Une reservas con hoteles
        ReservaRepository reservaRepository = new ReservaRepository();
        List<ReservaConHotel> reservasConHotel = reservaRepository.obtenerReservasConHotel(reservas, hoteles);


        // Preparas los datos separados por estado
        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel,"activo");
        Log.d("DebugReservas", "Cantidad activos: " + activos.size());
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
            // Asegúrate de que el objeto Reserva no sea null y el estado exista
            if (rh.getReserva() != null && rh.getReserva().getEstado() != null &&
                    rh.getReserva().getEstado().equalsIgnoreCase(estadoDeseado)) {
                resultado.add(rh);
            }
        }
        return resultado;
    }

    public static List<Hotel> obtenerHoteles() {
        List<Hotel> hoteles = new ArrayList<>();
        hoteles.add(new Hotel("H1", "Hotel Central", 4.5f, "$80", "Argentina", "High St 10, Old Town", R.drawable.hotel_decameron));
        hoteles.add(new Hotel("H2", "Hotel D'Cameron", 4.0f, "$120", "USA","Av. del Prado 123, Centro Histórico", R.drawable.hotel_aranwa));
        hoteles.add(new Hotel("H3", "Hotel Mar Azul", 3.8f, "$70", "Perú", "Paseo Marítimo 78, Playa Norte", R.drawable.hotel_boca_raton));
        return hoteles;
    }

    public static List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        // Corrección: Añadir los nuevos 8 parámetros al constructor de Reserva
        // Parámetros: checkInRealizado, checkOutRealizado, cobros_adicionales, estaCancelado, fechaCancelacion, idValoracion, roomNumber, tieneValoracion

        // Crear una instancia de SimpleDateFormat dentro del método si solo se usa aquí,
        // o fuera como atributo de clase si es global. Ya la hicimos atributo de clase arriba.
        // SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            reservas.add(new Reserva("R1", "U1", "H1", 2, 3, 1,
                    dateFormatter.parse("2025-05-01"), dateFormatter.parse("2025-05-03"),
                    "pasado", 160.0, true, true, 20.0, false,
                    dateFormatter.parse("2025-05-03"), "V1", "101", true));

            reservas.add(new Reserva("R2", "U1", "H2", 1, 2, 0,
                    dateFormatter.parse("2025-06-10"), dateFormatter.parse("2025-06-12"),
                    "activo", 240.0, false, false, 0.0, false,
                    null, null, "205", false)); // fechaCancelacion y idValoracion pueden ser null

            reservas.add(new Reserva("R3", "U1", "H3", 1, 2, 2,
                    dateFormatter.parse("2025-04-05"), dateFormatter.parse("2025-04-08"),
                    "cancelado", 0.0, false, false, 0.0, true,
                    dateFormatter.parse("2025-04-01"), null, null, false));

        } catch (ParseException e) {
            // Manejar la excepción si el formato de fecha no es correcto
            Log.e("MisReservasUser", "Error al parsear la fecha: " + e.getMessage());
            // Considera notificar al usuario o tomar alguna acción apropiada
        }
        return reservas;
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_reservas_user); // selecciona el tab actual

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = new Intent(this, ClienteActivity.class);

            if (itemId == R.id.nav_hoteles_user) {
                intent.putExtra("fragment_destino", "hoteles");
                startActivity(intent);
                // Si ClienteActivity va a manejar la navegación y no quieres volver aquí, puedes hacer finish()
                // finish(); // Descomenta si no quieres que MisReservasUser permanezca en la pila de actividades
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                return true; // Ya estás en MisReservasUser, no hay que hacer nada
            } else if (itemId == R.id.nav_chat_user) {
                intent.putExtra("fragment_destino", "chat");
                startActivity(intent);
                finish(); // Para evitar que se apilen muchas actividades en la pila
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                intent.putExtra("fragment_destino", "cuenta");
                startActivity(intent);
                finish(); // Para evitar que se apilen muchas actividades en la pila
                return true;
            }
            return false;
        });
    }
}