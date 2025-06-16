package com.example.hotroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.hotroid.bean.Admin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class SuperDetallesAdminActivity extends AppCompatActivity {

    private String adminFirestoreId;
    private Admin currentAdmin;
    private TextView tvUsuarioDetalle;
    private TextView tvApellidosDetalle;
    private TextView tvTipoDocumentoDetalle; // Nuevo TextView para Tipo de Documento
    private TextView tvNumeroDocumentoDetalle; // Nuevo TextView para Número de Documento
    private TextView tvFechaNacimientoDetalle;
    private TextView tvCorreoDetalle;
    private TextView tvTelefonoDetalle;
    private TextView tvDomicilioDetalle;
    private TextView tvHotelDetalle;
    private TextView tvNombreAdminActual;
    private ImageView fotoPrincipal;

    private Spinner spSeleccionarHotel;
    private Button btnAsignarHotelActivar;
    private Button btnDesactivar;
    private Button btnActivar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.super_detalles_admin);

        Intent incomingIntent = getIntent();
        adminFirestoreId = incomingIntent.getStringExtra("admin_firestore_id");

        String nombres = incomingIntent.getStringExtra("admin_nombres");
        String apellidos = incomingIntent.getStringExtra("admin_apellidos");
        String estado = incomingIntent.getStringExtra("admin_estado");
        // CAMBIOS AQUÍ: Recibir tipoDocumento y numeroDocumento en lugar de dni
        String tipoDocumento = incomingIntent.getStringExtra("admin_tipo_documento");
        String numeroDocumento = incomingIntent.getStringExtra("admin_numero_documento");
        String nacimiento = incomingIntent.getStringExtra("admin_nacimiento");
        String correo = incomingIntent.getStringExtra("admin_correo");
        String telefono = incomingIntent.getStringExtra("admin_telefono");
        String direccion = incomingIntent.getStringExtra("admin_direccion");
        String hotelAsignado = incomingIntent.getStringExtra("admin_hotelAsignado");
        String fotoPerfilUrl = incomingIntent.getStringExtra("admin_fotoPerfilUrl");

        // CAMBIOS AQUÍ: Usar el constructor de Admin con los nuevos campos
        currentAdmin = new Admin(nombres, apellidos, estado, tipoDocumento, numeroDocumento, nacimiento, correo, telefono, direccion, hotelAsignado);
        currentAdmin.setFirestoreId(adminFirestoreId);
        currentAdmin.setFotoPerfilUrl(fotoPerfilUrl);

        // Referenciar los TextViews y botones
        tvNombreAdminActual = findViewById(R.id.tvNombreAdminActual);
        tvNombreAdminActual.setText("Pedro Bustamante"); // Se mantiene para el Superadmin

        tvUsuarioDetalle = findViewById(R.id.tvUsuarioDetalle);
        tvApellidosDetalle = findViewById(R.id.tvApellidosDetalle);
        // CAMBIOS AQUÍ: Referenciar los nuevos TextViews
        tvTipoDocumentoDetalle = findViewById(R.id.tvTipoDocumentoDetalle);
        tvNumeroDocumentoDetalle = findViewById(R.id.tvNumeroDocumentoDetalle);
        tvFechaNacimientoDetalle = findViewById(R.id.tvFechaNacimientoDetalle);
        tvCorreoDetalle = findViewById(R.id.tvCorreoDetalle);
        tvTelefonoDetalle = findViewById(R.id.tvTelefonoDetalle);
        tvDomicilioDetalle = findViewById(R.id.tvDomicilioDetalle);
        tvHotelDetalle = findViewById(R.id.tvHotelDetalle);
        fotoPrincipal = findViewById(R.id.fotoPrincipal);

        spSeleccionarHotel = findViewById(R.id.spSeleccionarHotel);
        btnAsignarHotelActivar = findViewById(R.id.btnAsignarHotelActivar);
        btnDesactivar = findViewById(R.id.btnDesactivar);
        btnActivar = findViewById(R.id.btnActivar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.hotel_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSeleccionarHotel.setAdapter(adapter);

        updateUIWithAdminData();

        btnDesactivar.setOnClickListener(v -> {
            showConfirmationDialog("desactivar", "desactivar");
        });

        btnActivar.setOnClickListener(v -> {
            showConfirmationDialog("activar", "activar");
        });

        btnAsignarHotelActivar.setOnClickListener(v -> {
            String selectedHotel = spSeleccionarHotel.getSelectedItem().toString();
            if (selectedHotel.equals(getString(R.string.seleccione_el_hotel)) || selectedHotel.isEmpty()) {
                Toast.makeText(this, "Por favor, seleccione un hotel", Toast.LENGTH_SHORT).show();
            } else {
                showConfirmationDialog("activar", "asignar y activar");
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_usuarios);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_hoteles) {
                startActivity(new Intent(SuperDetallesAdminActivity.this, SuperActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_usuarios) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            } else if (itemId == R.id.nav_eventos) {
                startActivity(new Intent(SuperDetallesAdminActivity.this, SuperEventosActivity.class));
                finish();
                return true;
            }
            return false;
        });

        CardView cardPerfil = findViewById(R.id.cardPerfil);
        cardPerfil.setOnClickListener(v -> {
            startActivity(new Intent(SuperDetallesAdminActivity.this, SuperCuentaActivity.class));
        });
    }

    private void updateUIWithAdminData() {
        if (currentAdmin != null) {
            tvUsuarioDetalle.setText(currentAdmin.getNombres());
            tvApellidosDetalle.setText(currentAdmin.getApellidos());
            // CAMBIOS AQUÍ: Mostrar tipoDocumento y numeroDocumento
            tvTipoDocumentoDetalle.setText(currentAdmin.getTipoDocumento());
            tvNumeroDocumentoDetalle.setText(currentAdmin.getNumeroDocumento());

            tvFechaNacimientoDetalle.setText(currentAdmin.getNacimiento());
            tvCorreoDetalle.setText(currentAdmin.getCorreo());
            tvTelefonoDetalle.setText(currentAdmin.getTelefono());
            tvDomicilioDetalle.setText(currentAdmin.getDireccion());
            tvHotelDetalle.setText(currentAdmin.getHotelAsignado());

            if (currentAdmin.getFotoPerfilUrl() != null && !currentAdmin.getFotoPerfilUrl().isEmpty()) {
                Glide.with(this)
                        .load(currentAdmin.getFotoPerfilUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_user_placeholder)
                        .error(R.drawable.ic_user_error)
                        .into(fotoPrincipal);
            } else {
                fotoPrincipal.setImageResource(R.drawable.ic_user_placeholder);
            }

            boolean isActivado = Boolean.parseBoolean(currentAdmin.getEstado());

            if (isActivado) {
                btnDesactivar.setVisibility(View.VISIBLE);
                btnActivar.setVisibility(View.GONE);
                spSeleccionarHotel.setVisibility(View.GONE);
                btnAsignarHotelActivar.setVisibility(View.GONE);
            } else {
                btnDesactivar.setVisibility(View.GONE);
                if (currentAdmin.getHotelAsignado().equals("-") || currentAdmin.getHotelAsignado().isEmpty()) {
                    tvHotelDetalle.setText("No Asignado");
                    spSeleccionarHotel.setVisibility(View.VISIBLE);
                    btnAsignarHotelActivar.setVisibility(View.VISIBLE);
                    btnActivar.setVisibility(View.GONE);
                } else {
                    spSeleccionarHotel.setVisibility(View.GONE);
                    btnAsignarHotelActivar.setVisibility(View.GONE);
                    btnActivar.setVisibility(View.VISIBLE);
                }
            }

            if (isActivado && !currentAdmin.getHotelAsignado().equals("-")) {
                int spinnerPosition = ((ArrayAdapter)spSeleccionarHotel.getAdapter()).getPosition(currentAdmin.getHotelAsignado());
                if (spinnerPosition != -1) {
                    spSeleccionarHotel.setSelection(spinnerPosition);
                }
            }
        }
    }

    private void showConfirmationDialog(String actionType, String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Acción");
        builder.setMessage("¿Está seguro que desea " + buttonText + " a " + currentAdmin.getNombres() + " " + currentAdmin.getApellidos() + "?");

        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("desactivar".equals(actionType)) {
                    returnResult("desactivado", currentAdmin.getFirestoreId(), currentAdmin.getNombres(), currentAdmin.getApellidos(), null);
                } else if ("activar".equals(actionType)) {
                    String hotelToAssign = currentAdmin.getHotelAsignado();
                    if (spSeleccionarHotel.getVisibility() == View.VISIBLE) {
                        hotelToAssign = spSeleccionarHotel.getSelectedItem().toString();
                    }
                    returnResult("activado", currentAdmin.getFirestoreId(), currentAdmin.getNombres(), currentAdmin.getApellidos(), hotelToAssign);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void returnResult(String action, String firestoreId, String adminNombres, String adminApellidos, String hotelAsignado) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("action", action);
        resultIntent.putExtra("admin_firestore_id", firestoreId);
        resultIntent.putExtra("admin_nombres", adminNombres);
        resultIntent.putExtra("admin_apellidos", adminApellidos);
        // CAMBIOS AQUÍ: También debes enviar el tipo y número de documento si son relevantes al regresar.
        // Si no se modifican en esta vista, no es estrictamente necesario enviarlos de vuelta,
        // pero es una buena práctica si la vista que llama podría necesitarlos actualizados.
        resultIntent.putExtra("admin_tipo_documento", currentAdmin.getTipoDocumento());
        resultIntent.putExtra("admin_numero_documento", currentAdmin.getNumeroDocumento());

        if (hotelAsignado != null) {
            resultIntent.putExtra("nuevo_hotel_asignado", hotelAsignado);
        }
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}