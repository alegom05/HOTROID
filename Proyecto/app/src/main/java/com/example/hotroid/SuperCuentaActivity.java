package com.example.hotroid;

import android.content.Intent;
import android.content.SharedPreferences; // Import SharedPreferences
import android.os.Bundle;
import android.util.Log; // Import Log
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.authentication.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn; // Import GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Import GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Import GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth

import java.util.ArrayList;
import java.util.List;

public class SuperCuentaActivity extends AppCompatActivity {

    private RecyclerView recyclerCuenta;
    private SuperCuentaAdapter adapter;
    private List<String[]> listaDatosCuenta;
    private GoogleSignInClient googleSignInClient; // Declare GoogleSignInClient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_cuenta);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Make sure you have this in your strings.xml
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso); // Initialize googleSignInClient

        // Inicializar vistas
        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerCuenta = findViewById(R.id.recyclerCuenta);
        recyclerCuenta.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar la lista de datos de la cuenta
        listaDatosCuenta = new ArrayList<>();
        listaDatosCuenta.add(new String[]{"Nombres", "Pedro Miguel"});
        listaDatosCuenta.add(new String[]{"Apellidos", "Bustamante Melo"});
        listaDatosCuenta.add(new String[]{"Modo de Identificación", "DNI"});
        listaDatosCuenta.add(new String[]{"Número de Identidad", "71735617"});
        listaDatosCuenta.add(new String[]{"Fecha de nacimiento", "15 de septiembre de 2002"});
        listaDatosCuenta.add(new String[]{"Correo electrónico", "pm.bustamante@gmail.com"});
        listaDatosCuenta.add(new String[]{"Teléfono", "912345123"});
        listaDatosCuenta.add(new String[]{"Domicilio", "Av. Las Palmeras 123, Los Olivos"});

        // Inicializar y configurar el adaptador
        adapter = new SuperCuentaAdapter(listaDatosCuenta);
        recyclerCuenta.setAdapter(adapter);

        ImageButton btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(SuperCuentaActivity.this, SuperActivity.class); // Redirige a SuperActivity
            startActivity(intent); // Inicia la nueva actividad
        });

        // Configurar listeners
        // Reemplaza el listener existente con la llamada a mostrarDialogoCerrarSesion()
        btnCerrarSesion.setOnClickListener(v -> mostrarDialogoCerrarSesion());


        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_hoteles) {
                Intent intentInicio = new Intent(SuperCuentaActivity.this, SuperActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_usuarios) {
                Intent intentUbicacion = new Intent(SuperCuentaActivity.this, SuperUsuariosActivity.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_eventos) {
                Intent intentAlertas = new Intent(SuperCuentaActivity.this, SuperEventosActivity.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    // Mueve la función mostrarDialogoCerrarSesion() dentro de SuperCuentaActivity
    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this) // Use 'this' as the context for Activity
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión en Firebase
                    FirebaseAuth.getInstance().signOut();
                    // Cerrar sesión Google si aplica
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        // Limpiar preferencias
                        SharedPreferences prefs = getSharedPreferences("sesion_usuario", 0); // Use getSharedPreferences directly
                        prefs.edit().clear().apply();

                        // Redirigir a LoginActivity
                        Intent intent = new Intent(SuperCuentaActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Log.e("REDIRECCION", "Estoy redirigiendo a un activity", new Exception());
                        startActivity(intent);
                        // Finalizar la actividad actual
                        finish();
                    });

                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}