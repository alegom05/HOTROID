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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
    // No necesitamos imageUri si siempre convertimos a byte[] antes de enviar.
    // Solo si quisieras persistir la URI temporalmente por alguna razón.
    // private Uri imageUri;

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
        List<String> tiposDocumento = Arrays.asList("DNI", "Pasaporte", "Carnet de Extranjería");
        ArrayAdapter<String> adapterDocumento = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiposDocumento);
        adapterDocumento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoDocumento.setAdapter(adapterDocumento);

        // *** CORRECCIÓN: Lista de Hoteles
        List<String> hoteles = Arrays.asList(
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
        );
        ArrayAdapter<String> adapterHoteles = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoteles);
        adapterHoteles.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHotel.setAdapter(adapterHoteles);

        // Manejo de la fecha de nacimiento (DatePickerDialog)
        etFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());

        // Manejo del clic en la ImageView para la foto de perfil
        ivFotoPerfil.setOnClickListener(v -> mostrarOpcionesImagen());

        // Botón de Registrar
        Button btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(v -> mostrarDialogoConfirmacion());

        // Configuración del CardView de perfil (el de "Pedro Bustamante")
        CardView cardPerfil = findViewById(R.id.cardPerfil);
        cardPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(SuperDetallesAdminFormularioActivity.this, SuperCuentaActivity.class);
            startActivity(intent);
        });

        // BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperActivity.class));
                finish(); // Para que no se apilen las actividades
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                return true; // Ya estás en esta actividad o una relacionada
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperDetallesAdminFormularioActivity.this, SuperEventosActivity.class));
                finish(); // Para que no se apilen las actividades
                return true;
            }
            return false;
        });
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
            Toast.makeText(this, "Por favor, complete todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return; // No mostrar el diálogo si los campos no son válidos
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
        // En este punto, los campos ya han sido validados por mostrarDialogoConfirmacion()
        String nombre = etNombre.getText().toString().trim();
        String apellido = etApellido.getText().toString().trim();
        // String tipoDocumento = spTipoDocumento.getSelectedItem().toString(); // No se usa en Admin, pero puedes pasarlo si lo necesitas
        String numDocumento = etNumDocumento.getText().toString().trim();
        String fechaNacimiento = etFechaNacimiento.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String direccion = etDireccion.getText().toString().trim();
        String hotelAsignado = spHotel.getSelectedItem().toString();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("action", "registrado");
        resultIntent.putExtra("admin_nombres", nombre);
        resultIntent.putExtra("admin_apellidos", apellido);
        resultIntent.putExtra("admin_dni", numDocumento); // Asumiendo DNI es el número de documento
        resultIntent.putExtra("admin_nacimiento", fechaNacimiento);
        resultIntent.putExtra("admin_correo", correo);
        resultIntent.putExtra("admin_telefono", telefono);
        resultIntent.putExtra("admin_direccion", direccion);
        resultIntent.putExtra("admin_hotelAsignado", hotelAsignado);
        resultIntent.putExtra("admin_estado", "true"); // Por defecto, al registrar es activo

        // Obtener la imagen de la ImageView y convertirla a byte[]
        // Verificar que hay una imagen válida antes de enviarla
        if (ivFotoPerfil.getDrawable() != null && ivFotoPerfil.getDrawable() instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) ivFotoPerfil.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); // Comprimir a 80% de calidad
            byte[] imageData = baos.toByteArray();
            resultIntent.putExtra("admin_fotoPerfilBytes", imageData);
        } else {
            // Si no hay foto seleccionada, puedes enviar un valor nulo o un indicador
            resultIntent.putExtra("admin_fotoPerfilBytes", (byte[]) null);
            Log.d(TAG, "No se seleccionó ninguna foto de perfil.");
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean validarCampos() {
        boolean isValid = true;
        if (etNombre.getText().toString().trim().isEmpty()) {
            etNombre.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etApellido.getText().toString().trim().isEmpty()) {
            etApellido.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etNumDocumento.getText().toString().trim().isEmpty()) {
            etNumDocumento.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etFechaNacimiento.getText().toString().trim().isEmpty()) {
            etFechaNacimiento.setError("Seleccione una fecha");
            isValid = false;
        }
        if (etCorreo.getText().toString().trim().isEmpty()) {
            etCorreo.setError("Este campo es obligatorio");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString().trim()).matches()) {
            etCorreo.setError("Ingrese un correo válido");
            isValid = false;
        }
        if (etTelefono.getText().toString().trim().isEmpty()) {
            etTelefono.setError("Este campo es obligatorio");
            isValid = false;
        }
        if (etDireccion.getText().toString().trim().isEmpty()) {
            etDireccion.setError("Este campo es obligatorio");
            isValid = false;
        }
        // Puedes añadir validación para la foto si es obligatoria
        // if (ivFotoPerfil.getDrawable() == null || !(ivFotoPerfil.getDrawable() instanceof BitmapDrawable)) {
        //     Toast.makeText(this, "Debe seleccionar una foto de perfil.", Toast.LENGTH_SHORT).show();
        //     isValid = false;
        // }
        return isValid;
    }
}