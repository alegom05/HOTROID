package com.example.hotroid;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;




public class SuperDetallesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_hotel);

        Button btnVerReservas = findViewById(R.id.btnVerReservas);
        btnVerReservas.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesActivity.this, SuperReservasActivity.class);
            startActivity(intent);
        });
    }




}
