package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView; // Importar TextView

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List; // Necesario para acceder a adminDataList si no es estática global en SuperListaAdminActivity

public class SuperDetallesAdminActivadoActivity extends AppCompatActivity {

    private int adminPosition; // Para almacenar la posición del admin en la lista
    private String adminUsuario; // Para almacenar el nombre del admin (combinado)
    private List<String[]> adminDataList; // Referencia a la lista de datos del administrador

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin_activado);

        // Obtener datos del Intent que inició esta actividad
        Intent incomingIntent = getIntent();
        adminPosition = incomingIntent.getIntExtra("admin_position", -1); // Recuperar la posición
        adminUsuario = incomingIntent.getStringExtra("admin_usuario");   // Recuperar el nombre del usuario

        // Obtener una referencia a la lista de datos (asumiendo que es accesible estáticamente o a través de un Singleton)
        // En un proyecto real, usarías una base de datos o un repositorio.
        // Aquí, por simplicidad, asumimos que SuperListaAdminActivity.adminDataList es accesible.
        // Si adminDataList no es accesible directamente, deberías pasar todos los detalles del admin via Intent.
        // Para este ejemplo, simulo un acceso directo a la lista (NO RECOMENDADO PARA APPS REALES).
        // MEJOR PRÁCTICA: Pasar todos los datos del admin seleccionado a través del Intent o usar un ViewModel.
        try {
            // Esto es solo una simulación, en una app real no accederías así.
            // Para que esto funcione, adminDataList en SuperListaAdminActivity DEBE ser public static.
            // Si no lo es, deberías pasar cada campo del admin individualmente.
            java.lang.reflect.Field field = SuperListaAdminActivity.class.getDeclaredField("adminDataList");
            field.setAccessible(true);
            adminDataList = (List<String[]>) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            // Si la lista no es accesible, los detalles se mostrarán como los valores por defecto del XML.
            // Considera pasar todos los detalles en el Intent si no quieres acoplamiento directo.
        }


        // Referenciar los TextViews en el layout
        TextView tvUsuarioDetalle = findViewById(R.id.tvUsuarioDetalle);
        TextView tvNombresDetalle = findViewById(R.id.tvNombresDetalle);
        TextView tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        TextView tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        TextView tvNumDocumentoDetalle = findViewById(R.id.tvNumDocumentoDetalle);
        TextView tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        TextView tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        TextView tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        TextView tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        TextView tvHotelDetalle = findViewById(R.id.tvHotelDetalle);

        // Actualizar los TextViews con los datos del administrador seleccionado
        if (adminPosition != -1 && adminDataList != null && adminPosition < adminDataList.size()) {
            String[] adminData = adminDataList.get(adminPosition);
            // NOTA: adminDataList actualmente solo guarda {Usuario, Hotel, Estado}.
            // Para mostrar todos los detalles, necesitarías que adminDataList almacene más campos,
            // o que estos se pasen directamente desde SuperListaAdminActivity al Intent.
            // Por ahora, solo puedo mostrar lo que está disponible en adminDataList.
            // Si tu estructura de adminDataList se actualiza para incluir más, ajusta esto.
            tvUsuarioDetalle.setText(adminData[0]); // Usuario (Nombres Apellidos)
            tvHotelDetalle.setText(adminData[1]); // Hotel

            // Los demás campos (nombres, apellidos, documento, etc.) deberán ser pasados
            // explícitamente en el Intent o recuperados de una fuente de datos más completa.
            // Por ahora, mostrarán los valores por defecto del XML o estarán vacíos.
        } else {
            // Si no se encuentra el admin, mostrar un mensaje o dejar los valores por defecto del XML
            if (tvUsuarioDetalle != null) tvUsuarioDetalle.setText("N/A");
            if (tvHotelDetalle != null) tvHotelDetalle.setText("N/A");
            // ... para el resto de TextViews
        }

        // Opcional: Mostrar el nombre del administrador actual en la card de perfil superior
        TextView tvNombreAdminActual = findViewById(R.id.tvNombreAdminActual);
        if (tvNombreAdminActual != null) {
            // Si tienes un nombre fijo para el admin general, déjalo así.
            // Si quieres que refleje el usuario logueado, necesitarías pasarlo.
        }


        // Configuración de la barra de navegación inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperDetallesAdminActivadoActivity.this, SuperActivity.class));
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperDetallesAdminActivadoActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperDetallesAdminActivadoActivity.this, SuperEventosActivity.class));
                return true;
            }
            return false;
        });

        // Configuración del botón de Desactivar
        Button btnDesactivar = findViewById(R.id.btnDesactivar);
        btnDesactivar.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("action", "desactivado");
            resultIntent.putExtra("admin_position", adminPosition);
            resultIntent.putExtra("admin_usuario", adminUsuario); // Pasa el nombre para el mensaje de Toast/Notificación
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // Configuración del CardView de perfil (el de "Pedro Bustamante")
        CardView cardPerfil = findViewById(R.id.cardPerfil); // Usar el ID correcto
        cardPerfil.setOnClickListener(v -> {
            startActivity(new Intent(SuperDetallesAdminActivadoActivity.this, SuperCuentaActivity.class));
        });
    }
}
