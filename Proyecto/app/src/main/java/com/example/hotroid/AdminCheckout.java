package com.example.hotroid;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.CheckoutFirebase; // Ensure this class is updated
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.Cliente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private List<CheckoutFirebase> checkoutList;
    private List<CheckoutFirebase> originalCheckoutList;
    private FirebaseFirestore db;
    private List<Reserva> reservasList;
    private List<Cliente> clientesList;

    // Elementos del buscador
    private EditText etBuscadorCliente;
    private EditText etBuscadorDepartamento;
    private Button btnLimpiar;
    private Button btnFechaInicio;
    private Button btnFechaFin;

    private Calendar calendarFechaInicio;
    private Calendar calendarFechaFin;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Handle clicks for admin profile card
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminCheckout.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        db = FirebaseFirestore.getInstance();
        checkoutList = new ArrayList<>();
        originalCheckoutList = new ArrayList<>();
        reservasList = new ArrayList<>();
        clientesList = new ArrayList<>();

        rvCheckouts = findViewById(R.id.rvCheckouts);
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CheckoutAdapter(checkoutList);
        rvCheckouts.setAdapter(adapter);

        // Inicializar elementos del buscador y botones de fecha
        etBuscadorCliente = findViewById(R.id.etBuscadorCliente);
        etBuscadorDepartamento = findViewById(R.id.etBuscadorDepartamento);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnFechaFin = findViewById(R.id.btnFechaFin);

        calendarFechaInicio = Calendar.getInstance();
        calendarFechaFin = Calendar.getInstance();

        // Listener para el campo de búsqueda de cliente
        etBuscadorCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Listener para el campo de búsqueda de departamento
        etBuscadorDepartamento.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Listener para el botón "Limpiar"
        btnLimpiar.setOnClickListener(v -> {
            etBuscadorCliente.setText("");
            etBuscadorDepartamento.setText("");
            // Reset calendar to current date, then update button text
            calendarFechaInicio = Calendar.getInstance();
            btnFechaInicio.setText("Fecha Inicio");
            calendarFechaFin = Calendar.getInstance();
            btnFechaFin.setText("Fecha Fin");
            applyFilters();
        });

        // Listener para el botón "Fecha Inicio"
        btnFechaInicio.setOnClickListener(v -> {
            showDatePickerDialog(calendarFechaInicio, btnFechaInicio, true);
        });

        // Listener para el botón "Fecha Fin"
        btnFechaFin.setOnClickListener(v -> {
            showDatePickerDialog(calendarFechaFin, btnFechaFin, false);
        });

        loadReservasAndClientesThenGenerateCheckouts();

        adapter.setOnItemClickListener(new CheckoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CheckoutFirebase seleccionado = checkoutList.get(position);

                Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
                intent.putExtra("ID_CHECKOUT", seleccionado.getIdCheckout());
                intent.putExtra("ROOM_NUMBER", seleccionado.getRoomNumber());
                intent.putExtra("CLIENT_NAME", seleccionado.getClientName());
                intent.putExtra("BASE_RATE", seleccionado.getBaseRate());
                intent.putExtra("ADDITIONAL_CHARGES", seleccionado.getAdditionalCharges());
                intent.putExtra("CHECKIN_DATE", seleccionado.getCheckinDate().getTime());
                intent.putExtra("CHECKOUT_DATE", seleccionado.getCheckoutDate().getTime());
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentInicio = new Intent(AdminCheckout.this, AdminActivity.class);
                    startActivity(intentInicio);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminCheckout.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentAlertas = new Intent(AdminCheckout.this, AdminReportes.class);
                    startActivity(intentAlertas);
                    finish();
                    return true;
                }
                return false;
            });
        } else {
            Log.e("AdminCheckout", "BottomNavigationView con ID R.id.bottom_navigation no encontrada.");
        }
    }

    private void showDatePickerDialog(Calendar calendar, Button button, boolean isStartDate) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                AdminCheckout.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    button.setText(dateFormatter.format(calendar.getTime()));
                    applyFilters();
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void loadReservasAndClientesThenGenerateCheckouts() {
        db.collection("reservas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reservasList.clear();
                            originalCheckoutList.clear(); // Limpiamos la nueva lista
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reserva reserva = document.toObject(Reserva.class);
                                reserva.setIdReserva(document.getId());

                                if (!reserva.isCheckOutRealizado()) {
                                    // Creamos un CheckoutFirebase basado en la reserva
                                    CheckoutFirebase checkout = new CheckoutFirebase();
                                    checkout.setIdCheckout(reserva.getIdReserva());
                                    checkout.setClientName(reserva.getNombreCompleto());
                                    checkout.setRoomNumber(
                                            reserva.getRoomNumber() != null && !reserva.getRoomNumber().isEmpty()
                                                    ? reserva.getRoomNumber().get(0) : 0); // Toma la primera habitación
                                    checkout.setBaseRate(reserva.getPrecioTotal());
                                    checkout.setAdditionalCharges(reserva.getCobrosAdicionales());
                                    checkout.setCheckinDate(reserva.getFechaInicio());
                                    checkout.setCheckoutDate(reserva.getFechaFin());

                                    originalCheckoutList.add(checkout);
                                }
                                reservasList.add(reserva);
                            }

                            applyFilters();

                            Log.d("AdminCheckout", "Reservas cargadas exitosamente: " + reservasList.size());
                            if (originalCheckoutList.isEmpty()) {
                                Toast.makeText(AdminCheckout.this, "No hay checkouts pendientes.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de reservas: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar reservas: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    private void applyFilters() {
        List<CheckoutFirebase> filteredList = new ArrayList<>();
        String clientSearchText = etBuscadorCliente.getText().toString().toLowerCase(Locale.getDefault()).trim();
        String departmentSearchText = etBuscadorDepartamento.getText().toString().trim();

        Date startDate = (btnFechaInicio.getText().toString().equals("Fecha Inicio")) ? null : calendarFechaInicio.getTime();
        Date endDate = (btnFechaFin.getText().toString().equals("Fecha Fin")) ? null : calendarFechaFin.getTime();

        if (endDate != null) {
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            endDate = endCal.getTime();
        }

        for (CheckoutFirebase item : originalCheckoutList) {
            boolean matchesClient = true;
            boolean matchesDepartment = true;
            boolean matchesDateRange = true;

            // Filter by client name
            if (!clientSearchText.isEmpty()) {
                matchesClient = item.getClientName().toLowerCase(Locale.getDefault()).contains(clientSearchText);
            }

            // Filter by department number
            if (!departmentSearchText.isEmpty()) {
                try {
                    int roomNumberFromInput = Integer.parseInt(departmentSearchText);
                    // This comparison now directly uses the int from Firebase with the int from input
                    matchesDepartment = item.getRoomNumber() == roomNumberFromInput;
                } catch (NumberFormatException e) {
                    matchesDepartment = false; // Invalid number input, no match
                }
            }

            // Filter by date range (checkout date)
            if (startDate != null) {
                // Ensure the checkout date is on or after the start date
                matchesDateRange = !item.getCheckoutDate().before(startDate); // Equivalent to item.getCheckoutDate().after(startDate) || item.getCheckoutDate().equals(startDate)
            }
            if (endDate != null) {
                // Ensure the checkout date is on or before the end date (end of day)
                matchesDateRange = matchesDateRange && !item.getCheckoutDate().after(endDate); // Equivalent to item.getCheckoutDate().before(endDate) || item.getCheckoutDate().equals(endDate)
            }


            if (matchesClient && matchesDepartment && matchesDateRange) {
                filteredList.add(item);
            }
        }
        checkoutList.clear();
        checkoutList.addAll(filteredList);
        adapter.filterList(filteredList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recargar los checkouts cada vez que la actividad vuelve a estar en primer plano
        // Esto es crucial para ver la lista actualizada después de una eliminación o edición
        // Clear search fields and reset date pickers
        etBuscadorCliente.setText("");
        etBuscadorDepartamento.setText("");
        calendarFechaInicio = Calendar.getInstance();
        btnFechaInicio.setText("Fecha Inicio");
        calendarFechaFin = Calendar.getInstance();
        btnFechaFin.setText("Fecha Fin");
    }
}