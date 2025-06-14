package com.example.hotroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater; // Importa LayoutInflater
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.hotroid.bean.Reserva;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuperReservasActivity extends AppCompatActivity {

    private TextView tvHotelNombre;
    private LinearLayout llReservasContainer;
    private EditText etSearchUser;
    private Button btnLimpiar;

    private List<Reserva> allReservas;
    private List<Reserva> currentHotelReservas;

    private String selectedHotelName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_reservas_hotel);

        tvHotelNombre = findViewById(R.id.tvHotelNombre);
        llReservasContainer = findViewById(R.id.llReservasContainer);
        etSearchUser = findViewById(R.id.etSearchUser);
        btnLimpiar = findViewById(R.id.btnClearSearch);

        // 1. Obtener el nombre del hotel del Intent
        Intent intent = getIntent();
        selectedHotelName = "Hotel Desconocido";

        if (intent != null && intent.hasExtra("hotel_name")) {
            String receivedName = intent.getStringExtra("hotel_name");
            if (receivedName != null && !receivedName.isEmpty()) {
                selectedHotelName = receivedName;
            }
        }

        tvHotelNombre.setText("Reservas para " + selectedHotelName);

        // 2. Inicializar todos los datos de reservas (datos de prueba)
        initializeReservasData();

        // Filtrar reservas específicamente para el hotel actual
        currentHotelReservas = getReservationsForHotel(selectedHotelName);

        // 3. Mostrar las reservas iniciales (todas para este hotel, sin filtro de búsqueda aún)
        displayFilteredReservas(currentHotelReservas);

        // --- Funcionalidad de búsqueda (TextWatcher para filtrado dinámico) ---
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No usado
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar a medida que el usuario escribe
                filterReservas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No usado
            }
        });

        // --- Funcionalidad del botón Limpiar ---
        btnLimpiar.setOnClickListener(v -> {
            etSearchUser.setText(""); // Limpiar el texto de búsqueda
            // El TextWatcher automáticamente llamará a filterReservas("")
            Toast.makeText(this, "Búsqueda limpiada.", Toast.LENGTH_SHORT).show();
        });


        // --- Configuración de la barra de navegación inferior ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles); // Mantener 'Hoteles' seleccionado

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperReservasActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperReservasActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperReservasActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                finish();
                return true;
            }
            return false;
        });

        // --- Listener de clic en la tarjeta de administrador ---
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intentAccount = new Intent(SuperReservasActivity.this, SuperCuentaActivity.class);
            startActivity(intentAccount);
        });
    }

    private void initializeReservasData() {
        allReservas = new ArrayList<>();


        // LOS DATOS DE RESERVAS A CONTINUACIÓN DEBEN SER OBTENIDOS DE TU BASE DE DATOS.
        // ESTAS LÍNEAS SE MANTIENEN COMO COMENTARIO PARA REFERENCIA.
        // --- INICIO DE DATOS DE PRUEBA (COMENTADOS) ---
        // Asegúrate de que idHotel coincida con los nombres de hoteles en SuperActivity (ej. "Aranwa")
        /*
        allReservas.add(new Reserva("R002", "Aranwa", "Leandro Pérez", 1, 1, 0, "24/03/2025", "29/03/2025", "activo", 500));
        allReservas.add(new Reserva("R003", "Aranwa", "Augusto Medina", 2, 3, 1, "22/03/2025", "29/03/2025", "activo", 1200));
        allReservas.add(new Reserva("R004", "Aranwa", "Gustavo Cuya", 1, 2, 0, "20/03/2025", "26/03/2025", "activo", 750));
        allReservas.add(new Reserva("R005", "Aranwa", "Roger Palomo", 1, 2, 0, "15/04/2025", "20/04/2025", "activo", 900));
        allReservas.add(new Reserva("R025", "Aranwa", "Joaquín Álvarez", 1, 1, 0, "01/06/2025", "03/06/2025", "activo", 600));
        allReservas.add(new Reserva("R026", "Aranwa", "Nassim Ramírez", 2, 4, 2, "05/06/2025", "10/06/2025", "activo", 1500));
        allReservas.add(new Reserva("R040", "Aranwa", "Elias Pulgar", 1, 2, 0, "12/06/2025", "15/06/2025", "activo", 850));
        allReservas.add(new Reserva("R041", "Aranwa", "Mirko Paz", 1, 1, 0, "18/06/2025", "20/06/2025", "activo", 580));
        allReservas.add(new Reserva("R042", "Aranwa", "Eliezer Cruz", 1, 2, 1, "25/06/2025", "30/06/2025", "activo", 1100));

        // Reservas de Decameron

        allReservas.add(new Reserva("R007", "Decameron", "Roger Albino", 1, 1, 0, "10/05/2025", "13/05/2025", "activo", 600));
        allReservas.add(new Reserva("R027", "Decameron", "Julio Uribe", 1, 2, 0, "15/06/2025", "20/06/2025", "activo", 1000));
        allReservas.add(new Reserva("R028", "Decameron", "Brian Ali", 2, 3, 1, "22/06/2025", "28/06/2025", "activo", 1800));
        allReservas.add(new Reserva("R029", "Decameron", "Miguel Jara", 1, 1, 0, "01/07/2025", "03/07/2025", "activo", 700));
        allReservas.add(new Reserva("R043", "Decameron", "Flavio Farro", 1, 2, 0, "05/07/2025", "10/07/2025", "activo", 1200));
        allReservas.add(new Reserva("R044", "Decameron", "Jose Phan", 1, 1, 0, "12/07/2025", "14/07/2025", "activo", 650));

        // Reservas de Oro Verde
        //allReservas.add(new Reserva("R008", "Oro Verde", "Eduardo Campos", 1, 2, 0, "05/06/2025", "08/06/2025", "activo", 950));
        //allReservas.add(new Reserva("R030", "Oro Verde", "Rubén Cancho", 1, 2, 2, "10/07/2025", "15/07/2025", "activo", 1300));
        //allReservas.add(new Reserva("R031", "Oro Verde", "Aaron Villa", 1, 1, 0, "20/07/2025", "23/07/2025", "activo", 800));
        //allReservas.add(new Reserva("R045", "Oro Verde", "Ollanta Humala", 2, 3, 1, "01/08/2025", "06/08/2025", "activo", 1500));
        //allReservas.add(new Reserva("R046", "Oro Verde", "Nadine Heredia", 1, 2, 0, "10/08/2025", "13/08/2025", "activo", 900));

        // Reservas de Boca Ratón
        allReservas.add(new Reserva("R009", "Boca Ratón", "Sigrid Bazán", 1, 1, 0, "01/07/2025", "04/07/2025", "activo", 550));
        allReservas.add(new Reserva("R032", "Boca Ratón", "Daniel Abugattás", 2, 3, 0, "08/08/2025", "12/08/2025", "activo", 1000));
        allReservas.add(new Reserva("R033", "Boca Ratón", "Mauricio Mulder", 1, 2, 0, "15/08/2025", "18/08/2025", "activo", 650));
        allReservas.add(new Reserva("R047", "Boca Ratón", "Manuel Merino", 1, 1, 0, "20/08/2025", "22/08/2025", "activo", 580));

        // Reservas de Libertador
        allReservas.add(new Reserva("R010", "Libertador", "Pamela López", 1, 2, 0, "25/07/2025", "28/07/2025", "activo", 1300));
        allReservas.add(new Reserva("R034", "Libertador", "Carlos Álvarez", 1, 1, 0, "05/09/2025", "07/09/2025", "activo", 900));
        allReservas.add(new Reserva("R035", "Libertador", "Robert Prevost", 2, 4, 1, "10/09/2025", "15/09/2025", "activo", 2000));
        allReservas.add(new Reserva("R048", "Libertador", "Alan García", 1, 2, 0, "18/09/2025", "22/09/2025", "activo", 1400));

        // Reservas de Costa del Sol
        allReservas.add(new Reserva("R011", "Costa del Sol", "Alejandro Toledo", 1, 2, 0, "12/08/2025", "15/08/2025", "activo", 700));
        allReservas.add(new Reserva("R036", "Costa del Sol", "Keiko Fujimori", 1, 1, 0, "20/09/2025", "23/09/2025", "activo", 500));
        allReservas.add(new Reserva("R037", "Costa del Sol", "Pedro Castillo", 2, 3, 2, "01/10/2025", "07/10/2025", "activo", 1400));
        allReservas.add(new Reserva("R049", "Costa del Sol", "Agustín Lozano", 1, 2, 1, "10/10/2025", "13/10/2025", "activo", 800));

        // Reservas de Sonesta
        allReservas.add(new Reserva("R012", "Sonesta", "Oscar Ibáñez", 1, 2, 0, "01/09/2025", "04/09/2025", "activo", 850));
        allReservas.add(new Reserva("R038", "Sonesta", "Verónica Mendoza", 1, 1, 0, "10/10/2025", "12/10/2025", "activo", 600));
        allReservas.add(new Reserva("R039", "Sonesta", "Ismael Retes", 2, 3, 1, "15/10/2025", "20/10/2025", "activo", 1200));
        allReservas.add(new Reserva("R050", "Sonesta", "Patricia Benavides", 1, 2, 0, "22/10/2025", "25/10/2025", "activo", 950));
        */
        // --- FIN DE DATOS DE PRUEBA (COMENTADOS) ---

        // Aquí es donde en el futuro harías tu llamada a la base de datos
        // para poblar 'allReservas' con datos reales.
        // Por ahora, para que la app no falle sin datos, podrías agregar algunas reservas aquí
        // O asegurarte de que tu función de base de datos retorne una lista vacía si no hay datos.

        // Ejemplo: Si quieres mantener un par de datos para probar sin la base de datos:
        allReservas.add(new Reserva("R001", "Aranwa", "Juan Molleda", 1, 2, 0, "30/03/2025", "05/04/2025", "activo", 800));
        allReservas.add(new Reserva("R006", "Decameron", "Joaquín Pozo", 1, 2, 1, "01/05/2025", "07/05/2025", "activo", 1100));
        allReservas.add(new Reserva("R009", "Boca Ratón", "Sigrid Bazán", 1, 1, 0, "01/07/2025", "04/07/2025", "activo", 550));
    }


    private List<Reserva> getReservationsForHotel(String hotelIdentifier) {
        List<Reserva> filteredList = new ArrayList<>();
        // Asegúrate de que 'allReservas' no sea nulo antes de iterar
        if (allReservas != null) {
            for (Reserva reserva : allReservas) {
                if (reserva.getIdHotel().equals(hotelIdentifier)) {
                    filteredList.add(reserva);
                }
            }
        }
        return filteredList;
    }

    private void filterReservas(String searchText) {
        llReservasContainer.removeAllViews(); // Limpiar las vistas existentes

        List<Reserva> filteredReservas = new ArrayList<>();
        if (searchText.isEmpty()) {
            filteredReservas.addAll(currentHotelReservas);
        } else {
            String lowerCaseSearchText = searchText.toLowerCase(Locale.getDefault());
            for (Reserva reserva : currentHotelReservas) {
                if (reserva.getIdPersona().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText) ||
                        reserva.getIdReserva().toLowerCase(Locale.getDefault()).contains(lowerCaseSearchText)) {
                    filteredReservas.add(reserva);
                }
            }
        }

        if (filteredReservas.isEmpty()) {
            TextView noReservationsText = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            noReservationsText.setLayoutParams(lp);
            noReservationsText.setText("No hay reservas encontradas para '" + searchText + "' en " + selectedHotelName + ".");
            noReservationsText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            noReservationsText.setTextColor(Color.GRAY);
            noReservationsText.setGravity(Gravity.CENTER);
            noReservationsText.setPadding(0, dpToPx(32), 0, dpToPx(32));
            llReservasContainer.addView(noReservationsText);
        } else {
            displayFilteredReservas(filteredReservas);
        }
    }

    private void displayFilteredReservas(List<Reserva> reservasToDisplay) {
        llReservasContainer.removeAllViews(); // Limpiar las vistas existentes antes de añadir nuevas

        LayoutInflater inflater = LayoutInflater.from(this); // Obtener una instancia de LayoutInflater

        for (Reserva reserva : reservasToDisplay) {
            // Inflar el layout item_reserva_card.xml
            View cardView = inflater.inflate(R.layout.item_reserva_card, llReservasContainer, false);

            // Encontrar los TextViews dentro de la vista de la tarjeta inflada
            TextView tvClienteCard = cardView.findViewById(R.id.tvClienteCard);
            TextView tvFechaReservaCard = cardView.findViewById(R.id.tvFechaReservaCard);
            TextView tvHabitacionCard = cardView.findViewById(R.id.tvHabitacionCard);
            TextView tvPrecioTotalCard = cardView.findViewById(R.id.tvPrecioTotalCard);

            // Establecer los datos para cada TextView
            tvClienteCard.setText("Cliente: " + reserva.getIdPersona());
            tvFechaReservaCard.setText("Fecha de Reserva: " + reserva.getFechaInicio() + " - " + reserva.getFechaFin());
            tvHabitacionCard.setText("Habitaciones: " + reserva.getHabitaciones());
            tvPrecioTotalCard.setText(String.format(Locale.getDefault(), "Precio Total: $%d", reserva.getPrecioTotal()));


            // Opcional: Añadir un listener de clic a cada tarjeta
            cardView.setOnClickListener(v -> {
                Toast.makeText(this, "Reserva de " + reserva.getIdPersona() + " seleccionada.", Toast.LENGTH_SHORT).show();
                // Puedes abrir una nueva actividad aquí para mostrar los detalles completos de la reserva
                // Intent detailIntent = new Intent(SuperReservasActivity.this, ReservaDetalleActivity.class);
                // detailIntent.putExtra("reserva_id", reserva.getIdReserva());
                // startActivity(detailIntent);
            });

            // Añadir la vista de la tarjeta inflada al contenedor LinearLayout
            llReservasContainer.addView(cardView);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}