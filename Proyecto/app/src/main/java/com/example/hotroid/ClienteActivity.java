package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotroid.databinding.ActivityMainBinding;
import com.example.hotroid.databinding.ClienteMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ClienteActivity extends AppCompatActivity {

    private ClienteMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cliente_main);

        binding = ClienteMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item ->{
        int itemId = item.getItemId();
        if (itemId ==R.id.nav_hoteles_user){
            startActivity(new Intent(ClienteActivity.this, HotelesUser.class));
            return true;
        } else if (itemId == R.id.nav_reservas_user) {
            startActivity(new Intent(ClienteActivity.this, ReservaUser.class));
            return true;
        } else if (itemId == R.id.nav_chat_user) {
            startActivity(new Intent(ClienteActivity.this , ChatUser.class));
            return true;
        } else if (itemId == R.id.nav_cuenta) {
            startActivity(new Intent(ClienteActivity.this, AccountOptionUser.class));
            return true;
        }
        return false;
        });
    }
}
