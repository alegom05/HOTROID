package com.example.hotroid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.example.hotroid.R;
import com.example.hotroid.databinding.UserAccountOptionBinding;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountOptionUser extends AppCompatActivity {
    private UserAccountOptionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Aplica el tema según preferencia guardada
        SharedPreferences prefs = getSharedPreferences("ajustes", MODE_PRIVATE);
        boolean oscuro = prefs.getBoolean("modo_oscuro", false);
        AppCompatDelegate.setDefaultNightMode(
                oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        /*setContentView(R.layout.user_account_option);*/
        binding = UserAccountOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.informacionPersonalButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountOptionUser.this, InfoAccountUser.class);
            startActivity(intent);
        });

        binding.politicasPrivacidadButtom.setOnClickListener(v -> {
            Intent intent =new Intent(AccountOptionUser.this, SecurityPoliticsUser.class);
            startActivity(intent);
        });

        binding.hotelesFavoritosButton.setOnClickListener(v -> {
            Intent intent =new Intent(AccountOptionUser.this, FavoriteHotelsUser.class);
            startActivity(intent);
        });

        // Mostrar selector de tema al presionar el botón
        binding.temaButton.setOnClickListener(v -> mostrarDialogoDeTema());
    }

    /**Método para mostrar el popup de selección de tema*/
    private void mostrarDialogoDeTema(){
        View view = LayoutInflater.from(this).inflate(R.layout.menu_opcion_tema, null);
        RadioButton rbClaro = view.findViewById(R.id.rbClaro);
        RadioButton rbOscuro = view.findViewById(R.id.rbOscuro);
        Button btnOK = view.findViewById(R.id.btnOK);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        SharedPreferences prefs = getSharedPreferences("ajustes", MODE_PRIVATE);
        boolean esOscuro = prefs.getBoolean("modo_oscuro", false);
        rbClaro.setChecked(!esOscuro);
        rbOscuro.setChecked(esOscuro);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            boolean modoOscuroSeleccionado = rbOscuro.isChecked();
            prefs.edit().putBoolean("modo_oscuro", modoOscuroSeleccionado).apply();
            AppCompatDelegate.setDefaultNightMode(
                    modoOscuroSeleccionado ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            dialog.dismiss();
            recreate(); // recargar actividad con nuevo tema
        });
        dialog.show();
    }
}