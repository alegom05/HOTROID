package com.example.hotroid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SuperActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_main);
    }
}
