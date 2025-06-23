package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.hotroid.databinding.ClienteMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClienteActivity extends AppCompatActivity {

    private ClienteMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ClienteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener el fragmento de destino si existe en el intent
        String fragmentDestino = getIntent().getStringExtra("fragment_destino");

        if (fragmentDestino != null) {
            // Cargar el fragmento según el parámetro recibido
            switch (fragmentDestino) {
                case "hoteles":
                    loadFragment(new HotelesFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_hoteles_user);
                    break;
                case "chat":
                    loadFragment(new ChatFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_chat_user);
                    break;
                case "cuenta":
                    loadFragment(new CuentaFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_cuenta);
                    break;
                default:
                    loadFragment(new HotelesFragment());
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_hoteles_user);
                    break;
            }
        } else {
            // Cargar fragmento por defecto si no hay parámetro
            loadFragment(new HotelesFragment());
            binding.bottomNavigation.setSelectedItemId(R.id.nav_hoteles_user);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_hoteles_user) {
                loadFragment(new HotelesFragment());
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                // Redirigir a otra actividad en lugar de un fragment
                Intent intent = new Intent(this, MisReservasUser.class);
                startActivity(intent);
                finish(); // Añadido finish() para no acumular activities
                return true; // Cambié a true para mantener seleccionado
            } else if (itemId == R.id.nav_chat_user) {
                loadFragment(new ChatFragment());
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                loadFragment(new CuentaFragment());
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}