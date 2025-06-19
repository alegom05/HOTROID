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
import com.example.hotroid.repository.ReservaRepository; // Asumo que esta clase es correcta
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
import java.util.UUID; // Importar para generar IDs de ejemplo

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
        // Asumo que tu ReservaRepository.obtenerReservasConHotel() funciona correctamente.
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> { // Asegúrate que R.id.main sea el ID de tu layout principal
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
     * Los campos se llenan usando setters.
     * @return List<Hotel>
     */
    public static List<Hotel> obtenerHoteles() {
        List<Hotel> hoteles = new ArrayList<>();

        // HOTEL 1
        Hotel hotel1 = new Hotel(); // Usa el constructor vacío
        hotel1.setIdHotel("H1"); // ID como String
        hotel1.setName("Hotel Central");
        hotel1.setRating(4.5f);
        // hotel1.setPrice(130.00); // Asegúrate de que tu clase Hotel tenga setPrice si usas este campo
        hotel1.setPrice(130.00); // Usando setPrecioNoche, que es lo que tienes definido
        hotel1.setDireccion("Argentina");
        hotel1.setDireccionDetallada("High St 10, Old Town");
        // hotel1.setDescription("Un hotel céntrico con todas las comodidades en Argentina."); // Asegúrate de que tu clase Hotel tenga setDescription
        hotel1.setImageResourceId(R.drawable.hotel_decameron); // Usa R.drawable.tu_imagen_real
        hoteles.add(hotel1);

        // HOTEL 2
        Hotel hotel2 = new Hotel();
        hotel2.setIdHotel("H2");
        hotel2.setName("Hotel D'Cameron");
        hotel2.setRating(4.0f);
        // hotel2.setPrice(123.00);
        hotel2.setPrice(123.00);
        hotel2.setDireccion("USA");
        hotel2.setDireccionDetallada("Av. del Prado 123, Centro Histórico");
        // hotel2.setDescription("Ubicado en el corazón de USA, ideal para viajeros de negocios y placer.");
        hotel2.setImageResourceId(R.drawable.hotel_aranwa); // Usa R.drawable.tu_imagen_real
        hoteles.add(hotel2);

        // HOTEL 3
        Hotel hotel3 = new Hotel();
        hotel3.setIdHotel("H3");
        hotel3.setName("Hotel Mar Azul");
        hotel3.setRating(3.8f);
        // hotel3.setPrice(230.00);
        hotel3.setPrice(230.00);
        hotel3.setDireccion("Perú");
        hotel3.setDireccionDetallada("Paseo Marítimo 78, Playa Norte");
        // hotel3.setDescription("Un encantador hotel frente al mar en Perú, perfecto para unas vacaciones relajantes.");
        hotel3.setImageResourceId(R.drawable.hotel_boca_raton); // Usa R.drawable.tu_imagen_real
        hoteles.add(hotel3);

        return hoteles;
    }

    /**
     * Proporciona una lista estática de objetos Reserva, llenando todos los campos
     * del constructor completo de la clase Reserva.
     * @return List<Reserva>
     */
    public static List<Reserva> obtenerReservas() {
        List<Reserva> reservas = new ArrayList<>();
        try {
            // Reserva 1: Estado "pasado"
            reservas.add(new Reserva(
                    "R1-" + UUID.randomUUID().toString(), // idReserva (String)
                    "U1", // idPersona (String)
                    "Carlos", // nombresCliente (String)
                    "Ruiz",   // apellidosCliente (String)
                    "H1", // idHotel (String) - Coincide con id del Hotel 1
                    "Hotel Central", // nombreHotel (String) - Desnormalizado
                    2, // habitaciones (int)
                    3, // adultos (int)
                    1, // ninos (int)
                    dateFormatter.parse("2025-05-01"), // fechaInicio (Date)
                    dateFormatter.parse("2025-05-03"), // fechaFin (Date)
                    "pasado", // estado (String)
                    160.0, // precioTotal (double)
                    true, // checkInRealizado (boolean)
                    true, // checkOutRealizado (boolean)
                    20.0, // cobros_adicionales (double)
                    false, // estaCancelado (boolean)
                    null, // fechaCancelacion (Date) - null si no está cancelado
                    "V1", // idValoracion (String)
                    "101", // roomNumber (String)
                    true // tieneValoracion (boolean)
            ));

            // Reserva 2: Estado "activo"
            reservas.add(new Reserva(
                    "R2-" + UUID.randomUUID().toString(), // idReserva
                    "U1", // idPersona
                    "Ana",
                    "Martínez",
                    "H2", // idHotel - Coincide con id del Hotel 2
                    "Hotel D'Cameron", // nombreHotel
                    1,
                    2,
                    0,
                    dateFormatter.parse("2025-06-25"), // Fecha futura para activa
                    dateFormatter.parse("2025-06-28"),
                    "activo",
                    240.0,
                    false,
                    false,
                    0.0,
                    false,
                    null, // no cancelado
                    null, // no valorado aún
                    "205",
                    false
            ));

            // Reserva 3: Estado "cancelado"
            reservas.add(new Reserva(
                    "R3-" + UUID.randomUUID().toString(), // idReserva
                    "U1", // idPersona
                    "Pedro",
                    "García",
                    "H3", // idHotel - Coincide con id del Hotel 3
                    "Hotel Mar Azul", // nombreHotel
                    1,
                    2,
                    2,
                    dateFormatter.parse("2025-07-05"), // Fecha original
                    dateFormatter.parse("2025-07-08"),
                    "cancelado",
                    0.0, // Precio total 0 si es cancelada sin penalidad
                    false,
                    false,
                    0.0,
                    true, // ¡Esta reserva está cancelada!
                    dateFormatter.parse("2025-06-15"), // Fecha de cancelación
                    null,
                    null, // Room number null si se canceló antes de la asignación
                    false
            ));

        } catch (ParseException e) {
            // Registrar el error en Logcat
            Log.e("MisReservasUser", "Error al parsear la fecha en obtenerReservas(): " + e.getMessage());
            // Opcional: Mostrar un Toast al usuario
            // Toast.makeText(this, "Error al cargar las fechas de reserva.", Toast.LENGTH_LONG).show();
        }
        return reservas;
    }

    /**
     * Configura la barra de navegación inferior.
     */
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        // Asegurarse de que el ítem correcto esté seleccionado al inicio
        bottomNavigation.setSelectedItemId(R.id.nav_reservas_user);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = new Intent(this, ClienteActivity.class); // Asegúrate que ClienteActivity exista

            if (itemId == R.id.nav_hoteles_user) {
                intent.putExtra("fragment_destino", "hoteles");
                startActivity(intent);
                // Si la actividad actual no debe permanecer en el stack, puedes llamar finish()
                // finish();
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                // Ya estamos en MisReservasUser, no hacer nada o refrescar si es necesario
                return true;
            } else if (itemId == R.id.nav_chat_user) {
                intent.putExtra("fragment_destino", "chat");
                startActivity(intent);
                finish(); // Finalizar para no acumular actividades en el stack
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                intent.putExtra("fragment_destino", "cuenta");
                startActivity(intent);
                finish(); // Finalizar para no acumular actividades en el stack
                return true;
            }
            return false;
        });
    }
}