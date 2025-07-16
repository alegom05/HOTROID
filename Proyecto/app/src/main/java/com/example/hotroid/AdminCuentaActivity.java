package com.example.hotroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences; // Importar SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog; // Importar AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.authentication.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn; // Importar GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient; // Importar GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions; // Importar GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // Importar FirebaseAuth

public class AdminCuentaActivity extends AppCompatActivity {

    private FirebaseAuth mAuth; // Instancia de FirebaseAuth
    private GoogleSignInClient googleSignInClient; // Instancia de GoogleSignInClient (si usas Google Sign-In)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_cuenta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar GoogleSignInClient si usas Google Sign-In
        // Asegúrate de que tu GoogleSignInOptions sea el mismo que usas en LoginActivity
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Asegúrate de tener este string resource
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Button btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        btnCerrarSesion.setOnClickListener(v -> {
            mostrarDialogoCerrarSesion(); // Llamar a la función del diálogo
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminCuentaActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminCuentaActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminCuentaActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminCuentaActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(this) // Usar 'this' como contexto de la actividad
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión en Firebase
                    mAuth.signOut(); // Usar la instancia de FirebaseAuth

                    // Cerrar sesión Google si aplica (asegúrate de que googleSignInClient esté inicializado)
                    if (googleSignInClient != null) {
                        googleSignInClient.signOut().addOnCompleteListener(task -> {
                            // Limpiar preferencias
                            SharedPreferences prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE); // Usar MODE_PRIVATE
                            prefs.edit().clear().apply();

                            // Redirigir a LoginActivity
                            Intent intent = new Intent(AdminCuentaActivity.this, LoginActivity.class); // Usar 'this' como contexto
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Log.e("REDIRECCION", "Estoy redirigiendo a un activity", new Exception("Redirección a LoginActivity")); // Mejorar el log
                            startActivity(intent);

                            // Finalizar la actividad actual
                            finish(); // Usar 'finish()' directamente
                        });
                    } else {
                        // Si Google Sign-In no está configurado o no se usa, simplemente cierra sesión de Firebase y redirige
                        SharedPreferences prefs = getSharedPreferences("sesion_usuario", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Intent intent = new Intent(AdminCuentaActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Log.e("REDIRECCION", "Estoy redirigiendo a un activity (sin Google Sign-Out)", new Exception("Redirección a LoginActivity"));
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}