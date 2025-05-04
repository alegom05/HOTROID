package com.example.hotroid;

public class Notificacion {
    private String nombre;
    private String lugar;
    private String tiempo;

    public Notificacion(String nombre, String lugar, String tiempo) {
        this.nombre = nombre;
        this.lugar = lugar;
        this.tiempo = tiempo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public String getTiempo() {
        return tiempo;
    }
}
