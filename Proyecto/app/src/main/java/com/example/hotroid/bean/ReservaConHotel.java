package com.example.hotroid.bean;

public class ReservaConHotel {
    private Reserva reserva;
    private Hotel hotel;
    private Room habitacion;  // Datos de la habitación

    public ReservaConHotel(Reserva reserva, Hotel hotel) {
        this.reserva = reserva;
        this.hotel = hotel;
    }
    // Constructores
    public ReservaConHotel() {}

    public Reserva getReserva() {
        return reserva;
    }

    public Hotel getHotel() {
        return hotel;
    }
    public Room getHabitacion() { return habitacion; }
    public void setHabitacion(Room habitacion) { this.habitacion = habitacion; }
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}

