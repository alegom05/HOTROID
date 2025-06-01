package com.example.hotroid.repository;

import com.example.hotroid.bean.Hotel;
import com.example.hotroid.bean.Reserva;
import com.example.hotroid.bean.ReservaConHotel;

import java.util.ArrayList;
import java.util.List;

public class ReservaRepository {
    /**
     * Une reservas con su hotel correspondiente.
     * @param reservas Lista de reservas (cada una tiene idHotel).
     * @param hoteles Lista de hoteles (cada uno tiene idHotel).
     * @return Lista de objetos combinados ReservaConHotel.
     */
    public List<ReservaConHotel> obtenerReservasConHotel(List<Reserva> reservas, List<Hotel> hoteles) {
        List<ReservaConHotel> resultado = new ArrayList<>();

        for (Reserva r : reservas) {
            Hotel hotelEncontrado = buscarHotelPorId(hoteles, r.getIdHotel());
            if (hotelEncontrado != null) {
                resultado.add(new ReservaConHotel(r, hotelEncontrado));
            }
        }

        return resultado;
    }

    /**
     * Busca un hotel por su ID.
     * @param hoteles Lista de hoteles.
     * @param idHotel ID a buscar.
     * @return Hotel correspondiente, o null si no se encuentra.
     */
    private Hotel buscarHotelPorId(List<Hotel> hoteles, String idHotel) {
        for (Hotel h : hoteles) {
            if (h.getIdHotel().equals(idHotel)) {
                return h;
            }
        }
        return null;
    }
}

