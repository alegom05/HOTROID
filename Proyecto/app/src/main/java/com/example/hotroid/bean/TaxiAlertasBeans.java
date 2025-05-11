package com.example.hotroid.bean;

public class TaxiAlertasBeans {
    private String nombre;
    private String lugar;
    private String destino;
    private String tiempo;

    public TaxiAlertasBeans(String nombre, String lugar, String destino, String tiempo) {
        this.nombre = nombre;
        this.lugar = lugar;
        this.destino = destino;
        this.tiempo = tiempo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getLugar() {
        return lugar;
    }

    public String getDestino() {
        return destino;
    }

    public String getTiempo() {
        return tiempo;
    }
}
