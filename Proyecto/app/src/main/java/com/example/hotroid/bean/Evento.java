package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId; // Importar si quieres que Firestore asigne el ID del documento aquí

public class Evento {
    // Si quieres que el ID del documento de Firestore se guarde en este campo
    // @DocumentId
    // private String id; // Opcional: Para guardar el ID del documento de Firestore

    private String fecha;
    private String evento; // Título del evento
    private String hotel;
    private String descripcion; // Descripción detallada

    // Constructor vacío requerido por Firestore
    public Evento() {
        // Constructor sin argumentos
    }

    // Constructor original
    public Evento(String fecha, String evento, String hotel, String descripcion) {
        this.fecha = fecha;
        this.evento = evento;
        this.hotel = hotel;
        this.descripcion = descripcion;
    }

    // --- Getters ---
    // public String getId() { return id; } // Getter si añadiste el campo id
    public String getFecha() {
        return fecha;
    }

    public String getEvento() {
        return evento;
    }

    public String getHotel() {
        return hotel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // --- Setters ---
    // public void setId(String id) { this.id = id; } // Setter si añadiste el campo id
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setEvento(String evento) {
        this.evento = evento;
    }

    public void setHotel(String hotel) {
        this.hotel = hotel;
    }

    // CORRECCIÓN: Tu setter original de descripcion no tomaba un argumento.
    public void setDescripcion(String descripcion) { // <-- Asegúrate que este setter tome un argumento
        this.descripcion = descripcion;
    }
}