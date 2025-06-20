package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation); // Asocia el layout XML

        Button btnReturnToMain = findViewById(R.id.btnReturnToMain);
        btnReturnToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crea un Intent para regresar a TaxiActivity
                Intent intent = new Intent(ConfirmationActivity.this, TaxiActivity.class);
                // Opcional: Limpia la pila de actividades para que el usuario no pueda volver a esta pantalla con el botón "Atrás"
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Finaliza esta actividad
            }
        });
    }

    // Opcional: Deshabilita el botón de retroceso de Android para que siempre use el botón de la UI
    @Override
    public void onBackPressed() {
        // No hagas nada o llama a super.onBackPressed() si quieres que el botón atrás siga funcionando
        // super.onBackPressed(); // Si lo descomentas, permitirá usar el botón de retroceso
    }
}