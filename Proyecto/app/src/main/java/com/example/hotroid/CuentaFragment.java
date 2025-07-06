package com.example.hotroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.hotroid.authentication.LoginActivity;
import com.example.hotroid.databinding.UserAccountOptionBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CuentaFragment extends Fragment {

    private UserAccountOptionBinding binding;
    private GoogleSignInClient googleSignInClient;

    public CuentaFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserAccountOptionBinding.inflate(inflater, container, false);

        // Ya no es necesario configurar el tema aquí, se hace en la Application o MainActivity

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.informacionPersonalButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), InfoAccountUser.class));
        });

        binding.politicasPrivacidadButtom.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SecurityPoliticsUser.class));
        });

        binding.hotelesFavoritosButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), FavoriteHotelsUser.class));
        });

        binding.temaButton.setOnClickListener(v -> mostrarDialogoDeTema());

        // Agregar OnClickListener al botón de editar (FloatingActionButton)
        View rootView = binding.getRoot();
        rootView.findViewById(R.id.edit_button).setOnClickListener(v -> {
            // Inicia la actividad de edición
            startActivity(new Intent(requireContext(), EditAccountUser.class));
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid(); // clave del documento en Firestore

            FirebaseFirestore.getInstance()
                    .collection("usuarios") // o "persona", según tu colección
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            String apellido = documentSnapshot.getString("apellido");

                            // Validar nulos o vacíos
                            if (nombre == null || nombre.trim().isEmpty()) nombre = "";
                            if (apellido == null || apellido.trim().isEmpty()) apellido = "";

                            String nombreCompleto = (nombre + " " + apellido).trim();
                            if (nombreCompleto.isEmpty()) {
                                binding.nameProfileUser.setText("-");
                            } else {
                                binding.nameProfileUser.setText(nombreCompleto);
                            }
                        } else {
                            binding.nameProfileUser.setText("-");
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.nameProfileUser.setText("-");
                    });
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        // Agregar OnClickListener al botón de cerrar sesión
        binding.cerrarSesionButton.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }


    private void mostrarDialogoDeTema() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.menu_opcion_tema, null);
        RadioButton rbClaro = view.findViewById(R.id.rbClaro);
        RadioButton rbOscuro = view.findViewById(R.id.rbOscuro);
        Button btnOK = view.findViewById(R.id.btnOK);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        boolean esOscuro = ThemeManager.isDarkModeEnabled(requireContext());
        rbClaro.setChecked(!esOscuro);
        rbOscuro.setChecked(esOscuro);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            boolean modoOscuroSeleccionado = rbOscuro.isChecked();
            // Usa el ThemeManager para cambiar el tema
            ThemeManager.setDarkMode(requireContext(), modoOscuroSeleccionado);
            dialog.dismiss();
            requireActivity().recreate();
        });

        dialog.show();
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Cerrar sesión en Firebase
                    FirebaseAuth.getInstance().signOut();
                    // Cerrar sesión Google si aplica
                    googleSignInClient.signOut().addOnCompleteListener(task -> {
                        // Limpiar preferencias
                        SharedPreferences prefs = requireContext().getSharedPreferences("sesion_usuario", 0);
                        prefs.edit().clear().apply();

                        // Redirigir a LoginActivity
                        Intent intent = new Intent(requireContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Log.e("REDIRECCION", "Estoy redirigiendo a un activity", new Exception());
                        startActivity(intent);
                        // Finalizar la actividad actual
                        requireActivity().finish();
                    });

                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // evitar memory leaks
    }
}