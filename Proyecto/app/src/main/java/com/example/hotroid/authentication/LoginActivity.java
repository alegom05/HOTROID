package com.example.hotroid.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.AdminActivity;
import com.example.hotroid.ClienteActivity;
import com.example.hotroid.CloudinaryManager;
import com.example.hotroid.R;
import com.example.hotroid.SuperActivity;
import com.example.hotroid.TaxiActivity;
import com.example.hotroid.ThemeManager;
import com.example.hotroid.bean.Persona;
import com.example.hotroid.databinding.LoginBinding; // Ensure this is correct for your layout
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private LoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        EdgeToEdge.enable(this);
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CloudinaryManager.init(getApplicationContext());
        Log.d("LoginActivity", "CloudinaryManager.init() llamado desde LoginActivity onCreate.");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if the activity was launched from a successful registration (Taxista specifically)
        // This flag will be true if RegistroActivity2 sent it here for a Taxista.
        boolean fromRegistration = getIntent().getBooleanExtra("from_registration", false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Only attempt automatic redirection if a user is logged in AND we are NOT coming from a fresh registration.
        if (user != null && !fromRegistration) {
            db.collection("usuarios").document(user.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String rol = snapshot.getString("idRol");
                            redirectToRoleActivity(rol);
                        } else {
                            // User is authenticated but their data is missing in Firestore.
                            // This could happen if a Google sign-in creates a Firebase user
                            // but fails to save to Firestore for some reason.
                            Toast.makeText(this, "Usuario sin datos registrados en Firestore. Iniciando sesión como nuevo.", Toast.LENGTH_LONG).show();
                            // Optionally, sign out the user to force a fresh login/registration
                            mAuth.signOut();
                            if (googleSignInClient != null) {
                                googleSignInClient.signOut();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al verificar datos del usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        // Sign out the user on failure to avoid a stuck state
                        mAuth.signOut();
                        if (googleSignInClient != null) {
                            googleSignInClient.signOut();
                        }
                    });
        }
        // If 'fromRegistration' is true, the user will stay on LoginActivity,
        // allowing them to see the message/notification before attempting to log in.

        setupGoogleSignIn();

        // Login con correo y contraseña
        binding.btnLogin.setOnClickListener(v -> loginWithEmail());
        // Login con Google
        binding.btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        // Ir a actividad de registro
        binding.tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity1.class);
            startActivity(intent);
        });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.e("REDIRECCION", "Estoy redirigiendo a un activity por logueo (Google Sign-In)", new Exception());
    }

    private void loginWithEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            Log.d("Proceso de Logueo","Debe completar todos los campos de forma obligatoria");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> checkUserRoleAndRedirect())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Proceso de Logueo","Se ingresaron datos erróneos, verifique correo y/o contraseña");
                });
    }

    private void checkUserRoleAndRedirect() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String rol = snapshot.getString("idRol");
                redirectToRoleActivity(rol);
            } else {
                Toast.makeText(this, "Usuario no registrado en Firestore. Por favor, regístrese.", Toast.LENGTH_SHORT).show();
                mAuth.signOut(); // Sign out user if their data is missing in Firestore
                if (googleSignInClient != null) {
                    googleSignInClient.signOut();
                }
            }
        });
    }

    // This goToMain method seems redundant with redirectToRoleActivity.
    // Consider removing it or integrating its logic into redirectToRoleActivity if needed.
    // private void goToMain() {
    //     // ... (Your original goToMain method content) ...
    // }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkIfGoogleUserExistsOrRegister(account);
                    } else {
                        Toast.makeText(this, "Error con Google Sign-In: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LoginActivity", "Google Sign-In failed: " + task.getException().getMessage());
                    }
                });
            } catch (ApiException e) {
                Toast.makeText(this, "Fallo autenticación Google: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Google Sign-In ApiException: " + e.getStatusCode() + " - " + e.getMessage());
            }
        }
    }

    private void checkIfGoogleUserExistsOrRegister(GoogleSignInAccount account) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Already registered → Redirect based on role
                String rol = snapshot.getString("idRol");
                redirectToRoleActivity(rol);
            } else {
                // New Google user → Save to Firestore as "Cliente"
                FirebaseUser user = mAuth.getCurrentUser();

                String nombreCompleto = account.getDisplayName();
                String correo = user.getEmail();
                String nombre = "";
                String apellido = "";

                if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
                    String[] partes = nombreCompleto.trim().split("\\s+");
                    nombre = partes[0];
                    if (partes.length > 1) { // If there's more than just a first name
                        apellido = String.join(" ", Arrays.copyOfRange(partes, 1, partes.length));
                    }
                } else {
                    nombre = correo != null ? correo.split("@")[0] : "Usuario";
                    apellido = "";
                }

                Persona persona = new Persona();
                persona.setCorreo(correo);
                persona.setNombre(nombre);
                persona.setApellido(apellido);
                persona.setIdRol("Cliente"); // Default role for new Google sign-ins
                persona.setNacimiento(new Date()); // You might want to get this from user later

                db.collection("usuarios").document(uid).set(persona)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Registro de Google exitoso como Cliente.", Toast.LENGTH_SHORT).show();
                            redirectToRoleActivity("Cliente"); // Redirect immediately for Google Client
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar usuario Google: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void redirectToRoleActivity(String rol) {
        if (rol == null) {
            Toast.makeText(this, "Rol indefinido", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent;
        switch (rol) {
            case "Cliente":
                intent = new Intent(this, ClienteActivity.class);
                break;
            case "Taxista":
                intent = new Intent(this, TaxiActivity.class);
                break;
            case "Admin":
                intent = new Intent(this, AdminActivity.class);
                break;
            case "Superadmin":
                intent = new Intent(this, SuperActivity.class);
                break;
            // Removed "Superman" as it seemed to be for a SelectorDeRolActivity, which might not be needed here
            default:
                Toast.makeText(this, "Rol no reconocido: " + rol + ". Redirigiendo a Login.", Toast.LENGTH_SHORT).show();
                // For unrecognized roles, go back to login to prevent app getting stuck
                intent = new Intent(this, LoginActivity.class);
                break;
        }
        startActivity(intent);
        finish(); // Finish the current LoginActivity to prevent going back to it
    }
}