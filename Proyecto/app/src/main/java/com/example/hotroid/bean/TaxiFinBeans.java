package com.example.hotroid.bean;

public class TaxiFinBeans {
    private String nombre;
    private String horario; // Keep if you intend to add it to the item layout
    private String rol;     // Keep if you intend to add it to the item layout
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

    // Constructor if you only use name, origin, destination and image (as per your taxi_fin_item.xml)
    public TaxiFinBeans(String nombre, String origen, String destino, int imagenResId) {
        this.nombre = nombre;
        this.origen = origen;
        this.destino = destino;
        this.imagenResId = imagenResId;
        // You can set default/empty values for horario and rol if not used in this constructor
        this.horario = "";
        this.rol = "";
    }


    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getHorario() {
        return horario;
    }

    public String getRol() {
        return rol;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public int getImagenResId() {
        return imagenResId;
    }

    // Setters (if needed)
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public void setImagenResId(int imagenResId) {
        this.imagenResId = imagenResId;
    }
}