package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdminCheckout extends AppCompatActivity {

    private RecyclerView rvCheckouts;
    private CheckoutAdapter adapter;
    private ArrayList<Checkout> checkoutList;

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
        rvCheckouts = findViewById(R.id.rvCheckouts);
        rvCheckouts.setLayoutManager(new LinearLayoutManager(this));

        // Llenar la lista de datos
        checkoutList = new ArrayList<>();
        checkoutList.add(new Checkout("101", "Juan Pérez"));
        checkoutList.add(new Checkout("102", "Cesar Guarníz"));
        checkoutList.add(new Checkout("103", "Hiroshi Giotoku"));
        checkoutList.add(new Checkout("104", "River Quispe"));
        checkoutList.add(new Checkout("105", "Stefano Roldán"));

        // Adaptador
        adapter = new CheckoutAdapter(checkoutList);
        rvCheckouts.setAdapter(adapter);

        // Evento opcional al hacer clic en un item
        adapter.setOnItemClickListener(position -> {
            Checkout seleccionado = checkoutList.get(position);

            Intent intent = new Intent(AdminCheckout.this, AdminCheckoutDetalles.class);
            intent.putExtra("ROOM_NUMBER", seleccionado.getRoomNumber());
            intent.putExtra("CLIENT_NAME", seleccionado.getClientName());
            startActivity(intent);

        });
    }
}