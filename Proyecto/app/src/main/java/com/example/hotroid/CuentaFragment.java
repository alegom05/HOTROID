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

import com.example.hotroid.databinding.UserAccountOptionBinding;

public class CuentaFragment extends Fragment {

    private UserAccountOptionBinding binding;

    public CuentaFragment() {
        // Constructor vacío obligatorio
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = UserAccountOptionBinding.inflate(inflater, container, false);

        // Tema guardado
        SharedPreferences prefs = requireContext().getSharedPreferences("ajustes", 0);
        boolean oscuro = prefs.getBoolean("modo_oscuro", false);
        AppCompatDelegate.setDefaultNightMode(
                oscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

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

        // Agregar OnClickListener al botón de cerrar sesión
        binding.cerrarSesionButton.setOnClickListener(v -> mostrarDialogoCerrarSesion());
    }

    private void mostrarDialogoDeTema() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.menu_opcion_tema, null);
        RadioButton rbClaro = view.findViewById(R.id.rbClaro);
        RadioButton rbOscuro = view.findViewById(R.id.rbOscuro);
        Button btnOK = view.findViewById(R.id.btnOK);
        Button btnCancelar = view.findViewById(R.id.btnCancelar);

        SharedPreferences prefs = requireContext().getSharedPreferences("ajustes", 0);
        boolean esOscuro = prefs.getBoolean("modo_oscuro", false);
        rbClaro.setChecked(!esOscuro);
        rbOscuro.setChecked(esOscuro);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setCancelable(false)
                .create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());
        btnOK.setOnClickListener(v -> {
            boolean modoOscuroSeleccionado = rbOscuro.isChecked();
            prefs.edit().putBoolean("modo_oscuro", modoOscuroSeleccionado).apply();
            AppCompatDelegate.setDefaultNightMode(
                    modoOscuroSeleccionado ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            dialog.dismiss();
            Log.d("FragmentInfo", "Este fragment pertenece a: " + requireActivity().getClass().getSimpleName());
            requireActivity().recreate(); // recarga el fragmento desde la actividad
            Log.d("FragmentInfo", "Este fragment pertenece a: " + requireActivity().getClass().getSimpleName());

        });

        dialog.show();
    }

    private void mostrarDialogoCerrarSesion() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Limpiar datos de sesión si es necesario
                    SharedPreferences prefs = requireContext().getSharedPreferences("sesion_usuario", 0);
                    prefs.edit().clear().apply();

                    // Crear Intent para ir a MainActivity
                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    // Finalizar la actividad actual
                    requireActivity().finish();
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