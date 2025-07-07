package com.example.hotroid;

//import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InfoAccountUser extends AppCompatActivity {
    private TextView tvNombre, tvCorreo, tvTelefono, tvDni, tvNacimiento;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String uid;
    private TextView tvUserName, tvUserCorreo;
//    El DNI solo se puede editar si está vacío o null en Firestore y debe tener entre 7 y 11 dígitos numéricos.
//    private boolean dniEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.user_info_account);
        // Para el layout general
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        // Para el BottomNavigationView
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation), (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(0, 0, 0, bottomInset);
            return insets;
        });

        // Referencias
        tvNombre = findViewById(R.id.tv_full_name);
        tvCorreo = findViewById(R.id.tv_email);
        tvTelefono = findViewById(R.id.tv_phone);
        tvDni = findViewById(R.id.tv_dni);
        tvNacimiento = findViewById(R.id.tv_birth_date);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserCorreo = findViewById(R.id.tv_user_email);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        MaterialButton btnGuardar = findViewById(R.id.btn_save_changes);
        //para el menu inferior
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        configurarBottomNavigation(bottomNavigationView, this);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = currentUser.getUid();

        cargarDatosUsuario();

        // Listeners para editar campos
        findViewById(R.id.card_full_name).setOnClickListener(v -> mostrarDialogoEditar("Nombre Completo", tvNombre));
        findViewById(R.id.card_email).setOnClickListener(v -> Toast.makeText(this, "El correo no puede ser editado", Toast.LENGTH_SHORT).show());
        findViewById(R.id.card_phone).setOnClickListener(v -> mostrarDialogoEditar("Teléfono", tvTelefono));
        findViewById(R.id.card_dni).setOnClickListener(v -> mostrarDialogoEditar("DNI", tvDni));
//        findViewById(R.id.card_dni).setOnClickListener(v -> {
//            if (dniEditable) {
//                mostrarDialogoEditar("DNI", tvDni);
//            } else {
//                Toast.makeText(this, "El DNI no puede ser modificado", Toast.LENGTH_SHORT).show();
//            }
//        });
        findViewById(R.id.card_birth_date).setOnClickListener(v -> mostrarDialogoEditar("Fecha de Nacimiento", tvNacimiento));

        btnGuardar.setOnClickListener(v -> guardarCambios());

    }

    private void configurarBottomNavigation(BottomNavigationView bottomNavigationView, Context context) {
        // Desmarca todos los ítems visualmente
        bottomNavigationView.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            bottomNavigationView.getMenu().getItem(i).setChecked(false);
        }
        bottomNavigationView.getMenu().setGroupCheckable(0, true, true);

        // Listener de navegación
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles_user) {
                // Ir directamente a ClienteActivity que carga HotelesFragment por defecto
                startActivity(new Intent(this, ClienteActivity.class));
                finish();
                return true;
            }else if (itemId == R.id.nav_reservas_user) {
                Intent intent = new Intent(this, MisReservasUser.class);
                startActivity(intent);
                finish(); // Añadido finish() para no acumular activities
                return true; // Cambié a true para mantener seleccionado
            } else if (itemId == R.id.nav_chat_user) {
                // Ir a ClienteActivity y especificar cargar ChatFragment
                Intent intent = new Intent(this, ClienteActivity.class);
                intent.putExtra("fragment_destino", "chat");
                startActivity(intent);
                finish();
                return true;
            }else if (itemId == R.id.nav_cuenta) {
                // Ir a ClienteActivity y especificar cargar CuentaFragment
                Intent intent = new Intent(this, ClienteActivity.class);
                intent.putExtra("fragment_destino", "cuenta");
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void cargarDatosUsuario() {
        db.collection("usuarios").document(uid)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvNombre.setText(obtenerValor(documentSnapshot, "nombre") + " " + obtenerValor(documentSnapshot, "apellido"));
                        tvCorreo.setText(obtenerValor(documentSnapshot, "correo"));
                        tvTelefono.setText(obtenerValor(documentSnapshot, "telefono"));
                        tvDni.setText(obtenerValor(documentSnapshot, "dni"));
                        //String dni = obtenerValor(documentSnapshot, "dni");                        tvNacimiento.setText(obtenerValor(documentSnapshot, "fechaNacimiento"));
                        tvUserName.setText(obtenerValor(documentSnapshot, "nombre"));
                        tvUserCorreo.setText(obtenerValor(documentSnapshot,"correo"));
                        //tvDni.setText(dni);

                        // Habilitar edición del DNI solo si está vacío o null
//                        dniEditable = (dni == null || dni.trim().isEmpty());
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show());
    }

    private String obtenerValor(DocumentSnapshot doc, String key) {
        String valor = doc.getString(key);
        return (valor == null || valor.trim().isEmpty()) ? "" : valor.trim();
    }

    private void mostrarDialogoEditar(String titulo, TextView textView) {
        // Crear el layout del diálogo programáticamente
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(32, 24, 32, 16);

        // Título personalizado
        TextView titleView = new TextView(this);
        titleView.setText("Editar " + titulo);
        titleView.setTextSize(20);
        titleView.setTextColor(ContextCompat.getColor(this, R.color.black));
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setPadding(0, 0, 0, 24);

        // TextInputLayout con Material Design
        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setHint(titulo);
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxCornerRadii(12, 12, 12, 12);
        textInputLayout.setHintTextColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.naranjaTransparen)));
        textInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.naranjaTransparen));

        // EditText dentro del TextInputLayout
        TextInputEditText editText = new TextInputEditText(this);
        editText.setText(textView.getText());
        editText.setTextSize(16);
        editText.setTextColor(ContextCompat.getColor(this, R.color.black));
        editText.setSelectAllOnFocus(true);

        // Configurar el tipo de input según el campo
        if (titulo.contains("Correo")) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else if (titulo.contains("Teléfono")) {
            editText.setInputType(InputType.TYPE_CLASS_PHONE);
        } else if (titulo.contains("DNI")) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (titulo.contains("Fecha")) {
            editText.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
            editText.setFocusable(false);
            editText.setClickable(true);
            editText.setOnClickListener(v -> mostrarDatePicker(editText));
        }

        textInputLayout.addView(editText);

        // Agregar views al layout
        dialogLayout.addView(titleView);
        dialogLayout.addView(textInputLayout);

        // Crear el diálogo con Material Design
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setView(dialogLayout);

        // Botones personalizados
        builder.setPositiveButton("GUARDAR", null); // Se configurará después
        builder.setNegativeButton("CANCELAR", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        // Personalizar el diálogo después de crearlo
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Estilo de los botones
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.naranjaTransparen));
            negativeButton.setTextColor(ContextCompat.getColor(this, R.color.plomoTransparen));

            // Acción del botón guardar
            positiveButton.setOnClickListener(v -> {
                String nuevoTexto = editText.getText().toString().trim();
                if (nuevoTexto.isEmpty()) {
                    textInputLayout.setError("Este campo no puede estar vacío");
                    return;
                }
                // Validación DNI
                if (titulo.contains("DNI")) {
                    if (!nuevoTexto.matches("\\d{7,11}")) {
                        textInputLayout.setError("DNI debe tener entre 7 y 11 dígitos");
                        return;
                    }
                }
                // Validación fecha de nacimiento
                if (titulo.contains("Fecha")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        Date fechaIngresada = sdf.parse(nuevoTexto);
                        if (fechaIngresada.after(new Date())) {
                            textInputLayout.setError("La fecha debe ser anterior a hoy");
                            return;
                        }
                    } catch (ParseException e) {
                        textInputLayout.setError("Formato inválido (dd/MM/yyyy)");
                        return;
                    }
                }

                // Si pasó todas las validaciones
                textView.setText(nuevoTexto);
                dialog.dismiss();
            });

            // Mostrar el teclado automáticamente
            if (!titulo.contains("Fecha")) {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Personalizar la ventana del diálogo
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();
    }

    // Método auxiliar para mostrar el DatePicker
    private void mostrarDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                    editText.setText(fecha);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
    private void guardarCambios() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", obtenerTextoLimpio(tvNombre.getText().toString().split(" ")[0]));
        datos.put("apellido", obtenerTextoLimpio(tvNombre.getText().toString().split(" ").length > 1 ? tvNombre.getText().toString().split(" ")[1] : ""));
        datos.put("correo", obtenerTextoLimpio(tvCorreo.getText().toString()));
        datos.put("telefono", obtenerTextoLimpio(tvTelefono.getText().toString()));
        datos.put("dni", obtenerTextoLimpio(tvDni.getText().toString()));
        datos.put("fechaNacimiento", obtenerTextoLimpio(tvNacimiento.getText().toString()));

        db.collection("usuarios").document(uid)
                .update(datos)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private String obtenerTextoLimpio(String texto) {
        return (texto == null) ? "" : texto.trim();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Acción cuando se toca la flecha de la toolbar
        return true;
    }

}