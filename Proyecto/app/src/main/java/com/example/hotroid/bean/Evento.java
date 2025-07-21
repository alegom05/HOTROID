package com.example.hotroid.bean;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;

public class Evento {
    @DocumentId
    private String id;
    private Timestamp fechaHora;
    private String evento;
    private String hotel;
    private String descripcion;

    // Constructor vacío requerido por Firestore
    public Evento() {}

    // Constructor completo
    public Evento(Timestamp fechaHora, String evento, String hotel, String descripcion) {
        this.fechaHora = fechaHora;
        this.evento = evento;
        this.hotel = hotel;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Timestamp fechaHora) {
        this.fechaHora = fechaHora;
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

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}