package com.example.hotroid;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SuperDetallesAdminFormularioActivity extends AppCompatActivity {

    private static final String TAG = "AdminFormActivity";
    private EditText etFechaNacimiento;
    private EditText etNombre;
    private EditText etApellido;
    private Spinner spTipoDocumento;
    private EditText etNumDocumento;
    private EditText etCorreo;
    private EditText etTelefono;
    private EditText etDireccion;
    private Spinner spHotel;
    private ImageView ivFotoPerfil;

    // Launchers para tomar foto o seleccionar de galería
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getExtras() != null) {
                                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                                ivFotoPerfil.setImageBitmap(imageBitmap);
                            }
                        } else {
                            Toast.makeText(this, "Captura de foto cancelada.", Toast.LENGTH_SHORT).show();
                        }
                    });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri selectedImageUri = data.getData();
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                                    ivFotoPerfil.setImageBitmap(bitmap);
                                } catch (IOException e) {
                                    Log.e(TAG, "Error al cargar imagen de galería: " + e.getMessage());
                                    Toast.makeText(this, "Error al cargar imagen.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(this, "Selección de galería cancelada.", Toast.LENGTH_SHORT).show();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin_formulario);

        // Referencias a los elementos de la UI
        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        spTipoDocumento = findViewById(R.id.spTipoDocumento);
        etNumDocumento = findViewById(R.id.etNumDocumento);
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etDireccion = findViewById(R.id.etDireccion);
        spHotel = findViewById(R.id.spHotel);
        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);

        // Spinner Tipo de Documento
        List<String> tiposDocumento = new ArrayList<>(Arrays.asList("Seleccione tipo", "DNI", "Pasaporte", "Carnet de Extranjería"));
        ArrayAdapter<String> adapterDocumento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDocumento);
        adapterDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoDocumento.setAdapter(adapterDocumento);
        spTipoDocumento.setSelection(0); // Select "Seleccione tipo" by default

        // Lista de Hoteles
        List<String> hoteles = new ArrayList<>(Arrays.asList(
                "Seleccione hotel", // Added a default prompt
                "Hotel Oro Verde",
                "Sauce Resort",
                "Hotel Decameron",
                "Hotel Libertador",
                "Resort Paraíso",
                "Hotel Costa del Sol",
                "Aranwa Sacred Valley Hotel",
                "Tambomachay Hotel",
                "Belmond Hotel Monasterio",
                "Sol y Luna Hotel"
        ));
        ArrayAdapter<String> adapterHoteles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoteles);
        adapterHoteles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHotel.setAdapter(adapterHoteles);
        spHotel.setSelection(0); // Select "Seleccione hotel" by default

        // Manejo de la fecha de nacimiento (DatePickerDialog)
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
        etFechaNacimiento.setFocusable(false); // Make it non-editable to force DatePickerDialog
        etFechaNacimiento.setCursorVisible(false);

        // Manejo del clic en la ImageView para la foto de perfil
        ivFotoPerfil.setOnClickListener(v -> mostrarOpcionesImagen());

        // Botón de Registrar
        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        if (btnRegistrar != null) {
            btnRegistrar.setOnClickListener(v -> mostrarDialogoConfirmacion());
        }

        // Configuración del CardView de perfil (este cardview es de ejemplo,
        // no debería ser interactuable en un formulario de registro)
        CardView cardPerfil = findViewById(R.id.cardPerfil);
        if (cardPerfil != null) {
            // Se recomienda eliminar el setOnClickListener o cambiar su comportamiento
            // ya que este formulario es para REGISTRAR, no para ver el perfil del superadmin.
            cardPerfil.setOnClickListener(v -> {
                // Aquí podrías no hacer nada o simplemente loggear que fue clickeado
                Log.d(TAG, "Card de perfil clickeado en formulario de registro. Esto no debería redirigir.");
            });
        }

        // BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_hoteles) {
                    startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperActivity.class));
                    finish(); // To prevent stacking activities
                    return true;
                } else if (itemId == R.id.nav_usuarios) {
                    // Si ya estamos en una actividad relacionada con usuarios (este formulario),
                    // podríamos simplemente cerrar este formulario y regresar a la lista de admins.
                    // O si quieres que nav_usuarios siempre abra la lista, haz esto:
                    Intent intent = new Intent(SuperDetallesAdminFormularioActivity.this, SuperListaAdminActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_eventos) {
                    startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperEventosActivity.class));
                    finish(); // To prevent stacking activities
                    return true;
                }
                return false;
            });
        }
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
            etFechaNacimiento.setText(fecha);
        }, año, mes, día);
        datePicker.show();
    }

    private void mostrarOpcionesImagen() {
        final CharSequence[] opciones = {"Tomar Foto", "Elegir de Galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir Foto");
        builder.setItems(opciones, (dialog, item) -> {
            if (opciones[item].equals("Tomar Foto")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(takePictureIntent);
                } else {
                    Toast.makeText(this, "No hay aplicación de cámara disponible.", Toast.LENGTH_SHORT).show();
                }
            } else if (opciones[item].equals("Elegir de Galería")) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryLauncher.launch(pickPhotoIntent);
            } else if (opciones[item].equals("Cancelar")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void mostrarDialogoConfirmacion() {
        if (!validarCampos()) {
            // Toast message already shown by validarCampos for specific errors
            return; // Do not show the dialog if fields are not valid
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Registro")
                .setMessage("¿Estás seguro de registrar este administrador?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> registrarAdministrador())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void registrarAdministrador() {
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        String tipoDocumento = spTipoDocumento.getSelectedItem().toString();
        String numeroDocumento = etNumDocumento.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String hotelAsignado = spHotel.getSelectedItem().toString();

        CloudinaryManager.init(this); // Asegurar inicialización

        if (ivFotoPerfil.getDrawable() != null && ivFotoPerfil.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) ivFotoPerfil.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            MediaManager.get().upload(imageData)
                    .option("resource_type", "image")
                    .option("folder", "Administradores")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {}

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            guardarAdministradorEnFirestore(nombre, apellido, tipoDocumento, numeroDocumento,
                                    fechaNacimiento, correo, telefono, direccion,
                                    hotelAsignado, imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e(TAG, "Error al subir imagen: " + error.getDescription());
                            Toast.makeText(SuperDetallesAdminFormularioActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    })
                    .dispatch();
        } else {
            guardarAdministradorEnFirestore(nombre, apellido, tipoDocumento, numeroDocumento,
                    fechaNacimiento, correo, telefono, direccion,
                    hotelAsignado, ""); // Sin imagen
        }
    }



    private boolean validarCampos() {
        boolean isValid = true;

        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Este campo es obligatorio");
            isValid = false;
        } else {
            etNombre.setError(null); // Clear error if valid
        }

        if (etApellido.getText().toString().trim().isEmpty()) {
            etApellido.setError("Este campo es obligatorio");
            isValid = false;
        } else {
            etApellido.setError(null);
        }

        // Validación para Spinner de Tipo de Documento
        if (spTipoDocumento.getSelectedItemPosition() == 0) { // "Seleccione tipo"
            // Mejorar la forma de mostrar error para spinners
            Toast.makeText(this, "Por favor, seleccione un tipo de documento.", Toast.LENGTH_SHORT).show();
            // Puedes intentar cambiar el color del texto del spinner si quieres,
            // pero setError en el TextView interno es más común para EditText.
            // Para spinners, un Toast es una forma efectiva de notificar al usuario.
            isValid = false;
        }

        if (etNumDocumento.getText().toString().trim().isEmpty()) {
            etNumDocumento.setError("Este campo es obligatorio");
            isValid = false;
        } else {
            etNumDocumento.setError(null);
        }

        if (etFechaNacimiento.getText().toString().trim().isEmpty()) {
            etFechaNacimiento.setError("Seleccione una fecha");
            isValid = false;
        } else {
            etFechaNacimiento.setError(null);
        }

        if (etCorreo.getText().toString().trim().isEmpty()) {
            etCorreo.setError("Este campo es obligatorio");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString().trim()).matches()) {
            etCorreo.setError("Ingrese un correo válido");
            isValid = false;
        } else {
            etCorreo.setError(null);
        }

        if (etTelefono.getText().toString().trim().isEmpty()) {
            etTelefono.setError("Este campo es obligatorio");
            isValid = false;
        } else {
            etTelefono.setError(null);
        }

        if (etDireccion.getText().toString().trim().isEmpty()) {
            etDireccion.setError("Este campo es obligatorio");
            isValid = false;
        } else {
            etDireccion.setError(null);
        }

        // Validación para Spinner de Hotel
        if (spHotel.getSelectedItemPosition() == 0) { // "Seleccione hotel"
            // Mejorar la forma de mostrar error para spinners
            Toast.makeText(this, "Por favor, seleccione un hotel.", Toast.LENGTH_SHORT).show();
            isValid = false;
        }


        if (!isValid) {
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios y válidos.", Toast.LENGTH_LONG).show();
        }
        return isValid;
    }
    private void guardarAdministradorEnFirestore(String nombre, String apellido, String tipoDocumento, String numeroDocumento,
                                                 String fechaNacimiento, String correo, String telefono, String direccion,
                                                 String hotelAsignado, String fotoPerfilUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> admin = new HashMap<>();
        admin.put("nombres", nombre);
        admin.put("apellidos", apellido);
        admin.put("tipoDocumento", tipoDocumento);
        admin.put("numeroDocumento", numeroDocumento);
        admin.put("nacimiento", fechaNacimiento);
        admin.put("correo", correo);
        admin.put("telefono", telefono);
        admin.put("direccion", direccion);
        admin.put("hotelAsignado", hotelAsignado);
        admin.put("fotoPerfilUrl", fotoPerfilUrl);
        admin.put("estado", "true");

        db.collection("admins")
                .add(admin)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Administrador registrado correctamente", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("action", "registrado");
                    intent.putExtra("admin_nombres", nombre);
                    intent.putExtra("admin_apellidos", apellido);
                    setResult(RESULT_OK, intent); // <-- DEBES incluir el intent aquí

                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al registrar administrador: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}