package com.example.hotroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.hotroid.R;

public class MainActivity3 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.hotroid.R.layout.activity_main3);

        // actividad que recibe el Intent, se indica el resultado de
        // la operación (RESULT_OK), se manda la data necesaria (putExtra)
        // y se finaliza con finish()
        Button button = findViewById(R.id.buttonGuardar);
        button.setOnClickListener(view -> {
            EditText editText = findViewById(R.id.editTextApellido);
            String apellido = editText.getText().toString();

            Intent intent = new Intent();
            intent.putExtra("apellido",apellido);
            setResult(RESULT_OK,intent);
            finish();
        });

    }
}