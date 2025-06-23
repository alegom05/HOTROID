package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.CheckoutFirebase;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.Cliente;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Date; // Asegúrate de que Date esté importado

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private List<CheckoutFirebase> checkoutList; // Esta lista ahora será la que se muestra y se filtra
    private List<CheckoutFirebase> originalCheckoutList; // Esta lista mantendrá todos los checkouts sin filtrar
    private FirebaseFirestore db;
    private List<Reserva> reservasList;
    private List<Cliente> clientesList;

    // Elementos del buscador
    private EditText etBuscador;
    private Button btnLimpiar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        checkoutList = new ArrayList<>();
        originalCheckoutList = new ArrayList<>(); // Inicializar la lista original
        reservasList = new ArrayList<>();
        clientesList = new ArrayList<>();

        rvCheckouts = findViewById(R.id.rvCheckouts);
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CheckoutAdapter(checkoutList); // Pasar la lista que se usará para mostrar
        rvCheckouts.setAdapter(adapter);

        // Inicializar elementos del buscador
        etBuscador = findViewById(R.id.etBuscador);
        btnLimpiar = findViewById(R.id.btnLimpiar);

        // Listener para el campo de búsqueda
        etBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No se necesita implementación
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrar la lista cada vez que el texto cambia
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No se necesita implementación
            }
        });

        // Listener para el botón "Limpiar"
        btnLimpiar.setOnClickListener(v -> {
            etBuscador.setText(""); // Limpiar el texto del buscador
            adapter.filterList(originalCheckoutList); // Restaurar la lista original sin filtrar
            checkoutList.clear(); // Limpiar la lista visible
            checkoutList.addAll(originalCheckoutList); // Añadir todos los elementos de la lista original
        });


        // Primero, carga reservas y clientes, luego genera y carga checkouts
        loadReservasAndClientesThenGenerateCheckouts();


        adapter.setOnItemClickListener(new CheckoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CheckoutFirebase seleccionado = checkoutList.get(position); // Obtener de la lista actual/filtrada

                Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
                intent.putExtra("ID_CHECKOUT", seleccionado.getIdCheckout());
                intent.putExtra("ROOM_NUMBER", seleccionado.getRoomNumber());
                intent.putExtra("CLIENT_NAME", seleccionado.getClientName());
                intent.putExtra("BASE_RATE", seleccionado.getBaseRate());
                intent.putExtra("ADDITIONAL_CHARGES", seleccionado.getAdditionalCharges());
                intent.putExtra("CHECKIN_DATE", seleccionado.getCheckinDate().getTime());
                intent.putExtra("CHECKOUT_DATE", seleccionado.getCheckoutDate().getTime());
                startActivity(intent);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_checkout);

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) {
                    Intent intentInicio = new Intent(AdminCheckout.this, AdminActivity.class);
                    startActivity(intentInicio);
                    finish(); // Añadir finish()
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminCheckout.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    finish(); // Añadir finish()
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    // Si ya estamos en checkout, no hacer nada o recargar la vista si es necesario
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentAlertas = new Intent(AdminCheckout.this, AdminReportes.class);
                    startActivity(intentAlertas);
                    finish(); // Añadir finish()
                    return true;
                }
                return false;
            });
        } else {
            Log.e("AdminCheckout", "BottomNavigationView con ID R.id.bottom_navigation no encontrada.");
        }
    }

    private void loadReservasAndClientesThenGenerateCheckouts() {
        db.collection("reservas")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            reservasList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reserva reserva = document.toObject(Reserva.class);
                                reservasList.add(reserva);
                            }
                            Log.d("AdminCheckout", "Reservas cargadas exitosamente: " + reservasList.size());
                            loadClientesThenGenerateCheckouts();
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de reservas: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar reservas: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadClientesThenGenerateCheckouts() {
        db.collection("clientes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            clientesList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Cliente cliente = document.toObject(Cliente.class);
                                cliente.setFirestoreId(document.getId());
                                clientesList.add(cliente);
                            }
                            Log.d("AdminCheckout", "Clientes cargados exitosamente: " + clientesList.size());
                            // Solo se debe descomentar esta línea para la primera ejecución si quieres generar nuevos checkouts
                            // generateAndSaveRandomCheckouts(); // Asegúrate de que este método exista y sea seguro contra duplicados
                            loadCheckoutsFromFirestore();
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de clientes: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar clientes: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Método para generar y guardar checkouts aleatorios
    private void generateAndSaveRandomCheckouts() {
        if (reservasList.isEmpty() || clientesList.isEmpty()) {
            Log.w("AdminCheckout", "No hay suficientes reservas o clientes para generar checkouts aleatorios.");
            Toast.makeText(AdminCheckout.this, "No hay reservas o clientes para generar checkouts.", Toast.LENGTH_SHORT).show();
            return;
        }

        Random random = new Random();
        int numberOfCheckoutsToGenerate = 5;

        for (int i = 0; i < numberOfCheckoutsToGenerate; i++) {
            Reserva randomReserva = reservasList.get(random.nextInt(reservasList.size()));
            Cliente randomCliente = clientesList.get(random.nextInt(clientesList.size()));

            String clientFullName = randomCliente.getNombres() + " " + randomCliente.getApellidos();

            CheckoutFirebase newCheckout = new CheckoutFirebase(
                    null, // El ID se generará automáticamente por Firestore
                    randomReserva.getRoomNumber(),
                    clientFullName,
                    randomReserva.getPrecioTotal(),
                    randomReserva.getCobrosAdicionales(),
                    randomReserva.getFechaInicio(),
                    randomReserva.getFechaFin()
            );

            // Guardar el checkout en Firestore
            db.collection("checkouts")
                    .add(newCheckout)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AdminCheckout", "Checkout generado y guardado con ID: " + documentReference.getId());
                        // No necesitas añadirlo a checkoutList aquí, ya que loadCheckoutsFromFirestore() lo hará.
                    })
                    .addOnFailureListener(e -> {
                        Log.w("AdminCheckout", "Error al añadir checkout", e);
                        Toast.makeText(AdminCheckout.this, "Error al generar checkout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Método para cargar checkouts existentes desde Firestore
    private void loadCheckoutsFromFirestore() {
        db.collection("checkouts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            originalCheckoutList.clear(); // Limpiar la lista original
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CheckoutFirebase checkout = document.toObject(CheckoutFirebase.class);
                                originalCheckoutList.add(checkout);
                            }
                            // Después de cargar, inicializa ambas listas y notifica al adaptador
                            checkoutList.clear();
                            checkoutList.addAll(originalCheckoutList); // Copiar todos a la lista visible
                            adapter.filterList(checkoutList); // Pasar la lista ya copiada al adaptador
                            Log.d("AdminCheckout", "Checkouts cargados: " + checkoutList.size());
                            if (checkoutList.isEmpty()) {
                                Toast.makeText(AdminCheckout.this, "No hay checkouts pendientes.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.w("AdminCheckout", "Error al cargar checkouts: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar checkouts: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Método de filtrado
    private void filter(String text) {
        List<CheckoutFirebase> filteredList = new ArrayList<>();
        for (CheckoutFirebase item : originalCheckoutList) { // Filtrar desde la lista original
            if (item.getClientName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        checkoutList.clear(); // Limpiar la lista visible
        checkoutList.addAll(filteredList); // Añadir los elementos filtrados
        adapter.filterList(filteredList); // Notificar al adaptador con la lista filtrada
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recargar los checkouts cada vez que la actividad vuelve a estar en primer plano
        // Esto es crucial para ver la lista actualizada después de una eliminación
        loadCheckoutsFromFirestore();
        etBuscador.setText(""); // Asegurarse de que el buscador esté limpio al regresar
    }
}