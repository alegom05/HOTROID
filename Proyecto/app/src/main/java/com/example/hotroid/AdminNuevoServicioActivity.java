package com.example.hotroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AdminNuevoServicioActivity extends AppCompatActivity {
    private EditText etNombreServicio, etDescripcion, etPrecio;
    private static final int PICK_IMAGES_REQUEST = 1;
    private ArrayList<Uri> imagenesSeleccionadas = new ArrayList<>();
    private LinearLayout contenedorImagenes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_nuevo_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);

        // Inicializar contenedor de imágenes
        contenedorImagenes = findViewById(R.id.contenedorImagenes);

        // Botón para seleccionar imágenes
        findViewById(R.id.btnSeleccionarImagenes).setOnClickListener(v -> {
            if (imagenesSeleccionadas.size() >= 4) {
                Toast.makeText(this, "Máximo 4 imágenes permitidas", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addCategory(Intent.CATEGORY_OPENABLE); // Recomendado
            startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"), PICK_IMAGES_REQUEST);
        });



        // Acción del botón Guardar
        findViewById(R.id.btnGuardarHabitacion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí puedes agregar la lógica para guardar los datos
                String nombreServicio = etNombreServicio.getText().toString();
                String descripcion = etDescripcion.getText().toString();
                String precio = etPrecio.getText().toString();

                // Validación simple
                if (nombreServicio.isEmpty() || descripcion.isEmpty() || precio.isEmpty() || imagenesSeleccionadas.isEmpty()) {
                    Toast.makeText(AdminNuevoServicioActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
                } else {
                    // Aquí puedes guardar los datos en una base de datos o realizar la acción correspondiente
                    Toast.makeText(AdminNuevoServicioActivity.this, "Servicio registrado con éxito.", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("nombre", nombreServicio);
                    resultIntent.putExtra("descripcion", descripcion);
                    resultIntent.putExtra("precio", precio);

                    // Convertir URIs a Strings porque Intent no admite ArrayList<Uri> directamente
                    ArrayList<String> uriStrings = new ArrayList<>();
                    for (Uri uri : imagenesSeleccionadas) {
                        uriStrings.add(uri.toString());
                    }
                    resultIntent.putStringArrayListExtra("imagenes", uriStrings);

                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count && imagenesSeleccionadas.size() < 4; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        imagenesSeleccionadas.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    if (imagenesSeleccionadas.size() < 4) {
                        grantUriPermission(getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        imagenesSeleccionadas.add(imageUri);
                    }
                }
                mostrarImagenesSeleccionadas();
            }
        }
    }

    private void mostrarImagenesSeleccionadas() {
        contenedorImagenes.removeAllViews();

        for (Uri uri : imagenesSeleccionadas) {
            ImageView imageView = new ImageView(this);
            imageView.setImageURI(uri);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 200);
            params.setMargins(8, 8, 8, 8);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            contenedorImagenes.addView(imageView);
        }
    }


}