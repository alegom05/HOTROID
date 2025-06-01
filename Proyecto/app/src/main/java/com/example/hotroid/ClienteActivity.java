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

        // Cargar fragmento por defecto
        loadFragment(new HotelesFragment());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            //Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_hoteles_user) {
                loadFragment(new HotelesFragment());
                return true;
            } else if (itemId == R.id.nav_reservas_user) {
                // Redirigir a otra actividad en lugar de un fragment
                Intent intent = new Intent(this, MisReservasUser.class);
                startActivity(intent);
                return false; // O true, depende si quieres que el botón quede resaltado o no
            } else if (itemId == R.id.nav_chat_user) {
                loadFragment(new ChatFragment());
                return true;
            } else if (itemId == R.id.nav_cuenta) {
                loadFragment(new CuentaFragment());
                return true;
            }

//            if (selectedFragment != null) {
//                loadFragment(selectedFragment);
//                return true;
//            }
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
