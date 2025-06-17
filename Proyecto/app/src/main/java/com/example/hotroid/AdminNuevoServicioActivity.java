package com.example.hotroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotroid.bean.Servicios; // Make sure this bean is updated to use double for precio
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // Import MaterialButton
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AdminNuevoServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio, etHorario; // Added etHorario
    private MaterialButton btnSeleccionarImagenes, btnLimpiarImagenes, btnGuardarServicio; // Changed to MaterialButton
    private LinearLayout contenedorImagenes;

    private static final int PICK_IMAGES_REQUEST = 1;
    private ArrayList<String> imagenesSeleccionadasUris = new ArrayList<>(); // Store as String for Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_nuevo_servicio); // Ensure this matches your layout file name

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etHorario = findViewById(R.id.etHorario); // Initialize etHorario

        btnSeleccionarImagenes = findViewById(R.id.btnSeleccionarImagenes);
        btnLimpiarImagenes = findViewById(R.id.btnLimpiarImagenes); // Initialize new clear button
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio); // Changed ID in XML

        contenedorImagenes = findViewById(R.id.contenedorImagenes);

        // --- Event Listeners ---
        btnSeleccionarImagenes.setOnClickListener(v -> {
            if (imagenesSeleccionadasUris.size() >= 4) {
                Toast.makeText(this, "Máximo 4 imágenes permitidas", Toast.LENGTH_SHORT).show();
                return;
            }
            openImageChooser();
        });

        btnLimpiarImagenes.setOnClickListener(v -> {
            imagenesSeleccionadasUris.clear();
            contenedorImagenes.removeAllViews();
            Toast.makeText(this, "Imágenes limpiadas.", Toast.LENGTH_SHORT).show();
        });

        btnGuardarServicio.setOnClickListener(v -> saveNewService()); // Call a dedicated method for saving

        // Bottom Navigation (existing logic, no changes)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminNuevoServicioActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminNuevoServicioActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminNuevoServicioActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminNuevoServicioActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Opens an image chooser intent to select multiple images.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // ACTION_OPEN_DOCUMENT is preferred for persistent access
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Allow multiple image selection
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            // Keep existing images if selecting more, but limit to 4 total
            int currentSize = imagenesSeleccionadasUris.size();

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && (currentSize + i) < 4; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    // Take persistent URI permissions for future access
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagenesSeleccionadasUris.add(imageUri.toString()); // Store as String
                }
            } else if (data.getData() != null) { // Single image selected
                Uri imageUri = data.getData();
                if (currentSize < 4) { // Only add if not at max
                    // Take persistent URI permissions for future access
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagenesSeleccionadasUris.add(imageUri.toString()); // Store as String
                }
            }
            mostrarImagenesSeleccionadas(); // Re-display all images, including newly selected ones
        }
    }

    /**
     * Displays selected images in the LinearLayout.
     * Clears existing views before adding new ones.
     */
    private void mostrarImagenesSeleccionadas() {
        contenedorImagenes.removeAllViews(); // Clear existing image views

        for (String uriStr : imagenesSeleccionadasUris) {
            try {
                Uri uri = Uri.parse(uriStr);
                ImageView imageView = new ImageView(this);
                imageView.setImageURI(uri);
                imageView.setPadding(8, 0, 8, 0); // Add horizontal padding for spacing between images
                imageView.setContentDescription("Service Image"); // For accessibility

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200); // Fixed size
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Scale image to fill bounds

                contenedorImagenes.addView(imageView);
            } catch (Exception e) {
                // Log the error or show a toast if a URI string is invalid
                Toast.makeText(this, "Error al cargar una imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handles saving the new service to Firestore and returning the result.
     */
    private void saveNewService() {
        String nombreServicio = etNombreServicio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String horario = etHorario.getText().toString().trim(); // Get horario

        // Input validation
        if (nombreServicio.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || horario.isEmpty() || imagenesSeleccionadasUris.isEmpty()) {
            Toast.makeText(AdminNuevoServicioActivity.this, "Por favor, complete todos los campos y seleccione al menos una imagen.", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio < 0) { // Ensure price is not negative
                Toast.makeText(this, "El precio no puede ser negativo.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(AdminNuevoServicioActivity.this, "El precio debe ser un número válido (ej. 35.50).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new Servicios object (assuming your bean is named Servicios and uses double for price)
        Servicios nuevoServicio = new Servicios(
                nombreServicio,
                descripcion,
                precio, // Pass double price
                horario, // Pass horario
                imagenesSeleccionadasUris // Pass list of URI strings
        );

        // Save to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("servicios")
                .add(nuevoServicio)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AdminNuevoServicioActivity.this, "Servicio guardado con éxito.", Toast.LENGTH_SHORT).show();

                    // Return result to previous activity (AdminServiciosActivity)
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("nombre", nombreServicio);
                    resultIntent.putExtra("descripcion", descripcion);
                    resultIntent.putExtra("precio", precio); // Pass double price
                    resultIntent.putExtra("horario", horario); // Pass horario
                    resultIntent.putStringArrayListExtra("imagenes", imagenesSeleccionadasUris);

                    setResult(RESULT_OK, resultIntent);
                    finish(); // Close this activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminNuevoServicioActivity.this, "Error al guardar servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}