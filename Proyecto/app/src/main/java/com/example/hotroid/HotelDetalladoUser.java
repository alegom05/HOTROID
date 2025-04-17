package com.example.hotroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.hotroid.HotelImageAdapter;
import com.example.hotroid.databinding.UserHotelDetalladoBinding;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        // Configurar botón de reserva
        binding.bookButton.setOnClickListener(v -> reservarHotel());
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

    private void cargarDatosHotel() {
        // En una implementación real, estos datos vendrían de la base de datos o de un intent
        String hotelId = getIntent().getStringExtra("HOTEL_ID");

        // Por ahora, usamos datos estáticos
        binding.hotelName.setText("Grand Hotel Madrid");
        binding.hotelRating.setRating(4.5f);
        binding.ratingText.setText("4.5");
        binding.hotelLocation.setText("Calle Gran Vía 23, Madrid, España");
        binding.hotelPrice.setText("€145 por noche");
        binding.hotelDescription.setText("El Grand Hotel Madrid es un impresionante hotel de 5 estrellas ubicado en el corazón de Madrid. Con vistas panorámicas a la ciudad, ofrece habitaciones espaciosas y elegantes, varios restaurantes gourmet, un spa de lujo y una piscina en la azotea. Ideal para viajeros de negocios y turistas que buscan una experiencia excepcional en la capital española.");
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

    private void reservarHotel() {
        // Simulamos el proceso de reserva
        new AlertDialog.Builder(this)
                .setTitle("Confirmar reserva")
                .setMessage(String.format(
                        "¿Deseas reservar %d habitación%s en %s para %d adulto%s?\n\n" +
                                "Fechas: %s\n" +
                                "Precio total: %s",
                        numHabitaciones,
                        numHabitaciones > 1 ? "es" : "",
                        binding.hotelName.getText(),
                        numPersonas,
                        numPersonas > 1 ? "s" : "",
                        binding.selectedDatesText.getText(),
                        binding.hotelPrice.getText()
                ))
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    procesarReserva();
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Mira este hotel");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                "He encontrado este increíble hotel en Hotroid: " + binding.hotelName.getText() +
                        " en " + binding.hotelLocation.getText());
        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir hotel"));
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