package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;

public class DetalleHabitacionUser extends AppCompatActivity {
    private TextView roomGuestsText, precioText;
    private TextView tvTipoHabitacion, tvNumHabitacion, tvArea, tvCapacidad;
    private double precioBase = 0;
    private int cantidadHabitaciones = 1;
    private String roomId, hotelId;
    private ArrayList<String> roomImageUrls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Se comenta esta línea para corregir el problema
        // EdgeToEdge.enable(this);
        setContentView(R.layout.user_detalle_habitacion);

        // Se comenta este bloque que puede estar causando problemas
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        // Obtener los datos de la habitación desde el Intent
        obtenerDatosHabitacion();

        // Inicializar vistas
        inicializarVistas();

        // Cargar datos en la UI
        cargarDatosHabitacion();

        // Inicializar carrusel de imágenes
        inicializarCarruselImagenes();

        // Configurar selector de habitaciones
        configurarSelectorHabitaciones();

        // Botón de volver
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(v -> finish());

        // Botón de reservar
        MaterialButton btnReservar = findViewById(R.id.btnReservar);
        btnReservar.setOnClickListener(v -> {
//            //Intent intent = new Intent(DetalleHabitacionUser.this, ProcesoReservaUser.class);
//            intent.putExtra("ROOM_ID", roomId);
//            intent.putExtra("HOTEL_ID", hotelId);
//            intent.putExtra("CANTIDAD_HABITACIONES", cantidadHabitaciones);
//            intent.putExtra("PRECIO_TOTAL", cantidadHabitaciones * precioBase);
            //startActivity(intent);
        });
    }

    private void obtenerDatosHabitacion() {
        Intent intent = getIntent();
        roomId = intent.getStringExtra("ROOM_ID");
        String roomType = intent.getStringExtra("ROOM_TYPE");
        String roomNumber = intent.getStringExtra("ROOM_NUMBER");
        double area = intent.getDoubleExtra("ROOM_AREA", 0);
        int capacityAdults = intent.getIntExtra("ROOM_CAPACITY_ADULTS", 0);
        int capacityChildren = intent.getIntExtra("ROOM_CAPACITY_CHILDREN", 0);
        precioBase = intent.getDoubleExtra("ROOM_PRICE", 0);
        hotelId = intent.getStringExtra("HOTEL_ID");
        roomImageUrls = getIntent().getStringArrayListExtra("ROOM_IMAGE_URLS");

    }

    private void inicializarVistas() {
        tvTipoHabitacion = findViewById(R.id.tvNombreHabitacion); // Corregido para usar la referencia correcta del XML
        tvNumHabitacion = findViewById(R.id.tvNumHabitacion);
        tvArea = findViewById(R.id.tvArea);
        tvCapacidad = findViewById(R.id.tvCapacidad);
        roomGuestsText = findViewById(R.id.roomGuestsText);
        precioText = findViewById(R.id.tvPrecio);
    }

    private void cargarDatosHabitacion() {
        // Usar las variables de clase en lugar de obtener los datos del Intent nuevamente
        String roomType = getIntent().getStringExtra("ROOM_TYPE");
        String roomNumber = getIntent().getStringExtra("ROOM_NUMBER");
        double area = getIntent().getDoubleExtra("ROOM_AREA", 0);
        int capacityAdults = getIntent().getIntExtra("ROOM_CAPACITY_ADULTS", 0);
        int capacityChildren = getIntent().getIntExtra("ROOM_CAPACITY_CHILDREN", 0);

        // Mostrar los datos en la UI
        tvTipoHabitacion.setText(roomType);

        // Actualizar también el título de la toolbar
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        if (toolbarTitle != null) {
            toolbarTitle.setText(roomType);
        }

        tvNumHabitacion.setText("Habitación " + roomNumber);
        tvArea.setText(String.format(Locale.getDefault(), "%.2f m²", area));

        String capacidadText = capacityAdults + " adulto" + (capacityAdults > 1 ? "s" : "");
        if (capacityChildren > 0) {
            capacidadText += ", " + capacityChildren + " niño" + (capacityChildren > 1 ? "s" : "");
        }
        tvCapacidad.setText(capacidadText);

        // Configurar precio inicial
        precioText.setText(String.format(Locale.getDefault(), "S/. %.2f", precioBase));

        // Configurar texto inicial de selector de habitaciones
        roomGuestsText.setText("1 habitación");
    }

    private void inicializarCarruselImagenes() {
        ViewPager2 viewPager = findViewById(R.id.viewPagerImagenes);
        CircleIndicator3 indicator = findViewById(R.id.indicadorImagenes);

        if (roomImageUrls != null && !roomImageUrls.isEmpty()) {
            ImagenHabitacionAdapterUser adapter = new ImagenHabitacionAdapterUser(roomImageUrls);
            viewPager.setAdapter(adapter);
            indicator.setViewPager(viewPager);
        } else {
            // Si no hay URLs, puedes cargar imágenes por defecto
            List<String> imagenesDefault = new ArrayList<>();
            imagenesDefault.add("android.resource://" + getPackageName() + "/" + R.drawable.hotel_room);
            imagenesDefault.add("android.resource://" + getPackageName() + "/" + R.drawable.hotel_room_doble);
            ImagenHabitacionAdapterUser adapter = new ImagenHabitacionAdapterUser(imagenesDefault);
            viewPager.setAdapter(adapter);
            indicator.setViewPager(viewPager);
        }
    }

    private List<Integer> obtenerImagenesPorTipo(String roomType) {
        List<Integer> imagenes = new ArrayList<>();

        if (roomType == null) {
            return Arrays.asList(R.drawable.hotel_room, R.drawable.hotel_room_doble, R.drawable.hotel_room_deluxe);
        }

        switch (roomType.toLowerCase()) {
            case "suite":
            case "suite lujo":
            case "suite ejecutiva":
                imagenes.add(R.drawable.hotel_room_deluxe);
                imagenes.add(R.drawable.hotel_room);
                break;
            case "doble":
            case "doble superior":
                imagenes.add(R.drawable.hotel_room_doble);
                imagenes.add(R.drawable.hotel_room);
                break;
            case "individual":
            case "estándar":
            default:
                imagenes.add(R.drawable.hotel_room);
                imagenes.add(R.drawable.hotel_room_doble);
                break;
        }

        return imagenes;
    }

    private void configurarSelectorHabitaciones() {
        LinearLayout roomGuestsLayout = findViewById(R.id.roomGuestsLayout);

        roomGuestsLayout.setOnClickListener(v -> mostrarDialogoHabitaciones());
    }

    private void mostrarDialogoHabitaciones() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_habitaciones, null);
        NumberPicker npHabitaciones = dialogView.findViewById(R.id.npHabitaciones);

        npHabitaciones.setMinValue(1);
        npHabitaciones.setMaxValue(5);
        npHabitaciones.setValue(cantidadHabitaciones);

        new AlertDialog.Builder(this)
                .setTitle("Selecciona cantidad de habitaciones")
                .setView(dialogView)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    cantidadHabitaciones = npHabitaciones.getValue();
                    roomGuestsText.setText(cantidadHabitaciones + (cantidadHabitaciones == 1 ? " habitación" : " habitaciones"));

                    // Actualizar precio multiplicado por la cantidad de habitaciones
                    double precioTotal = precioBase * cantidadHabitaciones;
                    precioText.setText(String.format(Locale.getDefault(), "S/. %.2f", precioTotal));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}