package com.example.hotroid;

public class ViajeFinBeans {
    private String nombre;
    private String horario;
    private String rol;
    private int imagenResId;

    public ViajeFinBeans(String nombre, String horario, String rol, int imagenResId) {
        this.nombre = nombre;
        this.horario = horario;
        this.rol = rol;
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
}
