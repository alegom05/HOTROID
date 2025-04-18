package com.example.hotroid;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class OpcionesHabitacionUser extends AppCompatActivity {

    private Button btnVerMasEstandar;
    private Button btnVerMasDeluxe;
    private Button btnVerMasDoble;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_opciones_habitacion);

        // Configurar la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Habilitar el botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Inicializar los botones
        btnVerMasEstandar = findViewById(R.id.btn_ver_mas_estandar);
        btnVerMasDeluxe = findViewById(R.id.btn_ver_mas_deluxe);
        btnVerMasDoble = findViewById(R.id.btn_ver_mas_doble);

        // Configurar listeners para los botones
        btnVerMasEstandar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes navegar a una vista detallada de la habitación estándar
                Toast.makeText(OpcionesHabitacionUser.this, "Ver detalles de habitación estándar", Toast.LENGTH_SHORT).show();
                // Alternativa: Iniciar una nueva actividad
                // Intent intent = new Intent(OpcionesHabitacionUser.this, DetalleHabitacionActivity.class);
                // intent.putExtra("TIPO_HABITACION", "estandar");
                // startActivity(intent);
            }
        });

        btnVerMasDeluxe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes navegar a una vista detallada de la habitación deluxe
                Toast.makeText(OpcionesHabitacionUser.this, "Ver detalles de habitación deluxe", Toast.LENGTH_SHORT).show();
                // Alternativa: Iniciar una nueva actividad
                // Intent intent = new Intent(OpcionesHabitacionUser.this, DetalleHabitacionActivity.class);
                // intent.putExtra("TIPO_HABITACION", "deluxe");
                // startActivity(intent);
            }
        });

        btnVerMasDoble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes navegar a una vista detallada de la habitación deluxe
                Toast.makeText(OpcionesHabitacionUser.this, "Ver detalles de habitación doble", Toast.LENGTH_SHORT).show();
                // Alternativa: Iniciar una nueva actividad
                // Intent intent = new Intent(OpcionesHabitacionUser.this, DetalleHabitacionActivity.class);
                // intent.putExtra("TIPO_HABITACION", "deluxe");
                // startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Manejar el clic en la flecha de retroceso
        if (item.getItemId() == android.R.id.home) {
            // Simplemente finaliza esta actividad para volver a la anterior (user_hotel_detallado)
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}