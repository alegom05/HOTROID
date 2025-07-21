package com.example.hotroid.repository;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // For Java 8+ streams

public class ReservaRepository {

    public interface Callback {
        void onResult(List<ReservaConHotel> lista);
        void onError(Exception e);
    }

    /**
     * Une reservas con su hotel correspondiente.
     * @param reservas Lista de reservas (cada una tiene idHotel).
     * @param hoteles Lista de hoteles (cada uno tiene idHotel).
     * @return Lista de objetos combinados ReservaConHotel.
     */
    public void obtenerReservasConHotelFirestore(List<Reserva> reservas, Callback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("hoteles").get()
                .addOnSuccessListener(hotelDocs -> {
                    List<Hotel> hoteles = new ArrayList<>();
                    for (DocumentSnapshot doc : hotelDocs) {
                        Hotel h = doc.toObject(Hotel.class);
                        h.setIdHotel(doc.getId());
                        hoteles.add(h);
                    }

                    // Combinar reservas + hoteles
                    List<ReservaConHotel> resultado = new ArrayList<>();
                    Map<String, Hotel> hotelMap = new HashMap<>();
                    for (Hotel h : hoteles) {
                        hotelMap.put(h.getIdHotel(), h);
                    }

                    for (Reserva r : reservas) {
                        Hotel hotelEncontrado = hotelMap.get(r.getIdHotel());
                        if (hotelEncontrado != null) {
                            resultado.add(new ReservaConHotel(r, hotelEncontrado));
                        }
                    }

                    callback.onResult(resultado);
                })
                .addOnFailureListener(callback::onError);
    }

    // Esta función retorna una lista vacía
    public static List<Hotel> obtenerHoteles() {
        List<Hotel> hoteles = new ArrayList<>();
        return hoteles;
    }
}