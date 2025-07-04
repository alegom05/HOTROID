package com.example.hotroid.bean;

public class VentaClienteConsolidado {
    private String idCliente;
    private String nombreCompletoCliente; // Nombre completo del cliente (ej. "Juan Pérez")
    private double montoTotal; // Monto total gastado por este cliente
    private long cantidadTotalServicios; // Cantidad total de servicios comprados por este cliente

    public VentaClienteConsolidado() {
        // Constructor vacío requerido por Firestore (aunque no se usa directamente para deserializar)
    }

    public VentaClienteConsolidado(String idCliente, String nombreCompletoCliente, double montoTotal) {
        this.idCliente = idCliente;
        this.nombreCompletoCliente = nombreCompletoCliente;
        this.montoTotal = montoTotal;
        this.cantidadTotalServicios = 0; // Inicializar en 0, se actualizará al añadir ventas
    }

    // Método para añadir monto y cantidad (para consolidación)
    public void addMonto(double monto) {
        this.montoTotal += monto;
        this.cantidadTotalServicios++; // Incrementa la cantidad de servicios por cada venta
    }

    // Getters y Setters
    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCompletoCliente() {
        return nombreCompletoCliente;
    }

    public void setNombreCompletoCliente(String nombreCompletoCliente) {
        this.nombreCompletoCliente = nombreCompletoCliente;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public long getCantidadTotalServicios() {
        return cantidadTotalServicios;
    }

    public void setCantidadTotalServicios(long cantidadTotalServicios) {
        this.cantidadTotalServicios = cantidadTotalServicios;
    }
}