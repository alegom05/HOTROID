package com.example.hotroid;

import android.app.TimePickerDialog; // Importar TimePickerDialog
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

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar; // Importar Calendar
import java.util.HashMap;
import java.util.Map;

public class AdminEditarServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio, etHoraInicio, etHoraFin; // etHorario eliminado
    private MaterialButton btnToggleHabilitado, btnSeleccionarImagenes, btnLimpiarImagenes, btnGuardarCambios;
    private MaterialButton btnHoraInicio, btnHoraFin; // Nuevos botones para seleccionar hora
    private LinearLayout contenedorImagenes;

    private String documentId;
    private boolean estadoHabilitado;
    private ArrayList<String> currentImageUris = new ArrayList<>();
    private FirebaseFirestore db;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_servicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        etHoraInicio = findViewById(R.id.etHoraInicio); // Inicializar nuevo EditText
        etHoraFin = findViewById(R.id.etHoraFin);       // Inicializar nuevo EditText

        // Initialize MaterialButtons
        btnToggleHabilitado = findViewById(R.id.btnToggleHabilitado);
        btnSeleccionarImagenes = findViewById(R.id.btnSeleccionarImagenes);
        btnLimpiarImagenes = findViewById(R.id.btnLimpiarImagenes);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);
        btnHoraInicio = findViewById(R.id.btnHoraInicio); // Inicializar nuevo botón
        btnHoraFin = findViewById(R.id.btnHoraFin);       // Inicializar nuevo botón

        contenedorImagenes = findViewById(R.id.contenedorImagenes);

        // Get data from Intent
        documentId = getIntent().getStringExtra("documentId");
        String serviceName = getIntent().getStringExtra("Service_name");
        String serviceDescription = getIntent().getStringExtra("Service_description");
        double price = getIntent().getDoubleExtra("price", 0.0);
        String horaInicio = getIntent().getStringExtra("hora_inicio"); // Obtener horaInicio
        String horaFin = getIntent().getStringExtra("hora_fin");       // Obtener horaFin
        ArrayList<String> receivedImageUris = getIntent().getStringArrayListExtra("imagenes");
        estadoHabilitado = getIntent().getBooleanExtra("habilitado", true);

        // Populate EditText fields with received data
        etNombreServicio.setText(serviceName != null ? serviceName : "");
        etDescripcion.setText(serviceDescription != null ? serviceDescription : "");
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        etPrecio.setText(decimalFormat.format(price));
        etHoraInicio.setText(horaInicio != null ? horaInicio : ""); // Establecer horaInicio
        etHoraFin.setText(horaFin != null ? horaFin : "");       // Establecer horaFin

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
            estadoHabilitado = !estadoHabilitado;
            updateToggleButtonText();
            Toast.makeText(this, "Estado del servicio cambiado a " + (estadoHabilitado ? "Habilitado" : "Deshabilitado"), Toast.LENGTH_SHORT).show();
        });

        btnSeleccionarImagenes.setOnClickListener(v -> openImageChooser());

        btnLimpiarImagenes.setOnClickListener(v -> {
            currentImageUris.clear();
            contenedorImagenes.removeAllViews();
            Toast.makeText(this, "Imágenes limpiadas.", Toast.LENGTH_SHORT).show();
        });

        btnGuardarCambios.setOnClickListener(v -> {
            boolean hayImagenNueva = false;
            for (String uriStr : currentImageUris) {
                if (!uriStr.startsWith("http")) {
                    hayImagenNueva = true;
                    break;
                }
            }

            if (hayImagenNueva) {
                subirImagenesANubeYGuardar();
            } else {
                guardarEnFirestore(currentImageUris);
            }
        });

        // Listeners para los nuevos campos de hora
        etHoraInicio.setOnClickListener(v -> showTimePickerDialog(etHoraInicio));
        btnHoraInicio.setOnClickListener(v -> showTimePickerDialog(etHoraInicio));

        etHoraFin.setOnClickListener(v -> showTimePickerDialog(etHoraFin));
        btnHoraFin.setOnClickListener(v -> showTimePickerDialog(etHoraFin));

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
     * Shows a TimePickerDialog and sets the selected time to the target EditText.
     * @param targetEditText The EditText to set the selected time to.
     */
    private void showTimePickerDialog(final EditText targetEditText) {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    targetEditText.setText(formattedTime);
                }, hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    /**
     * Updates the text and background tint of the toggle habilitado button based on `estadoHabilitado`.
     */
    private void updateToggleButtonText() {
        if (estadoHabilitado) {
            btnToggleHabilitado.setText("Deshabilitar");
            btnToggleHabilitado.setBackgroundTintList(getResources().getColorStateList(R.color.red));
        } else {
            btnToggleHabilitado.setText("Habilitar");
            btnToggleHabilitado.setBackgroundTintList(getResources().getColorStateList(R.color.color_estado_habilitado));
        }
    }

    /**
     * Opens an image chooser intent to select multiple images.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
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
        contenedorImagenes.removeAllViews();
        if (uriStrings != null && !uriStrings.isEmpty()) {
            for (String uriStr : uriStrings) {
                try {
                    ImageView imageView = new ImageView(this);

                    Glide.with(this)
                            .load(uriStr)
                            .into(imageView);

                    imageView.setPadding(8, 0, 8, 0);
                    imageView.setContentDescription("Service Image");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(300, 300);
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    contenedorImagenes.addView(imageView);
                } catch (Exception e) {
                    Toast.makeText(this, "Error al cargar una imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {

            if (data.getClipData() != null) { // Selección múltiple
                int count = data.getClipData().getItemCount();
                int espacioDisponible = 4 - currentImageUris.size();

                for (int i = 0; i < count && i < espacioDisponible; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentImageUris.add(imageUri.toString());
                }

                if (count > espacioDisponible) {
                    Toast.makeText(this, "Solo se pueden agregar hasta 4 imágenes.", Toast.LENGTH_SHORT).show();
                }

            } else if (data.getData() != null) { // Selección única
                if (currentImageUris.size() < 4) {
                    Uri imageUri = data.getData();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    currentImageUris.add(imageUri.toString());
                } else {
                    Toast.makeText(this, "Máximo 4 imágenes permitidas.", Toast.LENGTH_SHORT).show();
                }
            }
            displayImages(currentImageUris);
        }
    }

    /**
     * Uploads new images to Cloudinary (if any) and then calls guardarEnFirestore.
     */
    private void subirImagenesANubeYGuardar() {
        // Collect existing Cloudinary URLs first
        ArrayList<String> urlsToUpload = new ArrayList<>();
        ArrayList<String> existingUrls = new ArrayList<>();
        for (String uriStr : currentImageUris) {
            if (uriStr.startsWith("http")) {
                existingUrls.add(uriStr); // Already a Cloudinary URL
            } else {
                urlsToUpload.add(uriStr); // New local URI to upload
            }
        }

        if (urlsToUpload.isEmpty()) {
            // No new images to upload, just save existing and original URLs
            guardarEnFirestore(currentImageUris);
            return;
        }

        final int[] uploadCount = {0}; // To track completion of uploads
        final ArrayList<String> finalUrls = new ArrayList<>(existingUrls); // Start with existing URLs

        CloudinaryManager.init(getApplicationContext()); // Ensure Cloudinary is initialized

        for (String uriStr : urlsToUpload) {
            Uri localUri = Uri.parse(uriStr);
            MediaManager.get().upload(localUri)
                    .option("folder", "Servicios") // Your Cloudinary folder
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String url = (String) resultData.get("secure_url");
                            synchronized (finalUrls) { // Synchronize access as callbacks can be async
                                finalUrls.add(url);
                                uploadCount[0]++;
                                if (uploadCount[0] == urlsToUpload.size()) {
                                    // All new images uploaded, now save to Firestore
                                    guardarEnFirestore(finalUrls);
                                }
                            }
                        }
                        @Override
                        public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                            Toast.makeText(AdminEditarServicioActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            // Handle error, maybe stop processing or indicate failure
                            uploadCount[0]++; // Still increment to prevent stuck state
                            if (uploadCount[0] == urlsToUpload.size()) {
                                // Even if some failed, try to save what succeeded or show error
                                guardarEnFirestore(finalUrls); // Save what's available
                            }
                        }
                        @Override
                        public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                    })
                    .dispatch();
        }
    }


    /**
     * Saves the changes to Firestore and returns the updated data to the previous activity.
     */
    private void guardarEnFirestore(ArrayList<String> urlsFinales) {
        String nombre = etNombreServicio.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String precioStr = etPrecio.getText().toString().trim();
        String horaInicio = etHoraInicio.getText().toString().trim(); // Obtener hora de inicio
        String horaFin = etHoraFin.getText().toString().trim();     // Obtener hora de fin

        // Input validation
        if (nombre.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || horaInicio.isEmpty() || horaFin.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio < 0) {
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
        updates.put("horaInicio", horaInicio); // Guardar hora de inicio
        updates.put("horaFin", horaFin);       // Guardar hora de fin
        updates.put("imagenes", urlsFinales);
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
                        resultIntent.putExtra("precio", precio);
                        resultIntent.putExtra("hora_inicio", horaInicio); // Pasar hora de inicio
                        resultIntent.putExtra("hora_fin", horaFin);       // Pasar hora de fin
                        resultIntent.putStringArrayListExtra("imagenes", urlsFinales);
                        resultIntent.putExtra("habilitado", estadoHabilitado);

                        setResult(RESULT_OK, resultIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminEditarServicioActivity.this, "Error al actualizar servicio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Error: ID de documento de servicio no encontrado.", Toast.LENGTH_SHORT).show();
        }
    }
}