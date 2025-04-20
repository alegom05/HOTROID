package com.example.hotroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserServTaxi extends AppCompatActivity {

    private TextView txtDriverInfo;
    private Button btnDriverDetails;
    private Button btnCancel;
    private ProgressBar mapLoading;
    private ImageView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_servicio_taxi);

        // Inicializar componentes
        txtDriverInfo = findViewById(R.id.txt_driver_info);
        btnDriverDetails = findViewById(R.id.btn_driver_details);
        btnCancel = findViewById(R.id.btn_cancel);
        mapLoading = findViewById(R.id.map_loading);
        mapView = findViewById(R.id.map_view);

        // Configurar estado inicial
        txtDriverInfo.setText(R.string.finding_driver);
        btnDriverDetails.setVisibility(View.GONE);

        // Configurar el botón de cancelar
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cancelar el servicio y volver a la actividad anterior
                finish();
            }
        });

        // Simular búsqueda de conductor durante 5 segundos
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Mostrar el mapa
                mapLoading.setVisibility(View.GONE);
                mapView.setVisibility(View.VISIBLE);

                // Actualizar la información del conductor
                txtDriverInfo.setText(getString(R.string.driver_assigned, "Juan Guillermo Gonzales Lozano"));

                // Mostrar el botón de detalles
                btnDriverDetails.setVisibility(View.VISIBLE);
            }
        }, 5000); // 5 segundos de delay

        // Configurar el botón de detalles del conductor
        btnDriverDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a la vista de detalles del taxista
                Intent intent = new Intent(UserServTaxi.this, UserDetalleTaxista.class);
                startActivity(intent);
            }
        });
    }
}