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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hotroid.HotelImageAdapter;
import com.example.hotroid.bean.ChatHotelItem;
import com.example.hotroid.FirestoreChatManager;
import com.example.hotroid.databinding.UserHotelDetalladoBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HotelDetalladoUser extends AppCompatActivity {

    private UserHotelDetalladoBinding binding;
    private boolean isFavorite = false;
    private int numHabitaciones = 1;
    private int numPersonas = 2;
    private List<Integer> hotelImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = UserHotelDetalladoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configurar la toolbar
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Configurar botón de favoritos
        configurarBotonFavoritos();

        // Cargar datos del hotel (simulado)
        cargarDatosHotel();

        // Configurar galería de imágenes
        configurarGaleriaImagenes();

        // Configurar selectores
        configurarCalendario();
        configurarSelectorPersonas();

        // Configurar botones de acción
        configurarBotones();

        // Configurar términos y condiciones
        configurarTerminosYCondiciones();
    }

    private void configurarBotones() {
        // Configurar botón de opciones de habitación (antes era reservar)
        binding.roomOptionsButton.setOnClickListener(v -> mostrarOpcionesHabitacion());

        // Configurar botón de chat
        binding.chatButton.setOnClickListener(v -> iniciarChat());
    }

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

    // En el método cargarDatosHotel() dentro de la clase HotelDetalladoUser.java

    private void cargarDatosHotel() {
        // Obtener datos del intent
        String hotelId = getIntent().getStringExtra("HOTEL_ID");
        String nombre = getIntent().getStringExtra("nombre");
        float rating = getIntent().getFloatExtra("rating", 0f);
        double precio = getIntent().getDoubleExtra("precio", 0.0);
        String direccion = getIntent().getStringExtra("direccion");
        String direccionDetallada = getIntent().getStringExtra("direccionDetallada");
        String descripcion = getIntent().getStringExtra("descripcion");
        int imagenId = getIntent().getIntExtra("imagen", R.drawable.hotel_decameron); // imagen por defecto

        // Mostrar datos en la UI
        binding.hotelName.setText(nombre);
        binding.hotelRating.setRating(rating);
        binding.ratingText.setText(String.valueOf(rating));
        binding.hotelLocation.setText(direccionDetallada != null ? direccionDetallada : direccion);
        binding.hotelPrice.setText(String.format(Locale.getDefault(), "S/. %.2f por noche", precio));
        binding.hotelDescription.setText(descripcion);

        // Si tienes una sola imagen principal, puedes añadirla a la galería
        if (hotelImages != null && !hotelImages.isEmpty()) {
            // Si ya hay imágenes cargadas, asegúrate de que la primera sea la imagen principal
            hotelImages.set(0, imagenId);
        }
    }

    private void configurarGaleriaImagenes() {
        // Simulamos tener varias imágenes del hotel
        // En una app real, estas imágenes se cargarían desde una base de datos o API
        hotelImages = new ArrayList<>(Arrays.asList(
                R.drawable.hotel_room,
                R.drawable.hotel_restaurant,
                R.drawable.hotel_park,
                R.drawable.hotel_pool,
                R.drawable.hotel_spa
        ));

        // Configurar el adaptador
        HotelImageAdapter imageAdapter = new HotelImageAdapter(hotelImages);
        binding.hotelImagesViewPager.setAdapter(imageAdapter);

        // Configurar los indicadores
        new TabLayoutMediator(binding.imageIndicator, binding.hotelImagesViewPager,
                (tab, position) -> {
                    // No necesitamos texto para las pestañas
                }).attach();

        // Configurar el cambio de página
        binding.hotelImagesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Aquí podríamos hacer algo cuando se selecciona una página
            }
        });
    }

    private void configurarCalendario() {
        binding.datePickerLayout.setOnClickListener(v -> {
            MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Selecciona las fechas");

            // Establecer fecha predeterminada (inicio: hoy, fin: mañana)
            long today = System.currentTimeMillis();
            long tomorrow = today + (24 * 60 * 60 * 1000);
            androidx.core.util.Pair<Long, Long> defaultDateRange =
                    new androidx.core.util.Pair<>(today, tomorrow);
            builder.setSelection(defaultDateRange);

            MaterialDatePicker<?> picker = builder.build();
            picker.show(getSupportFragmentManager(), picker.toString());

            picker.addOnPositiveButtonClickListener(selection -> {
                String fecha = picker.getHeaderText(); // ej. "14 abr – 15 abr 2025"
                binding.selectedDatesText.setText(fecha);

                // Actualizar el precio según las fechas seleccionadas
                actualizarPrecioTotal();
            });
        });
    }

    private void configurarSelectorPersonas() {
        binding.roomGuestsLayout.setOnClickListener(v -> {
            View dialogView = getLayoutInflater()
                    .inflate(R.layout.dialogo_personas_habitaciones, null);

            NumberPicker habitacionesPicker = dialogView.findViewById(R.id.npHabitaciones);
            NumberPicker personasPicker = dialogView.findViewById(R.id.npPersonas);

            habitacionesPicker.setMinValue(1);
            habitacionesPicker.setMaxValue(10);
            habitacionesPicker.setValue(numHabitaciones);

            personasPicker.setMinValue(1);
            personasPicker.setMaxValue(10);
            personasPicker.setValue(numPersonas);

            new AlertDialog.Builder(this)
                    .setTitle("Habitaciones y Huéspedes")
                    .setView(dialogView)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        numHabitaciones = habitacionesPicker.getValue();
                        numPersonas = personasPicker.getValue();
                        actualizarTextoHabitacionesPersonas();
                        actualizarPrecioTotal();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    private void actualizarTextoHabitacionesPersonas() {
        binding.roomGuestsText.setText(
                numHabitaciones + " habitación" + (numHabitaciones > 1 ? "es" : "") +
                        " · " + numPersonas + " adulto" + (numPersonas > 1 ? "s" : ""));
    }

    private void actualizarPrecioTotal() {
        // En una app real, calcularíamos el precio total basado en:
        // - Fechas seleccionadas (número de noches)
        // - Número de habitaciones
        // - Número de personas
        // - Tarifas del hotel

        // Por ahora, simulamos un cálculo simple
        String fechas = binding.selectedDatesText.getText().toString();
        int numeroDias = 1; // Por defecto 1 día

        // Parseamos las fechas para calcular los días (simplificado)
        if (fechas.contains("–")) {
            // Suponemos que son 2 días por defecto
            numeroDias = 2;
        }

        double precioPorNoche = 145;
        double precioTotal = precioPorNoche * numHabitaciones * numeroDias;

        binding.hotelPrice.setText(String.format("€%.0f por %d %s",
                precioTotal,
                numeroDias,
                numeroDias > 1 ? "noches" : "noche"));
    }

    // NUEVOS MÉTODOS PARA LAS FUNCIONALIDADES AÑADIDAS

    private void mostrarOpcionesHabitacion() {
        Intent intent = new Intent(this, OpcionesHabitacionUser.class);
        intent.putExtra("HOTEL_ID", getIntent().getStringExtra("HOTEL_ID"));
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

        String clientId = currentUser.getUid();
        String hotelId = getIntent().getStringExtra("hotelId");
        String hotelName = "Hotel"; // Obtener el nombre real desde tus datos

        FirestoreChatManager.getInstance().createOrGetChat(clientId, hotelId, hotelName,
                new FirestoreChatManager.ChatCreationListener() {
                    @Override
                    public void onChatCreated(ChatSession chat) {
                        Intent intent = new Intent(HotelDetalladoUser.this, ChatDetalladoUser.class);
                        intent.putExtra("hotelId", chat.getHotelId());
                        intent.putExtra("hotelName", chat.getHotelName());
                        intent.putExtra("chatId", chat.getChatId());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(HotelDetalladoUser.this, "Error al iniciar chat: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registrarChatEnFragment() {
        // Este método registrará el chat para que aparezca en ChatFragment
        // Podrías usar SharedPreferences, base de datos local, o un evento
        // Para simplificar, usaremos una clase singleton para manejar los chats activos

        String hotelId = getIntent().getStringExtra("HOTEL_ID");
        String hotelName = getIntent().getStringExtra("nombre");
        int profileImage = getIntent().getIntExtra("imagen", R.drawable.hotel_decameron);

        if (hotelId != null && hotelName != null) {
            ChatHotelItem chatItem = new ChatHotelItem();
            chatItem.setHotelId(hotelId);
            chatItem.setHotelName(hotelName);
            chatItem.setLastMessage("¡Hola! Bienvenido al asistente virtual...");
            chatItem.setLastMessageTime(System.currentTimeMillis());
            chatItem.setProfileImageRes(profileImage);
            chatItem.setHasUnreadMessages(false);
            chatItem.setUnreadCount(0);

            // Agregar a la lista de chats activos
            ChatListManager.getInstance().addOrUpdateChat(chatItem);
        }
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