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
import com.example.hotroid.bean.Reserva; // Importa la clase Reserva
import com.example.hotroid.bean.Cliente; // Importa la clase Cliente
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random; // Para la generación aleatoria
import java.util.Date; // Para manejar fechas

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private List<CheckoutFirebase> checkoutList;
    private FirebaseFirestore db;
    private List<Reserva> reservasList; // Lista para almacenar las reservas
    private List<Cliente> clientesList; // Lista para almacenar los clientes

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
        reservasList = new ArrayList<>(); // Inicializa la lista de reservas
        clientesList = new ArrayList<>(); // Inicializa la lista de clientes

        rvCheckouts = findViewById(R.id.rvCheckouts);
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CheckoutAdapter(checkoutList);
        rvCheckouts.setAdapter(adapter);

        // --- ATENCIÓN: ESTA LÍNEA ESTÁ DESCOMENTADA PARA POBLAR FIRESTORE UNA VEZ ---
        // DEBES COMENTARLA DESPUÉS DE LA PRIMERA EJECUCIÓN EXITOSA.
        //saveInitialCheckoutsToFirestore(); // <--- ¡COMENTA ESTA LÍNEA DESPUÉS DE LA PRIMERA EJECUCIÓN!

        // Primero, carga reservas y clientes, luego genera y carga checkouts
        loadReservasAndClientesThenGenerateCheckouts();

        adapter.setOnItemClickListener(new CheckoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CheckoutFirebase seleccionado = checkoutList.get(position);

                Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
                intent.putExtra("ROOM_NUMBER", seleccionado.getRoomNumber());
                intent.putExtra("CLIENT_NAME", seleccionado.getClientName());
                intent.putExtra("BASE_RATE", seleccionado.getBaseRate());
                intent.putExtra("ADDITIONAL_CHARGES", seleccionado.getAdditionalCharges());
                intent.putExtra("CHECKIN_DATE", seleccionado.getCheckinDate().getTime()); // Pasar como long
                intent.putExtra("CHECKOUT_DATE", seleccionado.getCheckoutDate().getTime()); // Pasar como long
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
                    return true;
                } else if (itemId == R.id.nav_taxistas) {
                    Intent intentUbicacion = new Intent(AdminCheckout.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    return true;
                } else if (itemId == R.id.nav_checkout) {
                    return true;
                } else if (itemId == R.id.nav_reportes) {
                    Intent intentAlertas = new Intent(AdminCheckout.this, AdminReportes.class);
                    startActivity(intentAlertas);
                    return true;
                }
                return false;
            });
        } else {
            Log.e("AdminCheckout", "BottomNavigationView con ID R.id.bottom_navigation no encontrada.");
        }
    }

    private void loadReservasAndClientesThenGenerateCheckouts() {
        // Cargar Reservas
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
                            // Una vez cargadas las reservas, cargar los clientes
                            loadClientesThenGenerateCheckouts();
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de reservas: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar reservas: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void loadClientesThenGenerateCheckouts() {
        // Cargar Clientes
        db.collection("clientes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            clientesList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Cliente cliente = document.toObject(Cliente.class);
                                // Es crucial que el id del documento se setee en el objeto Cliente
                                cliente.setFirestoreId(document.getId());
                                clientesList.add(cliente);
                            }
                            Log.d("AdminCheckout", "Clientes cargados exitosamente: " + clientesList.size());
                            // Una vez cargadas reservas y clientes, ahora podemos generar y guardar checkouts
                            // Solo se debe descomentar esta línea para la primera ejecución
                            generateAndSaveRandomCheckouts();
                            // Luego de generar, cargar los checkouts existentes
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
        int numberOfCheckoutsToGenerate = 5; // Puedes ajustar cuántos checkouts aleatorios quieres generar

        for (int i = 0; i < numberOfCheckoutsToGenerate; i++) {
            Reserva randomReserva = reservasList.get(random.nextInt(reservasList.size()));
            Cliente randomCliente = clientesList.get(random.nextInt(clientesList.size()));

            // Asegúrate de que el idPersona de la reserva coincida con el firestoreId del cliente
            // Si no hay coincidencia, esto podría generar checkouts con datos inconsistentes.
            // Para fines de esta tarea, asumimos que los IDs de persona en reserva
            // corresponden a los IDs de documentos de clientes.
            // Si necesitas una lógica más estricta, tendrías que iterar sobre reservas
            // y buscar el cliente correspondiente por su ID.

            String clientFullName = randomCliente.getNombres() + " " + randomCliente.getApellidos();

            CheckoutFirebase newCheckout = new CheckoutFirebase(
                    null, // El ID se generará automáticamente por Firestore
                    randomReserva.getRoomNumber(),
                    clientFullName,
                    randomReserva.getPrecioTotal(),
                    randomReserva.getCobros_adicionales(),
                    randomReserva.getFechaInicio(),
                    randomReserva.getFechaFin()
            );

            // Guardar el nuevo checkout en Firestore
            db.collection("checkouts")
                    .add(newCheckout)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Nuevo Checkout guardado con ID: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error al guardar nuevo checkout", e);
                    });
        }
        Toast.makeText(AdminCheckout.this, "Generación de checkouts aleatorios iniciada.", Toast.LENGTH_SHORT).show();
    }


    // Método para cargar checkouts desde Firestore
    private void loadCheckoutsFromFirestore() {
        db.collection("checkouts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            checkoutList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                CheckoutFirebase checkout = document.toObject(CheckoutFirebase.class);
                                checkoutList.add(checkout);
                            }
                            Log.d("AdminCheckout", "Checkouts cargados exitosamente: " + checkoutList.size());
                            adapter.actualizarLista(checkoutList);
                            Toast.makeText(AdminCheckout.this, "Checkouts cargados: " + checkoutList.size(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de checkouts: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar checkouts: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Este método es el original para poblar Firestore con datos "fijos".
    // DEBE ESTAR COMENTADO O ELIMINADO DESPUÉS DE LA PRIMERA EJECUCIÓN SI YA GENERAS CON EL MÉTODO ALEATORIO.
    private void saveInitialCheckoutsToFirestore() {
        // Esta función ahora está deshabilitada ya que la generación se hará con datos de reservas y clientes.
        // Si necesitas poblar con datos fijos, descomenta y usa esta función aparte.
        // List<CheckoutFirebase> initialCheckouts = new ArrayList<>();
        // initialCheckouts.add(new CheckoutFirebase(null, "101", "Juan Pérez", 150.0, 20.0, new Date(), new Date()));
        // ... (añade más datos si es necesario)
        // for (CheckoutFirebase checkout : initialCheckouts) {
        //     db.collection("checkouts")
        //             .add(checkout)
        //             .addOnSuccessListener(documentReference -> {
        //                 Log.d("Firestore", "Checkout guardado con ID: " + documentReference.getId());
        //                 Toast.makeText(AdminCheckout.this, "Checkout guardado!", Toast.LENGTH_SHORT).show();
        //             })
        //             .addOnFailureListener(e -> {
        //                 Log.w("Firestore", "Error al guardar checkout", e);
        //                 Toast.makeText(AdminCheckout.this, "Error al guardar checkout: " + e.getMessage(), Toast.LENGTH_LONG).show();
        //             });
        // }
    }
}