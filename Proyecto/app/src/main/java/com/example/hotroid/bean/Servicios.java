package com.example.hotroid.bean;

import java.util.ArrayList;

public class Servicios {
    private String nombre;
    private String descripcion;
    private double precio;
    private String horario;
    private ArrayList<String> imagenes;
    private boolean habilitado = true;
    private String documentId; // Campo para almacenar el ID de Firestore

    // Constructor vacío requerido por Firestore
    public Servicios() {}

    // Constructores con parámetros
    public Servicios(String nombre, String descripcion, double precio, String horario, ArrayList<String> imagenes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.horario = horario;
        this.imagenes = imagenes;
    }

    // Getters y setters (asegúrate de tener todos)
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId; // Setter crítico para el ID
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public ArrayList<String> getImagenes() {
        return imagenes;
    }

    public void setImagenes(ArrayList<String> imagenes) {
        this.imagenes = imagenes;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }
}