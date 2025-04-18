package com.example.hotroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.databinding.UserNotificacionesBinding;

public class NotificacionesUser extends AppCompatActivity {
    private UserNotificacionesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=UserNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}