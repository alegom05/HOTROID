package com.example.hotroid.bean;

public class TaxiAlertasBeans {
    private String nombre;
    private String origen; // Cambiado de 'lugar' a 'origen' para mayor claridad
    private String destino;
    private String tiempo;

    public TaxiAlertasBeans(String nombre, String origen, String destino, String tiempo) {
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.tiempo = tiempo;
    }

    // Ya no necesitas 'lugar' si 'origen' es el punto de partida
    // public String getLugar() {
    //     return lugar;
    // }

    public String getNombre() {
        return nombre;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getTiempo() {
        return tiempo;
    }
}