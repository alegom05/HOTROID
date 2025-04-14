package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import com.example.hotroid.R;
import com.example.hotroid.databinding.UserAccountOptionBinding;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountOptionUser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        /**setContentView(R.layout.user_account_option);**/
        UserAccountOptionBinding binding = UserAccountOptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.informacionPersonalButton.setOnClickListener(v -> {
            Intent intent = new Intent(AccountOptionUser.this, InfoAccountUser.class);
            startActivity(intent);
        });



    }
}