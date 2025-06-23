package com.example.hotroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton; // Import MaterialButton
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminEditarServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio, etHorario;
    private MaterialButton btnToggleHabilitado, btnSeleccionarImagenes, btnLimpiarImagenes, btnGuardarCambios; // Changed to MaterialButton
    private LinearLayout contenedorImagenes;

    private String documentId;
    private boolean estadoHabilitado;
    private ArrayList<String> currentImageUris = new ArrayList<>(); // To store selected image URIs as Strings
    private FirebaseFirestore db;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_servicio); // Make sure this matches your layout file name

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etHorario = findViewById(R.id.etHorario); // Initialize etHorario

        // Initialize MaterialButtons
        btnToggleHabilitado = findViewById(R.id.btnToggleHabilitado);
        btnSeleccionarImagenes = findViewById(R.id.btnSeleccionarImagenes);
        btnLimpiarImagenes = findViewById(R.id.btnLimpiarImagenes);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        contenedorImagenes = findViewById(R.id.contenedorImagenes);

        // Get data from Intent
        // Using null-safe defaults for String and 0.0 for double
        documentId = getIntent().getStringExtra("documentId");
        String serviceName = getIntent().getStringExtra("Service_name");
        String serviceDescription = getIntent().getStringExtra("Service_description");
        double price = getIntent().getDoubleExtra("price", 0.0); // Get price as double
        String schedule = getIntent().getStringExtra("schedule"); // Get schedule
        ArrayList<String> receivedImageUris = getIntent().getStringArrayListExtra("imagenes");
        estadoHabilitado = getIntent().getBooleanExtra("habilitado", true);

        // Populate EditText fields with received data
        etNombreServicio.setText(serviceName != null ? serviceName : "");
        etDescripcion.setText(serviceDescription != null ? serviceDescription : "");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        etPrecio.setText(decimalFormat.format(price)); // Format double to String for EditText
        etHorario.setText(schedule != null ? schedule : ""); // Set horario

        // Copy received image URIs to currentImageUris for modification
        if (receivedImageUris != null) {
            currentImageUris.addAll(receivedImageUris);
        }

        // Update Toggle Button text and color
        updateToggleButtonText();

        // Display existing images
        displayImages(currentImageUris);

        // --- Event Listeners ---
        btnToggleHabilitado.setOnClickListener(v -> {
            estadoHabilitado = !estadoHabilitado; // Toggle the state
            updateToggleButtonText(); // Update button UI
            Toast.makeText(this, "Estado del servicio cambiado a " + (estadoHabilitado ? "Habilitado" : "Deshabilitado"), Toast.LENGTH_SHORT).show();
        });

        btnSeleccionarImagenes.setOnClickListener(v -> openImageChooser());

        btnLimpiarImagenes.setOnClickListener(v -> {
            currentImageUris.clear(); // Clear all stored image URIs
            contenedorImagenes.removeAllViews(); // Remove all ImageViews from layout
            Toast.makeText(this, "Imágenes limpiadas.", Toast.LENGTH_SHORT).show();
        });

        btnGuardarCambios.setOnClickListener(v -> saveChanges());

        // Bottom Navigation (existing logic, no changes)
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_registros) {
                Intent intentInicio = new Intent(AdminEditarServicioActivity.this, AdminActivity.class);
                startActivity(intentInicio);
                return true;
            } else if (item.getItemId() == R.id.nav_taxistas) {
                Intent intentUbicacion = new Intent(AdminEditarServicioActivity.this, AdminTaxistas.class);
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.nav_checkout) {
                Intent intentAlertas = new Intent(AdminEditarServicioActivity.this, AdminCheckout.class);
                startActivity(intentAlertas);
                return true;
            } else if (item.getItemId() == R.id.nav_reportes) {
                Intent intentAlertas = new Intent(AdminEditarServicioActivity.this, AdminReportes.class);
                startActivity(intentAlertas);
                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Updates the text and background tint of the toggle habilitado button based on `estadoHabilitado`.
     */
    private void updateToggleButtonText() {
        if (estadoHabilitado) {
            btnToggleHabilitado.setText("Deshabilitar");
            btnToggleHabilitado.setBackgroundTintList(getResources().getColorStateList(R.color.red)); // Use ColorStateList for tint
        } else {
            btnToggleHabilitado.setText("Habilitar");
            btnToggleHabilitado.setBackgroundTintList(getResources().getColorStateList(R.color.color_estado_habilitado)); // Use ColorStateList for tint
        }
    }

    /**
     * Opens an image chooser intent to select multiple images.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT); // <-- cambio importante
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imágenes"), PICK_IMAGE_REQUEST);
    }

    /**
     * Displays images in the LinearLayout from a list of URI strings.
     * Clears existing views before adding new ones.
     */
    private void displayImages(ArrayList<String> uriStrings) {
        contenedorImagenes.removeAllViews(); // Clear existing image views
        if (uriStrings != null && !uriStrings.isEmpty()) {
            for (String uriStr : uriStrings) {
                try {
                    Uri uri = Uri.parse(uriStr);
                    ImageView imageView = new ImageView(this);
                    imageView.setImageURI(uri);
                    imageView.setPadding(8, 0, 8, 0); // Add horizontal padding for spacing between images
                    imageView.setContentDescription("Service Image"); // For accessibility

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300); // Fixed size for consistency
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); // Scale image to fill bounds

                    contenedorImagenes.addView(imageView);
                } catch (Exception e) {
                    // Log the error or show a toast if a URI string is invalid
                    Toast.makeText(this, "Error al cargar una imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            currentImageUris.clear(); // Clear old images before adding new ones

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    // Take persistent URI permissions
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentImageUris.add(imageUri.toString()); // Store as String
                }
            } else if (data.getData() != null) { // Single image selected
                Uri imageUri = data.getData();
                // Take persistent URI permissions
                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                currentImageUris.add(imageUri.toString()); // Store as String
            }
            displayImages(currentImageUris); // Re-display all images, including newly selected ones
        }
    }

    /**
     * Saves the changes to Firestore and returns the updated data to the previous activity.
     */
    private void saveChanges() {
        String nombre = etNombreServicio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String horario = etHorario.getText().toString().trim();

        // Input validation
        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || horario.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "El precio debe ser un número válido (ej. 35.50).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare data for Firestore update
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("descripcion", descripcion);
        updates.put("precio", precio);
        updates.put("horario", horario);
        updates.put("imagenes", currentImageUris); // Save image URIs as Strings
        updates.put("habilitado", estadoHabilitado);

        // Update document in Firestore
        if (documentId != null && !documentId.isEmpty()) {
            db.collection("servicios").document(documentId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AdminEditarServicioActivity.this, "Servicio actualizado con éxito.", Toast.LENGTH_SHORT).show();

                        // Prepare result Intent to send back to AdminServiciosDetallesActivity
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("nombre", nombre);
                        resultIntent.putExtra("descripcion", descripcion);
                        resultIntent.putExtra("precio", precio); // Pass double
                        resultIntent.putExtra("horario", horario);
                        resultIntent.putStringArrayListExtra("imagenes", currentImageUris);
                        resultIntent.putExtra("habilitado", estadoHabilitado);

                        setResult(RESULT_OK, resultIntent);
                        finish(); // Close activity
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminEditarServicioActivity.this, "Error al actualizar servicio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Error: ID de documento de servicio no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}