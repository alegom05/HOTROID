package com.example.hotroid;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class AdminFotosActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 100;
    private final List<ImageView> imageViews = new ArrayList<>();
    private final List<FrameLayout> frameLayouts = new ArrayList<>();
    private final List<Uri> imageUris = new ArrayList<>();

    private ImageView btnAdd;
    private Button btnGuardar;

    private boolean editMode = true; // true = puedes editar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_fotos);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Referencias
        btnAdd = findViewById(R.id.btnSeleccionarImagenes);
        btnGuardar = findViewById(R.id.btnGuardarUbicacion);

        imageViews.add(findViewById(R.id.img1));
        imageViews.add(findViewById(R.id.img2));
        imageViews.add(findViewById(R.id.img3));
        imageViews.add(findViewById(R.id.img4));

        frameLayouts.add(findViewById(R.id.frame1));
        frameLayouts.add(findViewById(R.id.frame2));
        frameLayouts.add(findViewById(R.id.frame3));
        frameLayouts.add(findViewById(R.id.frame4));

        // Evento del botón +
        btnAdd.setOnClickListener(v -> abrirGaleria());

        // Evento del botón Guardar / Actualizar
        btnGuardar.setOnClickListener(v -> {
            if (editMode) {
                // Guardar
                editMode = false;
                btnAdd.setVisibility(View.GONE);
                btnGuardar.setText("Actualizar");

                // Desactivar clicks
                for (ImageView img : imageViews) {
                    img.setOnClickListener(null);
                }
            } else {
                // Activar edición nuevamente
                editMode = true;
                btnAdd.setVisibility(View.VISIBLE);
                btnGuardar.setText("Guardar");

                // Habilitar edición
                btnAdd.setOnClickListener(view -> abrirGaleria());
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminFotosActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminFotosActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminFotosActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminFotosActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"), PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            imageUris.clear();

            if (data.getClipData() != null) {
                int count = Math.min(data.getClipData().getItemCount(), 4);
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            mostrarImagenes();
        }
    }
    private void mostrarImagenes() {
        for (int i = 0; i < 4; i++) {
            if (i < imageUris.size()) {
                frameLayouts.get(i).setVisibility(View.VISIBLE);
                imageViews.get(i).setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViews.get(i).setImageURI(imageUris.get(i));
            } else {
                frameLayouts.get(i).setVisibility(View.GONE);
            }
        }
    }
}