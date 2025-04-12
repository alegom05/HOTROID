package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SuperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_main);

        // Ejemplo para un Hotel (debes hacerlo para cada hotel)
        CardView cardAranwa = findViewById(R.id.cardHotel1); // Asegúrate que este ID coincida con tu XML
        cardAranwa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SuperActivity.this, SuperDetallesActivity.class);
                startActivity(intent);
            }
        });
    }
}