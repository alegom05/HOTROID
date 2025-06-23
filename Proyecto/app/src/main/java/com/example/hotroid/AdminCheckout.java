package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private List<CheckoutFirebase> checkoutList;
    private FirebaseFirestore db;
    private List<Reserva> reservasList;
    private List<Cliente> clientesList;

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
        reservasList = new ArrayList<>();
        clientesList = new ArrayList<>();

        rvCheckouts = findViewById(R.id.rvCheckouts);
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CheckoutAdapter(checkoutList);
        rvCheckouts.setAdapter(adapter);

        // ATENCIÓN: ESTA LÍNEA ESTÁ DESCOMENTADA PARA POBLAR FIRESTORE UNA VEZ
        // DEBES COMENTARLA DESPUÉS DE LA PRIMERA EJECUCIÓN EXITOSA.
        // saveInitialCheckoutsToFirestore(); // No veo este método definido, si lo tienes, asegúrate de que no genere duplicados.
        // El método 'generateAndSaveRandomCheckouts' está bien, pero descomenta solo cuando necesites.

        // Primero, carga reservas y clientes, luego genera y carga checkouts
        loadReservasAndClientesThenGenerateCheckouts();


        adapter.setOnItemClickListener(new CheckoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CheckoutFirebase seleccionado = checkoutList.get(position);

                Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
                intent.putExtra("ID_CHECKOUT", seleccionado.getIdCheckout()); // <--- AÑADIR ESTA LÍNEA
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
                            // generateAndSaveRandomCheckouts();
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
                            checkoutList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Aquí es donde el ID del documento se mapea automáticamente a 'idCheckout'
                                // gracias a @DocumentId en tu clase CheckoutFirebase.
                                CheckoutFirebase checkout = document.toObject(CheckoutFirebase.class);
                                checkoutList.add(checkout);
                            }
                            adapter.notifyDataSetChanged();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar los checkouts cada vez que la actividad vuelve a estar en primer plano
        // Esto es crucial para ver la lista actualizada después de una eliminación
        loadCheckoutsFromFirestore();
    }
}