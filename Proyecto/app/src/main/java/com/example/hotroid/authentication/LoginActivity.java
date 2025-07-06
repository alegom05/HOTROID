package com.example.hotroid.authentication;

import android.app.DatePickerDialog;
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
import com.example.hotroid.R;
import com.example.hotroid.SuperActivity;
import com.example.hotroid.TaxiActivity;
import com.example.hotroid.bean.Persona;
import com.example.hotroid.databinding.LoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private LoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;     //codigo personalizado para el login con google(codigo de solicitud/request code)
    //private Date fechaNacimiento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.login);
        binding = LoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Ya logueado → Redirige según el rol en Firestore
            db.collection("usuarios").document(user.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String rol = snapshot.getString("idRol");
                            redirectToRoleActivity(rol);
                        } else {
                            Toast.makeText(this, "Usuario sin datos registrados", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

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
                .requestIdToken(getString(R.string.default_web_client_id)) // tu client ID
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
        Log.e("REDIRECCION", "Estoy redirigiendo a un activity por logueo", new Exception());

    }

    /*private void loginWithEmail() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            Log.d("Proceso de Logueo","Debe completar todos los campos de forma obligatoria");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(authResult -> {
            checkIfUserExists();
        }).addOnFailureListener(e -> {
            Log.d("Proceso de Logueo","Se ingreso datos erroneos, verifique correo y/o contrasenia");
            Toast.makeText(this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }*/

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
                    Log.d("Proceso de Logueo","Se ingreso datos erroneos, verifique correo y/o contrasenia");
                });
    }

    private void checkUserRoleAndRedirect() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String rol = snapshot.getString("idRol");
                redirectToRoleActivity(rol);
            } else {
                Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToMain() {
        String uid = mAuth.getCurrentUser().getUid();

        db.collection("usuarios").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String rol = documentSnapshot.getString("idRol");
                Intent intent;

                if (rol == null) {
                    Toast.makeText(this, "Rol no definido", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (rol) {
                    case "Cliente": // por si usas un formato de ID como este
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
                    default:
                        Toast.makeText(this, "Rol desconocido: " + rol, Toast.LENGTH_SHORT).show();
                        return;
                }

                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show();
        });
    }

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
                        Toast.makeText(this, "Error con Google Sign-In", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (ApiException e) {
                Toast.makeText(this, "Fallo autenticación Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkIfGoogleUserExistsOrRegister(GoogleSignInAccount account) {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("usuarios").document(uid).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Ya registrado → Redirige según rol
                String rol = snapshot.getString("idRol");
                redirectToRoleActivity(rol);
            } else {
                // Nuevo usuario Google → Guardar en Firestore como "Cliente"
                FirebaseUser user = mAuth.getCurrentUser();

                String displayName = account.getDisplayName();
                String correo = user.getEmail();
                String nombre = "", apellido = "";
                if (displayName != null && !displayName.contains("@")) {
                    String[] partes = displayName.trim().split("\\s+");
                    nombre = partes[0];
                    if (partes.length > 1) {
                        apellido = partes[1];
                    }
                } else if (correo != null && correo.contains("@")) {
                    nombre = correo.substring(0, correo.indexOf("@"));
                    apellido = "";
                }

                Persona persona = new Persona();
                persona.setCorreo(correo);
                persona.setNombre(nombre);
                persona.setApellido(apellido);
                persona.setIdRol("Cliente");
                persona.setNacimiento(new Date()); // puedes dejar en null o solicitar después

                db.collection("usuarios").document(uid).set(persona)
                        .addOnSuccessListener(unused -> redirectToRoleActivity("Cliente"))
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al registrar usuario Google", Toast.LENGTH_SHORT).show());
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
            default:
                Toast.makeText(this, "Rol no reconocido: " + rol, Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }

}