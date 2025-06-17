package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Make sure to import Log
import android.widget.TextView; // Make sure to import TextView
import android.widget.Toast; // Make sure to import Toast

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Import FirebaseUser
import com.google.firebase.firestore.DocumentReference; // Import DocumentReference
import com.google.firebase.firestore.FirebaseFirestore; // Import FirebaseFirestore

public class AdminActivity extends AppCompatActivity {

    private TextView tvAdminStatus; // This will hold the dynamic "Activado"/"Desactivado" (tvNombre2)
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_main);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI component for status (only the dynamic text view)
        tvAdminStatus = findViewById(R.id.tvNombre2); // Corresponds to dynamic status text in XML

        // Fetch and display admin status
        fetchAdminStatus();

        // Handle clicks for admin profile card
        CardView cardAdmin = findViewById(R.id.cardAdmin);
        cardAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminCuentaActivity.class);
            startActivity(intent);
        });

        // Handle clicks for main section cards
        CardView cardUbicacion = findViewById(R.id.cardUbicacion);
        cardUbicacion.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, UbicacionActivity.class);
            startActivity(intent);
        });

        CardView cardFotos = findViewById(R.id.cardFotos);
        cardFotos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminFotosActivity.class);
            startActivity(intent);
        });

        CardView cardHabitaciones = findViewById(R.id.cardHabitaciones);
        cardHabitaciones.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminHabitacionesActivity.class);
            startActivity(intent);
        });

        CardView cardServicios = findViewById(R.id.cardServicios);
        cardServicios.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminServiciosActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_registros) {
                return true;
            } else if (itemId == R.id.nav_taxistas) {
                startActivity(new Intent(AdminActivity.this, AdminTaxistas.class));
                return true;
            } else if (itemId == R.id.nav_checkout) {
                startActivity(new Intent(AdminActivity.this, AdminCheckout.class));
                return true;
            } else if (itemId == R.id.nav_reportes) {
                startActivity(new Intent(AdminActivity.this, AdminReportes.class));
                return true;
            }
            return false;
        });
    }

    /**
     * Fetches the administrator's status from Firestore and updates the UI.
     */
    private void fetchAdminStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String adminUid = currentUser.getUid();
            DocumentReference adminRef = db.collection("users").document(adminUid);

            adminRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean isHabilitado = documentSnapshot.getBoolean("habilitado");

                            if (isHabilitado != null) {
                                updateAdminStatusUI(isHabilitado);
                            } else {
                                Log.w("AdminActivity", "Field 'habilitado' not found or is null for admin: " + adminUid);
                                updateAdminStatusUI(false); // Default to deactivated if status not found
                            }
                        } else {
                            Log.d("AdminActivity", "No such document for admin: " + adminUid);
                            updateAdminStatusUI(false); // Default to deactivated if document doesn't exist
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminActivity", "Error fetching admin status: " + e.getMessage());
                        Toast.makeText(AdminActivity.this, "Error al cargar estado del administrador.", Toast.LENGTH_SHORT).show();
                        updateAdminStatusUI(false); // Default to deactivated on error
                    });
        } else {
            updateAdminStatusUI(false); // No user logged in, default to deactivated
            Toast.makeText(this, "No hay administrador logeado.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the UI elements based on the admin's habilitado status.
     * @param isHabilitado True if the admin is enabled, false otherwise.
     */
    private void updateAdminStatusUI(boolean isHabilitado) {
        if (isHabilitado) {
            tvAdminStatus.setText("Activado");
            tvAdminStatus.setTextColor(getResources().getColor(R.color.color_estado_habilitado, getTheme()));
        } else {
            tvAdminStatus.setText("Desactivado");
            tvAdminStatus.setTextColor(getResources().getColor(R.color.red_status, getTheme()));
        }
        // tvRol2 ("Estado:") is a static label, so it doesn't need to be updated here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchAdminStatus(); // Refresh status when returning to the activity
    }
}