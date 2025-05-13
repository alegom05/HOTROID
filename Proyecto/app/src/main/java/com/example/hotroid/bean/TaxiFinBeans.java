package com.example.hotroid.bean;

public class TaxiFinBeans {
    private String nombre;
    private String horario;
    private String rol;
    private String origen;
    private String destino;

    private int imagenResId;

    public TaxiFinBeans(String nombre, String horario, String rol, String origen, String destino, int imagenResId) {
        this.nombre = nombre;
        this.horario = horario;
        this.rol = rol;
        this.origen = origen;
        this.destino = destino;
        this.imagenResId = imagenResId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getHorario() {
        return horario;
    }

    public String getRol() {
        return rol;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }
}
