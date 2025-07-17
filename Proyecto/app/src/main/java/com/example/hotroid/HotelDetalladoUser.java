package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

// Agregar imports necesarios al inicio del archivo:
import com.example.hotroid.bean.ChatSession;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

//        //recibiendo datos de filtros previos
//        long filtroInicio = getIntent().getLongExtra("fechaInicio",0);
//        long filtroFin = getIntent().getLongExtra("fechaFin", filtroInicio+86400000);//+1 DÍA
//
//        fechaInicioSeleccionado = new Date(filtroInicio);
//        fechaFinSeleccionado = new Date(filtroFin);
//
//        int filtroHabitaciones = getIntent().getIntExtra("habitaciones",1);
//        int filtroPersonas = getIntent().getIntExtra("personas",2);
        // Configurar datos del hotel
        setupHotelData();

        // Configurar listeners
        setupListeners();

        // Cargar imágenes del hotel
        loadHotelImages();

        // Configurar botón de favoritos
//        configurarBotonFavoritos();
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
        binding.hotelPrice.setText(String.format(Locale.getDefault(), "€%.0f por noche", precioMinimo));
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



//    private void configurarBotones() {
//        // Configurar botón de opciones de habitación (antes era reservar)
//        binding.roomOptionsButton.setOnClickListener(v -> mostrarOpcionesHabitacion());
//
//        // Configurar botón de chat
//        binding.chatButton.setOnClickListener(v -> iniciarChat());
//    }

    private void configurarBotonFavoritos() {
        binding.favoriteIcon.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            if (isFavorite) {
                binding.favoriteIcon.setImageResource(R.drawable.ic_favorite);
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
            } else {
                binding.favoriteIcon.setImageResource(R.drawable.ic_favorite_border);
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //modificar fechas inicio y fin de reserva
    private String formatearRangoFechas(Date inicio, Date fin) {
        SimpleDateFormat formato = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return formato.format(inicio) + " - " + formato.format(fin);
    }

    // En el método cargarDatosHotel() dentro de la clase HotelDetalladoUser.java

//    private void cargarDatosHotel() {
//        // Obtener datos del intent
//        String idHotelSeleccionado = getIntent().getStringExtra("idHotel");
//        FirebaseFirestore.getInstance()
//                .collection("hoteles")
//                .document(hotelId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Hotel hotel = documentSnapshot.toObject(Hotel.class);
//                        if (hotel != null) {
//                            hotel.setIdHotel(hotelId); // redundante si usas @DocumentId
//                            mostrarDatosEnPantalla(hotel);
//                        }
//                    } else {
//                        Toast.makeText(this, "Hotel no encontrado", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Error al cargar el hotel", Toast.LENGTH_SHORT).show();
//                    finish();
//                });
////
////        // Si tienes una sola imagen principal, puedes añadirla a la galería
////        if (hotelImages != null && !hotelImages.isEmpty()) {
////            // Si ya hay imágenes cargadas, asegúrate de que la primera sea la imagen principal
////            hotelImages.set(0, imagenId);
////        }
//    }

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


//    private void configurarGaleriaImagenes() {
//        // Simulamos tener varias imágenes del hotel
//        // En una app real, estas imágenes se cargarían desde una base de datos o API
//        hotelImages = new ArrayList<>(Arrays.asList(
//                R.drawable.hotel_room,
//                R.drawable.hotel_restaurant,
//                R.drawable.hotel_park,
//                R.drawable.hotel_pool,
//                R.drawable.hotel_spa
//        ));
//
//        // Configurar el adaptador
//        HotelImageAdapter imageAdapter = new HotelImageAdapter(hotelImages);
//        binding.hotelImagesViewPager.setAdapter(imageAdapter);
//
//        // Configurar los indicadores
//        new TabLayoutMediator(binding.imageIndicator, binding.hotelImagesViewPager,
//                (tab, position) -> {
//                    // No necesitamos texto para las pestañas
//                }).attach();
//
//        // Configurar el cambio de página
//        binding.hotelImagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                // Aquí podríamos hacer algo cuando se selecciona una página
//            }
//        });
//    }

//    private void configurarCalendario() {
//        binding.datePickerLayout.setOnClickListener(v -> {
//            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
//                    MaterialDatePicker.Builder.dateRangePicker();
//            builder.setTitleText("Selecciona las fechas");
//
//            builder.setSelection(androidx.core.util.Pair.create(
//                    fechaInicioSeleccionado.getTime(), fechaFinSeleccionado.getTime()
//            ));
//
//            MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();
//            picker.show(getSupportFragmentManager(), picker.toString());
//
//            picker.addOnPositiveButtonClickListener(selection -> {
//                long inicio = selection.first;
//                long fin = selection.second;
//                if (fin <= inicio){
//                    Toast.makeText(this, "La fecha de salida debe ser posterior a la fecha de inicio", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                fechaInicioSeleccionado = new Date(inicio);
//                fechaFinSeleccionado = new Date(fin);
//
//                binding.selectedDatesText.setText(formatearRangoFechas(fechaInicioSeleccionado, fechaFinSeleccionado));
//                // Actualizar el precio según las fechas seleccionadas
//                actualizarPrecioTotal();
//            });
//        });
//    }

//    private void configurarSelectorPersonas() {
//        binding.roomGuestsLayout.setOnClickListener(v -> {
//            View dialogView = getLayoutInflater()
//                    .inflate(R.layout.dialogo_personas_habitaciones, null);
//
//            NumberPicker habitacionesPicker = dialogView.findViewById(R.id.npHabitaciones);
//            NumberPicker personasPicker = dialogView.findViewById(R.id.npPersonas);
//
//            habitacionesPicker.setMinValue(1);
//            habitacionesPicker.setMaxValue(10);
//            habitacionesPicker.setValue(numHabitaciones);
//
//            personasPicker.setMinValue(1);
//            personasPicker.setMaxValue(10);
//            personasPicker.setValue(numPersonas);
//
//            new AlertDialog.Builder(this)
//                    .setTitle("Habitaciones y Huéspedes")
//                    .setView(dialogView)
//                    .setPositiveButton("Aceptar", (dialog, which) -> {
//                        numHabitaciones = habitacionesPicker.getValue();
//                        numPersonas = personasPicker.getValue();
//                        actualizarTextoHabitacionesPersonas();
//                        actualizarPrecioTotal();
//                    })
//                    .setNegativeButton("Cancelar", null)
//                    .show();
//        });
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