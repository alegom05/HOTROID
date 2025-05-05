package com.example.hotroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class AdminEditarServicioActivity extends AppCompatActivity {
    private static final int PICK_IMAGES_REQUEST = 1;
    private EditText etNombreServicio, etDescripcion, etPrecio;
    private ArrayList<Uri> imagenesSeleccionadas = new ArrayList<>();
    private LinearLayout contenedorImagenes;
    private boolean estadoHabilitado = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_editar_servicio);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Inicializar los campos
        etNombreServicio = findViewById(R.id.etNombreServicio);
        etDescripcion = findViewById(R.id.etDescripcion);
        etPrecio = findViewById(R.id.etPrecio);
        contenedorImagenes = findViewById(R.id.contenedorImagenes);
        estadoHabilitado = getIntent().getBooleanExtra("habilitado", true);
        Button btnDeshabilitar = findViewById(R.id.btnDeshabilitarServicio);
        actualizarTextoEstado(btnDeshabilitar);


        // Obtener los datos de la habitación pasados a través del Intent
        String ServicioNombre = getIntent().getStringExtra("Service_name");
        String Descripcion = getIntent().getStringExtra("Service_description");
        String precioValue = getIntent().getStringExtra("price");
        ArrayList<String> uriStrings = getIntent().getStringArrayListExtra("imagenes");

        // Establecer los datos en los campos EditText
        etNombreServicio.setText(ServicioNombre);
        etDescripcion.setText(Descripcion);
        etPrecio.setText(precioValue);
        // Convertir URIs
        if (uriStrings != null) {
            for (String uriStr : uriStrings) {
                imagenesSeleccionadas.add(Uri.parse(uriStr));
            }
        }
        mostrarImagenesSeleccionadas();

        // Seleccionar nuevas imágenes
        findViewById(R.id.btnSeleccionarImagenes).setOnClickListener(v -> {
            if (imagenesSeleccionadas.size() >= 4) {
                Toast.makeText(this, "Máximo 4 imágenes permitidas", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Selecciona hasta 4 imágenes"), PICK_IMAGES_REQUEST);
        });
        findViewById(R.id.btnLimpiarImagenes).setOnClickListener(v -> {
            imagenesSeleccionadas.clear();
            contenedorImagenes.removeAllViews();
            Toast.makeText(this, "Imágenes eliminadas. Puedes seleccionar nuevas.", Toast.LENGTH_SHORT).show();
        });


        // Acción para guardar los cambios
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> {
            // Aquí puedes agregar la lógica para guardar los datos actualizados
            String NombreServicio = etNombreServicio.getText().toString();
            String DescripcionServicio = etDescripcion.getText().toString();
            String precio = etPrecio.getText().toString();

            // Validación simple
            if (NombreServicio.isEmpty() || DescripcionServicio.isEmpty() || precio.isEmpty() || imagenesSeleccionadas.isEmpty()) {
                Toast.makeText(AdminEditarServicioActivity.this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show();
            } else {
                // Aquí puedes guardar los datos actualizados en una base de datos o realizar la acción correspondiente
                Toast.makeText(AdminEditarServicioActivity.this, "Servicio actualizado con éxito.", Toast.LENGTH_SHORT).show();
                // Redirigir a la actividad AdminHabitacionesActivity después de guardar
                Intent result = new Intent();
                result.putExtra("nombre", NombreServicio);
                result.putExtra("descripcion", DescripcionServicio);
                result.putExtra("precio", precio);

                ArrayList<String> imagenesString = new ArrayList<>();
                for (Uri uri : imagenesSeleccionadas) {
                    imagenesString.add(uri.toString());
                }
                result.putStringArrayListExtra("imagenes", imagenesString);
                result.putExtra("habilitado", estadoHabilitado);  // 👈 ESTA LÍNEA FALTABA
                setResult(RESULT_OK, result);
                finish();  // Finaliza la actividad actual para evitar que el usuario regrese a ella con el botón de atrás
            }
        });
        btnDeshabilitar.setOnClickListener(v -> {
            estadoHabilitado = !estadoHabilitado;
            actualizarTextoEstado(btnDeshabilitar);
        });



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // BottomNavigationView o Barra inferior de menú
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




    // Utilidad: crear fila nueva
    private LinearLayout crearNuevaFila() {
        LinearLayout fila = new LinearLayout(this);
        fila.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setClipChildren(false);
        fila.setClipToPadding(false);
        return fila;
    }
    private void actualizarTextoEstado(Button btn) {
        if (estadoHabilitado) {
            btn.setText("Habilitado");
            btn.setBackgroundTintList(getResources().getColorStateList(R.color.color_estado_habilitado));
        } else {
            btn.setText("Deshabilitado");
            btn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count && imagenesSeleccionadas.size() < 4; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        imagenesSeleccionadas.add(uri);
                    }
                } else if (data.getData() != null) {
                    if (imagenesSeleccionadas.size() < 4) {
                        imagenesSeleccionadas.add(data.getData());
                    }
                }
                mostrarImagenesSeleccionadas();
            }
        }
    }
}