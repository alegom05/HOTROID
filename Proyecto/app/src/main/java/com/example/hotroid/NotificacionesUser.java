package com.example.hotroid;
import com.example.hotroid.R;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotroid.bean.Notificacion;
import com.example.hotroid.databinding.UserNotificacionesBinding;

import java.util.ArrayList;
import java.util.List;

public class NotificacionesUser extends AppCompatActivity {
    private UserNotificacionesBinding binding;
    private NotificacionesAdapterUser adapter;
    private List<Notificacion> listaNotificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=UserNotificacionesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listaNotificaciones=generarNotificacionesEjem();
        //configuración del recyclerview
        adapter= new NotificacionesAdapterUser(listaNotificaciones);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }


    private List<Notificacion> generarNotificacionesEjem(){
        List<Notificacion> lista = new ArrayList<>();
        lista.add(new Notificacion(
                "Se realiza el envio del código que debe presentar al taxista cuando llegue...",
                "Se realiza el envio del código que debe presentar al taxista cuando llegue al hotel para que pueda dirigirse al aeropuerto",
                R.drawable.qr_code_2_24,
                "Ay6gfJGg2r67g54"
        ));
        lista.add(new Notificacion(
                "Se ha realizado con éxito el pago por la reservación de 1 habitación y servicio al cuarto, además se está registrando su salida del hotel. ¡Gracias por su preferencia!...",
                "Se ha realizado con éxito el pago por la reservación de 1 habitación y servicio al cuarto, además se está registrando su salida del hotel. ¡Gracias por su preferencia!",
                R.drawable.ic_euro
        ));
        lista.add(new Notificacion(
                "A partir de este momento se registra su ingreso al hotel y se le brindará acceso a todos los servicios...",
                "A partir de este momento se registra su ingreso al hotel y se le brindará acceso a todos los servicios solicitados.",
                R.drawable.qr_code_2_24,
                "Ay6gfJGg2r67gF"
        ));
        return lista;
    }
}