package com.example.hotroid;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button; // Changed from ImageButton to Button
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
    private Button btnLimpiar; // Changed from ImageButton to Button

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
        btnLimpiar = findViewById(R.id.btnClearSearch); // Initialize the Button

        // 1. Get the hotel name from the Intent
        Intent intent = getIntent();
        selectedHotelName = "Hotel Desconocido";

        if (intent != null && intent.hasExtra("hotel_name")) {
            String receivedName = intent.getStringExtra("hotel_name");
            if (receivedName != null && !receivedName.isEmpty()) {
                selectedHotelName = receivedName;
            }
        }

        tvHotelNombre.setText("Reservas para " + selectedHotelName);

        // 2. Initialize all dummy reservation data
        initializeReservasData();

        // Filter reservations specifically for the current hotel
        currentHotelReservas = getReservationsForHotel(selectedHotelName);

        // 3. Display initial reservations (all for this hotel, no search filter yet)
        displayFilteredReservas(currentHotelReservas);

        // --- Search functionality (TextWatcher for dynamic filtering) ---
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter as the user types
                filterReservas(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // --- Limpiar button functionality ---
        btnLimpiar.setOnClickListener(v -> {
            etSearchUser.setText(""); // Clear the search text
            // The TextWatcher will automatically call filterReservas("")
            Toast.makeText(this, "Búsqueda limpiada.", Toast.LENGTH_SHORT).show();
        });


        // --- Bottom Navigation View Setup ---
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles); // Keep 'Hoteles' selected

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

        // --- Admin Card Click Listener ---
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intentAccount = new Intent(SuperReservasActivity.this, SuperCuentaActivity.class);
            startActivity(intentAccount);
        });
    }

    private void initializeReservasData() {
        allReservas = new ArrayList<>();

        // Ensure idHotel matches hotel names in SuperActivity (e.g., "Aranwa")

        // Aranwa Reservations
        allReservas.add(new Reserva("R001", "Aranwa", "Juan Molleda", 1, 2, 0, "30/03/2025", "05/04/2025", "activo", 800));
        allReservas.add(new Reserva("R002", "Aranwa", "Leandro Pérez", 1, 1, 0, "24/03/2025", "29/03/2025", "activo", 500));
        allReservas.add(new Reserva("R003", "Aranwa", "Augusto Medina", 2, 3, 1, "22/03/2025", "29/03/2025", "activo", 1200));
        allReservas.add(new Reserva("R004", "Aranwa", "Gustavo Cuya", 1, 2, 0, "20/03/2025", "26/03/2025", "activo", 750));
        allReservas.add(new Reserva("R005", "Aranwa", "Roger Palomo", 1, 2, 0, "15/04/2025", "20/04/2025", "activo", 900));
        allReservas.add(new Reserva("R025", "Aranwa", "Joaquín Álvarez", 1, 1, 0, "01/06/2025", "03/06/2025", "activo", 600));
        allReservas.add(new Reserva("R026", "Aranwa", "Nassim Ramírez", 2, 4, 2, "05/06/2025", "10/06/2025", "activo", 1500));
        allReservas.add(new Reserva("R040", "Aranwa", "Elias Pulgar", 1, 2, 0, "12/06/2025", "15/06/2025", "activo", 850));
        allReservas.add(new Reserva("R041", "Aranwa", "Mirko Paz", 1, 1, 0, "18/06/2025", "20/06/2025", "activo", 580));
        allReservas.add(new Reserva("R042", "Aranwa", "Eliezer Cruz", 1, 2, 1, "25/06/2025", "30/06/2025", "activo", 1100));

        // Decameron Reservations
        allReservas.add(new Reserva("R006", "Decameron", "P006", 1, 2, 1, "01/05/2025", "07/05/2025", "activo", 1100));
        allReservas.add(new Reserva("R007", "Decameron", "P007", 1, 1, 0, "10/05/2025", "13/05/2025", "activo", 600));
        allReservas.add(new Reserva("R027", "Decameron", "P027", 1, 2, 0, "15/06/2025", "20/06/2025", "activo", 1000));
        allReservas.add(new Reserva("R028", "Decameron", "P028", 2, 3, 1, "22/06/2025", "28/06/2025", "activo", 1800));
        allReservas.add(new Reserva("R029", "Decameron", "P029", 1, 1, 0, "01/07/2025", "03/07/2025", "activo", 700));
        allReservas.add(new Reserva("R043", "Decameron", "P043", 1, 2, 0, "05/07/2025", "10/07/2025", "activo", 1200));
        allReservas.add(new Reserva("R044", "Decameron", "P044", 1, 1, 0, "12/07/2025", "14/07/2025", "activo", 650));

        // Oro Verde Reservations
        allReservas.add(new Reserva("R008", "Oro Verde", "P008", 1, 2, 0, "05/06/2025", "08/06/2025", "activo", 950));
        allReservas.add(new Reserva("R030", "Oro Verde", "P030", 1, 2, 2, "10/07/2025", "15/07/2025", "activo", 1300));
        allReservas.add(new Reserva("R031", "Oro Verde", "P031", 1, 1, 0, "20/07/2025", "23/07/2025", "activo", 800));
        allReservas.add(new Reserva("R045", "Oro Verde", "P045", 2, 3, 1, "01/08/2025", "06/08/2025", "activo", 1500));
        allReservas.add(new Reserva("R046", "Oro Verde", "P046", 1, 2, 0, "10/08/2025", "13/08/2025", "activo", 900));

        // Boca Ratón Reservations
        allReservas.add(new Reserva("R009", "Boca Ratón", "P009", 1, 1, 0, "01/07/2025", "04/07/2025", "activo", 550));
        allReservas.add(new Reserva("R032", "Boca Ratón", "P032", 2, 3, 0, "08/08/2025", "12/08/2025", "activo", 1000));
        allReservas.add(new Reserva("R033", "Boca Ratón", "P033", 1, 2, 0, "15/08/2025", "18/08/2025", "activo", 650));
        allReservas.add(new Reserva("R047", "Boca Ratón", "P047", 1, 1, 0, "20/08/2025", "22/08/2025", "activo", 580));

        // Libertador Reservations
        allReservas.add(new Reserva("R010", "Libertador", "P010", 1, 2, 0, "25/07/2025", "28/07/2025", "activo", 1300));
        allReservas.add(new Reserva("R034", "Libertador", "P034", 1, 1, 0, "05/09/2025", "07/09/2025", "activo", 900));
        allReservas.add(new Reserva("R035", "Libertador", "P035", 2, 4, 1, "10/09/2025", "15/09/2025", "activo", 2000));
        allReservas.add(new Reserva("R048", "Libertador", "P048", 1, 2, 0, "18/09/2025", "22/09/2025", "activo", 1400));

        // Costa del Sol Reservations
        allReservas.add(new Reserva("R011", "Costa del Sol", "P011", 1, 2, 0, "12/08/2025", "15/08/2025", "activo", 700));
        allReservas.add(new Reserva("R036", "Costa del Sol", "P036", 1, 1, 0, "20/09/2025", "23/09/2025", "activo", 500));
        allReservas.add(new Reserva("R037", "Costa del Sol", "P037", 2, 3, 2, "01/10/2025", "07/10/2025", "activo", 1400));
        allReservas.add(new Reserva("R049", "Costa del Sol", "P049", 1, 2, 1, "10/10/2025", "13/10/2025", "activo", 800));

        // Sonesta Reservations
        allReservas.add(new Reserva("R012", "Sonesta", "P012", 1, 2, 0, "01/09/2025", "04/09/2025", "activo", 850));
        allReservas.add(new Reserva("R038", "Sonesta", "P038", 1, 1, 0, "10/10/2025", "12/10/2025", "activo", 600));
        allReservas.add(new Reserva("R039", "Sonesta", "P039", 2, 3, 1, "15/10/2025", "20/10/2025", "activo", 1200));
        allReservas.add(new Reserva("R050", "Sonesta", "P050", 1, 2, 0, "22/10/2025", "25/10/2025", "activo", 950));
    }

    private List<Reserva> getReservationsForHotel(String hotelIdentifier) {
        List<Reserva> filteredList = new ArrayList<>();
        for (Reserva reserva : allReservas) {
            if (reserva.getIdHotel().equals(hotelIdentifier)) {
                filteredList.add(reserva);
            }
        }
        return filteredList;
    }

    private void filterReservas(String searchText) {
        llReservasContainer.removeAllViews();

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
        llReservasContainer.removeAllViews();

        for (Reserva reserva : reservasToDisplay) {
            String clientDisplayName = reserva.getIdPersona();
            String reservationDate = reserva.getFechaInicio();
            String roomOrDepartment = String.valueOf(reserva.getHabitaciones());

            addReservaRow(clientDisplayName, reservationDate, roomOrDepartment);
        }
    }

    private void addReservaRow(String clientName, String reservationDate, String roomNumber) {
        LinearLayout rowLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, dpToPx(8));
        rowLayout.setLayoutParams(layoutParams);
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        rowLayout.setBackgroundResource(R.drawable.border_bottom);

        TextView tvClient = new TextView(this);
        LinearLayout.LayoutParams clientParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvClient.setLayoutParams(clientParams);
        tvClient.setText(clientName);
        tvClient.setTextColor(Color.BLACK);
        tvClient.setGravity(Gravity.START);
        tvClient.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        rowLayout.addView(tvClient);

        TextView tvDate = new TextView(this);
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvDate.setLayoutParams(dateParams);
        tvDate.setText(reservationDate);
        tvDate.setTextColor(Color.BLACK);
        tvDate.setGravity(Gravity.CENTER);
        tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        rowLayout.addView(tvDate);

        TextView tvRoom = new TextView(this);
        LinearLayout.LayoutParams roomParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvRoom.setLayoutParams(roomParams);
        tvRoom.setText(roomNumber);
        tvRoom.setTextColor(Color.BLACK);
        tvRoom.setGravity(Gravity.END);
        tvRoom.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        rowLayout.addView(tvRoom);

        llReservasContainer.addView(rowLayout);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}