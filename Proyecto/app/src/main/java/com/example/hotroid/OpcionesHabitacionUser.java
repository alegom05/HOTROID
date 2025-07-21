package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.Room;
import com.example.hotroid.bean.RoomGroupOption;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OpcionesHabitacionUser extends AppCompatActivity {

    private static final String TAG = "OpcionesHabitacionUser";

    // UI Components
    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;
    private OpcionesAdapter adapter;
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton continueButton;
    private TextView resultsInfo;
    private LinearLayout capacityLayout;
    private TextView capacityFilterText;
    private MaterialButton applyFiltersButton;
    private TextView availableRoomsTitle, noResultsText;

    // Data
    private List<Room> roomList;
    private FirebaseFirestore db;
    private String hotelId;
    private int adultsFilter = 0;
    private int childrenFilter = 0;
    private List<RoomGroupOption> listaOpciones = new ArrayList<>();
    private Date fechaInicio;
    private Date fechaFin;
    private int cantidadPersonas;
    private int ninios;
    private int numHabitaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_opciones_habitacion);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();
        long fechaInicioLong = getIntent().getLongExtra("fechaInicio", 0);
        long fechaFinLong = getIntent().getLongExtra("fechaFin", 0);
        fechaInicio = new Date(fechaInicioLong);
        fechaFin = new Date(fechaFinLong);

        cantidadPersonas = getIntent().getIntExtra("cantidadPersonas", 1);
        ninios = getIntent().getIntExtra("niniosSolicitados", 0);
        numHabitaciones = getIntent().getIntExtra("numHabitaciones", 1);

        // Inicializar UI components
        initUI();

        // Cargar habitaciones
//        loadRooms();
    }

    private void initUI() {
        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Habilitar el botón de retroceso en la toolbar
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setDisplayShowHomeEnabled(true);
//        }

        // Inicializar componentes
        roomsRecyclerView = findViewById(R.id.roomOptionsRecyclerView);
        continueButton = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressBar1);
        availableRoomsTitle = findViewById(R.id.availableRoomsTitle);
        noResultsText = findViewById(R.id.noResultsText);
//        capacityLayout = findViewById(R.id.capacityLayout);
//        capacityFilterText = findViewById(R.id.capacityFilterText);
//        applyFiltersButton = findViewById(R.id.applyFiltersButton);

        // Configurar RecyclerView
        adapter = new OpcionesAdapter(listaOpciones, this, new OpcionesAdapter.OnOpcionClickListener() {
            @Override
            public void onVerDetalle(RoomGroupOption opcion) {
                Intent intent = new Intent(OpcionesHabitacionUser.this, DetalleHabitacionUser.class);
                intent.putExtra("opcion", opcion);
                startActivity(intent);
            }
            @Override
            public void onSeleccionar(RoomGroupOption opcion) {
                continueButton.setVisibility(View.VISIBLE);
            }
        }, fechaInicio, fechaFin, cantidadPersonas, ninios);

        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        roomsRecyclerView.setAdapter(adapter);
//        roomList = new ArrayList<>();
//        roomAdapter = new RoomAdapter(roomList, this);
//        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        roomsRecyclerView.setAdapter(roomAdapter);

        // ⬇️ Aquí usas los datos recibidos por Intent
        ArrayList<RoomGroupOption> opciones = getIntent().getParcelableArrayListExtra("opciones");
        Log.d(TAG, "Opciones recibidas: " + opciones);

        if (opciones != null && !opciones.isEmpty()) {
            Log.d(TAG, "Cantidad de opciones: " + opciones.size());
            listaOpciones.clear();
            listaOpciones.addAll(opciones);
            availableRoomsTitle.setText("Habitaciones disponibles (" + listaOpciones.size() + ")");
            adapter.notifyDataSetChanged();
            if (!listaOpciones.isEmpty()) {
                adapter.getOpcionSeleccionada(); // Solo para forzar la selección inicial si deseas
                continueButton.setVisibility(View.VISIBLE);
            }
        } else {
            noResultsText.setVisibility(View.VISIBLE);
        }

        if (!listaOpciones.isEmpty()) {
            adapter.getOpcionSeleccionada(); // Solo para forzar la selección inicial si deseas
            continueButton.setVisibility(View.VISIBLE);
        }


        continueButton.setOnClickListener(v -> {
            RoomGroupOption seleccionada = adapter.getOpcionSeleccionada();

            if (seleccionada != null) {
                List<Room> disponibles = seleccionada.getHabitacionesSeleccionadas();
                // Asegurar que hay suficientes habitaciones
                if (disponibles.size() >= numHabitaciones) {
                    // Seleccionamos las primeras N habitaciones disponibles
                    List<Integer> roomNumbersSeleccionados = new ArrayList<>();
                    ArrayList<Room> habitacionesSeleccionadas = new ArrayList<>();

                    for (int i = 0; i < numHabitaciones; i++) {
                        roomNumbersSeleccionados.add(disponibles.get(i).getRoomNumber());
                        habitacionesSeleccionadas.add(disponibles.get(i));
                    }
                    // Ahora roomNumbersSeleccionados contiene [108, 109, 110], por ejemplo

                    Intent intent = new Intent(OpcionesHabitacionUser.this, ProcesoReservaUser.class);
                    intent.putExtra("opcionSeleccionada", seleccionada); // Parcelable
                    intent.putExtra("roomNumbersSeleccionados", new ArrayList<>(roomNumbersSeleccionados)); // List<Integer>
                    intent.putExtra("fechaInicio", fechaInicio.getTime()); // long
                    intent.putExtra("fechaFin", fechaFin.getTime()); // long
                    intent.putExtra("cantidadPersonas", cantidadPersonas); // int
                    intent.putExtra("niniosSolicitados", ninios); // int
                    intent.putExtra("numHabitaciones", numHabitaciones); // int
                    intent.putParcelableArrayListExtra("habitacionesSeleccionadas", habitacionesSeleccionadas);

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "No hay suficientes habitaciones disponibles", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Debes seleccionar una habitación", Toast.LENGTH_SHORT).show();
            }
        });

//        // Configurar filtros
//        capacityLayout.setOnClickListener(v -> showCapacityFilterDialog());
//        applyFiltersButton.setOnClickListener(v -> applyFilters());
    }

    private void loadRooms() {
        showLoading(true);

        // Realizar una consulta a Firestore para obtener todas las habitaciones
        db.collection("habitaciones")
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful()) {
                        roomList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                // Convertir el documento a un objeto Room
                                Room room = new Room();
                                room.setId(document.getId());

                                // Manejar roomNumber (puede venir como String, Long o Integer)
                                Object roomNumberObj = document.get("roomNumber");
                                if (roomNumberObj != null) {
                                    if (roomNumberObj instanceof String) {
                                        try {
                                            room.setRoomNumber(Integer.parseInt((String) roomNumberObj));
                                        } catch (NumberFormatException e) {
                                            Log.e(TAG, "Error al convertir roomNumber a int: " + roomNumberObj, e);
                                            room.setRoomNumber(0); // Valor por defecto
                                        }
                                    } else if (roomNumberObj instanceof Long) {
                                        room.setRoomNumber(((Long) roomNumberObj).intValue());
                                    } else if (roomNumberObj instanceof Integer) {
                                        room.setRoomNumber((Integer) roomNumberObj);
                                    }
                                }

                                room.setRoomType(document.getString("roomType"));

                                // Obtener campos numéricos
                                Long capacityAdultsLong = document.getLong("capacityAdults");
                                Long capacityChildrenLong = document.getLong("capacityChildren");
                                Double areaDouble = document.getDouble("area");

                                if (capacityAdultsLong != null) {
                                    room.setCapacityAdults(capacityAdultsLong.intValue());
                                }

                                if (capacityChildrenLong != null) {
                                    room.setCapacityChildren(capacityChildrenLong.intValue());
                                }

                                if (areaDouble != null) {
                                    room.setArea(areaDouble);
                                }

                                room.setStatus(document.getString("status"));

                                // Asignar hotelId
                                room.setHotelId(hotelId);

                                // Asignar un precio dependiendo del tipo de habitación
                                assignRoomPrice(room);

                                // Agregar a la lista
                                roomList.add(room);

                            } catch (Exception e) {
                                Log.e(TAG, "Error al procesar documento: " + document.getId(), e);
                            }
                        }

                        // Filtrar por hotel si es necesario
                        if (hotelId != null && !hotelId.isEmpty()) {
                            List<Room> filteredRooms = new ArrayList<>();
                            for (Room room : roomList) {
                                if (hotelId.equals(room.getHotelId())) {
                                    filteredRooms.add(room);
                                }
                            }
                            roomList.clear();
                            roomList.addAll(filteredRooms);
                        }

                        // Actualizar el adaptador
                        roomAdapter.updateRooms(roomList);

                        // Actualizar información de resultados
                        updateResultsInfo();

                    } else {
                        Log.e(TAG, "Error al obtener habitaciones", task.getException());
                        Toast.makeText(OpcionesHabitacionUser.this,
                                "Error al cargar habitaciones", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void assignRoomPrice(Room room) {
        // Asignar precio según el tipo de habitación
        String roomType = room.getRoomType() != null ? room.getRoomType().toLowerCase() : "";

        switch (roomType) {
            case "individual":
            case "estándar":
                room.setPrice(800);
                break;
            case "doble":
            case "doble superior":
                room.setPrice(1200);
                break;
            case "matrimonial":
                room.setPrice(1500);
                break;
            case "suite":
            case "suite lujo":
            case "suite ejecutiva":
                room.setPrice(2000);
                break;
            default:
                room.setPrice(1000);
                break;
        }
    }

    private void showCapacityFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_personas_habitaciones, null);

        android.widget.NumberPicker adultsPickerView = dialogView.findViewById(R.id.npHabitaciones);
        android.widget.NumberPicker childrenPickerView = dialogView.findViewById(R.id.npPersonas);

        // Configurar pickers
        adultsPickerView.setMinValue(0);
        adultsPickerView.setMaxValue(10);
        adultsPickerView.setValue(adultsFilter);

        childrenPickerView.setMinValue(0);
        childrenPickerView.setMaxValue(6);
        childrenPickerView.setValue(childrenFilter);

        // Título personalizado
        TextView titleView = new TextView(this);
        titleView.setText("Filtrar por capacidad");
        titleView.setPadding(20, 30, 20, 30);
        titleView.setTextSize(20);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        new AlertDialog.Builder(this)
                .setCustomTitle(titleView)
                .setView(dialogView)
                .setPositiveButton("Aplicar", (dialog, which) -> {
                    adultsFilter = adultsPickerView.getValue();
                    childrenFilter = childrenPickerView.getValue();

                    // Actualizar texto del filtro
                    if (adultsFilter == 0 && childrenFilter == 0) {
                        capacityFilterText.setText("Cualquiera");
                    } else {
                        String filterText = "";
                        if (adultsFilter > 0) {
                            filterText += adultsFilter + " adulto" + (adultsFilter > 1 ? "s" : "");
                        }
                        if (childrenFilter > 0) {
                            if (!filterText.isEmpty()) filterText += ", ";
                            filterText += childrenFilter + " niño" + (childrenFilter > 1 ? "s" : "");
                        }
                        capacityFilterText.setText(filterText);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void applyFilters() {
        showLoading(true);

        // Si no hay filtros, mostrar todas las habitaciones
        if (adultsFilter == 0 && childrenFilter == 0) {
            roomAdapter.updateRooms(roomList);
            updateResultsInfo();
            showLoading(false);
            return;
        }

        // Filtrar habitaciones por capacidad
        List<Room> filteredRooms = new ArrayList<>();
        for (Room room : roomList) {
            if (room.getCapacityAdults() >= adultsFilter && room.getCapacityChildren() >= childrenFilter) {
                filteredRooms.add(room);
            }
        }

        // Actualizar el adaptador con las habitaciones filtradas
        roomAdapter.updateRooms(filteredRooms);

        // Actualizar información de resultados
        resultsInfo.setText("Habitaciones disponibles (" + filteredRooms.size() + ")");

        showLoading(false);
    }

    private void updateResultsInfo() {
        resultsInfo.setText("Habitaciones disponibles (" + roomList.size() + ")");
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        roomsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Manejar el clic en la flecha de retroceso
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}