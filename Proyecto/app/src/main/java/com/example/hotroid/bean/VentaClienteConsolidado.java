package com.example.hotroid.bean;

public class VentaClienteConsolidado {
    private String idCliente;
    private String nombreCompletoCliente;
    private double montoTotal;

    public VentaClienteConsolidado() {}

    public VentaClienteConsolidado(String idCliente, String nombreCompletoCliente, double montoTotal) {
        this.idCliente = idCliente;
        this.nombreCompletoCliente = nombreCompletoCliente;
        this.montoTotal = montoTotal;
    }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public String getNombreCompletoCliente() { return nombreCompletoCliente; }
    public void setNombreCompletoCliente(String nombreCompletoCliente) { this.nombreCompletoCliente = nombreCompletoCliente; }

    public double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(double montoTotal) { this.montoTotal = montoTotal; }

    public void addMonto(double amount) {
        this.montoTotal += amount;
    }
}