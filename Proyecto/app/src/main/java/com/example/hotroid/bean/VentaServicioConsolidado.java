// app/src/main/java/com/example/hotroid/bean/VentaServicioConsolidado.java
package com.example.hotroid.bean;

public class VentaServicioConsolidado {
    private String nombreServicio;
    private int cantidadTotal;
    private double montoTotal;

    public VentaServicioConsolidado(String nombreServicio, int cantidadTotal, double montoTotal) {
        this.nombreServicio = nombreServicio;
        this.cantidadTotal = cantidadTotal;
        this.montoTotal = montoTotal;
    }

    // --- Getters ---
    public String getNombreServicio() {
        return nombreServicio;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    // --- Setters (optional, but good practice if you ever modify these objects) ---
    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public void setCantidadTotal(int cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}