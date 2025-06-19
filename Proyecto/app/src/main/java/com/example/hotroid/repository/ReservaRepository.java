package com.example.hotroid.repository;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // For Java 8+ streams

public class ReservaRepository {
    /**
     * Une reservas con su hotel correspondiente.
     * @param reservas Lista de reservas (cada una tiene idHotel).
     * @param hoteles Lista de hoteles (cada uno tiene idHotel).
     * @return Lista de objetos combinados ReservaConHotel.
     */
    public List<ReservaConHotel> obtenerReservasConHotel(List<Reserva> reservas, List<Hotel> hoteles) {
        List<ReservaConHotel> resultado = new ArrayList<>();

        // Optimize hotel lookup using a Map
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

        return resultado;
    }
}