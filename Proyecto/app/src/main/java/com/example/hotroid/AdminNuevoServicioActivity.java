package com.example.hotroid;

import android.app.TimePickerDialog; // Import TimePickerDialog
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.hotroid.bean.Servicios; // Make sure this bean is updated to use double for precio
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar; // Import Calendar
import java.util.Locale;   // Import Locale
import java.util.Map;

public class AdminNuevoServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio; // etHorario removed
    private MaterialButton btnSeleccionarImagenes, btnLimpiarImagenes, btnGuardarServicio;
    private MaterialButton btnSelectHoraInicio, btnSelectHoraFin; // New buttons for time pickers
    private TextView tvHoraInicio, tvHoraFin; // TextViews to display selected times
    private LinearLayout contenedorImagenes;

    private static final int PICK_IMAGES_REQUEST = 1;
    private ArrayList<String> imagenesSeleccionadasUris = new ArrayList<>();

    private String selectedHoraInicio = "";
    private String selectedHoraFin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_nuevo_servicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        CloudinaryManager.init(getApplicationContext());

        // Initialize UI components
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        // etHorario = findViewById(R.id.etHorario); // REMOVED

        btnSelectHoraInicio = findViewById(R.id.btnSelectHoraInicio); // Initialize new time buttons
        btnSelectHoraFin = findViewById(R.id.btnSelectHoraFin);       // Initialize new time buttons
        tvHoraInicio = findViewById(R.id.tvHoraInicio);               // Initialize new TextViews
        tvHoraFin = findViewById(R.id.tvHoraFin);                     // Initialize new TextViews

        btnSeleccionarImagenes = findViewById(R.id.btnSeleccionarImagenes);
        btnLimpiarImagenes = findViewById(R.id.btnLimpiarImagenes);
        btnGuardarServicio = findViewById(R.id.btnGuardarServicio);

        contenedorImagenes = findViewById(R.id.contenedorImagenes);

        // --- Event Listeners ---
        btnSelectHoraInicio.setOnClickListener(v -> showTimePickerDialog(true));
        btnSelectHoraFin.setOnClickListener(v -> showTimePickerDialog(false));

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

        btnGuardarServicio.setOnClickListener(v -> saveNewService());

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
     * Shows a TimePickerDialog to select either start or end time.
     * @param isStartTime True if selecting start time, false for end time.
     */
    private void showTimePickerDialog(boolean isStartTime) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    if (isStartTime) {
                        selectedHoraInicio = formattedTime;
                        tvHoraInicio.setText("Inicio: " + formattedTime);
                    } else {
                        selectedHoraFin = formattedTime;
                        tvHoraFin.setText("Fin: " + formattedTime);
                    }
                }, hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    /**
     * Opens an image chooser intent to select multiple images.
     */
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            int currentSize = imagenesSeleccionadasUris.size();

            if (data.getClipData() != null) { // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count && (currentSize + i) < 4; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagenesSeleccionadasUris.add(imageUri.toString());
                }
            } else if (data.getData() != null) { // Single image selected
                Uri imageUri = data.getData();
                if (currentSize < 4) {
                    getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagenesSeleccionadasUris.add(imageUri.toString());
                }
            }
            mostrarImagenesSeleccionadas();
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
                imageView.setPadding(8, 0, 8, 0);
                imageView.setContentDescription("Service Image");

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
                imageView.setLayoutParams(params);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                contenedorImagenes.addView(imageView);
            } catch (Exception e) {
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

        // Input validation
        if (nombreServicio.isEmpty() || descripcion.isEmpty() || precioStr.isEmpty() || selectedHoraInicio.isEmpty() || selectedHoraFin.isEmpty() || imagenesSeleccionadasUris.isEmpty()) {
            Toast.makeText(AdminNuevoServicioActivity.this, "Por favor, complete todos los campos, seleccione las horas y al menos una imagen.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(AdminNuevoServicioActivity.this, "El precio debe ser un número válido (ej. 35.50).", Toast.LENGTH_SHORT).show();
            return;
        }
        subirImagenesACloudinary(nombreServicio, descripcion, precio, selectedHoraInicio, selectedHoraFin); // Pass new time fields
    }

    private void subirImagenesACloudinary(String nombre, String descripcion, double precio, String horaInicio, String horaFin) {
        ArrayList<String> urlsFinales = new ArrayList<>();

        if (imagenesSeleccionadasUris.isEmpty()) {
            // If no images are selected but validation passed, directly save to Firestore
            // This case should ideally not be reached if validation is correct
            guardarEnFirestore(nombre, descripcion, precio, horaInicio, horaFin, urlsFinales);
            return;
        }

        // Counter to track successful uploads
        final int[] uploadedCount = {0};

        for (String localUri : imagenesSeleccionadasUris) {
            try {
                Uri uri = Uri.parse(localUri);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                File tempFile = File.createTempFile("upload_", ".jpg", getCacheDir());
                OutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                inputStream.close();
                outputStream.close();

                MediaManager.get().upload(tempFile.getAbsolutePath())
                        .option("folder", "Servicios")
                        .callback(new UploadCallback() {
                            @Override
                            public void onStart(String requestId) {}

                            @Override
                            public void onProgress(String requestId, long bytes, long totalBytes) {}

                            @Override
                            public void onSuccess(String requestId, Map resultData) {
                                String url = (String) resultData.get("secure_url");
                                urlsFinales.add(url);
                                uploadedCount[0]++;

                                if (uploadedCount[0] == imagenesSeleccionadasUris.size()) {
                                    guardarEnFirestore(nombre, descripcion, precio, horaInicio, horaFin, urlsFinales);
                                }
                            }

                            @Override
                            public void onError(String requestId, ErrorInfo error) {
                                Toast.makeText(AdminNuevoServicioActivity.this, "Error al subir imagen: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                                // Handle error: perhaps stop the process or show a warning
                                // For simplicity, we continue with available images
                            }

                            @Override
                            public void onReschedule(String requestId, ErrorInfo error) {}
                        }).dispatch();

            } catch (Exception e) {
                Toast.makeText(this, "Error al preparar imagen para subir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void guardarEnFirestore(String nombre, String descripcion, double precio, String horaInicio, String horaFin, ArrayList<String> urlsFinales) {
        // Create a new Servicios object using the updated constructor
        Servicios nuevoServicio = new Servicios(nombre, descripcion, precio, horaInicio, horaFin, urlsFinales);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("servicios")
                .add(nuevoServicio)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Servicio guardado con éxito.", Toast.LENGTH_SHORT).show();

                    // Prepare result Intent for AdminServiciosActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("nombre", nombre);
                    resultIntent.putExtra("descripcion", descripcion);
                    resultIntent.putExtra("precio", String.valueOf(precio)); // Pass price as String
                    resultIntent.putExtra("horaInicio", horaInicio); // New
                    resultIntent.putExtra("horaFin", horaFin);       // New
                    resultIntent.putStringArrayListExtra("imagenes", urlsFinales);

                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar servicio: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}