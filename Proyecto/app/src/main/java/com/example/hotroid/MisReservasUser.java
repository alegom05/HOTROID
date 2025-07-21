package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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
import com.example.hotroid.ReservaService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MisReservasUser extends AppCompatActivity {

    private static final String TAG = "MisReservasUser";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private List<Reserva> listaReservas = new ArrayList<>();
    private List<Hotel> listaHoteles = new ArrayList<>();
    private ReservaService reservaService;
    ReservasPagerAdapterUser adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*EdgeToEdge.enable(this);*/
        setContentView(R.layout.user_mis_reservas);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        reservaService = new ReservaService();

        // Inicializar vistas
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        setupBottomNavigation();

//        // Cargar datos
//        cargarHoteles();
        cargarReservasDesdeFirebase();

        // Ajustar insets para barras del sistema si EdgeToEdge está habilitado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarReservasDesdeFirebase() {
        // Verificar si el usuario está autenticado
        if (auth.getCurrentUser() == null) {
            Log.w(TAG, "Usuario no autenticado");
            return;
        }

        // Obtener el ID del usuario actual
        String userId = auth.getCurrentUser().getUid();

        // Consultar las reservas del usuario en Firestore
        reservaService.obtenerReservasUsuario( reservas -> {
            listaReservas = reservas;
            Log.d(TAG, "Reservas obtenidas desde Firestore: " + listaReservas.size());
            mostrarReservasDinamicas();
            }, e -> {
            Log.e(TAG, "Error al obtener reservas: " + e.getMessage());
            Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
        });
    }

    private void cargarHoteles() {
        // Cargar hoteles desde Firestore o usar datos estáticos
        // Por simplicidad, usamos los datos estáticos
        listaHoteles = obtenerHoteles();
    }
    private void mostrarReservasDinamicas() {
        // Usar ReservaRepository para combinar Reservas y Hoteles
        ReservaRepository reservaRepository = new ReservaRepository();
        reservaRepository.obtenerReservasConHotelFirestore(listaReservas, new ReservaRepository.Callback() {
                    @Override
                    public void onResult(List<ReservaConHotel> reservasConHotel) {
                        // Filtrar las reservas por estado
                        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel, "activo");
                        List<ReservaConHotel> pasados = filtrarPorEstado(reservasConHotel, "pasado");
                        List<ReservaConHotel> cancelados = filtrarPorEstado(reservasConHotel, "cancelado");

                        Log.d(TAG, "Cantidad de reservas activas: " + activos.size());
                        Log.d(TAG, "Cantidad de reservas pasadas: " + pasados.size());
                        Log.d(TAG, "Cantidad de reservas canceladas: " + cancelados.size());

                        // Agrupar en el orden de tabs
                        List<List<ReservaConHotel>> listasParaPagerAdapter = Arrays.asList(activos, pasados, cancelados);
                        // Crear e inicializar el ViewPager con el adaptador
                        adapter = new ReservasPagerAdapterUser(MisReservasUser.this, listasParaPagerAdapter);
                        viewPager.setAdapter(adapter);
                        // Conectar el TabLayout con el ViewPager2
                        conectarTabsConViewPager();
                    }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error al obtener hoteles: " + e.getMessage());
                Toast.makeText(MisReservasUser.this, "Error al obtener hoteles", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void mostrarReservasDinamicasDesdeFirestore() {
//        db = FirebaseFirestore.getInstance();
//        auth = FirebaseAuth.getInstance();
//        String currentUserId = auth.getCurrentUser().getUid();
//
//        db.collection("reservas")
//                .whereEqualTo("idPersona", currentUserId)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Reserva> activas = new ArrayList<>();
//                    List<Reserva> pasadas = new ArrayList<>();
//                    List<Reserva> canceladas = new ArrayList<>();
//
//                    Date hoy = new Date();
//
//                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
//                        Reserva reserva = doc.toObject(Reserva.class);
//                        reserva.setIdReserva(doc.getId()); // Si quieres guardar el ID del doc
//
//                        if (reserva.getEstado().equals("Cancelada")) {
//                            canceladas.add(reserva);
//                        } else if (reserva.getFechaFin().before(hoy)) {
//                            pasadas.add(reserva);
//                        } else {
//                            activas.add(reserva);
//                        }
//                    }
//
//                    // Asignar listas a cada fragmento
//                    adapter = new ReservasPagerAdapterUser(this);
//                    adapter.setListasReservas(activas, pasadas, canceladas);
//                    viewPager.setAdapter(adapter);
//                    new TabLayoutMediator(tabLayout, viewPager,
//                            (tab, position) -> {
//                                if (position == 0) tab.setText("Activos");
//                                else if (position == 1) tab.setText("Pasados");
//                                else tab.setText("Cancelados");
//                            }).attach();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show();
//                    Log.e("MisReservasUser", "Error Firestore: " + e.getMessage());
//                });
//
//        // Usar ReservaRepository para combinar Reservas y Hoteles
//        ReservaRepository reservaRepository = new ReservaRepository();
//        List<ReservaConHotel> reservasConHotel = reservaRepository.obtenerReservasConHotel(
//                reservas, listaHoteles);
//
//        // Filtrar las reservas por estado
//        List<ReservaConHotel> activos = filtrarPorEstado(reservasConHotel, "activo");
//        List<ReservaConHotel> pasados = filtrarPorEstado(reservasConHotel, "pasado");
//        List<ReservaConHotel> cancelados = filtrarPorEstado(reservasConHotel, "cancelado");
//
//        Log.d(TAG, "Cantidad de reservas activas (estáticas): " + activos.size());
//        Log.d(TAG, "Cantidad de reservas pasadas (estáticas): " + pasados.size());
//        Log.d(TAG, "Cantidad de reservas canceladas (estáticas): " + cancelados.size());
//
//        // Agrupar las listas filtradas para el PagerAdapter
//        List<List<ReservaConHotel>> listasParaPagerAdapter = Arrays.asList(activos, pasados, cancelados);
//
//        // Crear e inicializar el ViewPager con el adaptador
//        ReservasPagerAdapterUser adapter = new ReservasPagerAdapterUser(this, listasParaPagerAdapter);
//        viewPager.setAdapter(adapter);
//
//        // Conectar el TabLayout con el ViewPager2
//        conectarTabsConViewPager();
//    }

    private void conectarTabsConViewPager() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Activos"); break;
                case 1: tab.setText("Pasados"); break;
                case 2: tab.setText("Cancelados"); break;
            }
        }).attach();
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
        return hoteles;
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

    /**
     * Método para manejar la cancelación de reservas
     * Este método se llamaría desde el ReservaItemAdapter cuando se presiona el botón Cancelar
     */
    public void cancelarReserva(String idReserva) {
        reservaService.cancelarReserva(
                idReserva,
                // Éxito
                () -> {
                    // Mostrar mensaje de éxito
                    android.widget.Toast.makeText(this,
                            "Reserva cancelada con éxito",
                            android.widget.Toast.LENGTH_SHORT).show();

                    // Recargar las reservas
                    cargarReservasDesdeFirebase();
                },
                // Error
                e -> {
                    android.widget.Toast.makeText(this,
                            "Error al cancelar la reserva: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG).show();
                }
        );
    }
}