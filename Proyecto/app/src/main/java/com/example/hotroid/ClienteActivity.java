package com.example.hotroid;

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
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles_user) {
                selectedFragment = new HotelesFragment();
            } else if (itemId == R.id.nav_reservas_user) {
                selectedFragment = new ReservasFragment();
            } else if (itemId == R.id.nav_chat_user) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.nav_cuenta) {
                selectedFragment = new CuentaFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
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
