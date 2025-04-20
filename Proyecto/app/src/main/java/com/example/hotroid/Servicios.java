package com.example.hotroid;

import android.net.Uri;

import java.util.ArrayList;

public class Servicios {
    private String nombre;
    private String descripcion;
    private String precio;
    private ArrayList<Uri> imagenes;

    public Servicios(String nombre, String descripcion, String precio, ArrayList<Uri> imagenes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenes = imagenes;
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

    public ArrayList<Uri> getImagenes() { return imagenes; }
    public void setImagenes(ArrayList<Uri> imagenes) { this.imagenes = imagenes; }
}
