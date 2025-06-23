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
import java.util.UUID;

public class MisReservasUser extends AppCompatActivity {

    // SimpleDateFormat es estático y final porque no cambia y es eficiente
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

        // Obtener datos estáticos de ejemplo
        List<Reserva> reservas = obtenerReservas();
        List<Hotel> hoteles = obtenerHoteles();

        // Usar ReservaRepository para combinar Reservas y Hoteles
        ReservaRepository reservaRepository = new ReservaRepository();
        List<ReservaConHotel> reservasConHotel = reservaRepository.obtenerReservasConHotel(reservas, hoteles);

        // Filtrar las reservas por estado
        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel, "activo");
        Log.d("MisReservasUser", "Cantidad de reservas activas: " + activos.size());
        List<ReservaConHotel> pasados = filtrarPorEstado(reservasConHotel, "pasado");
        Log.d("MisReservasUser", "Cantidad de reservas pasadas: " + pasados.size());
        List<ReservaConHotel> cancelados = filtrarPorEstado(reservasConHotel, "cancelado");
        Log.d("MisReservasUser", "Cantidad de reservas canceladas: " + cancelados.size());

        // Agrupar las listas filtradas para el PagerAdapter
        List<List<ReservaConHotel>> listasParaPagerAdapter = Arrays.asList(activos, pasados, cancelados);

        // Crear e inicializar el ViewPager con el adaptador
        ReservasPagerAdapterUser adapter = new ReservasPagerAdapterUser(this, listasParaPagerAdapter);
        viewPager.setAdapter(adapter);

        // Conectar el TabLayout con el ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Activos"); break;
                case 1: tab.setText("Pasados"); break;
                case 2: tab.setText("Cancelados"); break;
            }
        }).attach();

        // Ajustar insets para barras del sistema si EdgeToEdge está habilitado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Filtra una lista de ReservaConHotel por un estado específico.
     * @param todas La lista completa de reservas combinadas con hoteles.
     * @param estadoDeseado El estado por el cual filtrar (ej. "activo", "pasado", "cancelado").
     * @return Una nueva lista con las reservas que coinciden con el estado deseado.
     */
    private List<ReservaConHotel> filtrarPorEstado(List<ReservaConHotel> todas, String estadoDeseado) {
        List<ReservaConHotel> resultado = new ArrayList<>();
        if (todas != null) {
            for (ReservaConHotel rh : todas) {
                // Comprobaciones de nulidad para evitar NullPointerExceptions
                if (rh != null && rh.getReserva() != null && rh.getReserva().getEstado() != null &&
                        rh.getReserva().getEstado().equalsIgnoreCase(estadoDeseado)) {
                    resultado.add(rh);
                }
            }
        }
        return resultado;
    }

    /**
     * Proporciona una lista estática de objetos Hotel.
     * @return List<Hotel>
     */
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
        hotel1.setImageResourceId(R.drawable.hotel_decameron);
        hoteles.add(hotel1);

        // HOTEL 2
        Hotel hotel2 = new Hotel();
        hotel2.setIdHotel("H2");
        hotel2.setName("Hotel D'Cameron");
        hotel2.setRating(4.0f);
        hotel2.setPrice(123.00);
        hotel2.setDireccion("USA");
        hotel2.setDireccionDetallada("Av. del Prado 123, Centro Histórico");
        hotel2.setImageResourceId(R.drawable.hotel_aranwa);
        hoteles.add(hotel2);

        // HOTEL 3
        Hotel hotel3 = new Hotel();
        hotel3.setIdHotel("H3");
        hotel3.setName("Hotel Mar Azul");
        hotel3.setRating(3.8f);
        hotel3.setPrice(230.00);
        hotel3.setDireccion("Perú");
        hotel3.setDireccionDetallada("Paseo Marítimo 78, Playa Norte");
        hotel3.setImageResourceId(R.drawable.hotel_boca_raton);
        hoteles.add(hotel3);

        return hoteles;
    }

    /**
     * Proporciona una lista estática de objetos Reserva.
     * @return List<Reserva>
     */
    public static List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            // Reserva 1: Estado "pasado"
            reservas.add(new Reserva(
                    "R1-" + UUID.randomUUID().toString(),
                    "U1",
                    "Carlos",
                    "Ruiz",
                    "H1",
                    "Hotel Central",
                    2,
                    3,
                    1,
                    dateFormatter.parse("2025-05-01"),
                    dateFormatter.parse("2025-05-03"),
                    "pasado",
                    160.0,
                    true,
                    true,
                    20.0,
                    false,
                    null,
                    "V1",
                    "101",
                    true
            ));

            // Reserva 2: Estado "activo"
            reservas.add(new Reserva(
                    "R2-" + UUID.randomUUID().toString(),
                    "U1",
                    "Ana",
                    "Martínez",
                    "H2",
                    "Hotel D'Cameron",
                    1,
                    2,
                    0,
                    dateFormatter.parse("2025-06-25"),
                    dateFormatter.parse("2025-06-28"),
                    "activo",
                    240.0,
                    false,
                    false,
                    0.0,
                    false,
                    null,
                    null,
                    "205",
                    false
            ));

            // Reserva 3: Estado "cancelado"
            reservas.add(new Reserva(
                    "R3-" + UUID.randomUUID().toString(),
                    "U1",
                    "Pedro",
                    "García",
                    "H3",
                    "Hotel Mar Azul",
                    1,
                    2,
                    2,
                    dateFormatter.parse("2025-07-05"),
                    dateFormatter.parse("2025-07-08"),
                    "cancelado",
                    0.0,
                    false,
                    false,
                    0.0,
                    true,
                    dateFormatter.parse("2025-06-15"),
                    null,
                    null,
                    false
            ));

        } catch (ParseException e) {
            Log.e("MisReservasUser", "Error al parsear la fecha en obtenerReservas(): " + e.getMessage());
        }
        return reservas;
    }

    /**
     * Configura la barra de navegación inferior.
     * CORREGIDO: Ahora utiliza referencias directas a las actividades en lugar de
     * pasar a través de ClienteActivity con extras.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_reservas_user);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_hoteles_user) {
                // Ir directamente a ClienteActivity que carga HotelesFragment por defecto
                startActivity(new Intent(this, ClienteActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                // Ya estamos en MisReservasUser, no hacer nada
                return true;
            } else if (itemId == R.id.nav_chat_user) {
                // Ir a ClienteActivity y especificar cargar ChatFragment
                Intent intent = new Intent(this, ClienteActivity.class);
                intent.putExtra("fragment_destino", "chat");
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                // Ir a ClienteActivity y especificar cargar CuentaFragment
                Intent intent = new Intent(this, ClienteActivity.class);
                intent.putExtra("fragment_destino", "cuenta");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}