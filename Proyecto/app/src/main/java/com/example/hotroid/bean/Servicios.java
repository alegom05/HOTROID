package com.example.hotroid.bean;

import java.util.ArrayList;

public class Servicios {
    private String nombre;
    private String descripcion;
    private double precio;
    private String horaInicio; // Changed from horario
    private String horaFin;    // New field
    private ArrayList<String> imagenes;
    private boolean habilitado = true;
    private String documentId;

    // Constructor vacío requerido por Firestore
    public Servicios() {}

    // Constructor con parámetros actualizado
    public Servicios(String nombre, String descripcion, double precio, String horaInicio, String horaFin, ArrayList<String> imagenes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.imagenes = imagenes;
    }

    // Getters y setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    // Updated getter/setter for horaInicio
    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    // New getter/setter for horaFin
    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
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