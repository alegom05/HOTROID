package com.example.hotroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Mantengo Log para Firebase si se sigue usando aquí
import android.view.View;
import android.view.Window; // Para la barra de estado
import android.widget.Button; // Si usas un botón en la vista de alertas
import androidx.annotation.NonNull; // Si usas Firebase u otras anotaciones
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat; // Para el color de la barra de estado
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotroid.bean.TaxiAlertasBeans; // Asegúrate de que esta clase exista
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot; // Si sigues usando Firebase aquí
import com.google.firebase.firestore.FirebaseFirestore; // Si sigues usando Firebase aquí

import java.util.ArrayList;
import java.util.List;

public class TaxiActivity extends AppCompatActivity { // Esta Activity es ahora la nueva "Home" de Alertas

    // Puedes mantener la inicialización de Firebase aquí si la consideras global para la app
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taxi_main); // Ahora taxi_main.xml es la vista de alertas

        // Inicialización de Firebase Firestore (si es necesario para la vista de alertas)
        // Puedes mover esto a un Application class o Singleton si necesitas una instancia global
        db = FirebaseFirestore.getInstance();
        // Ejemplo de uso de Firebase que estaba en el TaxiActivity original, mantenido aquí
        // si la "nueva" Home (Alertas) lo requiere. Si no, quítalo.
        UsuarioDto usuario = new UsuarioDto();
        usuario.setNombre("Juan");
        usuario.setCorreo("juan.perez@pucp.edu.pe");
        usuario.setDni("12345678");
        db.collection("usuarios")
                .add(usuario)
                .addOnSuccessListener(unused -> {
                    Log.d("msg-test","Data guardada exitosamente");
                })
                .addOnFailureListener(e -> e.printStackTrace());

        // Configurar color de la barra de estado para esta Activity (Alertas)
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.verdejade));


        // Lógica específica de la vista de Alertas (movida de la antigua TaxiAlertas.java)
        CardView cardUsuario = findViewById(R.id.cardUsuario);
        cardUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirige a TaxiCuenta al hacer clic en la tarjeta de usuario
                Intent intent = new Intent(TaxiActivity.this, TaxiCuenta.class);
                startActivity(intent);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerNotificaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<TaxiAlertasBeans> lista = new ArrayList<>();
        // Creamos las notificaciones con Origen y Destino claros
        lista.add(new TaxiAlertasBeans("Mauricio Guerra", "Hotel Marriot","Hotel Miraflores", "ahora"));
        lista.add(new TaxiAlertasBeans("Lisa Cáceres", "Hotel Marriot", "Grand Hotel Madrid","hace 1 minuto"));
        lista.add(new TaxiAlertasBeans("Sol Díaz", "Hotel Marriot", "Valencia Beach Resort","hace 20 minutos"));

        TaxiAlertasAdapter adapter = new TaxiAlertasAdapter(this, lista); // Asegúrate de que TaxiAlertasAdapter exista
        recyclerView.setAdapter(adapter);


        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.wifi); // Este es ahora el icono de la nueva Home (Alertas)

        // Listener para la barra de navegación inferior
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.wifi) { // Ya estás en la nueva Home (Alertas)
                return true;
            } else if (item.getItemId() == R.id.location) {
                Intent intentUbicacion = new Intent(TaxiActivity.this, TaxiLocation.class); // Asegúrate de que TaxiLocation exista
                startActivity(intentUbicacion);
                return true;
            } else if (item.getItemId() == R.id.notify) { // Este icono ahora apunta a la antigua Home (Dashboard)
                Intent intentDashboard = new Intent(TaxiActivity.this, TaxiDashboardActivity.class);
                startActivity(intentDashboard);
                return true;
            }
            return false;
        });
    }
}
