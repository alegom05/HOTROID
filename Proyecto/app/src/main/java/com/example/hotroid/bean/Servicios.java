package com.example.hotroid.bean;

import android.net.Uri;
import java.util.ArrayList;

public class Servicios {
    private String nombre;
    private String descripcion;
    // --- KEY CHANGE: precio is now a double ---
    private double precio;
    // ------------------------------------------
    private String horario;
    private ArrayList<String> imagenes;
    private boolean habilitado = true;
    private String documentId;

    public Servicios() {
        // Required empty constructor for Firestore deserialization
    }

    // --- Update constructor parameter type for precio ---
    public Servicios(String nombre, String descripcion, double precio, String horario, ArrayList<String> imagenes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio; // Use double here
        this.horario = horario;
        this.imagenes = imagenes;
        this.habilitado = true;
    }

    // --- Getters and Setters (ensure they match the new type) ---

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

    // --- Getter and Setter for precio (now double) ---
    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
    // --------------------------------------------------

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