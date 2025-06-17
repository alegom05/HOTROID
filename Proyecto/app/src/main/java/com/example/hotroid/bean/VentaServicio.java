// app/src/main/java/com/example/hotroid/bean/VentaServicio.java
package com.example.hotroid.bean;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class VentaServicio {
    private String id; // Document ID from Firestore
    private String idServicio; // Reference to the service document ID
    private int cantidad;
    private double precioVentaUnitario; // Price at the time of sale
    private double totalVenta;
    @ServerTimestamp // Automatically set by Firestore when the document is created/updated
    private Date fechaVenta;

    public VentaServicio() {
        // Public no-argument constructor needed for Firestore deserialization
    }

    public VentaServicio(String id, String idServicio, int cantidad, double precioVentaUnitario, double totalVenta, Date fechaVenta) {
        this.id = id;
        this.idServicio = idServicio;
        this.cantidad = cantidad;
        this.precioVentaUnitario = precioVentaUnitario;
        this.totalVenta = totalVenta;
        this.fechaVenta = fechaVenta;
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getIdServicio() {
        return idServicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public Date getFechaVenta() {
        return fechaVenta;
    }

    // --- Setters (needed for Firestore deserialization) ---
    public void setId(String id) {
        this.id = id;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setPrecioVentaUnitario(double precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public void setFechaVenta(Date fechaVenta) {
        this.fechaVenta = fechaVenta;
    }
}