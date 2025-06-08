package com.example.hotroid.bean;

public class Evento {
    private String fecha;
    private String evento;
    private String hotel;
    private String descripcion;

    public Evento(String fecha, String evento, String hotel, String descripcion) {
        this.fecha = fecha;
        this.evento = evento;
        this.hotel = hotel;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion() {
        this.descripcion = descripcion;
    }

}