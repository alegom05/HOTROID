package com.example.hotroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CheckOutUser extends AppCompatActivity {

    private Button btnPagar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_check_out);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Botón de pago
        btnPagar = findViewById(R.id.btnPagar);

        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarPopupValoracion();
            }
        });
    }

    private void mostrarPopupValoracion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CheckOutUser.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.user_popup_valoracion, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etComentario = dialogView.findViewById(R.id.etComentario);
        Button btnEnviar = dialogView.findViewById(R.id.btnEnviar);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float estrellas = ratingBar.getRating();
                String comentario = etComentario.getText().toString().trim();

                if (estrellas == 0 || comentario.isEmpty()) {
                    Toast.makeText(CheckOutUser.this, "Por favor califica con estrellas y agrega un comentario.", Toast.LENGTH_SHORT).show();
                } else {
                    // Aquí puedes guardar la valoración en la base de datos si lo deseas

                    Toast.makeText(CheckOutUser.this, "¡Gracias por tu valoración!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Redirigir a otra actividad (por ejemplo, pantalla de inicio o resumen)
                    Intent intent = new Intent(CheckOutUser.this, ReservasFragment.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
