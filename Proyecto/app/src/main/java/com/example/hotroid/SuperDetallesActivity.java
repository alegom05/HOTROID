package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView; // Import ImageView
import android.widget.TextView;  // Import TextView
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

public class SuperDetallesActivity extends AppCompatActivity {

    private ImageView imgHotel;
    private TextView tvNombreHotel;
    private TextView tvDescripcion;

    // Map to store detailed descriptions for each hotel
    private Map<String, String> hotelDescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_hotel); // Ensure this matches your XML file name for hotel details

        // Initialize views from the layout
        imgHotel = findViewById(R.id.imgHotel);
        tvNombreHotel = findViewById(R.id.tvNombreHotel);
        tvDescripcion = findViewById(R.id.tvDescripcion);
        Button btnVerReservas = findViewById(R.id.btnVerReservas);

        // Initialize hotel descriptions map with custom text
        initializeHotelDescriptions();

        // Get data from the Intent that launched this activity
        Intent intent = getIntent();
        if (intent != null) {
            String hotelName = intent.getStringExtra("hotel_name");
            String hotelLocation = intent.getStringExtra("hotel_location");
            int hotelImageResId = intent.getIntExtra("hotel_image_res_id", 0); // 0 is a default value if not found

            // Set the hotel image using the resource ID
            if (hotelImageResId != 0) {
                imgHotel.setImageResource(hotelImageResId);
            } else {
                // Fallback: If no image resource is passed or it's invalid, use a generic placeholder
                imgHotel.setImageResource(R.drawable.placeholder_hotel); // Make sure you have a 'placeholder_hotel.png' in your drawable folder
            }

            // Set the hotel name and location text
            if (hotelName != null && hotelLocation != null) {
                tvNombreHotel.setText(hotelName + ", " + hotelLocation); // Display "Hotel Name, Location"
            } else if (hotelName != null) {
                tvNombreHotel.setText(hotelName); // Just display name if location is missing
            } else {
                tvNombreHotel.setText("Hotel Desconocido"); // Default text if no name is provided
            }

            // Set the detailed description based on the hotel name
            if (hotelName != null && hotelDescriptions.containsKey(hotelName)) {
                tvDescripcion.setText(hotelDescriptions.get(hotelName));
            } else {
                tvDescripcion.setText("No hay una descripción detallada disponible para este hotel.");
            }
        }

        // Set up click listener for the "Ver Reservas" button
        btnVerReservas.setOnClickListener(v -> {
            Intent reservasIntent = new Intent(SuperDetallesActivity.this, SuperReservasActivity.class);
            startActivity(reservasIntent);
        });

        // Set up BottomNavigationView (same logic as SuperActivity)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_hoteles); // Keep 'Hoteles' highlighted if coming from it

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                // If already on "Hoteles" section (via SuperActivity which opened this),
                // going back to SuperActivity. If coming from other section, navigate.
                Intent intentInicio = new Intent(SuperDetallesActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperDetallesActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            }
            return false;
        });

        // Set up click listener for the Super Admin profile card (same as SuperActivity)
        CardView cardSuper = findViewById(R.id.cardSuper);
        cardSuper.setOnClickListener(v -> {
            Intent intentAccount = new Intent(SuperDetallesActivity.this, SuperCuentaActivity.class);
            startActivity(intentAccount);
        });
    }

    // Private helper method to populate the hotel descriptions
    private void initializeHotelDescriptions() {
        hotelDescriptions = new HashMap<>();
        hotelDescriptions.put("Aranwa",
                "Ubicado estratégicamente en la bahía de Paracas, Ica, el Aranwa Paracas Resort y Spa es un oasis de tranquilidad. Ofrece lujosas habitaciones con vistas panorámicas al mar y acceso directo a playas privadas. Sus piscinas infinitas se fusionan con el horizonte, creando un ambiente de relajación inigualable. El diseño arquitectónico combina la elegancia moderna con toques rústicos peruanos, priorizando el confort y la conexión con la naturaleza. Su restaurante 'El Candelabro' es famoso por su gastronomía marina fresca y platos fusión. Además, el hotel organiza exclusivas excursiones a la Reserva Nacional de Paracas y las Islas Ballestas, ofreciendo una experiencia completa de aventura y descanso."
        );
        hotelDescriptions.put("Decameron",
                "El Decameron Punta Sal, situado en las cálidas playas de Piura, es un vibrante resort 'todo incluido' ideal para familias y parejas. Sus amplias instalaciones incluyen múltiples piscinas, canchas deportivas y acceso directo a la playa. Las habitaciones están diseñadas para ofrecer comodidad y vistas espectaculares al Pacífico. Destaca por su variada oferta gastronómica con restaurantes temáticos que van desde la cocina local hasta la internacional. El entretenimiento es constante, con shows nocturnos, actividades diurnas y deportes acuáticos. Es el lugar perfecto para desconectar y disfrutar del sol peruano con todas las comodidades a mano."
        );
        hotelDescriptions.put("Oro Verde",
                "Inmerso en la exuberante selva amazónica de Iquitos, Loreto, el Hotel Oro Verde ofrece una experiencia única de lujo y aventura. Sus habitaciones y bungalows están construidos con materiales naturales, integrándose armoniosamente con el entorno, muchos con vistas al río Amazonas. El hotel es un punto de partida ideal para explorar la biodiversidad de la región, con tours guiados a la selva, paseos en bote y visitas a comunidades nativas. Su piscina con forma de laguna y su restaurante 'La Piraña' sirven platos exóticos con ingredientes locales. Es un refugio para quienes buscan una conexión profunda con la naturaleza sin renunciar al confort."
        );
        hotelDescriptions.put("Boca Ratón",
                "El Hotel Boca Ratón en Tarapoto, San Martín, es un santuario de paz en el corazón de la selva alta. Rodeado de cascadas naturales y vegetación tropical, ofrece un ambiente sereno y revitalizante. Sus cómodas habitaciones con balcones privados invitan a la contemplación del paisaje. El hotel se especializa en experiencias de bienestar, con un spa que utiliza productos amazónicos y rutas de senderismo hacia cataratas escondidas. El restaurante 'El Tucán' deleita con sabores amazónicos auténticos. Es el destino ideal para ecoturistas y aquellos que buscan una escapada de la rutina en un entorno natural impresionante."
        );
        hotelDescriptions.put("Libertador",
                "El Hotel Libertador Machu Picchu, ubicado estratégicamente en Aguas Calientes, Cusco, es la puerta de entrada a la maravilla inca. Ofrece una combinación perfecta de historia y confort, con habitaciones que reflejan la cultura andina y vistas al río Urubamba. Su proximidad a la estación de autobuses y al sitio arqueológico lo convierte en la opción ideal para los exploradores. El hotel cuenta con un restaurante 'El Mapi' que sirve cocina peruana contemporánea y un bar acogedor. Es un punto de descanso y preparación para la inolvidable visita a Machu Picchu, brindando un servicio impecable y una atmósfera mística."
        );
        hotelDescriptions.put("Costa del Sol",
                "El Hotel Costa del Sol en Trujillo, La Libertad, es un elegante establecimiento que combina la tradición colonial con la modernidad. Ideal para viajeros de negocios y turistas, se encuentra cerca de los principales atractivos de la ciudad. Sus amplias habitaciones y salones de eventos lo convierten en un centro de actividad. El restaurante 'El Mochica' ofrece lo mejor de la gastronomía trujillana y peruana. Además, cuenta con una piscina y un gimnasio. Es el punto de partida perfecto para explorar las ruinas de Chan Chan y las Huacas del Sol y la Luna, sumergiéndose en la rica historia del norte de Perú."
        );
        hotelDescriptions.put("Sonesta",
                "El Hotel Sonesta en Arequipa es una joya arquitectónica que fusiona el encanto colonial con la elegancia contemporánea. Ubicado en el corazón de la 'Ciudad Blanca', ofrece acceso privilegiado a la Plaza de Armas y al Monasterio de Santa Catalina. Sus habitaciones están decoradas con buen gusto, muchas con vistas a los volcanes Misti y Chachani. El hotel es reconocido por su servicio personalizado y su ambiente sofisticado. El restaurante 'La Casona' propone una exquisita cocina arequipeña e internacional. Es el lugar ideal para sumergirse en la cultura arequipeña y disfrutar de la calidez de la ciudad."
        );
    }
}