package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TaxiActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main);


        CardView cardTaxista = findViewById(R.id.cardTaxista);
        cardTaxista.setOnClickListener(v -> {
            Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
            startActivity(intent);
        });





    }
}
