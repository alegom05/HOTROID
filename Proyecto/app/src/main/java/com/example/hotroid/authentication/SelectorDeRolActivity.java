package com.example.hotroid.authentication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import com.example.hotroid.AdminActivity;
import com.example.hotroid.ClienteActivity;
import com.example.hotroid.R;
import com.example.hotroid.SuperActivity;
import com.example.hotroid.TaxiActivity;

public class SelectorDeRolActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector_de_rol);

        findViewById(R.id.btnIrCliente).setOnClickListener(v -> {
            Intent intent = new Intent(SelectorDeRolActivity.this, ClienteActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnIrAdmin).setOnClickListener(v -> {
            Intent intent = new Intent(SelectorDeRolActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnIrTaxista).setOnClickListener(v -> {
            Intent intent = new Intent(SelectorDeRolActivity.this, TaxiActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnIrSuperadmin).setOnClickListener(v -> {
            Intent intent = new Intent(SelectorDeRolActivity.this, SuperActivity.class);
            startActivity(intent);
        });
    }
}
