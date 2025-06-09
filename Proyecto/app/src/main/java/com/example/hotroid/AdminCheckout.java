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

import com.example.hotroid.bean.CheckoutFirebase; // Importa la nueva clase CheckoutFirebase
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List; // Usamos List para mayor flexibilidad

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private List<CheckoutFirebase> checkoutList; // Cambiado a List de CheckoutFirebase
    private FirebaseFirestore db; // Instancia de Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.admin_checkout); // Asegúrate de que este layout existe y es correcto
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance(); // Inicializa Firestore
        checkoutList = new ArrayList<>(); // Inicializa la lista vacía

        rvCheckouts = findViewById(R.id.rvCheckouts); // Asegúrate de que este ID exista en admin_checkout.xml
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        // Inicializa el adaptador con la lista vacía
        adapter = new CheckoutAdapter(checkoutList);
        rvCheckouts.setAdapter(adapter);

        // --- ATENCIÓN: ESTA LÍNEA ESTÁ DESCOMENTADA PARA POBLAR FIRESTORE UNA VEZ ---
        // DEBES COMENTARLA DESPUÉS DE LA PRIMERA EJECUCIÓN EXITOSA.
        saveInitialCheckoutsToFirestore(); // <--- ¡AQUÍ ESTÁ DESCOMENTADA!

        // Llama al método para cargar los checkouts desde Firestore
        loadCheckoutsFromFirestore();


        // Evento opcional al hacer clic en un item
        adapter.setOnItemClickListener(new CheckoutAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                CheckoutFirebase seleccionado = checkoutList.get(position); // Ahora es CheckoutFirebase

                Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
                intent.putExtra("ROOM_NUMBER", seleccionado.getRoomNumber());
                intent.putExtra("CLIENT_NAME", seleccionado.getClientName());
                // Puedes añadir más extras si tu AdminCheckoutDetalles los necesita
                // intent.putExtra("CHECKOUT_ID", seleccionado.getIdCheckout());
                startActivity(intent);
            }
        });

        // Configuración de la BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation); // Asegúrate de que este ID exista en tu layout
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_checkout); // Asegúrate de que este ID exista en tu menú

            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_registros) { // Asegúrate de que este ID exista en tu menú
                    Intent intentInicio = new Intent(AdminCheckout.this, AdminActivity.class);
                    startActivity(intentInicio);
                    return true;
                } else if (itemId == R.id.nav_taxistas) { // Asegúrate de que este ID exista en tu menú
                    Intent intentUbicacion = new Intent(AdminCheckout.this, AdminTaxistas.class);
                    startActivity(intentUbicacion);
                    return true;
                } else if (itemId == R.id.nav_checkout) { // Asegúrate de que este ID exista en tu menú
                    // Ya estamos en AdminCheckout, no es necesario iniciarla de nuevo
                    return true;
                } else if (itemId == R.id.nav_reportes) { // Asegúrate de que este ID exista en tu menú
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

    // Método para cargar checkouts desde Firestore
    private void loadCheckoutsFromFirestore() {
        db.collection("checkouts") // Nombre de tu colección en Firestore (debe coincidir exactamente)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            checkoutList.clear(); // Limpia la lista existente
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convierte el documento de Firestore a un objeto CheckoutFirebase
                                CheckoutFirebase checkout = document.toObject(CheckoutFirebase.class);
                                checkoutList.add(checkout);
                            }
                            Log.d("AdminCheckout", "Checkouts cargados exitosamente: " + checkoutList.size());
                            adapter.actualizarLista(checkoutList); // Notifica al adaptador que los datos han cambiado
                            Toast.makeText(AdminCheckout.this, "Checkouts cargados: " + checkoutList.size(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("AdminCheckout", "Error al obtener documentos de checkouts: ", task.getException());
                            Toast.makeText(AdminCheckout.this, "Error al cargar checkouts: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Método para guardar algunos checkouts iniciales en Firestore
    // Ejecutar SOLO UNA VEZ para poblar la BD. Luego, comentar o eliminar.
    private void saveInitialCheckoutsToFirestore() {
        List<CheckoutFirebase> initialCheckouts = new ArrayList<>();
        //initialCheckouts.add(new CheckoutFirebase(null, "101", "Juan Pérez"));
        // initialCheckouts.add(new CheckoutFirebase(null, "102", "Cesar Guarníz")); // Corregido de initialHotels a initialCheckouts
        //initialCheckouts.add(new CheckoutFirebase(null, "103", "Hiroshi Giotoku"));
        //initialCheckouts.add(new CheckoutFirebase(null, "104", "River Quispe"));
        //initialCheckouts.add(new CheckoutFirebase(null, "105", "Stefano Roldán"));

        for (CheckoutFirebase checkout : initialCheckouts) {
            db.collection("checkouts")
                    .add(checkout)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("Firestore", "Checkout guardado con ID: " + documentReference.getId());
                        Toast.makeText(AdminCheckout.this, "Checkout guardado!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error al guardar checkout", e);
                        Toast.makeText(AdminCheckout.this, "Error al guardar checkout: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }
}