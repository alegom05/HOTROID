package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

// Agregar imports necesarios al inicio del archivo:
import com.example.hotroid.bean.ChatSession;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.Room;
import com.example.hotroid.bean.RoomGroupOption;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.hotroid.HotelImageAdapter;
import com.example.hotroid.bean.ChatHotelItem;
import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Valoracion;
import com.example.hotroid.FirestoreChatManager;
import com.example.hotroid.databinding.UserHotelDetalladoBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import java.io.Serializable;


public class HotelDetalladoUser extends AppCompatActivity {
    private static final String TAG = "HotelDetalladoUser";

    private UserHotelDetalladoBinding binding;
    private String hotelNameStr;
    private float hotelRatingValue;
    private String hotelDireccion;
    private double precioMinimo;
    private boolean isFavorite = false;
    private int numHabitaciones;
    private int numPersonas;
    private int niniosSolicitados = 0;
    private List<Integer> hotelImages;
    private List<String> imageUrls = new ArrayList<>();
    private Date fechaInicioSeleccionado;
    private Date fechaFinSeleccionado;
    // Adaptador para imágenes
    private HotelImageAdapter imageAdapter;
    private String hotelId;
    private FirebaseFirestore db;
    private List<Valoracion> comentarios = new ArrayList<>();
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserHotelDetalladoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        // Configurar la toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Obtener datos del Intent
        getIntentData();
        // Configurar datos del hotel
        setupHotelData();
        // Configurar listeners
        setupListeners();
        // Cargar imágenes del hotel
        loadHotelImages();

        // Configurar botón de favoritos
        configurarBotonFavoritos();
        // Cargar datos del hotel (simulado)
//        cargarDatosHotel();
        // Configurar galería de imágenes
        //configurarGaleriaImagenes();
        // Configurar selectores
        //configurarCalendario();
        //configurarSelectorPersonas();
        // Configurar botones de acción
        //configurarBotones();
        // Configurar términos y condiciones
        configurarTerminosYCondiciones();
    }
    private void getIntentData(){
        Intent intent = getIntent();
        hotelId = intent.getStringExtra("hotelId");
        hotelNameStr = intent.getStringExtra("hotelName");
        hotelRatingValue =intent.getFloatExtra("hotelRating", 0.0f);
        hotelDireccion = intent.getStringExtra("hotelDireccion");
        precioMinimo = intent.getDoubleExtra("precioMinimo",0.0);
        // Obtener fechas si existen
        long fechaInicioLong = intent.getLongExtra("fechaInicio", 0);
        long fechaFinLong = intent.getLongExtra("fechaFin", 0);
        if (fechaInicioLong != 0 && fechaFinLong != 0) {
            fechaInicioSeleccionado = new Date(fechaInicioLong);
            fechaFinSeleccionado = new Date(fechaFinLong);
        } else {
            // Fechas por defecto (hoy y mañana)
            Calendar cal = Calendar.getInstance();
            fechaFinSeleccionado = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 1);
            fechaFinSeleccionado = cal.getTime();
        }
        // Obtener datos de búsqueda
        numHabitaciones = intent.getIntExtra("numHabitaciones", 1);
        numPersonas = intent.getIntExtra("numPersonas", 1);
        niniosSolicitados = intent.getIntExtra("niniosSolicitados", 0);
    }

    private void setupHotelData(){
        // Configurar datos básicos del hotel
        binding.hotelName.setText(hotelNameStr);
        binding.hotelRating.setRating(hotelRatingValue);
        binding.ratingText.setText(String.valueOf(hotelRatingValue));
        binding.hotelLocation.setText(hotelDireccion);
        binding.hotelPrice.setText(String.format(Locale.getDefault(), "Desde : S/. %.0f por noche", precioMinimo));
        // Configurar fechas seleccionadas
        updateSelectedDatesText();

        // Configurar texto de habitaciones y huéspedes
        updateRoomGuestsText();

    }

    private void updateSelectedDatesText() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        String fechaInicioStr = dateFormat.format(fechaInicioSeleccionado);
        String fechaFinStr = dateFormat.format(fechaFinSeleccionado);
        binding.selectedDatesText.setText(fechaInicioStr + " - " + fechaFinStr);
    }
    private void updateRoomGuestsText() {
        String texto = numHabitaciones + " habitación" + (numHabitaciones > 1 ? "es" : "") +
                " · " + numPersonas + " adulto" + (numPersonas > 1 ? "s" : "");

        if (niniosSolicitados > 0) {
            texto += " · " + niniosSolicitados + " niño" + (niniosSolicitados > 1 ? "s" : "");
        }

        binding.roomGuestsText.setText(texto);
    }

    private void setupListeners(){
        // Listener para el icono de favoritos
        binding.favoriteIcon.setOnClickListener(v -> configurarBotonFavoritos());
        // Listener para el selector de fechas
        binding.datePickerLayout.setOnClickListener(v -> showDatePicker());
        // Listener para el selector de habitaciones/huéspedes
        binding.roomGuestsLayout.setOnClickListener(v -> showRoomGuestsDialog());
        binding.roomOptionsButton.setOnClickListener(v -> filtrarHabitacionesDisponibles());

        // Listener para el botón de chat
//        binding.chatButton.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ChatDetalladoUser.class);
//            intent.putExtra("hotelId", hotelId);
//            intent.putExtra("hotelName", hotelNameStr);
//            startActivity(intent);
//        });
        // Configurar botón de chat
        binding.chatButton.setOnClickListener(v -> iniciarChat());
    }

    private void showDatePicker() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Selecciona rango de fechas");
        // Por defecto, mostrar fechas actuales seleccionadas
        if (fechaInicioSeleccionado != null && fechaFinSeleccionado != null) {
            builder.setSelection(new Pair<>(fechaInicioSeleccionado.getTime(), fechaFinSeleccionado.getTime()));
        }
        // Restringir fechas pasadas
        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now());
        builder.setCalendarConstraints(constraintsBuilder.build());
        //usar en activities
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.show(getSupportFragmentManager(), "DATE_PICKER");
//        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();  <--usar en fragments
//        picker.show(getParentFragmentManager(), picker.toString());
        picker.addOnPositiveButtonClickListener(selection -> {
            long start = selection.first;
            long end = selection.second;
            fechaInicioSeleccionado = new Date(start);
            fechaFinSeleccionado = new Date(end);
            updateSelectedDatesText();
        });
    }

    private void showRoomGuestsDialog() {
        HabitacionesAdultosNiniosDialogoFragment dialogo = new HabitacionesAdultosNiniosDialogoFragment(
                numHabitaciones, numPersonas, niniosSolicitados);
        dialogo.show(getSupportFragmentManager(), "HabitacionesDialog");

    }

    private void filtrarHabitacionesDisponibles() {
        if (fechaInicioSeleccionado == null || fechaFinSeleccionado == null) {
            Toast.makeText(this, "Selecciona un rango de fechas válido", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("habitaciones")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("status", "Available")
                .get()
                .addOnSuccessListener(habitacionQuery -> {
                    List<Room> habitacionesDisponibles = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : habitacionQuery) {
                        Room habitacion = doc.toObject(Room.class);
                        habitacion.setId(doc.getId());
                        habitacionesDisponibles.add(habitacion);
                        Log.d("HABITACION A REVISAR","se añade a lista para posterior analisis de disponibilidad: "+habitacion.getId()+" | con doc id: "+doc.getId());
                    }

                    verificarDisponibilidadPorFechas(habitacionesDisponibles);
//                    List<RoomGroupOption> opciones = generarOpcionesPorTipo(habitacionesDisponibles, numPersonas, niniosSolicitados, numHabitaciones);
//                    mostrarOpciones(opciones);

                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al obtener habitaciones", e));

    }

    private void loadHotelImages() {
        // Cargar imágenes del hotel desde Firebase
        db.collection("hoteles").document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> imageUrls = (List<String>) documentSnapshot.get("imageUrls");
                        String description = documentSnapshot.getString("description");
                        String pais = documentSnapshot.getString("Pais");
                        String ciudad = documentSnapshot.getString("direccion");
                        String avenida = documentSnapshot.getString("direccionDetallada");

                        if (description != null) {
                            binding.hotelDescription.setText(description);
                        }else{
                            binding.hotelDescription.setText("Actualizando la información");
                        }
                        if (avenida != null) {
                            binding.hotelLocation.setText(avenida +" " + ciudad);
                        }else{
                            binding.hotelLocation.setText("No encontrado en el mapa,"+ ciudad);
                        }

                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            setupImageViewPager(imageUrls);
                        } else {
                            // Usar imagen por defecto si no hay imágenes
                            setupImageViewPager(List.of("default_hotel_image"));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar detalles del hotel", Toast.LENGTH_SHORT).show();
                });
    }
    private void setupImageViewPager(List<String> imageUrls) {
        imageAdapter = new HotelImageAdapter(imageUrls);
        binding.hotelImagesViewPager.setAdapter(imageAdapter);

        // Mostrar un indicador de páginas si hay más de 1 imagen
        if (imageUrls.size() > 1) {
            binding.imageIndicator.setVisibility(View.VISIBLE);
            new TabLayoutMediator(binding.imageIndicator, binding.hotelImagesViewPager,
                    (tab, position) -> {
                        // Solo se necesita para sincronización
                    }
            ).attach();
        } else {
            binding.imageIndicator.setVisibility(View.GONE);
        }
    }

    private void verificarDisponibilidadPorFechas(List<Room> habitaciones) {
        db.collection("reservas")
                .whereEqualTo("idHotel", hotelId)
                .whereEqualTo("estaCancelada", false)
                .get()
                .addOnSuccessListener(reservaQuery -> {
                    List<Room> disponibles = new ArrayList<>();

                    for (Room habitacion : habitaciones) {
                        boolean ocupada = false;

                        for (QueryDocumentSnapshot reservaDoc : reservaQuery) {
                            Reserva reserva = reservaDoc.toObject(Reserva.class);

                            if (reserva.getRoomNumber().contains(habitacion.getRoomNumber())) {
                                Date inicio = reserva.getFechaInicio();
                                Date fin = reserva.getFechaFin();

                                if (fechaInicioSeleccionado.before(fin) && fechaFinSeleccionado.after(inicio)) {
                                    ocupada = true;
                                    break;
                                }
                            }
                        }

                        if (!ocupada) disponibles.add(habitacion);
                    }

//                    buscarCombinacionesValidas(disponibles);
                    List<RoomGroupOption> opciones = generarOpcionesPorTipo(disponibles, numPersonas, niniosSolicitados, numHabitaciones);
                    mostrarOpciones(opciones);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al verificar reservas", e));
    }

    private void buscarCombinacionesValidas(List<Room> habitacionesDisponibles) {
        List<Room> seleccionadas = new ArrayList<>();

        int adultosRestantes = numPersonas;
        int niñosRestantes = niniosSolicitados;

        // Ordenar por mayor capacidad (opcional)
        habitacionesDisponibles.sort((a, b) ->
                (b.getCapacityAdults() + b.getCapacityChildren()) -
                        (a.getCapacityAdults() + a.getCapacityChildren()));

        for (Room habitacion : habitacionesDisponibles) {
            if (seleccionadas.size() >= numHabitaciones) break;

            seleccionadas.add(habitacion);
            adultosRestantes -= habitacion.getCapacityAdults();
            niñosRestantes -= habitacion.getCapacityChildren();

            if (adultosRestantes <= 0 && niñosRestantes <= 0) break;
        }

        if (adultosRestantes <= 0 && niñosRestantes <= 0) {
            //mostrarHabitacionesSeleccionadas(seleccionadas);
        } else {
            Toast.makeText(this, "No hay suficientes habitaciones para cubrir los huéspedes solicitados", Toast.LENGTH_LONG).show();
        }
    }

    private List<RoomGroupOption> generarOpcionesPorTipo(List<Room> habitacionesDisponibles,
                                                         int adultosSolicitados, int niñosSolicitados, int habitacionesMax) {
        Map<String, List<Room>> habitacionesPorTipo = new HashMap<>();

        for (Room room : habitacionesDisponibles) {
            habitacionesPorTipo.computeIfAbsent(room.getRoomType(), k -> new ArrayList<>()).add(room);
        }

        List<RoomGroupOption> opciones = new ArrayList<>();

        for (Map.Entry<String, List<Room>> entry : habitacionesPorTipo.entrySet()) {
            String tipo = entry.getKey();
            List<Room> rooms = entry.getValue();

            rooms.sort((a, b) -> {
                int totalA = a.getCapacityAdults() + a.getCapacityChildren();
                int totalB = b.getCapacityAdults() + b.getCapacityChildren();
                return Integer.compare(totalB, totalA);
            });

            int adultsRemaining = adultosSolicitados;
            int childrenRemaining = niñosSolicitados;
            List<Room> seleccionadas = new ArrayList<>();

            for (Room room : rooms) {
                if (seleccionadas.size() >= habitacionesMax) break;

                seleccionadas.add(room);
                adultsRemaining -= room.getCapacityAdults();
                childrenRemaining -= room.getCapacityChildren();

                if (adultsRemaining <= 0 && childrenRemaining <= 0) break;
            }

            if (adultsRemaining <= 0 && childrenRemaining <= 0) {
                RoomGroupOption option = new RoomGroupOption();
                option.setRoomType(tipo);
                option.setHabitacionesSeleccionadas(seleccionadas);
                option.setHabitacionesNecesarias(seleccionadas.size());
                option.setDisponibles(rooms.size());
                option.setTotalAdults(adultosSolicitados);
                option.setTotalChildren(niñosSolicitados);
                option.setPrecioPorHabitacion(seleccionadas.get(0).getPrice());
                opciones.add(option);
            }
        }

        return opciones;
    }

    private void mostrarOpciones(List<RoomGroupOption> opciones) {

        if (opciones.isEmpty()) {
            Toast.makeText(this, "No se encontraron opciones con la cantidad de personas y fechas seleccionadas", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(this, OpcionesHabitacionUser.class);
        intent.putParcelableArrayListExtra("opciones", new ArrayList<>(opciones));

        if (fechaInicioSeleccionado != null && fechaFinSeleccionado != null) {
            intent.putExtra("fechaInicio", fechaInicioSeleccionado.getTime());
            intent.putExtra("fechaFin", fechaFinSeleccionado.getTime());
        }else{
            Toast.makeText(this, "Debe seleccionar alguna fecha disponible para realizar la reserva", Toast.LENGTH_LONG).show();
            return;
        }
        intent.putExtra("cantidadPersonas", numPersonas); // o "numPersonas"
        intent.putExtra("niniosSolicitados", niniosSolicitados);
        intent.putExtra("numHabitaciones", numHabitaciones);
        startActivity(intent);
    }


//    private void configurarBotones() {
//        // Configurar botón de opciones de habitación (antes era reservar)
//        binding.roomOptionsButton.setOnClickListener(v -> mostrarOpcionesHabitacion());
//
//        // Configurar botón de chat
//        binding.chatButton.setOnClickListener(v -> iniciarChat());
//    }

    private void configurarBotonFavoritos() {
        // Primero verificar si ya está en favoritos
        verificarEstadoFavorito();

        binding.favoriteIcon.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión para usar favoritos", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();
            isFavorite = !isFavorite;

            if (isFavorite) {
                // Añadir a favoritos
                añadirAFavoritos(userId);
                binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                // Quitar de favoritos
                quitarDeFavoritos(userId);
                binding.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verificarEstadoFavorito() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> favoritos = (List<String>) documentSnapshot.get("hotelesFav");
                        if (favoritos != null && favoritos.contains(hotelId)) {
                            isFavorite = true;
                            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                        } else {
                            isFavorite = false;
                            binding.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error verificando favorito", e));
    }

    private void añadirAFavoritos(String userId) {
        db.collection("usuarios").document(userId)
                .update("hotelesFav", com.google.firebase.firestore.FieldValue.arrayUnion(hotelId))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al añadir favorito", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error añadiendo favorito", e);
                    // Revertir el estado visual si hay error
                    isFavorite = false;
                    binding.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                });
    }

    private void quitarDeFavoritos(String userId) {
        db.collection("usuarios").document(userId)
                .update("hotelesFav", com.google.firebase.firestore.FieldValue.arrayRemove(hotelId))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al quitar favorito", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error quitando favorito", e);
                    // Revertir el estado visual si hay error
                    isFavorite = true;
                    binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_filled);
                });
    }

    //modificar fechas inicio y fin de reserva
    private String formatearRangoFechas(Date inicio, Date fin) {
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formato.format(inicio) + " - " + formato.format(fin);
    }

    // En el método cargarDatosHotel() dentro de la clase HotelDetalladoUser.java



//    private void mostrarDatosEnPantalla(Hotel hotel) {
//        binding.hotelName.setText(hotel.getName());
//        binding.hotelRating.setRating(hotel.getRating());
//        binding.ratingText.setText(String.format(Locale.getDefault(), "%.1f", hotel.getRating()));
//        binding.hotelLocation.setText(hotel.getDireccionDetallada()!= null ? hotel.getDireccionDetallada() : hotel.getDireccion());
//        binding.hotelDescription.setText(hotel.getDescription());
//        binding.hotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f por noche", hotel.getPrice()));
//
//        // Cargar imágenes con Glide (si usas Cloudinary URLs)
//        if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty()) {
//            Glide.with(this)
//                    .load(hotel.getImageUrls().get(0)) // solo la principal
//                    .into(binding.hotelImagesViewPager); // o adaptador de ViewPager
//        }
//    }



    public void actualizarHuespedes(int habitaciones, int adultos, int ninos) {
        this.numHabitaciones = habitaciones;
        this.numPersonas = adultos;
        this.niniosSolicitados = ninos;
        updateRoomGuestsText(); // actualiza el texto del botón con los nuevos valores
    }


//    private void actualizarPrecioTotal() {
//        // En una app real, calcularíamos el precio total basado en:
//        // - Fechas seleccionadas (número de noches)
//        // - Número de habitaciones
//        // - Número de personas
//        // - Tarifas del hotel
//
//        // Por ahora, simulamos un cálculo simple
//        String fechas = binding.selectedDatesText.getText().toString();
//        int numeroDias = 1; // Por defecto 1 día
//
//        // Parseamos las fechas para calcular los días (simplificado)
//        if (fechas.contains("–")) {
//            // Suponemos que son 2 días por defecto
//            numeroDias = 2;
//        }
//
//        double precioPorNoche = 145;
//        double precioTotal = precioPorNoche * numHabitaciones * numeroDias;
//
//        binding.hotelPrice.setText(String.format("€%.0f por %d %s",
//                precioTotal,
//                numeroDias,
//                numeroDias > 1 ? "noches" : "noche"));
//    }

    // NUEVOS MÉTODOS PARA LAS FUNCIONALIDADES AÑADIDAS

    private void mostrarOpcionesHabitacion() {
        Intent intent = new Intent(this, OpcionesHabitacionUser.class);

        intent.putExtra("HOTEL_ID", hotelId);
        intent.putExtra("fechaInicio", fechaInicioSeleccionado.getTime());
        intent.putExtra("fechaFin", fechaFinSeleccionado.getTime());
        intent.putExtra("habitaciones", numHabitaciones);
        intent.putExtra("personas", numPersonas);
        startActivity(intent);
    }


    // Agregar este método actualizado a la clase HotelDetalladoUser.java

    // Agregar estos imports y método al archivo existente

    private void iniciarChat() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para usar el chat", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener datos del hotel desde el Intent
        String clientId = currentUser.getUid();
        String hotelId = getIntent().getStringExtra("hotelId");
        String hotelName = getIntent().getStringExtra("hotelName");
        String hotelImage = getIntent().getStringExtra("hotelImage");

        // Validar que tengamos los datos necesarios
        if (hotelId == null || hotelName == null) {
            Toast.makeText(this, "Error: No se pudieron obtener los datos del hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar loading
        Toast.makeText(this, "Iniciando chat...", Toast.LENGTH_SHORT).show();

        // Crear o obtener el chat desde Firestore
        FirestoreChatManager.getInstance().createOrGetChat(clientId, hotelId, hotelName,
                new FirestoreChatManager.ChatCreationListener() {
                    @Override
                    public void onChatCreated(ChatSession chat) {
                        // Chat creado exitosamente, abrir ChatDetalladoUser
                        Intent intent = new Intent(HotelDetalladoUser.this, ChatDetalladoUser.class);
                        intent.putExtra("chat_id", chat.getChatId());
                        intent.putExtra("hotel_name", chat.getHotelName());
                        intent.putExtra("hotel_id", chat.getHotelId());
                        intent.putExtra("hotel_rating", getIntent().getFloatExtra("rating", 0f));
                        intent.putExtra("hotel_price", getIntent().getDoubleExtra("precio", 0.0));
                        intent.putExtra("hotel_direccion", getIntent().getStringExtra("direccion"));
                        intent.putExtra("hotel_description", getIntent().getStringExtra("descripcion"));
                        intent.putExtra("profile_image", getIntent().getIntExtra("imagen", R.drawable.hotel_decameron));

                        startActivity(intent);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(HotelDetalladoUser.this, "Error al iniciar chat: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void configurarTerminosYCondiciones() {
        binding.termsAndConditionsLink.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Términos y Condiciones");

            // Contenido de los términos y condiciones
            builder.setMessage(
                    "TÉRMINOS Y CONDICIONES DE RESERVA\n\n" +
                            "1. POLÍTICA DE PAGO\n" +
                            "• Se requiere un depósito del 30% del total para confirmar la reserva.\n" +
                            "• El saldo restante se pagará a la llegada al hotel.\n" +
                            "• Aceptamos tarjetas de crédito/débito y efectivo.\n\n" +
                            "2. POLÍTICA DE CANCELACIÓN\n" +
                            "• Cancelaciones realizadas con más de 48 horas de antelación: reembolso completo.\n" +
                            "• Cancelaciones realizadas con menos de 48 horas: cargo del 30% del total.\n" +
                            "• No presentación: cargo del 100% de la primera noche.\n\n" +
                            "3. CHECK-IN Y CHECK-OUT\n" +
                            "• Check-in: a partir de las 14:00h.\n" +
                            "• Check-out: antes de las 12:00h.\n" +
                            "• Late check-out disponible con cargo adicional (sujeto a disponibilidad).\n\n" +
                            "4. NORMAS GENERALES\n" +
                            "• No se permiten mascotas excepto animales de asistencia.\n" +
                            "• Prohibido fumar en todas las instalaciones.\n" +
                            "• Los huéspedes son responsables de cualquier daño causado.\n\n" +
                            "Para cualquier consulta adicional, contacte con nuestro servicio de atención al cliente."
            );

            builder.setPositiveButton("Aceptar", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void procesarReserva() {
        // Aquí se realizaría el proceso de reserva real
        // Por ejemplo:
        // 1. Verificar disponibilidad en tiempo real
        // 2. Procesar pago
        // 3. Guardar reserva en la base de datos
        // 4. Enviar confirmación por email/SMS

        // Simulamos éxito en la reserva
        mostrarConfirmacionReserva();
    }

    private void mostrarConfirmacionReserva() {
        // Muestra un diálogo de confirmación final
        new AlertDialog.Builder(this)
                .setTitle("¡Reserva confirmada!")
                .setMessage("Tu reserva ha sido procesada correctamente. Recibirás un correo electrónico con los detalles de tu reserva.")
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Volvemos a la pantalla anterior o a la pantalla principal
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        // Podría mostrar una confirmación si hay cambios pendientes
        super.onBackPressed();
    }

    // Este método se llamaría desde un menú de opciones o botón de compartir
    public void compartirHotel() {
        // Implementar funcionalidad para compartir los detalles del hotel
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Mira este hotel");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "He encontrado este increíble hotel en Hotroid: " + binding.hotelName.getText() +
                        " en " + binding.hotelLocation.getText());
        startActivity(Intent.createChooser(shareIntent, "Compartir hotel"));
    }

    // Método para mostrar información detallada de los servicios
    public void mostrarDetallesServicios(View view) {
        // Se podría implementar para mostrar más detalles sobre algún servicio específico
        // cuando el usuario hace clic en uno de los iconos de servicios
        String servicioSeleccionado = view.getTag().toString();

        new AlertDialog.Builder(this)
                .setTitle("Información del servicio")
                .setMessage("Detalles sobre: " + servicioSeleccionado)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}