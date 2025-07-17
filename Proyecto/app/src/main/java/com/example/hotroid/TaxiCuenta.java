package com.example.hotroid;

import android.content.DialogInterface; // Importar DialogInterface
import android.content.Intent;
import android.content.SharedPreferences; // Importar SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog; // Importar AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.authentication.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn; // Importar GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Importar GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Importar GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // Importar FirebaseAuth

import java.util.ArrayList;
import java.util.List;

public class TaxiCuenta extends AppCompatActivity {

    private FirebaseAuth mAuth; // Instancia de FirebaseAuth
    private GoogleSignInClient googleSignInClient; // Instancia de GoogleSignInClient (si usas Google Sign-In)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_cuenta);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar GoogleSignInClient (si usas Google Sign-In)
        // Asegúrate de que tu GoogleSignInOptions sea el mismo que usas en LoginActivity
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de tener este string resource
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        Button btnVehiculo = findViewById(R.id.btnVehiculo);
        ImageButton btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            // Acción cuando el botón es clickeado
            Intent intent = new Intent(TaxiCuenta.this, TaxiActivity.class); // Redirige a TaxiActivity
            startActivity(intent); // Inicia la nueva actividad
            // Si quieres que no puedan volver a TaxiCuenta desde TaxiActivity, puedes añadir finish();
            // finish();
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerCuenta);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String[]> datos = new ArrayList<>();
        datos.add(new String[]{"Nombre:", "Alejandro"});
        datos.add(new String[]{"Apellidos:", "Gómez Mostacero"});
        datos.add(new String[]{"Tipo de documento:", "DNI"});
        datos.add(new String[]{"Número de documento:", "45464546"});
        datos.add(new String[]{"Fecha de nacimiento:", "28 de mayo de 1998"});
        datos.add(new String[]{"Teléfono:", "913454319"});
        datos.add(new String[]{"Dirección:", "Av. Los Taxis 123, Lima"});

        TaxiCuentaAdapter adapter = new TaxiCuentaAdapter(datos);
        recyclerView.setAdapter(adapter);

        // Ya tenías un listener para btnVehiculo aquí, lo mantengo:
        btnVehiculo.setOnClickListener(v -> {
            Log.d("TaxiCuenta", "Botón de Vehículo presionado");
            Intent intent = new Intent(TaxiCuenta.this, TaxiVehiculo.class);
            startActivity(intent);
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Obtener el ID del ítem seleccionado
            if (itemId == R.id.wifi) { // Asumiendo que 'wifi' es el icono/id para la navegación principal del taxi
                Intent intentInicio = new Intent(TaxiCuenta.this, TaxiActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (itemId == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiCuenta.this, TaxiLocation.class);
                startActivity(intentUbicacion);
                return true;
            } else if (itemId == R.id.notify) {
                Intent intentAlertas = new Intent(TaxiCuenta.this, TaxiDashboardActivity.class);
                startActivity(intentAlertas);
                return true;
            }
            return false; // Devuelve false si no se seleccionó ningún ítem válido
        });

        // Duplicidad de btnVehiculo.setOnClickListener, eliminando el segundo y manteniendo el primero.
        // El último listener es el que prevalece, pero para claridad es mejor tener uno solo.
        // El de arriba ya lo maneja.

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        btnCerrarSesion.setOnClickListener(v -> {
            mostrarDialogoCerrarSesion(); // Llama a la nueva función
        });
    }

    /**
     * Muestra un diálogo de confirmación para cerrar la sesión del usuario.
     * Cierra la sesión de Firebase, Google (si aplica), limpia las SharedPreferences
     * y redirige a la LoginActivity, borrando la pila de actividades.
     */
    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión en Firebase
                    mAuth.signOut();

                    // Cerrar sesión Google si aplica (asegúrate de que googleSignInClient esté inicializado)
                    if (googleSignInClient != null) {
                        googleSignInClient.signOut().addOnCompleteListener(task -> {
                            // Limpiar preferencias
                            SharedPreferences prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE);
                            prefs.edit().clear().apply();

                            // Redirigir a LoginActivity
                            Intent intent = new Intent(TaxiCuenta.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Log.e("REDIRECCION", "Estoy redirigiendo a un activity desde TaxiCuenta", new Exception("Redirección a LoginActivity"));
                            startActivity(intent);

                            // Finalizar la actividad actual
                            finish();
                        });
                    } else {
                        // Si Google Sign-In no está configurado o no se usa, simplemente cierra sesión de Firebase y redirige
                        SharedPreferences prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(TaxiCuenta.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Log.e("REDIRECCION", "Estoy redirigiendo a un activity desde TaxiCuenta (sin Google Sign-Out)", new Exception("Redirección a LoginActivity"));
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}