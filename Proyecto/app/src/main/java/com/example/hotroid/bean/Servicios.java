// app/src/main/java/com/example/hotroid/bean/Servicios.java
package com.example.hotroid.bean;

import android.net.Uri;
import java.util.ArrayList;

public class Servicios {
    private String nombre;
    private String descripcion;
    private double precio;
    private String horario;
    private ArrayList<String> imagenes;
    private boolean habilitado = true;
    private String documentId; // Este campo puede ser null en Firestore si no lo guardas explícitamente

    public Servicios() {
        // Required empty constructor for Firestore deserialization
    }

    // Constructor que deberías usar para añadir datos si no tienes imágenes o si las tienes en la lista de Strings
    public Servicios(String nombre, String descripcion, double precio, String horario, ArrayList<String> imagenes) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.horario = horario;
        this.imagenes = imagenes;
        this.habilitado = true;
    }

    // Si también usas un constructor sin la lista de imágenes, asegúrate de tenerlo
    public Servicios(String nombre, String descripcion, double precio, String horario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.horario = horario;
        this.imagenes = new ArrayList<>(); // Inicializa la lista vacía
        this.habilitado = true;
    }


    // --- Getters and Setters ---
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