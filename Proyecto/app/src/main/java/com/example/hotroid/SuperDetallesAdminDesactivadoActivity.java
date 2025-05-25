package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Arrays;
import java.util.List;

public class SuperDetallesAdminDesactivadoActivity extends AppCompatActivity {

    private int adminPosition;
    private String adminUsuario;
    private TextView tvHotelDetalle;
    private Spinner spSeleccionarHotel;
    private Button btnActivar; // Botón general de activar
    private Button btnAsignarHotelActivar; // Botón para asignar hotel y activar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin_desactivado);

        // Obtener datos del Intent que inició esta actividad
        Intent incomingIntent = getIntent();
        adminPosition = incomingIntent.getIntExtra("admin_position", -1);
        adminUsuario = incomingIntent.getStringExtra("admin_usuario");

        // Referenciar los TextViews y otros elementos en el layout
        TextView tvUsuarioDetalle = findViewById(R.id.tvUsuarioDetalle);
        tvHotelDetalle = findViewById(R.id.tvHotelDetalle); // Referenciar el TextView del hotel
        spSeleccionarHotel = findViewById(R.id.spSeleccionarHotel); // Referenciar el Spinner
        btnActivar = findViewById(R.id.btnActivar); // Referenciar el botón Activar
        btnAsignarHotelActivar = findViewById(R.id.btnAsignarHotelActivar); // Referenciar el nuevo botón

        // Cargar la lista de hoteles en el Spinner
        List<String> hoteles = Arrays.asList(
                "Hotel Miraflores Park - Lima", "JW Marriott - Lima", "Hotel Libertador - Arequipa",
                "Casa Andina Premium - Cusco", "Hotel Paracas - Paracas", "Costa del Sol Wyndham - Trujillo",
                "Inkaterra Machu Picchu Pueblo - Cusco", "Hotel Novotel - Cusco",
                "Sonesta Hotel - El Olivar, Lima", "Hotel Taypikala - Puno"
        );
        ArrayAdapter<String> adapterHoteles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoteles);
        adapterHoteles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSeleccionarHotel.setAdapter(adapterHoteles);


        // --- LÓGICA PARA MOSTRAR DATOS Y CONTROLAR VISIBILIDAD DE BOTONES/SPINNER ---
        if (adminPosition != -1 && adminPosition < SuperListaAdminActivity.adminDataList.size()) {
            String[] adminData = SuperListaAdminActivity.adminDataList.get(adminPosition);
            tvUsuarioDetalle.setText(adminData[0]); // Usuario (Nombre Completo)

            // Si el admin está desactivado, siempre mostramos "-" en el hotel
            tvHotelDetalle.setText("-");

            // Si el hotel es "-", entonces mostrar el Spinner y el botón de asignar
            if (adminData[1].equals("-")) { // Comprobamos el valor real del hotel en la lista de datos
                spSeleccionarHotel.setVisibility(View.VISIBLE);
                btnAsignarHotelActivar.setVisibility(View.VISIBLE);
                btnActivar.setVisibility(View.GONE); // Ocultar el botón simple "Activar"
            } else {
                // Si el admin está desactivado pero tenía un hotel asignado previamente (situación rara),
                // o si el diseño lo requiere, podrías simplemente mostrar el botón "Activar"
                // sin el Spinner. Para este caso, oculto ambos y solo el botón de activar.
                spSeleccionarHotel.setVisibility(View.GONE);
                btnAsignarHotelActivar.setVisibility(View.GONE);
                btnActivar.setVisibility(View.VISIBLE); // Mostrar el botón simple "Activar"
            }
        } else {
            // Caso de error: Administrador no encontrado
            if (tvUsuarioDetalle != null) tvUsuarioDetalle.setText("N/A");
            if (tvHotelDetalle != null) tvHotelDetalle.setText("N/A");
            spSeleccionarHotel.setVisibility(View.GONE);
            btnActivar.setVisibility(View.GONE);
            btnAsignarHotelActivar.setVisibility(View.GONE);
            Toast.makeText(this, "Error al cargar datos del administrador.", Toast.LENGTH_LONG).show();
        }
        // --- FIN DE LA LÓGICA DE VISIBILIDAD ---


        // Configuración de los botones de acción
        btnActivar.setOnClickListener(v -> {
            // Lógica para activar sin asignar un hotel (si el hotel no es "-")
            Intent resultIntent = new Intent();
            resultIntent.putExtra("action", "activado");
            resultIntent.putExtra("admin_position", adminPosition);
            resultIntent.putExtra("admin_usuario", adminUsuario);
            resultIntent.putExtra("nuevo_hotel_asignado", SuperListaAdminActivity.adminDataList.get(adminPosition)[1]); // Re-envía el hotel existente
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnAsignarHotelActivar.setOnClickListener(v -> {
            String selectedHotel = spSeleccionarHotel.getSelectedItem().toString();
            if (selectedHotel.isEmpty() || selectedHotel.equals(getString(R.string.seleccione_el_hotel))) { // Asumiendo que el prompt es el primer item
                Toast.makeText(this, "Por favor, seleccione un hotel.", Toast.LENGTH_SHORT).show();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "activado");
                resultIntent.putExtra("admin_position", adminPosition);
                resultIntent.putExtra("admin_usuario", adminUsuario);
                resultIntent.putExtra("nuevo_hotel_asignado", selectedHotel); // Enviar el hotel seleccionado
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });


        // Configuración de la barra de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperDetallesAdminDesactivadoActivity.this, SuperActivity.class));
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesAdminDesactivadoActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperDetallesAdminDesactivadoActivity.this, SuperEventosActivity.class));
                return true;
            }
            return false;
        });

        // Configuración del CardView de perfil
        CardView cardPerfil = findViewById(R.id.cardPerfil);
        cardPerfil.setOnClickListener(v -> {
            startActivity(new Intent(SuperDetallesAdminDesactivadoActivity.this, SuperCuentaActivity.class));
        });
    }
}