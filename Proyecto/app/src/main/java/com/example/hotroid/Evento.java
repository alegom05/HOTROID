package com.example.hotroid;

public class Evento {
    private String fecha;
    private String evento;
    private String hotel;

    public Evento(String fecha, String evento, String hotel) {
        this.fecha = fecha;
        this.evento = evento;
        this.hotel = hotel;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getEvento() {
        return evento;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public String getHotel() {
        return hotel;
    }

    public void setHotel(String hotel) {
        this.hotel = hotel;
    }
}