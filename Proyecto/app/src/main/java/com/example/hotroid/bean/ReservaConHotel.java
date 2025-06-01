package com.example.hotroid.bean;

public class ReservaConHotel {
    private Reserva reserva;
    private Hotel hotel;

    public ReservaConHotel(Reserva reserva, Hotel hotel) {
        this.reserva = reserva;
        this.hotel = hotel;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public Hotel getHotel() {
        return hotel;
    }
}

