package com.example.hotroid;

import java.util.ArrayList;

public class ServicioFirebase {
    private String nombre;
    private String descripcion;
    private String precio;
    private ArrayList<String> imagenes;
    private boolean habilitado;

    public ServicioFirebase() {
        // Constructor vacío necesario para Firestore
    }

    public ServicioFirebase(String nombre, String descripcion, String precio, ArrayList<String> imagenes, boolean habilitado) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenes = imagenes;
        this.habilitado = habilitado;
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

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
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
