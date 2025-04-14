package com.example.hotroid;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import androidx.appcompat.app.AppCompatActivity;


public class SuperEventosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        {
            setContentView(R.layout.super_eventos); // Corregido el nombre del layout

            LinearLayout evento1 = findViewById(R.id.evento1);
            evento1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SuperEventosActivity.this, SuperDetallesEventosActivity.class);
                    startActivity(intent);
                }
            });
        }

    }}
