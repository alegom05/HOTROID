package com.example.hotroid.bean;

public class VentaServicioConsolidado {
    private String nombreServicio;
    private long cantidadTotal;
    private double montoTotal;

    public VentaServicioConsolidado() {}

    public VentaServicioConsolidado(String nombreServicio, long cantidadTotal, double montoTotal) {
        this.nombreServicio = nombreServicio;
        this.cantidadTotal = cantidadTotal;
        this.montoTotal = montoTotal;
    }

    // Getters y setters
    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public long getCantidadTotal() {
        return cantidadTotal;
    }

    public void setCantidadTotal(long cantidadTotal) {
        this.cantidadTotal = cantidadTotal;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }
}