package com.example.hotroid;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.example.hotroid.R;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class FavoriteHotelsUser extends AppCompatActivity {
    private ImageButton deleSelectionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.user_favorite_hotels);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout backAndTitle = findViewById(R.id.back_and_tittle);
        //se señala(y crea)  el boton de eliminar pero permanece oculto
        deleSelectionButton = new ImageButton(this);
        deleSelectionButton.setImageResource(R.drawable.delete_24);
        deleSelectionButton.setContentDescription("Eliminar de favoritos");
        deleSelectionButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0,2,5,2);
        deleSelectionButton.setLayoutParams(params);

        // Agrega el botón al layout y lo oculta
        backAndTitle.addView(deleSelectionButton);
        deleSelectionButton.setVisibility(View.GONE);

        //al presionar el boton de delete en el toolbar
        deleSelectionButton.setOnClickListener(v1 -> {
            Toast.makeText(FavoriteHotelsUser.this, "Eliminando seleccionados", Toast.LENGTH_SHORT).show();
        });

        //llamado del longpress de los cards
        handleLongPress();
    }


     /**cambiando la vista (barra superior) al mantener presionado un card*/
    private void handleLongPress(){
        LinearLayout backAndTitle = findViewById(R.id.back_and_tittle);
        TextView titleText = findViewById(R.id.title_text);
        ImageButton backButtom = findViewById(R.id.back_button);

        MaterialCardView card1 = findViewById(R.id.card1);
        ImageView check1 = findViewById(R.id.checkicon1);

        card1.setOnLongClickListener(v -> {
            titleText.setText("Elemento(s) seleccionado(s)"); //cmbia el texto del titulo
            check1.setVisibility(View.VISIBLE); //el check se vuelve visible
            backAndTitle.setBackgroundColor(ContextCompat.getColor(FavoriteHotelsUser.this, R.color.azulNoti)); // Change background color if needed
            backButtom.setImageResource(R.drawable.baseline_close_24); // se cambia el icono de retroceso a "x"
            backButtom.setContentDescription("Cancelar selección");
            deleSelectionButton.setVisibility(View.VISIBLE);
            //mostramos el boton de delete en el toolbar
            /*deleSelectionButton.setOnClickListener(v1 -> {
                Toast.makeText(FavoriteHotelsUser.this, "Eliminando seleccionados", Toast.LENGTH_SHORT).show();
            });
            backAndTitle.addView(deleSelectionButton); *///se agrega el boton de delete a la barra(toolbar)

            return true;
        });

        /** se regresa el icono de "x" a "<-" para cancelar la selección**/
        backButtom.setOnClickListener(v -> {
            titleText.setText("Holteles Favoritos");
            backButtom.setImageResource(R.drawable.baseline_arrow_back_24);
            backButtom.setContentDescription("Volver");

            // Ocultar botón de eliminar y check
            if (deleSelectionButton != null)
                deleSelectionButton.setVisibility(View.GONE);
            check1.setVisibility(View.GONE);  // Hide the checkmark
        });
    }

}