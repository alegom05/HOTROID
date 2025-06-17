package com.example.hotroid.bean;

import com.google.firebase.firestore.DocumentId; // Import for mapping Firestore document ID
import java.util.Date;

public class VentaServicio {
    @DocumentId // This annotation maps the Firestore document ID to this field
    private String id; // Represents the Firestore document ID
    private String idServicio;
    private String idCliente; // Added client ID
    private int cantidad;
    private double precioUnitario;
    private double totalVenta;
    private Date fechaVenta;

    public VentaServicio() {} // Required no-argument constructor for Firestore

    // Full constructor including idCliente
    public VentaServicio(String id, String idServicio, String idCliente, int cantidad, double precioUnitario, double totalVenta, Date fechaVenta) {
        this.id = id; // Can be null when creating a new object
        this.idServicio = idServicio;
        this.idCliente = idCliente;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalVenta = totalVenta;
        this.fechaVenta = fechaVenta;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdServicio() { return idServicio; }
    public void setIdServicio(String idServicio) { this.idServicio = idServicio; }

    public String getIdCliente() { return idCliente; }
    public void setIdCliente(String idCliente) { this.idCliente = idCliente; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getTotalVenta() { return totalVenta; }
    public void setTotalVenta(double totalVenta) { this.totalVenta = totalVenta; }

    public Date getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(Date fechaVenta) { this.fechaVenta = fechaVenta; }
}